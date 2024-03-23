package com.choru.island.repository;

import com.choru.island.model.entity.Shop;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestShopRepository extends JpaRepository<Shop, Long> {
}
