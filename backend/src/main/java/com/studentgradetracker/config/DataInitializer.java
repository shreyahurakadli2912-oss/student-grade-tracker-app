package com.studentgradetracker.config;

import com.studentgradetracker.entity.Subject;
import com.studentgradetracker.repository.SubjectRepository;
import com.studentgradetracker.service.GradeScaleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final GradeScaleService gradeScaleService;
    private final SubjectRepository subjectRepository;

    public DataInitializer(GradeScaleService gradeScaleService, SubjectRepository subjectRepository) {
        this.gradeScaleService = gradeScaleService;
        this.subjectRepository = subjectRepository;
    }

    @Override
    public void run(String... args) {
        log.info("Initializing system configurations...");

        // Ensure default grade boundaries exist
        gradeScaleService.initDefaultRulesIfEmpty();

        // Seed initial standard subjects if database is fresh
        if (subjectRepository.count() == 0) {
            List<Subject> initialSubjects = List.of(
                    new Subject("ENG101", "English Language & Literature", 100.0, 40.0, true),
                    new Subject("MTH101", "Mathematics & Calculus", 100.0, 40.0, true),
                    new Subject("SCI101", "General Science & Physics", 100.0, 40.0, true)
            );
            subjectRepository.saveAll(initialSubjects);
            log.info("Initialized default curriculum subjects (ENG101, MTH101, SCI101).");
        }

        log.info("Student Grade Management System initialized successfully.");
    }
}
