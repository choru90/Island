package com.choru.island;

import com.choru.island.controller.dto.BookingCreateReq;
import com.choru.island.controller.dto.BookingUserListRes;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

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
        islandBookingRepository.deleteAll();;
        classRepository.deleteAll();
        memberRepository.deleteAll();
        subjectRepository.deleteAll();
        shopRepository.deleteAll();
    }

    @Test
    @DisplayName("예약 api : 400 - 유저 정보가 없는 경우")
    void createBookingBadRequestNoMemberTest(){
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
    @DisplayName("예약 api 예약이 중복된 경우 : 400")
    void createBookingDuplicateTest() throws NoSuchFieldException, IllegalAccessException {
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
        islandBookingRepository.save(new IslandBooking(member, savedShopClass));
        // when
        ExtractableResponse<Response> extract = request()
                .body(new BookingCreateReq(savedShopClass.getId(), member.getUid()))
                .post("/booking")
                .then().log().all().extract();

        // then
        assertEquals(HttpStatus.BAD_REQUEST.value(), extract.statusCode());
    }

    @Test
    @DisplayName("예약 api 예약 날짜가 잘못된 경우 : 400")
    void createBookingDateExceptionTest() throws NoSuchFieldException, IllegalAccessException {
        // given
        ParentMember member = memberRepository.save(new ParentMember("홍길동", "abc123@gmail.com"));

        ShopClass shopClass = new ShopClass();

        Field memberMaxField = shopClass.getClass().getDeclaredField("memberMax");
        memberMaxField.setAccessible(true);
        memberMaxField.set(shopClass, 20);

        Field classDateField = shopClass.getClass().getDeclaredField("classDate");
        classDateField.setAccessible(true);
        classDateField.set(shopClass, LocalDate.of(2023, 03, 24));

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
        classDateField.set(shopClass, LocalDate.now().plusDays(2));

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


    @Test
    @DisplayName("예약 취소 api :200")
    void cancelTest() throws NoSuchFieldException, IllegalAccessException {
        // given
        ParentMember member = memberRepository.save(new ParentMember("홍길동", "abc123@gmail.com"));

        ShopClass shopClass = new ShopClass();

        Field memberMaxField = shopClass.getClass().getDeclaredField("memberMax");
        memberMaxField.setAccessible(true);
        memberMaxField.set(shopClass, 20);

        Field classDateField = shopClass.getClass().getDeclaredField("classDate");
        classDateField.setAccessible(true);
        classDateField.set(shopClass, LocalDate.now().plusDays(2));

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

        IslandBooking booking = new IslandBooking(member, shopClass);
        IslandBooking savedBooking = islandBookingRepository.save(booking);
        // when
        ExtractableResponse<Response> extract = request()
                .param("bookingId", savedBooking.getId())
                .delete("/booking")
                .then().log().all().extract();
        // then
        List<IslandBooking> all = islandBookingRepository.findAll();
        assertTrue(all.isEmpty());
    }

    @Test
    @DisplayName("예약 취소 api - id가 올바르지 않은 경우:400")
    void cancelIdExceptionTest() throws NoSuchFieldException, IllegalAccessException {
        // given

        // when
        ExtractableResponse<Response> extract = request()
                .param("bookingId", 1L)
                .delete("/booking")
                .then().log().all().extract();
        // then
        assertEquals(HttpStatus.BAD_REQUEST.value(), extract.statusCode());
    }

    @Test
    @DisplayName("예약자 현황 API : 200")
    void getBookingUserList() throws NoSuchFieldException, IllegalAccessException {
        // given
        ParentMember member = memberRepository.save(new ParentMember("홍길동", "abc123@gmail.com"));

        ShopClass shopClass = new ShopClass();

        Field memberMaxField = shopClass.getClass().getDeclaredField("memberMax");
        memberMaxField.setAccessible(true);
        memberMaxField.set(shopClass, 20);

        Field classDateField = shopClass.getClass().getDeclaredField("classDate");
        classDateField.setAccessible(true);
        classDateField.set(shopClass, LocalDate.now().plusDays(2));

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

        classRepository.save(shopClass);
        IslandBooking booking = new IslandBooking(member, shopClass);
        IslandBooking savedBooking = islandBookingRepository.save(booking);
        // when
        ExtractableResponse<Response> extract = request()
                .param("classId", shopClass.getId())
                .get("/booking")
                .then().log().all().extract();
        // then
        assertEquals(HttpStatus.OK.value(), extract.statusCode());
        List<BookingUserListRes> list = extract.body().jsonPath().getList(".", BookingUserListRes.class);
        assertEquals(shop.getName(),list.get(0).shopName());
        assertEquals(savedSubject.getName(),list.get(0).className());
        assertEquals(member.getName(),list.get(0).name());
    }

    @Test
    @DisplayName("예약자 현황 API - 수업 id가 올바르지 않은 경우 : 400")
    void getBookingUserListException(){

        // when
        ExtractableResponse<Response> extract = request()
                .param("classId", 1L)
                .get("/booking")
                .then().log().all().extract();
        // then
        assertEquals(HttpStatus.BAD_REQUEST.value(), extract.statusCode());
    }

    @Test
    @DisplayName("예약 현황 API : 200")
    void getBookingStatus() throws NoSuchFieldException, IllegalAccessException {
        // given

        ParentMember member = memberRepository.save(new ParentMember("홍길동", "abc123@gmail.com"));

        ShopClass shopClass = new ShopClass();

        Field memberMaxField = shopClass.getClass().getDeclaredField("memberMax");
        memberMaxField.setAccessible(true);
        memberMaxField.set(shopClass, 20);

        Field classDateField = shopClass.getClass().getDeclaredField("classDate");
        classDateField.setAccessible(true);
        classDateField.set(shopClass, LocalDate.now().plusDays(2));

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

        ShopClass savedClass = classRepository.save(shopClass);
        IslandBooking booking = new IslandBooking(member, shopClass);
        islandBookingRepository.save(booking);

        // when
        ExtractableResponse<Response> extract = request()
                .param("classId", savedClass.getId())
                .get("/booking/status")
                .then().log().all().extract();
        // then
        assertEquals(HttpStatus.OK.value(), extract.statusCode());
        assertEquals(savedSubject.getName(), extract.body().jsonPath().getString("name"));
        assertEquals(shopClass.getMemberMax(), extract.body().jsonPath().getInt("memberMaxCount"));
        assertEquals(islandBookingRepository.count(), extract.body().jsonPath().getInt("memberCount"));

    }

    @Test
    @DisplayName("예약 현황 API 수업 ID가 올바르지 않은 경우 : 400")
    void getBookingStatusException() throws NoSuchFieldException, IllegalAccessException {
        // given
        // when
        ExtractableResponse<Response> extract = request()
                .param("classId", 1L)
                .get("/booking/status")
                .then().log().all().extract();
        // then
        assertEquals(HttpStatus.BAD_REQUEST.value(), extract.statusCode());
    }


    public static RequestSpecification request() {
        return RestAssured.given()
                .contentType("application/json")
                .log()
                .all();
    }
}
