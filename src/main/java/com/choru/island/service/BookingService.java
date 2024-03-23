package com.choru.island.service;

import com.choru.island.controller.dto.BookingStatusRes;
import com.choru.island.controller.dto.BookingUserListRes;
import com.choru.island.model.entity.IslandBooking;
import com.choru.island.model.entity.ParentMember;
import com.choru.island.model.entity.ShopClass;
import com.choru.island.model.repository.IslandBookingRepository;
import com.choru.island.model.repository.ParentMemberRepository;
import com.choru.island.model.repository.ShopClassRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final IslandBookingRepository islandBookingRepository;
    private final ParentMemberRepository memberRepository;
    private final ShopClassRepository classRepository;

    private final static int ADD_MEMBER_COUNT = 1;

    @Transactional
    public void booking(Long classId, UUID uid) {
        ParentMember member = memberRepository.findById(uid).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "유저 정보가 올바르지 않습니다."));
        ShopClass shopClass = classRepository.findById(classId).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "수업 정보가 올바르지 않습니다."));

        if(shopClass.getMemberMax() <= islandBookingRepository.countByShopClass(shopClass) + ADD_MEMBER_COUNT){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "예약 인원을 초과 하였습니다.");
        }

        if(islandBookingRepository.existsByParentMemberAndShopClass(member, shopClass)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "예약이 중복되 었습니다.");
        }

        if(LocalDate.now().plusDays(1).isAfter(shopClass.getClassDate())
                && LocalDate.now().plusDays(14).isBefore(shopClass.getClassDate())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "예약날짜가 올바르지 않습니다.");
        }

        IslandBooking booking = new IslandBooking(member, shopClass);
        islandBookingRepository.save(booking);
    }

    @Transactional
    public void cancel(Long bookingId) {
        IslandBooking booking = islandBookingRepository.findById(bookingId).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "취소하고자 하는 예약 정보가 올바르지 않습니다."));
        islandBookingRepository.delete(booking);
    }

    public List<BookingUserListRes> getBookingUserList(Long classId) {
        ShopClass shopClass = classRepository.findById(classId).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "수업정보가 올바르지 않습니다."));
        List<IslandBooking> bookingList = islandBookingRepository.findAllByShopClass(shopClass);

        List<BookingUserListRes> resList = bookingList.stream()
                                                      .map(it -> new BookingUserListRes(it.getShopClass().getShop().getName(),
                                                                                        it.getShopClass().getSubject().getName(),
                                                                                        it.getParentMember().getName()))
                                                      .toList();
        return resList;
    }

    public BookingStatusRes getBookingStatus(Long classId) {
        ShopClass shopClass = classRepository.findById(classId).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "수업정보가 올바르지 않습니다."));
        long memberCount = islandBookingRepository.countByShopClass(shopClass);
        return new BookingStatusRes(shopClass.getSubject().getName(), shopClass.getMemberMax(), memberCount);
    }

    public UUID createParentMember(String name, String email){
        ParentMember saved = memberRepository.save(new ParentMember(name, email));
        return saved.getUid();
    }
}
