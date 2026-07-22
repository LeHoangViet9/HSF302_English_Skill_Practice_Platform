package com.edu.espp.service.history.impl;

import com.edu.espp.dto.exam.response.ExamHistoryResponse;
import com.edu.espp.entity.ExamHistory;
import com.edu.espp.repository.ExamHistoryRepository;
import com.edu.espp.service.history.ExamHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExamHistoryServiceImpl implements ExamHistoryService {
    private final ExamHistoryRepository examHistoryRepository;

    @Override
    public Page<ExamHistoryResponse> getUserExamHistory(Long userId, Pageable pageable) {
        Page<ExamHistory> histories = examHistoryRepository.findByUserId(userId, pageable);
        return histories.map(this::mapSingleToResponse);
    }

    @Override
    public List<ExamHistoryResponse> getUserExamHistory(Long userId) {
        List<ExamHistory> histories = examHistoryRepository.findByUserIdOrderByTestedAtDesc(userId);
        return histories.stream().map(this::mapSingleToResponse).toList();
    }

    @Override
    public Page<ExamHistoryResponse> getAllExamHistories(Pageable pageable) {
        Page<ExamHistory> histories = examHistoryRepository.findAll(pageable);
        return histories.map(this::mapSingleToResponse);
    }
    

    private ExamHistoryResponse mapSingleToResponse(ExamHistory history) {
        return ExamHistoryResponse.builder()
                .id(history.getId())
                .userId(history.getUser() != null ? history.getUser().getId() : null)
                .fullName(history.getUser() != null ? history.getUser().getFullName() : "Học viên đã xóa")
                .examId(history.getExam() != null ? history.getExam().getId() : null)
                .examTitle(history.getExam() != null ? history.getExam().getTitle() : "Đề thi đã xóa")
                .score(history.getScore())
                .correctAnswersCount(history.getCorrectAnswersCount())
                .timeSpent(history.getTimeSpent())
                .testedAt(history.getTestedAt())
                .build();
    }



}