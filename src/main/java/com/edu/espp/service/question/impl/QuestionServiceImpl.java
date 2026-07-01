package com.edu.espp.service.question.impl;

import com.edu.espp.common.enums.QuestionSkill;
import com.edu.espp.common.exception.ResourceNotFoundException;
import com.edu.espp.dto.question.request.QuestionRequest;
import com.edu.espp.dto.question.response.QuestionResponse;
import com.edu.espp.entity.Exam;
import com.edu.espp.entity.Question;
import com.edu.espp.repository.ExamRepository;
import com.edu.espp.repository.QuestionRepository;
import com.edu.espp.service.question.QuestionService;
import com.fasterxml.jackson.core.type.TypeReference; // 🌟 Thêm import này
import com.fasterxml.jackson.databind.ObjectMapper;       // 🌟 Thêm import này
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap; // 🌟 Thêm import này
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {
    private final QuestionRepository questionRepository;
    private final ExamRepository examRepository;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public QuestionResponse getQuestionById(Long id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy câu hỏi với ID: " + id));
        return convertToResponse(question);
    }

    @Override
    public List<QuestionResponse> getQuestionsByExam(Long examId) {
        if (!examRepository.existsById(examId)) {
            throw new ResourceNotFoundException("Không tìm thấy bài thi với ID: " + examId);
        }
        List<Question> questions = questionRepository.findByExamId(examId);
        return questions.stream().map(this::convertToResponse).toList();
    }

    @Override
    public List<QuestionResponse> getQuestionsBySkill(QuestionSkill skill) {
        List<Question> questions = questionRepository.findBySkill(skill);
        return questions.stream().map(this::convertToResponse).toList();
    }

    @Override
    public List<QuestionResponse> getQuestionsByExamAndSkill(Long examId, QuestionSkill skill) {
        if (!examRepository.existsById(examId)) {
            throw new ResourceNotFoundException("Không tìm thấy bài thi với ID: " + examId);
        }
        List<Question> questions = questionRepository.findByExamIdAndSkill(examId, skill);
        return questions.stream().map(this::convertToResponse).toList();
    }

    @Override
    @Transactional
    public QuestionResponse createQuestion(QuestionRequest request) {
        Exam exam = examRepository.findById(request.getExamId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài thi với ID: " + request.getExamId()));

        Question question = Question.builder()
                .exam(exam)
                .skill(request.getSkill())
                .questionText(request.getQuestionText())
                .audioUrl(request.getAudioUrl())
                .options(request.getOptions())
                .correctAnswer(request.getCorrectAnswer())
                .explanation(request.getExplanation())
                .build();

        Question saved = questionRepository.save(question);
        return convertToResponse(saved);
    }

    @Override
    @Transactional
    public List<QuestionResponse> createBulkQuestions(List<QuestionRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return List.of();
        }

        List<Question> questions = requests.stream().map(request -> {
            Exam exam = examRepository.findById(request.getExamId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài thi với ID: " + request.getExamId()));

            return Question.builder()
                    .exam(exam)
                    .skill(request.getSkill())
                    .questionText(request.getQuestionText())
                    .audioUrl(request.getAudioUrl())
                    .options(request.getOptions())
                    .correctAnswer(request.getCorrectAnswer())
                    .explanation(request.getExplanation())
                    .build();
        }).toList();

        List<Question> savedQuestions = questionRepository.saveAll(questions);
        return savedQuestions.stream().map(this::convertToResponse).toList();
    }

    @Override
    @Transactional
    public QuestionResponse updateQuestion(Long id, QuestionRequest request) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy câu hỏi để cập nhật"));

        Exam exam = examRepository.findById(request.getExamId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài thi với ID: " + request.getExamId()));

        question.setExam(exam);
        question.setSkill(request.getSkill());
        question.setQuestionText(request.getQuestionText());
        question.setAudioUrl(request.getAudioUrl());
        question.setOptions(request.getOptions());
        question.setCorrectAnswer(request.getCorrectAnswer());
        question.setExplanation(request.getExplanation());

        Question updated = questionRepository.save(question);
        return convertToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteQuestion(Long id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy câu hỏi để xóa"));
        questionRepository.delete(question);
    }

    private QuestionResponse convertToResponse(Question question) {
        if (question == null) return null;

        Map<String, String> parsedOptions = new HashMap<>();
        try {
            if (question.getOptions() != null && !question.getOptions().isBlank()) {
                parsedOptions = objectMapper.readValue(
                        question.getOptions(),
                        new TypeReference<Map<String, String>>() {}
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