package com.choru.island.model.repository;

import com.choru.island.model.entity.IslandBooking;
import com.choru.island.model.entity.ParentMember;
import com.choru.island.model.entity.ShopClass;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IslandBookingRepository extends JpaRepository<IslandBooking, Long> {

    List<IslandBooking> findAllByShopClass(ShopClass classId);
    long countByShopClass(ShopClass shopClass);

    boolean existsByParentMemberAndShopClass(ParentMember parentMember, ShopClass shopClass);
}
