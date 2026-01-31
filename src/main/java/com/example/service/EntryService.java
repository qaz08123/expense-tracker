package com.example.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.entity.Category;
import com.example.entity.Entry;
import com.example.entity.EntryType;
import com.example.entity.User;
import com.example.repository.CategoryRepository;
import com.example.repository.EntryRepository;
import com.example.repository.UserRepository;

@Service
public class EntryService {
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private EntryRepository entryRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Transactional
    public List<Entry> listForUsers(String username){
        User user = userRepository.findByUsername(username).get();
        return entryRepository.findByUserOrderByDateDesc(user);
    }
    
    public List<Entry> findByMonth(String username, YearMonth month){
        LocalDate start = month.atDay(1);
        LocalDate end = month.plusMonths(1).atDay(1);
        return entryRepository.findByUserUsernameAndDateGreaterThanEqualAndDateLessThanOrderByDateDesc(username, start, end);
    }
    
    public List<Entry> findByMonthAndType(String username, EntryType  type,YearMonth month){
        LocalDate start = month.atDay(1);
        LocalDate end = month.plusMonths(1).atDay(1);
        return entryRepository.findByUserUsernameAndTypeAndDateBetweenOrderByDateDesc(username, type, start, end);
    }
    
    @Transactional
    public Entry getOwnedEntry(String username, Long entryId) {
        User user = userRepository.findByUsername(username).get();
        Entry entry = entryRepository.findById(entryId).get();
        if (!entry.getUser().getId().equals(user.getId())) {
            throw new SecurityException("You cannot acess other user's entry");
        }
        return entry;
    }
    
    @Transactional
    public void create(String username, Entry entry, Long categoryId) {
        User user = userRepository.findByUsername(username).get();
        Category category = categoryRepository.findById(categoryId).get();
        if (!category.isSystem()) {
            if(category.getOwner() == null || !category.getOwner().getId().equals(user.getId())) {
                throw new SecurityException("You cannot use other's category");
            }
        }
        //normalizeEntry(entry);
        
        entry.setId(null);
        entry.setUser(user);
        entry.setCategory(category);
        
        LocalDate now = LocalDate.now();
        
        entry.setDate(now);
        
        entryRepository.save(entry);
    }
    
    /**private void normalizeEntry(Entry entry) {
        if (entry.getType() == null) {
            throw new IllegalArgumentException("Entry type is required");
        }
        if (entry.getAmount() == null) {
            throw new IllegalArgumentException("Amount is required");
        }
        if(entry.getDate() == null) {
            entry.setDate(LocalDate.now());
        }
        if (entry.getNote() != null && entry.getNote().length() > 255) {
            entry.setNote(entry.getNote().substring(0, 255));
        }
    }**/
    
    @Transactional
    public void update(String username, Long entryId, Entry entry, Long categoryId) {
        Entry existingEntry = getOwnedEntry(username, entryId);
        Category category = categoryRepository.findById(categoryId).get();
        
        //normalizeEntry(entry);
        
        existingEntry.setType(entry.getType());
        existingEntry.setAmount(entry.getAmount());
        if (entry.getDate() != null) {
            existingEntry.setDate(entry.getDate());
        }
        existingEntry.setNote(entry.getNote());
        existingEntry.setCategory(category);
        
        entryRepository.save(existingEntry);
    }
    
    @Transactional
    public void delete(String username, Long entryId) {
        Entry entry = getOwnedEntry(username, entryId);
        entryRepository.delete(entry);
    }
    
    @Transactional
    public BigDecimal getMonthlyTotal(String username, EntryType type, YearMonth yearmonth) {
        User user = userRepository.findByUsername(username).get();
        LocalDate start = yearmonth.atDay(1);
        LocalDate end = yearmonth.plusMonths(1).atDay(1);
        
        return entryRepository.sumAmountByUserAndTypeAndMonth(user, type, start, end);
    }
    
    @Transactional
    public Map<String, BigDecimal> getMonthlySummary(String username, YearMonth yearmonth){
        BigDecimal income = getMonthlyTotal(username, EntryType.INCOME, yearmonth);
        BigDecimal expense = getMonthlyTotal(username, EntryType.EXPENSE, yearmonth);
        
        return Map.of("income",income, 
                              "expense",expense,
                              "net",income.subtract(expense));
    }
}
