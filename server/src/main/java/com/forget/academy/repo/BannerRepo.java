package com.forget.academy.repo;

import com.forget.academy.entity.Banner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BannerRepo extends JpaRepository<Banner, Long> {
    long countByCampusId(String campusId);

    List<Banner> findByCampusIdOrderBySortOrderAscIdAsc(String campusId);

    List<Banner> findByCampusIdAndEnabledTrueOrderBySortOrderAscIdAsc(String campusId);

    List<Banner> findByEnabledTrueOrderBySortOrderAscIdAsc();

    List<Banner> findAllByOrderBySortOrderAscIdAsc();
}
