package com.theatermgnt.theatermgnt.authorization.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.theatermgnt.theatermgnt.authorization.entity.Permission;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, String> {}
