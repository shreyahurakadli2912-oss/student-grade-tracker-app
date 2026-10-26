package com.studentgradetracker.service;

import com.studentgradetracker.dto.SubjectRequest;
import com.studentgradetracker.dto.SubjectResponse;
import com.studentgradetracker.entity.Subject;
import com.studentgradetracker.exception.BadRequestException;
import com.studentgradetracker.exception.DuplicateResourceException;
import com.studentgradetracker.exception.ResourceNotFoundException;
import com.studentgradetracker.repository.StudentSubjectMarkRepository;
import com.studentgradetracker.repository.SubjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final StudentSubjectMarkRepository studentSubjectMarkRepository;

    public SubjectService(SubjectRepository subjectRepository, StudentSubjectMarkRepository studentSubjectMarkRepository) {
        this.subjectRepository = subjectRepository;
        this.studentSubjectMarkRepository = studentSubjectMarkRepository;
    }

    public SubjectResponse createSubject(SubjectRequest request) {
        validateSubjectRequest(request);

        if (subjectRepository.existsByCodeIgnoreCase(request.getCode().trim())) {
            throw new DuplicateResourceException("Subject code '" + request.getCode() + "' is already in use.");
        }

        Subject subject = new Subject();
        subject.setCode(request.getCode().trim().toUpperCase());
        subject.setName(request.getName().trim());
        subject.setMaxMarks(request.getMaxMarks());
        subject.setPassMarks(request.getPassMarks());
        subject.setIsCompulsory(request.getIsCompulsory() != null ? request.getIsCompulsory() : true);

        Subject saved = subjectRepository.save(subject);
        return mapToResponse(saved);
    }

    public SubjectResponse updateSubject(Long id, SubjectRequest request) {
        validateSubjectRequest(request);

        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject with ID " + id + " not found."));

        if (subjectRepository.existsByCodeIgnoreCaseAndIdNot(request.getCode().trim(), id)) {
            throw new DuplicateResourceException("Subject code '" + request.getCode() + "' is already in use by another subject.");
        }

        subject.setCode(request.getCode().trim().toUpperCase());
        subject.setName(request.getName().trim());
        subject.setMaxMarks(request.getMaxMarks());
        subject.setPassMarks(request.getPassMarks());
        subject.setIsCompulsory(request.getIsCompulsory() != null ? request.getIsCompulsory() : true);

        Subject updated = subjectRepository.save(subject);
        return mapToResponse(updated);
    }

    public void deleteSubject(Long id) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject with ID " + id + " not found."));

        if (studentSubjectMarkRepository.existsBySubjectId(id)) {
            throw new BadRequestException("Cannot delete subject '" + subject.getName() + "' because student marks have already been recorded for it.");
        }

        subjectRepository.delete(subject);
    }

    @Transactional(readOnly = true)
    public List<SubjectResponse> getAllSubjects() {
        return subjectRepository.findAllByOrderByCodeAsc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SubjectResponse getSubjectById(Long id) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject with ID " + id + " not found."));
        return mapToResponse(subject);
    }

    private void validateSubjectRequest(SubjectRequest request) {
        if (request.getCode() == null || request.getCode().trim().isEmpty()) {
            throw new BadRequestException("Subject code cannot be empty.");
        }
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new BadRequestException("Subject name cannot be empty.");
        }
        if (request.getMaxMarks() == null || request.getMaxMarks() <= 0) {
            throw new BadRequestException("Maximum marks must be greater than zero.");
        }
        if (request.getPassMarks() == null || request.getPassMarks() <= 0) {
            throw new BadRequestException("Passing marks must be greater than zero.");
        }
        if (request.getPassMarks() > request.getMaxMarks()) {
            throw new BadRequestException("Passing marks cannot exceed maximum marks.");
        }
    }

    private SubjectResponse mapToResponse(Subject s) {
        return new SubjectResponse(
                s.getId(),
                s.getCode(),
                s.getName(),
                s.getMaxMarks(),
                s.getPassMarks(),
                s.getIsCompulsory()
        );
    }
}
