package com.choru.island.controller;

import com.choru.island.controller.dto.BookingCreateReq;
import com.choru.island.controller.dto.BookingStatusRes;
import com.choru.island.controller.dto.BookingUserListRes;
import com.choru.island.controller.dto.ParentMemberCreateReq;
import com.choru.island.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequiredArgsConstructor
public class BookingController {

    private final BookingService service;

    @PostMapping("/booking")
    public void booking(@RequestBody BookingCreateReq req){
        service.booking(req.classId(), req.uid());
    }

    @DeleteMapping("/booking")
    public void cancel(@RequestParam(value = "bookingId")Long bookingId){
        service.cancel(bookingId);
    }

    @GetMapping("/booking")
    public List<BookingUserListRes> getBookingUserList(@RequestParam(value = "classId") Long classId){
        return service.getBookingUserList(classId);
    }

    @GetMapping("/booking/status")
    public BookingStatusRes getBookingStatus(@RequestParam(value = "classId") Long classId){
        return service.getBookingStatus(classId);
    }


    @PostMapping("/member")
    public UUID createMember(@RequestBody ParentMemberCreateReq req){
        return service.createParentMember(req.name(), req.email());
    }
}
