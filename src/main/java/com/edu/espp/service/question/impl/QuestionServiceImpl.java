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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {
    private final QuestionRepository questionRepository;
    private final ExamRepository examRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public QuestionResponse getQuestionById(Long id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy câu hỏi với ID: " + id));
        return convertToResponse(question);
    }


    @Override
    public Page<QuestionResponse> getQuestionsByExam(Long examId, Pageable pageable) {
        if (!examRepository.existsById(examId)) {
            throw new ResourceNotFoundException("Không tìm thấy bài thi với ID: " + examId);
        }
        Page<Question> questions = questionRepository.findByExamId(examId, pageable);
        return questions.map(this::convertToResponse);
    }


    @Override
    public Page<QuestionResponse> getQuestionsBySkill(QuestionSkill skill, Pageable pageable) {
        Page<Question> questions = questionRepository.findBySkill(skill, pageable);
        return questions.map(this::convertToResponse);
    }



    @Override
    public Page<QuestionResponse> getQuestionsByExamAndSkill(Long examId, QuestionSkill skill, Pageable pageable) {
        if (!examRepository.existsById(examId)) {
            throw new ResourceNotFoundException("Không tìm thấy bài thi với ID: " + examId);
        }
        Page<Question> questions = questionRepository.findByExamIdAndSkill(examId, skill, pageable);
        return questions.map(this::convertToResponse);
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
                .examTitle(question.getExam() != null ? question.getExam().getTitle() : null)
                .skill(question.getSkill())
                .questionText(question.getQuestionText())
                .audioUrl(question.getAudioUrl())
                .options(parsedOptions)
                .correctAnswer(question.getCorrectAnswer())
                .explanation(question.getExplanation())
                .build();
    }
}