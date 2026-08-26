package com.forget.academy.repo;

import com.forget.academy.entity.School;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SchoolRepo extends JpaRepository<School, Long> {
    List<School> findByEnabledTrueOrderBySortOrderAscIdAsc();

    List<School> findAllByOrderBySortOrderAscIdAsc();

    Optional<School> findByName(String name);

    boolean existsByName(String name);

    long countByName(String name);
}
