package com.example.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.entity.Category;
import com.example.entity.User;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long>{
    @Query(" select c from Category c where c.isSystem = true or c.owner = :user order by c.name " )
    List<Category> findAvailableCategories(@Param("user") User user);
    List<Category> findByOwner(User owner);
    List<Category> findByIsSystemTrue();
    boolean existsByOwnerAndNameIgnoreCase(User owner, String name);
    boolean existsByIsSystemTrueAndNameIgnoreCase(String name);
}
