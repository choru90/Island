package com.choru.island.model.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IslandBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "parent_member_uid")
    private ParentMember parentMember;

    @ManyToOne
    @JoinColumn(name = "shop_class_id")
    private ShopClass shopClass;

    @Column
    @CreationTimestamp
    private LocalDateTime createdAt;

    public IslandBooking(ParentMember parentMember, ShopClass shopClass) {
        this.parentMember = parentMember;
        this.shopClass = shopClass;
    }
}
