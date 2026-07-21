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
    public List<ExamHistoryResponse> getUserExamHistory(Long userId) {
        List<ExamHistory> histories = examHistoryRepository.findByUserIdOrderByTestedAtDesc(userId);
        return mapToResponse(histories);
    }

    @Override
    public Page<ExamHistoryResponse> getUserExamHistory(Long userId, Pageable pageable) {
        Page<ExamHistory> histories = examHistoryRepository.findByUserId(userId, pageable);
        return histories.map(this::mapSingleToResponse);
    }


    @Override
    public Page<ExamHistoryResponse> getAllExamHistories(Pageable pageable) {
        Page<ExamHistory> histories = examHistoryRepository.findAll(pageable);
        return histories.map(this::mapSingleToResponse);
    }

    private List<ExamHistoryResponse> mapToResponse(List<ExamHistory> histories) {
        return histories.stream().map(this::mapSingleToResponse).toList();
    }

    private ExamHistoryResponse mapSingleToResponse(ExamHistory history) {
        return ExamHistoryResponse.builder()
                .id(history.getId())
                .userId(history.getUser().getId())
                .fullName(history.getUser().getFullName())
                .examId(history.getExam().getId())
                .examTitle(history.getExam().getTitle())
                .score(history.getScore())
                .correctAnswersCount(history.getCorrectAnswersCount())
                .timeSpent(history.getTimeSpent())
                .testedAt(history.getTestedAt())
                .build();
    }



}