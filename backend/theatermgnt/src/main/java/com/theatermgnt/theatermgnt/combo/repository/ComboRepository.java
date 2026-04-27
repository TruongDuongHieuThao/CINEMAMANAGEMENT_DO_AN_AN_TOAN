package com.theatermgnt.theatermgnt.combo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.theatermgnt.theatermgnt.combo.entity.Combo;

public interface ComboRepository extends JpaRepository<Combo, String> {
    boolean existsByName(String name);
}
