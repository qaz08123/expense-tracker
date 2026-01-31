package com.example.controller;

import java.time.YearMonth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.entity.User;
import com.example.repository.UserRepository;
import com.example.service.EntryService;

import lombok.experimental.var;

@Controller
public class WelcomeController {
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private EntryService entryService;
    
    @GetMapping("/")
    public String welcome(@AuthenticationPrincipal UserDetails userDetails, @RequestParam(value = "month", required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month, Model model) {
        if (userDetails == null) {
            return "welcome/welcomepage";
        }
        
        User user = userRepository.findByUsername(userDetails.getUsername()).get();
        model.addAttribute("userName", user.getUsername());
        
        String username = userDetails.getUsername();
        YearMonth currentmonth = (month != null) ? month : YearMonth.now();
        var summary = entryService.getMonthlySummary(username, currentmonth);
        model.addAttribute("currentMonth",currentmonth);
        model.addAttribute("previousMonth",currentmonth.minusMonths(1));
        model.addAttribute("nextMonth",currentmonth.plusMonths(1));
        
        model.addAttribute("monthIncome",summary.get("income"));
        model.addAttribute("monthExpense",summary.get("expense"));
        model.addAttribute("monthNet",summary.get("net"));
        
        return "welcome/welcomepage";
    }
}
