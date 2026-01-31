package com.example.security;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.entity.Category;
import com.example.repository.CategoryRepository;

@Configuration
public class CategoryInitializerConfig {

    @Bean
    ApplicationRunner initCategories(CategoryRepository categoryRepository) {
        return args -> {
            if (!categoryRepository.findByIsSystemTrue().isEmpty()) {
                return;
            }
            
            String[] defaultCategories = {
                    "Dining", "Transportation", "Groceries", "Rent", "Water", "Electricity", "Gas", "Internet", "Phone", "Medical", "Shopping", "Entertainment", "Education", "Others", "Salary", "Bonus"
            };
            
            for (String name : defaultCategories) {
                Category category = new Category();
                category.setName(name);
                category.setSystem(true);
                category.setOwner(null);
                
                categoryRepository.save(category);
            }
        };
    }
}
