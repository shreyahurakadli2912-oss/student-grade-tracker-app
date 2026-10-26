package com.studentgradetracker.service;

import com.studentgradetracker.dto.GradeScaleDto;
import com.studentgradetracker.entity.GradeScaleRule;
import com.studentgradetracker.repository.GradeScaleRuleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class GradeScaleService {

    private final GradeScaleRuleRepository repository;

    public GradeScaleService(GradeScaleRuleRepository repository) {
        this.repository = repository;
    }

    public void initDefaultRulesIfEmpty() {
        if (repository.count() == 0) {
            resetToDefaults();
        }
    }

    public List<GradeScaleDto> getAllRules() {
        return repository.findAllByOrderBySortOrderAsc().stream()
                .map(r -> new GradeScaleDto(r.getId(), r.getGrade(), r.getMinPercentage(), r.getMaxPercentage(), r.getDescription(), r.getSortOrder()))
                .collect(Collectors.toList());
    }

    public List<GradeScaleDto> resetToDefaults() {
        repository.deleteAll();
        List<GradeScaleRule> defaults = List.of(
                new GradeScaleRule("A", 90.0, 100.0, "Excellent (90% - 100%)", 1),
                new GradeScaleRule("B", 80.0, 89.99, "Very Good (80% - 89.99%)", 2),
                new GradeScaleRule("C", 70.0, 79.99, "Good (70% - 79.99%)", 3),
                new GradeScaleRule("D", 60.0, 69.99, "Satisfactory / Pass (60% - 69.99%)", 4),
                new GradeScaleRule("F", 0.0, 59.99, "Fail (Below 60%)", 5)
        );
        repository.saveAll(defaults);
        return getAllRules();
    }
}
