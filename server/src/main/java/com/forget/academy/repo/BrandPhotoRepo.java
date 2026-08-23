package com.forget.academy.repo;

import com.forget.academy.entity.BrandPhoto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BrandPhotoRepo extends JpaRepository<BrandPhoto, Long> {
    List<BrandPhoto> findAllByOrderBySortOrderAscIdAsc();
}
