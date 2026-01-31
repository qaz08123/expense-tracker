package com.example.controller;

import java.time.YearMonth;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.entity.Entry;
import com.example.entity.EntryType;
import com.example.service.CategoryService;
import com.example.service.EntryService;

@Controller
@RequestMapping("/entries")
public class EntryController {
    @Autowired
    private EntryService entryService;
    
    @Autowired
    private CategoryService categoryService;
    
    @GetMapping
    public String list(@AuthenticationPrincipal UserDetails userDetails, @RequestParam(value = "month", required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month,  @RequestParam(value = "type", required = false) EntryType  type, Model model) {
        String username = userDetails.getUsername();
        model.addAttribute("entries", entryService.listForUsers(username));
        
        YearMonth currentmonth = (month != null) ? month : YearMonth.now();
        List<Entry> entries;
        if (type != null) {
            entries = entryService.findByMonthAndType(username, type, currentmonth);
        }else {
            entries = entryService.findByMonth(username, currentmonth);
        }
        model.addAttribute("entries", entries);
        model.addAttribute("currentMonth",currentmonth);
        model.addAttribute("previousMonth",currentmonth.minusMonths(1));
        model.addAttribute("nextMonth",currentmonth.plusMonths(1));
        model.addAttribute("selectedType", type == null ? null : type.name());
        return "entries/list";
    }
    
    @GetMapping("/create")
    public String showCreateForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String username = userDetails.getUsername();
        model.addAttribute("entry", new Entry());
        model.addAttribute("types", EntryType.values());
        model.addAttribute("categories", categoryService.getAvailableCategoriesForUser(username));
        return "entries/create";
    }
    
    @PostMapping("/create")
    public String create(@AuthenticationPrincipal UserDetails userDetails, @Valid @ModelAttribute Entry entry, BindingResult result, @RequestParam("categoryId") Long categoryId, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "entries/create";
        }
        String username = userDetails.getUsername();
        entryService.create(username, entry, categoryId);
        redirectAttributes.addFlashAttribute("successMessage", "Entry created");
        return "redirect:/entries";
    }
    
    @GetMapping("/edit/{id}")
    public String showEditForm(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long id, Model model) {
        String username = userDetails.getUsername();
        Entry existingEntry = entryService.getOwnedEntry(username, id);
        
        model.addAttribute("entry", existingEntry);
        model.addAttribute("types", EntryType.values());
        model.addAttribute("categories", categoryService.getAvailableCategoriesForUser(username));
        
        return "entries/edit";
    }
    
    @PostMapping("/edit/{id}")
    public String edit(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long id, @Valid @ModelAttribute Entry entry, BindingResult result, @RequestParam("categoryId") Long categoryId, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "entries/edit";
        }
        
        String username = userDetails.getUsername();
        entryService.update(username, id, entry, categoryId);
        redirectAttributes.addFlashAttribute("successMessage", "Entry updated");
        
        return "redirect:/entries";
    }
    
    @PostMapping("/delete/{id}")
    public String delete(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long id, RedirectAttributes redirectAttributes) {
        String username = userDetails.getUsername();
        entryService.delete(username, id);
        redirectAttributes.addFlashAttribute("successMessage", "Entry deleted");
        
        return "redirect:/entries";
    }
}
