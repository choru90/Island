package com.choru.island.model.repository;

import com.choru.island.model.entity.ParentMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ParentMemberRepository extends JpaRepository<ParentMember, UUID> {
}
