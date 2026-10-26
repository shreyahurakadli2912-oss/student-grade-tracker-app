package com.studentgradetracker.repository;

import com.studentgradetracker.entity.StudentSubjectMark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentSubjectMarkRepository extends JpaRepository<StudentSubjectMark, Long> {

    List<StudentSubjectMark> findByStudentId(Long studentId);

    Optional<StudentSubjectMark> findByStudentIdAndSubjectId(Long studentId, Long subjectId);

    void deleteByStudentId(Long studentId);

    boolean existsBySubjectId(Long subjectId);
}
