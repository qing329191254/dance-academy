package com.forget.academy.repo;

import com.forget.academy.entity.GrowthTrack;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GrowthTrackRepo extends JpaRepository<GrowthTrack, Long> {
    long count();

    long countByCampusId(String campusId);

    Optional<GrowthTrack> findByTrackKey(String trackKey);

    Optional<GrowthTrack> findByCampusIdAndTrackKey(String campusId, String trackKey);

    List<GrowthTrack> findByCampusIdOrderByLineKeyAscSortOrderAscIdAsc(String campusId);

    List<GrowthTrack> findByCampusIdAndLineKeyAndEnabledTrueOrderBySortOrderAscIdAsc(String campusId, String lineKey);

    List<GrowthTrack> findByLineKeyAndEnabledTrueOrderBySortOrderAscIdAsc(String lineKey);

    List<GrowthTrack> findAllByOrderByLineKeyAscSortOrderAscIdAsc();
}
