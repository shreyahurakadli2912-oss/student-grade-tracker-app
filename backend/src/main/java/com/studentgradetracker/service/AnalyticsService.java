package com.studentgradetracker.service;

import com.studentgradetracker.dto.ClassStatisticsResponse;
import com.studentgradetracker.dto.StudentResponse;
import com.studentgradetracker.dto.SubjectMarkResponse;
import com.studentgradetracker.entity.Student;
import com.studentgradetracker.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AnalyticsService {

    private final StudentRepository studentRepository;
    private final GradeCalculationService gradeCalculationService;

    public AnalyticsService(StudentRepository studentRepository, GradeCalculationService gradeCalculationService) {
        this.studentRepository = studentRepository;
        this.gradeCalculationService = gradeCalculationService;
    }

    public ClassStatisticsResponse getClassStatistics(String className) {
        ClassStatisticsResponse stats = new ClassStatisticsResponse();
        boolean isAll = (className == null || className.isBlank() || className.equalsIgnoreCase("ALL"));
        stats.setClassName(isAll ? "All Classes" : className);

        List<Student> students = isAll ? studentRepository.findAll() : studentRepository.findByClassNameIgnoreCase(className.trim());

        if (students.isEmpty()) {
            stats.setTotalStudents(0);
            stats.setAveragePercentage(0.0);
            stats.setHighestMarks(0.0);
            stats.setLowestMarks(0.0);
            stats.setTotalPassed(0);
            stats.setTotalFailed(0);
            stats.setPassPercentage(0.0);
            return stats;
        }

        List<StudentResponse> studentResponses = students.stream()
                .map(gradeCalculationService::mapToStudentResponse)
                .collect(Collectors.toList());

        List<StudentResponse> ranked = gradeCalculationService.assignDenseRankings(studentResponses);

        long total = ranked.size();
        stats.setTotalStudents(total);

        double sumPercentage = 0.0;
        double maxMark = -1.0;
        double minMark = Double.MAX_VALUE;
        long passedCount = 0;
        long failedCount = 0;

        Map<String, Long> gradeDist = new HashMap<>();
        gradeDist.put("A", 0L);
        gradeDist.put("B", 0L);
        gradeDist.put("C", 0L);
        gradeDist.put("D", 0L);
        gradeDist.put("F", 0L);

        Map<String, Double> subjectSum = new HashMap<>();
        Map<String, Integer> subjectCount = new HashMap<>();

        for (StudentResponse s : ranked) {
            double p = s.getPercentage();
            sumPercentage += p;

            double m = s.getMarks() != null ? s.getMarks() : 0.0;
            if (m > maxMark) {
                maxMark = m;
            }
            if (m < minMark) {
                minMark = m;
            }

            if ("PASS".equalsIgnoreCase(s.getStatus())) {
                passedCount++;
            } else {
                failedCount++;
            }

            String g = s.getGrade();
            if (g != null && gradeDist.containsKey(g)) {
                gradeDist.put(g, gradeDist.get(g) + 1);
            }

            // Subject level marks
            if (s.getSubjectMarks() != null) {
                for (SubjectMarkResponse sm : s.getSubjectMarks()) {
                    String subKey = sm.getSubjectName() != null ? sm.getSubjectName() : sm.getSubjectCode();
                    if (subKey != null && sm.getMarksObtained() != null) {
                        subjectSum.put(subKey, subjectSum.getOrDefault(subKey, 0.0) + sm.getMarksObtained());
                        subjectCount.put(subKey, subjectCount.getOrDefault(subKey, 0) + 1);
                    }
                }
            }
        }

        double avgPercentage = GradeCalculationService.roundTwoDecimals(sumPercentage / total);
        stats.setAveragePercentage(avgPercentage);
        stats.setHighestMarks(maxMark >= 0 ? GradeCalculationService.roundTwoDecimals(maxMark) : 0.0);
        stats.setLowestMarks(minMark != Double.MAX_VALUE ? GradeCalculationService.roundTwoDecimals(minMark) : 0.0);
        stats.setTotalPassed(passedCount);
        stats.setTotalFailed(failedCount);

        double passRate = total > 0 ? GradeCalculationService.roundTwoDecimals((double) passedCount / total * 100.0) : 0.0;
        stats.setPassPercentage(passRate);

        // Highest and Lowest students (handle ties)
        final double finalMax = stats.getHighestMarks();
        final double finalMin = stats.getLowestMarks();

        List<String> highestStudents = ranked.stream()
                .filter(s -> s.getMarks() != null && Math.abs(s.getMarks() - finalMax) < 0.001)
                .map(s -> s.getName() + " (" + s.getStudentId() + ")")
                .collect(Collectors.toList());

        List<String> lowestStudents = ranked.stream()
                .filter(s -> s.getMarks() != null && Math.abs(s.getMarks() - finalMin) < 0.001)
                .map(s -> s.getName() + " (" + s.getStudentId() + ")")
                .collect(Collectors.toList());

        stats.setHighestScoringStudents(highestStudents);
        stats.setLowestScoringStudents(lowestStudents);
        stats.setGradeDistribution(gradeDist);

        // Grade Percentages
        Map<String, Double> gradePct = new HashMap<>();
        for (Map.Entry<String, Long> entry : gradeDist.entrySet()) {
            double pct = total > 0 ? GradeCalculationService.roundTwoDecimals((double) entry.getValue() / total * 100.0) : 0.0;
            gradePct.put(entry.getKey(), pct);
        }
        stats.setGradePercentages(gradePct);

        // Top performers (top 5)
        List<StudentResponse> topPerformers = ranked.stream()
                .limit(5)
                .collect(Collectors.toList());
        stats.setTopPerformers(topPerformers);

        // Subject averages
        Map<String, Double> subAverages = new HashMap<>();
        for (String sub : subjectSum.keySet()) {
            int cnt = subjectCount.getOrDefault(sub, 0);
            if (cnt > 0) {
                subAverages.put(sub, GradeCalculationService.roundTwoDecimals(subjectSum.get(sub) / cnt));
            }
        }
        stats.setSubjectAverages(subAverages);

        return stats;
    }
}
