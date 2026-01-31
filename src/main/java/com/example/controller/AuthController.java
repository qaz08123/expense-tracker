package com.example.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.entity.User;
import com.example.repository.UserRepository;

@Controller
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @GetMapping("/login")
    public String showLoginForm() {
        return "auth/login";
    }
    
    @GetMapping("/register")
    public String showRegisterationForm(@ModelAttribute("user") User user) {
        return "auth/register";
    }
    
    @PostMapping("/register")
    public String registerUser(@Validated @ModelAttribute("user") User user, BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (userRepository.existsByEmail(user.getEmail())) {
            result.rejectValue("email", "error.user", "This email has been used");
        }
        if (userRepository.existsByUsername(user.getUsername())) {
            result.rejectValue("username", "error.user", "This username has been used");
        }
        if (result.hasErrors()) {
            return "auth/register";
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("successMessage", "Registration successful");
        
        return "redirect:/auth/login";
        
    }
}
