package com.edu.espp.service.exam.impl;

import com.edu.espp.common.enums.QuestionSkill;
import com.edu.espp.common.exception.ConflictException;
import com.edu.espp.common.exception.ResourceNotFoundException;
import com.edu.espp.dto.exam.request.ExamRequest;
import com.edu.espp.dto.exam.request.ExamSubmitRequest;
import com.edu.espp.dto.exam.response.ExamResponse;
import com.edu.espp.dto.exam.response.ExamResultResponse;
import com.edu.espp.dto.question.response.QuestionResponse;
import com.edu.espp.entity.*;
import com.edu.espp.repository.*;
import com.edu.espp.service.exam.ExamService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExamServiceImpl implements ExamService {
    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final ExamHistoryRepository examHistoryRepository;
    private final ExamAttemptDetailRepository examAttemptDetailRepository;

    private static final String EXAM_NOT_FOUND_MSG = "Không tìm thấy bài thi";

    @Override
    public List<ExamResponse> getAllExams() {
        List<Exam> exams = examRepository.findAll();
        return exams.stream().map(this::convertToResponse).toList();
    }

    @Override
    public List<QuestionResponse> getQuestionsByExam(Long examId) {
        examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException(EXAM_NOT_FOUND_MSG));

        List<Question> questions = questionRepository.findByExamId(examId);
        return questions.stream()
                .map(this::convertQuestionToResponse)
                .toList();
    }

    @Override
    @Transactional
    public ExamResultResponse submitExam(Long examId, Long userId, ExamSubmitRequest request) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException(EXAM_NOT_FOUND_MSG));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
        List<Question> questions = questionRepository.findByExamId(examId);

        int correctCount = 0;
        Map<Long, String> userAnswers = request.getUserAnswers();
        List<ExamAttemptDetail> details = new ArrayList<>();

        ExamHistory history = ExamHistory.builder()
                .user(user)
                .exam(exam)
                .timeSpent(request.getTimeSpent())
                .testedAt(LocalDateTime.now(ZoneId.systemDefault()))
                .build();

        ExamHistory savedHistory = examHistoryRepository.save(history);

        if (userAnswers != null) {
            for (Question q : questions) {
                String selectedAnswer = userAnswers.get(q.getId());
                boolean isCorrect = false;

                if (selectedAnswer != null && q.getCorrectAnswer() != null
                        && selectedAnswer.trim().equalsIgnoreCase(q.getCorrectAnswer().trim())) {
                    correctCount++;
                    isCorrect = true;
                }

                ExamAttemptDetail detail = ExamAttemptDetail.builder()
                        .examHistory(savedHistory)
                        .question(q)
                        .selectedAnswer(selectedAnswer)
                        .isCorrect(isCorrect)
                        .build();

                details.add(detail);
            }
        }

        examAttemptDetailRepository.saveAll(details);

        int total = (exam.getTotalQuestions() != null && exam.getTotalQuestions() > 0)
                ? exam.getTotalQuestions()
                : questions.size();

        double score = (total == 0) ? 0.0 : ((double) correctCount / total) * 10.0;
        score = Math.round(score * 100.0) / 100.0;

        savedHistory.setScore(score);
        savedHistory.setCorrectAnswersCount(correctCount);
        examHistoryRepository.save(savedHistory);

        return ExamResultResponse.builder()
                .score(score)
                .correctAnswersCount(correctCount)
                .totalQuestions(total)
                .timeSpent(request.getTimeSpent())
                .build();
    }

    @Override
    public List<QuestionResponse> getQuestionsByExamAndSkill(Long examId, QuestionSkill skill) {
        examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException(EXAM_NOT_FOUND_MSG));

        List<Question> questions = questionRepository.findByExamIdAndSkill(examId, skill);
        return questions.stream()
                .map(this::convertQuestionToResponse)
                .toList();
    }

    @Override
    @Transactional
    public ExamResponse createExam(ExamRequest request) {
        if(examRepository.existsByTitle(request.getTitle())){
            throw new ConflictException("Bài thi với tiêu đề " + request.getTitle() + " đã tồn tại");
        }
        Exam exam = Exam.builder()
                .title(request.getTitle())
                .type(request.getType())
                .duration(request.getDuration())
                .totalQuestions(request.getTotalQuestions())
                .build();

        return convertToResponse(examRepository.save(exam));
    }

    @Override
    public ExamResponse getExamById(Long examId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException(EXAM_NOT_FOUND_MSG + " với ID: " + examId));
        return convertToResponse(exam);
    }

    @Override
    @Transactional
    public ExamResponse updateExam(Long examId, ExamRequest request) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException(EXAM_NOT_FOUND_MSG + " để cập nhật"));
        if (examRepository.existsByTitleAndIdNot(request.getTitle(), examId)) {
            throw new IllegalArgumentException("Tên bài thi '" + request.getTitle() + "' đã tồn tại ở một đề thi khác!");
        }
        exam.setTitle(request.getTitle());
        exam.setType(request.getType());
        exam.setDuration(request.getDuration());
        exam.setTotalQuestions(request.getTotalQuestions());

        return convertToResponse(examRepository.save(exam));
    }

    @Override
    @Transactional
    public void deleteExam(Long examId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException(EXAM_NOT_FOUND_MSG + " để xóa"));
        examRepository.delete(exam);
    }

    private ExamResponse convertToResponse(Exam exam) {
        if (exam == null) return null;
        return ExamResponse.builder()
                .id(exam.getId())
                .title(exam.getTitle())
                .type(exam.getType())
                .duration(exam.getDuration())
                .totalQuestions(exam.getTotalQuestions())
                .build();
    }

    private QuestionResponse convertQuestionToResponse(Question question) {
        if (question == null) return null;
        return QuestionResponse.builder()
                .id(question.getId())
                .skill(question.getSkill())
                .questionText(question.getQuestionText())
                .audioUrl(question.getAudioUrl())
                .options(question.getOptions())
                .explanation(question.getExplanation())
                .build();
    }
}