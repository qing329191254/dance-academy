package com.forget.academy.repo;

import com.forget.academy.entity.Banner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BannerRepo extends JpaRepository<Banner, Long> {
    List<Banner> findByEnabledTrueOrderBySortOrderAscIdAsc();

    List<Banner> findAllByOrderBySortOrderAscIdAsc();
}
