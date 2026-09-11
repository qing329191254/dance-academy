package com.forget.academy.service;

import com.forget.academy.entity.Schedule;
import com.forget.academy.repo.AppUserRepo;
import com.forget.academy.repo.BookingRepo;
import com.forget.academy.repo.CheckinPendingRepo;
import com.forget.academy.repo.ClassSessionCancelRepo;
import com.forget.academy.repo.ScheduleRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingCancelSessionLowEnrollmentGuardTest {

    @Mock private ScheduleRepo scheduleRepo;
    @Mock private AdminAccessService adminAccessService;
    @Mock private BookingRepo bookingRepo;
    @Mock private AppUserRepo appUserRepo;
    @Mock private BookingRemindService bookingRemindService;
    @Mock private UserCardService userCardService;
    @Mock private UserCampusService userCampusService;
    @Mock private DanceCategoryService danceCategoryService;
    @Mock private ClassSessionCancelRepo classSessionCancelRepo;
    @Mock private CheckinPendingRepo checkinPendingRepo;

    private BookingService service;

    @BeforeEach
    void setUp() {
        service = new BookingService(
                scheduleRepo,
                adminAccessService,
                bookingRepo,
                appUserRepo,
                bookingRemindService,
                userCardService,
                userCampusService,
                danceCategoryService,
                classSessionCancelRepo,
                checkinPendingRepo
        );
    }

    @Test
    void cancelSessionForLowEnrollment_skipsWhenCompletedBookingsExist() {
        Schedule schedule = new Schedule();
        schedule.setId(55L);
        when(classSessionCancelRepo.existsByScheduleIdAndClassDate(55L, "2026-09-11")).thenReturn(false);
        when(bookingRepo.countByScheduleIdAndClassDateAndStatus(55L, "2026-09-11", "已完成")).thenReturn(5L);

        service.cancelSessionForLowEnrollment(schedule, "2026-09-11", 0, 6);

        verify(classSessionCancelRepo, never()).save(any());
        verify(bookingRepo, never()).findByScheduleIdAndClassDateAndStatusOrderByIdAsc(anyLong(), anyString(), anyString());
    }
}
