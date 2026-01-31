package com.example.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.entity.Category;
import com.example.entity.Entry;
import com.example.entity.EntryType;
import com.example.entity.User;

@Repository
public interface EntryRepository extends JpaRepository<Entry, Long> {
    List<Entry> findByUserOrderByDateDesc(User user);
    List<Entry> findByUserAndCategory(User user, Category category);
    List<Entry> findByUserUsernameAndTypeAndDateBetweenOrderByDateDesc(String username, EntryType  type, LocalDate start, LocalDate end);
    /*List<Entry> findByUserAndDateBetween(User user, LocalDate startDate, LocalDate endDate);*/
    List<Entry> findByUserUsernameAndDateGreaterThanEqualAndDateLessThanOrderByDateDesc(String username, LocalDate start, LocalDate end);
    long countByCategory(Category category);
    @Query("select coalesce(sum(entry.amount),0) from Entry entry where entry.user = :user and entry.type = :type and entry.date >= :start and entry.date < :end")
    BigDecimal sumAmountByUserAndTypeAndMonth(
            @Param("user") User user,
            @Param("type") EntryType type,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);
}
