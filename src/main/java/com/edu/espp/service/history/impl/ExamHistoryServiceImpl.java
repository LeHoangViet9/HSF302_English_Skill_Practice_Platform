package com.edu.espp.service.history.impl;

import com.edu.espp.dto.exam.response.ExamAttemptDetailResponse;
import com.edu.espp.dto.exam.response.ExamHistoryResponse;
import com.edu.espp.entity.ExamAttemptDetail;
import com.edu.espp.entity.ExamHistory;
import com.edu.espp.repository.ExamAttemptDetailRepository;
import com.edu.espp.repository.ExamHistoryRepository;
import com.edu.espp.service.history.ExamHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExamHistoryServiceImpl implements ExamHistoryService {
    private final ExamHistoryRepository examHistoryRepository;
    private final ExamAttemptDetailRepository examAttemptDetailRepository;

    @Override
    public List<ExamHistoryResponse> getUserExamHistory(Long userId) {
        List<ExamHistory> histories = examHistoryRepository.findByUserIdOrderByTestedAtDesc(userId);
        return mapToResponse(histories);
    }

    @Override
    public List<ExamHistoryResponse> getAllExamHistories() {
        List<ExamHistory> histories = examHistoryRepository.findAllByOrderByTestedAtDesc();
        return mapToResponse(histories);
    }

    private List<ExamHistoryResponse> mapToResponse(List<ExamHistory> histories) {
        return histories.stream().map(history -> ExamHistoryResponse.builder()
                .id(history.getId())
                .userId(history.getUser().getId())
                .fullName(history.getUser().getFullName())
                .examId(history.getExam().getId())
                .examTitle(history.getExam().getTitle())
                .score(history.getScore())
                .correctAnswersCount(history.getCorrectAnswersCount())
                .timeSpent(history.getTimeSpent())
                .testedAt(history.getTestedAt())
                .build()
        ).toList();
    }

    @Override
    public List<ExamAttemptDetailResponse> getExamAttemptDetails(Long examHistoryId) {
        List<ExamAttemptDetail> details = examAttemptDetailRepository.findByExamHistoryId(examHistoryId);

        return details.stream().map(detail -> {
            var question = detail.getQuestion();

            return ExamAttemptDetailResponse.builder()
                    .questionId(question != null ? question.getId() : null)
                    .questionText(question != null ? question.getQuestionText() : null)
                    .options(question != null && question.getOptions() != null
                            ? List.of(question.getOptions())
                            : null)
                    .correctAnswer(question != null ? question.getCorrectAnswer() : null)
                    .selectedAnswer(detail.getSelectedAnswer())
                    .isCorrect(detail.getIsCorrect())
                    .explanation(question != null ? question.getExplanation() : null)
                    .build();
        }).toList();
    }
}