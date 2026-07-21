package com.edu.espp.service.exam.impl;

import com.edu.espp.common.enums.TypeQuiz;
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
import com.fasterxml.jackson.core.type.TypeReference; // 🌟 Thêm import này
import com.fasterxml.jackson.databind.ObjectMapper;       // 🌟 Thêm import này
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.edu.espp.common.enums.ApprovalStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap; // 🌟 Thêm import này
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

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String EXAM_NOT_FOUND_MSG = "Không tìm thấy bài thi";


    @Override
    public List<ExamResponse> searchExams(String keyword, TypeQuiz type, boolean includeAllStatuses) {
        List<Exam> exams;
        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
        boolean hasType = type != null;

        if (hasKeyword && hasType) {
            exams = examRepository.findByTitleContainingIgnoreCaseAndType(keyword.trim(), type);
        } else if (hasKeyword) {
            exams = examRepository.findByTitleContainingIgnoreCase(keyword.trim());
        } else if (hasType) {
            exams = examRepository.findByType(type);
        } else {
            exams = includeAllStatuses ? examRepository.findAll() : examRepository.findByApprovalStatus(ApprovalStatus.APPROVED);
        }
        
        // Filter out non-approved exams for the searches that don't have custom queries yet
        if (!includeAllStatuses && (hasKeyword || hasType)) {
            exams = exams.stream().filter(e -> e.getApprovalStatus() == ApprovalStatus.APPROVED).toList();
        }
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
    @Transactional
    public ExamResponse createExam(ExamRequest request) {
        if(examRepository.existsByTitle(request.getTitle())){
            throw new ConflictException("Bài thi với tiêu đề " + request.getTitle() + " đã tồn tại");
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = null;
        ApprovalStatus status = ApprovalStatus.PENDING;
        
        if (auth != null && auth.getName() != null && !auth.getName().equals("anonymousUser")) {
            currentUser = userRepository.findByEmail(auth.getName()).orElse(null);
            if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                status = ApprovalStatus.APPROVED;
            }
        }

        Exam exam = Exam.builder()
                .title(request.getTitle())
                .type(request.getType())
                .duration(request.getDuration())
                .description(request.getDescription())
                .totalQuestions(request.getTotalQuestions())
                .createdBy(currentUser)
                .approvalStatus(status)
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
        exam.setDescription(request.getDescription());
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

    @Override
    public org.springframework.data.domain.Page<ExamResponse> getPendingExams(org.springframework.data.domain.Pageable pageable) {
        return examRepository.findByApprovalStatus(ApprovalStatus.PENDING, pageable).map(this::convertToResponse);
    }

    @Override
    @Transactional
    public void approveExam(Long examId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException(EXAM_NOT_FOUND_MSG + " để duyệt"));
        exam.setApprovalStatus(ApprovalStatus.APPROVED);
        examRepository.save(exam);
    }

    @Override
    @Transactional
    public void rejectExam(Long examId, String reason) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException(EXAM_NOT_FOUND_MSG + " để từ chối"));
        exam.setApprovalStatus(ApprovalStatus.REJECTED);
        exam.setRejectReason(reason);
        examRepository.save(exam);
    }

    private ExamResponse convertToResponse(Exam exam) {
        if (exam == null) return null;
        return ExamResponse.builder()
                .id(exam.getId())
                .title(exam.getTitle())
                .type(exam.getType())
                .duration(exam.getDuration())
                .description(exam.getDescription())
                .totalQuestions(exam.getTotalQuestions())
                .approvalStatus(exam.getApprovalStatus())
                .rejectReason(exam.getRejectReason())
                .build();
    }

    private QuestionResponse convertQuestionToResponse(Question question) {
        if (question == null) return null;

        Map<String, String> parsedOptions = new HashMap<>();
        try {
            if (question.getOptions() != null && !question.getOptions().isBlank()) {
                // Parse chuỗi JSON String từ database sang cấu trúc Map thực thụ
                parsedOptions = objectMapper.readValue(
                        question.getOptions(),
                        new TypeReference<>() {
                        }
                );
            }
        } catch (Exception e) {
            System.err.println("Lỗi parse JSON options tại Question ID " + question.getId() + ": " + e.getMessage());
        }

        return QuestionResponse.builder()
                .id(question.getId())
                .examId(question.getExam() != null ? question.getExam().getId() : null)
                .skill(question.getSkill())
                .questionText(question.getQuestionText())
                .audioUrl(question.getAudioUrl())
                .options(parsedOptions)
                .correctAnswer(question.getCorrectAnswer())
                .explanation(question.getExplanation())
                .build();
    }
}