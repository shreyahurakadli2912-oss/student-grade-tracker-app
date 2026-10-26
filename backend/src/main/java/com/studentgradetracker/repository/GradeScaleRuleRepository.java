package com.studentgradetracker.repository;

import com.studentgradetracker.entity.GradeScaleRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GradeScaleRuleRepository extends JpaRepository<GradeScaleRule, Long> {

    List<GradeScaleRule> findAllByOrderBySortOrderAsc();
}
