package com.studentgradetracker.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassStatisticsResponse {
    private String className;
    private long totalStudents;
    private double averagePercentage;
    private double highestMarks;
    private List<String> highestScoringStudents = new ArrayList<>();
    private double lowestMarks;
    private List<String> lowestScoringStudents = new ArrayList<>();
    private long totalPassed;
    private long totalFailed;
    private double passPercentage;
    private Map<String, Long> gradeDistribution = new HashMap<>();
    private Map<String, Double> gradePercentages = new HashMap<>();
    private List<StudentResponse> topPerformers = new ArrayList<>();
    private Map<String, Double> subjectAverages = new HashMap<>();

    public ClassStatisticsResponse() {
        this.gradeDistribution.put("A", 0L);
        this.gradeDistribution.put("B", 0L);
        this.gradeDistribution.put("C", 0L);
        this.gradeDistribution.put("D", 0L);
        this.gradeDistribution.put("F", 0L);
        this.gradePercentages.put("A", 0.0);
        this.gradePercentages.put("B", 0.0);
        this.gradePercentages.put("C", 0.0);
        this.gradePercentages.put("D", 0.0);
        this.gradePercentages.put("F", 0.0);
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public long getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(long totalStudents) {
        this.totalStudents = totalStudents;
    }

    public double getAveragePercentage() {
        return averagePercentage;
    }

    public void setAveragePercentage(double averagePercentage) {
        this.averagePercentage = averagePercentage;
    }

    public double getHighestMarks() {
        return highestMarks;
    }

    public void setHighestMarks(double highestMarks) {
        this.highestMarks = highestMarks;
    }

    public List<String> getHighestScoringStudents() {
        return highestScoringStudents;
    }

    public void setHighestScoringStudents(List<String> highestScoringStudents) {
        this.highestScoringStudents = highestScoringStudents;
    }

    public double getLowestMarks() {
        return lowestMarks;
    }

    public void setLowestMarks(double lowestMarks) {
        this.lowestMarks = lowestMarks;
    }

    public List<String> getLowestScoringStudents() {
        return lowestScoringStudents;
    }

    public void setLowestScoringStudents(List<String> lowestScoringStudents) {
        this.lowestScoringStudents = lowestScoringStudents;
    }

    public long getTotalPassed() {
        return totalPassed;
    }

    public void setTotalPassed(long totalPassed) {
        this.totalPassed = totalPassed;
    }

    public long getTotalFailed() {
        return totalFailed;
    }

    public void setTotalFailed(long totalFailed) {
        this.totalFailed = totalFailed;
    }

    public double getPassPercentage() {
        return passPercentage;
    }

    public void setPassPercentage(double passPercentage) {
        this.passPercentage = passPercentage;
    }

    public Map<String, Long> getGradeDistribution() {
        return gradeDistribution;
    }

    public void setGradeDistribution(Map<String, Long> gradeDistribution) {
        this.gradeDistribution = gradeDistribution;
    }

    public Map<String, Double> getGradePercentages() {
        return gradePercentages;
    }

    public void setGradePercentages(Map<String, Double> gradePercentages) {
        this.gradePercentages = gradePercentages;
    }

    public List<StudentResponse> getTopPerformers() {
        return topPerformers;
    }

    public void setTopPerformers(List<StudentResponse> topPerformers) {
        this.topPerformers = topPerformers;
    }

    public Map<String, Double> getSubjectAverages() {
        return subjectAverages;
    }

    public void setSubjectAverages(Map<String, Double> subjectAverages) {
        this.subjectAverages = subjectAverages;
    }
}
