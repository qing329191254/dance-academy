package com.forget.academy.repo;

import com.forget.academy.entity.DanceCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DanceCategoryRepo extends JpaRepository<DanceCategory, Long> {
    Optional<DanceCategory> findByCode(String code);

    List<DanceCategory> findByParentIdIsNullOrderBySortOrderAscIdAsc();

    List<DanceCategory> findByParentIdOrderBySortOrderAscIdAsc(Long parentId);

    List<DanceCategory> findByParentIdIsNullAndEnabledTrueOrderBySortOrderAscIdAsc();

    List<DanceCategory> findByParentIdAndEnabledTrueOrderBySortOrderAscIdAsc(Long parentId);

    List<DanceCategory> findAllByOrderBySortOrderAscIdAsc();

    long countByParentId(Long parentId);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);
}
