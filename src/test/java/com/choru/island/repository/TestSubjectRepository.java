package com.choru.island.repository;

import com.choru.island.model.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestSubjectRepository extends JpaRepository<Subject, Long> {
}
