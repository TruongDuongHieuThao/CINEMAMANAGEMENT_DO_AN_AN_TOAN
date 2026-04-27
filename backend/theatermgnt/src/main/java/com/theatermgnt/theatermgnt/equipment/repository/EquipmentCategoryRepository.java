package com.theatermgnt.theatermgnt.equipment.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.theatermgnt.theatermgnt.equipment.entity.EquipmentCategory;

@Repository
public interface EquipmentCategoryRepository extends JpaRepository<EquipmentCategory, String> {
    Optional<EquipmentCategory> findByName(String name);

    boolean existsByName(String name);
}
