package com.studentgradetracker.service;

import com.studentgradetracker.dto.StudentResponse;
import com.studentgradetracker.dto.SubjectMarkResponse;
import com.studentgradetracker.entity.GradeScaleRule;
import com.studentgradetracker.entity.Student;
import com.studentgradetracker.entity.StudentSubjectMark;
import com.studentgradetracker.repository.GradeScaleRuleRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class GradeCalculationService {

    public static final double DEFAULT_PASSING_PERCENTAGE = 60.0;

    private final GradeScaleRuleRepository gradeScaleRuleRepository;

    public GradeCalculationService(GradeScaleRuleRepository gradeScaleRuleRepository) {
        this.gradeScaleRuleRepository = gradeScaleRuleRepository;
    }

    public static double roundTwoDecimals(double value) {
        return BigDecimal.valueOf(value)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    /**
     * Determines letter grade from percentage using configured or default boundaries.
     */
    public String calculateGrade(double percentage) {
        double rounded = roundTwoDecimals(percentage);
        List<GradeScaleRule> rules = gradeScaleRuleRepository.findAllByOrderBySortOrderAsc();
        if (rules != null && !rules.isEmpty()) {
            for (GradeScaleRule rule : rules) {
                if (rounded >= rule.getMinPercentage() && rounded <= rule.getMaxPercentage()) {
                    return rule.getGrade();
                }
            }
        }

        // Standard Default Fallback:
        // 90 to 100: A
        // 80 to below 90: B
        // 70 to below 80: C
        // 60 to below 70: D
        // 0 to below 60: F
        if (rounded >= 90.0) {
            return "A";
        } else if (rounded >= 80.0) {
            return "B";
        } else if (rounded >= 70.0) {
            return "C";
        } else if (rounded >= 60.0) {
            return "D";
        } else {
            return "F";
        }
    }

    /**
     * Converts a Student entity to a fully calculated StudentResponse.
     */
    public StudentResponse mapToStudentResponse(Student student) {
        StudentResponse response = new StudentResponse();
        response.setId(student.getId());
        response.setStudentId(student.getStudentId());
        response.setName(student.getName());
        response.setClassName(student.getClassName());
        response.setEmail(student.getEmail());
        response.setCreatedAt(student.getCreatedAt());
        response.setUpdatedAt(student.getUpdatedAt());

        List<StudentSubjectMark> subjectMarks = student.getSubjectMarks();
        List<SubjectMarkResponse> markResponses = new ArrayList<>();
        List<String> failedSubjects = new ArrayList<>();

        double totalObtained = 0.0;
        double totalMax = 0.0;
        boolean hasCompulsoryFailure = false;

        if (subjectMarks != null && !subjectMarks.isEmpty()) {
            for (StudentSubjectMark sm : subjectMarks) {
                double obtained = sm.getMarksObtained() != null ? sm.getMarksObtained() : 0.0;
                double max = sm.getMaxMarks() != null ? sm.getMaxMarks() : 100.0;
                double passMarks = (sm.getSubject() != null && sm.getSubject().getPassMarks() != null)
                        ? sm.getSubject().getPassMarks() : 40.0;
                boolean isCompulsory = (sm.getSubject() != null && sm.getSubject().getIsCompulsory() != null)
                        ? sm.getSubject().getIsCompulsory() : true;

                double subPercentage = max > 0 ? roundTwoDecimals((obtained / max) * 100.0) : 0.0;
                boolean isPassed = obtained >= passMarks;

                if (!isPassed && isCompulsory) {
                    hasCompulsoryFailure = true;
                    String subName = (sm.getSubject() != null) ? sm.getSubject().getName() : "Subject #" + sm.getId();
                    failedSubjects.add(subName);
                }

                totalObtained += obtained;
                totalMax += max;

                SubjectMarkResponse mr = new SubjectMarkResponse(
                        sm.getSubject() != null ? sm.getSubject().getId() : null,
                        sm.getSubject() != null ? sm.getSubject().getCode() : null,
                        sm.getSubject() != null ? sm.getSubject().getName() : null,
                        obtained,
                        max,
                        passMarks,
                        subPercentage,
                        isPassed,
                        isCompulsory
                );
                markResponses.add(mr);
            }

            double percentage = totalMax > 0 ? roundTwoDecimals((totalObtained / totalMax) * 100.0) : 0.0;
            response.setMarks(roundTwoDecimals(totalObtained));
            response.setTotalMaxMarks(roundTwoDecimals(totalMax));
            response.setPercentage(percentage);
            response.setGrade(calculateGrade(percentage));
            response.setSubjectMarks(markResponses);
            response.setFailedSubjects(failedSubjects);

            // Pass status: Must not fail any compulsory subject and percentage >= pass mark
            if (!hasCompulsoryFailure && percentage >= DEFAULT_PASSING_PERCENTAGE) {
                response.setStatus("PASS");
            } else {
                response.setStatus("FAIL");
            }
        } else {
            // Overall marks mode (marks out of 100)
            double marks = student.getMarks() != null ? student.getMarks() : 0.0;
            double percentage = roundTwoDecimals(marks);

            response.setMarks(percentage);
            response.setTotalMaxMarks(100.0);
            response.setPercentage(percentage);
            response.setGrade(calculateGrade(percentage));
            response.setStatus(percentage >= DEFAULT_PASSING_PERCENTAGE ? "PASS" : "FAIL");
        }

        return response;
    }

    /**
     * Helper to obtain actual numeric marks for comparison and ranking.
     */
    public static double getEffectiveMarks(StudentResponse s) {
        if (s == null) {
            return 0.0;
        }
        if (s.getMarks() != null) {
            return s.getMarks();
        }
        if (s.getPercentage() != null) {
            return s.getPercentage();
        }
        return 0.0;
    }

    /**
     * Computes Dense Rankings on a list of students:
     * - Students are sorted by marks descending.
     * - Students with identical numeric marks receive the same rank.
     * - When marks decrease to a new distinct numeric value, increment rank by exactly 1.
     * - No ranks are skipped after ties.
     * - Ties are ordered visually by name and student ID without changing their rank.
     */
    public List<StudentResponse> assignDenseRankings(List<StudentResponse> students) {
        if (students == null || students.isEmpty()) {
            return Collections.emptyList();
        }

        // Sort descending by marks, then name, then student ID for stable visual order
        List<StudentResponse> sorted = new ArrayList<>(students);
        sorted.sort((s1, s2) -> {
            double m1 = getEffectiveMarks(s1);
            double m2 = getEffectiveMarks(s2);
            int cmp = Double.compare(m2, m1);
            if (cmp != 0) return cmp;

            String name1 = s1.getName() != null ? s1.getName() : "";
            String name2 = s2.getName() != null ? s2.getName() : "";
            int nameCmp = name1.compareToIgnoreCase(name2);
            if (nameCmp != 0) return nameCmp;

            String id1 = s1.getStudentId() != null ? s1.getStudentId() : "";
            String id2 = s2.getStudentId() != null ? s2.getStudentId() : "";
            return id1.compareToIgnoreCase(id2);
        });

        int currentRank = 1;
        for (int i = 0; i < sorted.size(); i++) {
            if (i == 0) {
                sorted.get(0).setRank(currentRank);
            } else {
                StudentResponse prev = sorted.get(i - 1);
                StudentResponse curr = sorted.get(i);
                double prevMarks = getEffectiveMarks(prev);
                double currMarks = getEffectiveMarks(curr);

                if (Double.compare(prevMarks, currMarks) == 0 || Math.abs(prevMarks - currMarks) < 1e-9) {
                    curr.setRank(prev.getRank());
                } else {
                    currentRank++;
                    curr.setRank(currentRank);
                }
            }
        }

        return sorted;
    }

    /**
     * Backward-compatible method delegating to assignDenseRankings.
     */
    public List<StudentResponse> assignCompetitionRankings(List<StudentResponse> students) {
        return assignDenseRankings(students);
    }
}
