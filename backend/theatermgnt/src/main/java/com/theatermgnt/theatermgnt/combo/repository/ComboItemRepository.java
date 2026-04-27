package com.theatermgnt.theatermgnt.combo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.theatermgnt.theatermgnt.combo.entity.ComboItem;

public interface ComboItemRepository extends JpaRepository<ComboItem, String> {
    List<ComboItem> findByComboId(String comboId);

    boolean existsByNameAndComboId(String name, String comboId);
}
