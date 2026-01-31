package com.example.service;

import java.util.List;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.entity.Category;
import com.example.entity.User;
import com.example.repository.CategoryRepository;
import com.example.repository.EntryRepository;
import com.example.repository.UserRepository;

@Service
public class CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private EntryRepository entryRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Transactional
    public List<Category> getAvailableCategoriesForUser(String username){
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        return categoryRepository.findAvailableCategories(user);
    }
    
    @Transactional
    public List<Category> getCustomCategoriesForUser(String username){
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        return categoryRepository.findByOwner(user);
    }
    
    @Transactional
    public void createCustomCategory(String username, String rawName) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        String name = normalizeName(rawName);
        /*if (categoryRepository.existsByOwnerAndName(user, name)) {
            throw new IllegalArgumentException("Category name already exsisted: " + name);
        }*/
        
        Category category = new Category();
        category.setName(name);
        category.setSystem(false);
        category.setOwner(user);
        
        categoryRepository.save(category);
    }
    
    private String normalizeName(String raw) {
        if (raw == null) {
            return " ";
        }
        String name = raw.trim().replaceAll("\\s+", " ");
        return name;
    }
    
    @Transactional
    public void deleteCustomCategory(String username, Long categoryId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categoryId));
        
        /*if (category.isSystem()) {
            throw new IllegalStateException("System default categories cannot be deleted");
        }*/
        /*if (category.getOwner() == null || !category.getOwner().getId().equals(user.getId())) {
            throw new SecurityException("Cannot delete categories owned by other users");
        }
        if (entryRepository.countByCategory(category) > 0) {
            throw new IllegalStateException("This category is in use. Please update the related records before deleting it.");
        }*/
        categoryRepository.delete(category);
    }
}
