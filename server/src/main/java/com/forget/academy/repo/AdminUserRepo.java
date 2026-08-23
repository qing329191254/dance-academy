package com.forget.academy.repo;

import com.forget.academy.entity.AdminUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminUserRepo extends JpaRepository<AdminUser, Long> {
    Optional<AdminUser> findByUsername(String username);
}
