package com.example.controller;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.entity.Category;
import com.example.entity.User;
import com.example.repository.CategoryRepository;
import com.example.repository.EntryRepository;
import com.example.repository.UserRepository;
import com.example.service.CategoryService;

@Controller
@RequestMapping("/categories")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private EntryRepository entryRepository;
    
    /*@GetMapping
    public String list(@AuthenticationPrincipal UserDetails userDetails,  Model model) {
        String username = userDetails.getUsername();
        model.addAttribute("categories", categoryService.getCustomCategoriesForUser(username));
        return "categories/list";
    }*/
    
    @GetMapping("/create")
    public String showCreateForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String username = userDetails.getUsername();
        model.addAttribute("categories", categoryService.getCustomCategoriesForUser(username));
        if (!model.containsAttribute("category")) {
            model.addAttribute("category", new Category());
            }
        return "categories/create";
    }
    
    @PostMapping("/create")
    public String create(@AuthenticationPrincipal UserDetails userDetails, @Valid@ModelAttribute Category category, BindingResult result, RedirectAttributes redirectAttributes) {
        User user = userRepository.findByUsername(userDetails.getUsername()).get();
        String name = category.getName().trim();
        if (result.hasErrors()) {
            return "categories/create";
        }
        
        if (categoryRepository.existsByIsSystemTrueAndNameIgnoreCase(name)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Category name already exists in defaults: " + name);
            return "redirect:/categories/create";
        }
        if (categoryRepository.existsByOwnerAndNameIgnoreCase(user, name)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Category name already exsisted: " + name);
            return "redirect:/categories/create";
        }
        String username = userDetails.getUsername();
        categoryService.createCustomCategory(username, name);
        redirectAttributes.addFlashAttribute("successMessage", "Category added: " + name);
        return "redirect:/categories/create";
    }
    
    @PostMapping("/delete/{id}")
    public String delete(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long id, RedirectAttributes redirectAttributes) {
        Category category = categoryRepository.findById(id).get();
        User user = userRepository.findByUsername(userDetails.getUsername()).get();
        if (category.getOwner() == null || !category.getOwner().getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cannot delete categories owned by other users");
            return "redirect:/categories/create";
        }
        if (entryRepository.countByCategory(category) > 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "This category is in use. Please update the related records before deleting it.");
            return "redirect:/categories/create";
        }
        String username = userDetails.getUsername();
        categoryService.deleteCustomCategory(username, id);
        redirectAttributes.addFlashAttribute("successMessage", "Category deleted");
        
        return "redirect:/categories/create";
    }
}
