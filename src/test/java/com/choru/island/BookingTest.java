package com.choru.island;

import com.choru.island.controller.dto.BookingCreateReq;
import com.choru.island.model.entity.*;
import com.choru.island.model.repository.IslandBookingRepository;
import com.choru.island.model.repository.ParentMemberRepository;
import com.choru.island.model.repository.ShopClassRepository;
import com.choru.island.repository.TestShopRepository;
import com.choru.island.repository.TestSubjectRepository;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BookingTest {

    @LocalServerPort
    int port;

    @Autowired
    ParentMemberRepository memberRepository;

    @Autowired
    ShopClassRepository classRepository;

    @Autowired
    TestSubjectRepository subjectRepository;

    @Autowired
    TestShopRepository shopRepository;

    @Autowired
    IslandBookingRepository islandBookingRepository;

    @BeforeEach
    public void setUp() {
        RestAssured.port = port;
    }

    @Test
    @DisplayName("예약 api : 400 - 유저 정보가 없는 경우")
    void createBookingBadRequestNoMemberTest(){
        // given


        // when
        ExtractableResponse<Response> extract = request()
                .body(new BookingCreateReq(1L, UUID.randomUUID()))
                .post("/booking")
                .then().log().all().extract();
        // then

        assertEquals(HttpStatus.BAD_REQUEST.value(), extract.statusCode());
    }

    @Test
    @DisplayName("예약 api : 400 - 수업 정보가 없는 경우")
    void createBookingBadRequestNoHShopClassTest(){
        // given
        memberRepository.save(new ParentMember("홍길동","abc123@gmail.com"));

        // when
        ExtractableResponse<Response> extract = request()
                .body(new BookingCreateReq(1L, UUID.randomUUID()))
                .post("/booking")
                .then().log().all().extract();
        // then

        assertEquals(HttpStatus.BAD_REQUEST.value(), extract.statusCode());
    }

    @Test
    @DisplayName("예약 api : 400 - 최대인원 초과인 경우")
    void createBookingBadRequestMaxCountTest() throws NoSuchFieldException, IllegalAccessException {
        // given
        ParentMember member = memberRepository.save(new ParentMember("홍길동", "abc123@gmail.com"));

        ShopClass shopClass = new ShopClass();

        Field idField = shopClass.getClass().getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(shopClass, 30000L);

        Field memberMaxField = shopClass.getClass().getDeclaredField("memberMax");
        memberMaxField.setAccessible(true);
        memberMaxField.set(shopClass, 1);

        Field classDateField = shopClass.getClass().getDeclaredField("classDate");
        classDateField.setAccessible(true);
        classDateField.set(shopClass, LocalDate.of(2024, 03, 24));

        Field classTimeField = shopClass.getClass().getDeclaredField("classTime");
        classTimeField.setAccessible(true);
        classTimeField.set(shopClass, LocalTime.of(12, 00, 00));

        Shop shop = new Shop();

        Shop savedShop = shopRepository.save(shop);
        Subject savedSubject = subjectRepository.save(new Subject());

        Field shopField = shopClass.getClass().getDeclaredField("shop");
        shopField.setAccessible(true);
        shopField.set(shopClass, savedShop);

        Field subjectField = shopClass.getClass().getDeclaredField("subject");
        subjectField.setAccessible(true);
        subjectField.set(shopClass, savedSubject);

        ShopClass savedShopClass = classRepository.save(shopClass);
        // when
        ExtractableResponse<Response> extract = request()
                .body(new BookingCreateReq(savedShopClass.getId(), member.getUid()))
                .post("/booking")
                .then().log().all().extract();
        // then

        assertEquals(HttpStatus.BAD_REQUEST.value(), extract.statusCode());
    }

    @Test
    @DisplayName("예약 api : 200")
    void createBookingTest() throws NoSuchFieldException, IllegalAccessException {
        // given
        ParentMember member = memberRepository.save(new ParentMember("홍길동", "abc123@gmail.com"));

        ShopClass shopClass = new ShopClass();

        Field memberMaxField = shopClass.getClass().getDeclaredField("memberMax");
        memberMaxField.setAccessible(true);
        memberMaxField.set(shopClass, 20);

        Field classDateField = shopClass.getClass().getDeclaredField("classDate");
        classDateField.setAccessible(true);
        classDateField.set(shopClass, LocalDate.of(2024, 03, 24));

        Field classTimeField = shopClass.getClass().getDeclaredField("classTime");
        classTimeField.setAccessible(true);
        classTimeField.set(shopClass, LocalTime.of(12, 00, 00));

        Shop shop = new Shop();

        Shop savedShop = shopRepository.save(shop);
        Subject savedSubject = subjectRepository.save(new Subject());

        Field shopField = shopClass.getClass().getDeclaredField("shop");
        shopField.setAccessible(true);
        shopField.set(shopClass, savedShop);

        Field subjectField = shopClass.getClass().getDeclaredField("subject");
        subjectField.setAccessible(true);
        subjectField.set(shopClass, savedSubject);

        ShopClass savedShopClass = classRepository.save(shopClass);
        // when
        ExtractableResponse<Response> extract = request()
                .body(new BookingCreateReq(savedShopClass.getId(), member.getUid()))
                .post("/booking")
                .then().log().all().extract();
        // then

        assertEquals(HttpStatus.OK.value(), extract.statusCode());
        List<IslandBooking> bookingList = islandBookingRepository.findAll();
        assertTrue(0 < bookingList.size());
        assertEquals(member.getUid(), bookingList.get(0).getParentMember().getUid());
        assertEquals(shopClass.getId(), bookingList.get(0).getShopClass().getId());
    }

    // 예약 중복 case
    // 예약날짜 오류 case


    // 삭제
    // 예약 유저 목록 ㅈ회
    // 예약 현황 조회
    public static RequestSpecification request() {
        return RestAssured.given()
                .contentType("application/json")
                .log()
                .all();
    }
}
