package com.studentgradetracker.repository;

import com.studentgradetracker.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByStudentId(String studentId);

    boolean existsByStudentId(String studentId);

    boolean existsByStudentIdAndIdNot(String studentId, Long id);

    List<Student> findByClassNameIgnoreCase(String className);

    @Query("SELECT DISTINCT s.className FROM Student s WHERE s.className IS NOT NULL ORDER BY s.className ASC")
    List<String> findDistinctClassNames();

    @Query("SELECT s FROM Student s WHERE " +
           "(:query IS NULL OR :query = '' OR LOWER(s.studentId) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(s.name) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:className IS NULL OR :className = '' OR :className = 'ALL' OR LOWER(s.className) = LOWER(:className))")
    List<Student> searchAndFilterStudents(@Param("query") String query, @Param("className") String className);

    @Query("SELECT s FROM Student s WHERE " +
           "(:query IS NULL OR :query = '' OR LOWER(s.studentId) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(s.name) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:className IS NULL OR :className = '' OR :className = 'ALL' OR LOWER(s.className) = LOWER(:className))")
    Page<Student> searchAndFilterStudentsPaged(@Param("query") String query, @Param("className") String className, Pageable pageable);
}
