package com.edu.espp.config;

import com.edu.espp.common.enums.*;
import com.edu.espp.entity.*;
import com.edu.espp.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {

    private final StudentUserRepository studentUserRepository;
    private final UserRepository userRepository;
    private final LessonRepository lessonRepository;
    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;
    private final ExamHistoryRepository examHistoryRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("DatabaseSeeder checking and seeding test data...");

        String passwordHash = passwordEncoder.encode("123456");

        // 1. Seed Admin
        User admin = seedUser("admin@espp.com", passwordHash, "Admin ESPP", Role.ADMIN);

        // 2. Seed Staff
        User staff = seedUser("staff@espp.com", passwordHash, "Staff ESPP", Role.STAFF);

        // 3. Seed Students
        User student1 = seedStudentUser("student1@espp.com", passwordHash, "Nguyễn Văn Học Sinh 1");
        User student2 = seedStudentUser("student2@espp.com", passwordHash, "Trần Thị Học Sinh 2");
        User student3 = seedStudentUser("student3@espp.com", passwordHash, "Lê Minh Học Sinh 3");

        // 3. Seed Lessons
        if (false && lessonRepository.count() == 0) {
            log.info("Seeding lessons...");
            lessonRepository.saveAll(Arrays.asList(
                    Lesson.builder().title("Thì hiện tại đơn (Present Simple)").level(LevelLesson.A1).type(TypeLesson.GRAMMAR).createdBy(admin).approvalStatus(ApprovalStatus.APPROVED).description("Học về cách sử dụng thì hiện tại đơn trong tiếng Anh hàng ngày.").build(),
                    Lesson.builder().title("Từ vựng về Gia đình").level(LevelLesson.A1).type(TypeLesson.VOCABULARY).createdBy(admin).approvalStatus(ApprovalStatus.APPROVED).description("Các từ vựng cơ bản về các thành viên trong gia đình.").build(),
                    Lesson.builder().title("Phát âm đuôi -ed và -s/es").level(LevelLesson.A2).type(TypeLesson.PRONUNCIATION).createdBy(admin).approvalStatus(ApprovalStatus.APPROVED).description("Quy tắc phát âm đuôi ed và s/es siêu dễ nhớ.").build(),
                    Lesson.builder().title("Câu điều kiện loại 1 & 2").level(LevelLesson.B1).type(TypeLesson.GRAMMAR).createdBy(admin).approvalStatus(ApprovalStatus.APPROVED).description("Hướng dẫn phân biệt và sử dụng If loại 1, 2.").build(),
                    Lesson.builder().title("Từ vựng chủ đề Công nghệ (Technology)").level(LevelLesson.B2).type(TypeLesson.VOCABULARY).createdBy(admin).approvalStatus(ApprovalStatus.APPROVED).description("Nâng cao vốn từ vựng về công nghệ thông tin.").build()
            ));
        }

        // 4. Seed Exams & Questions
        log.info("Checking exams and questions...");
        
        if (!examRepository.existsByTitle("Đề kiểm tra Ngữ pháp Cơ bản")) {
            Exam exam1 = Exam.builder()
                .title("Đề kiểm tra Ngữ pháp Cơ bản")
                .type(TypeQuiz.QUIZ)
                .duration(15)
                .totalQuestions(3)
                .description("Bài kiểm tra 15 phút về thì hiện tại đơn.")
                .createdBy(admin)
                .approvalStatus(ApprovalStatus.APPROVED)
                .build();
            exam1 = examRepository.save(exam1);

            questionRepository.saveAll(Arrays.asList(
                    Question.builder().exam(exam1).skill(QuestionSkill.READING).questionText("He ___ to school every day.").options("{\"A\": \"go\", \"B\": \"goes\", \"C\": \"going\", \"D\": \"gone\"}").correctAnswer("B").explanation("Ngôi thứ 3 số ít dùng goes.").build(),
                    Question.builder().exam(exam1).skill(QuestionSkill.READING).questionText("I ___ a student.").options("{\"A\": \"am\", \"B\": \"is\", \"C\": \"are\", \"D\": \"be\"}").correctAnswer("A").explanation("I đi với am.").build(),
                    Question.builder().exam(exam1).skill(QuestionSkill.READING).questionText("They ___ play football on Sundays.").options("{\"A\": \"don't\", \"B\": \"doesn't\", \"C\": \"not\", \"D\": \"isn't\"}").correctAnswer("A").explanation("They dùng trợ động từ do + not = don't.").build()
            ));
        }

        if (!examRepository.existsByTitle("Đề thi thử TOEIC Reading (Mini)")) {
            Exam exam2 = Exam.builder()
                .title("Đề thi thử TOEIC Reading (Mini)")
                .type(TypeQuiz.MOCK_TEST)
                .duration(30)
                .totalQuestions(2)
                .description("Đề thi thử TOEIC rút gọn phần Đọc hiểu.")
                .createdBy(admin)
                .approvalStatus(ApprovalStatus.APPROVED)
                .build();
            exam2 = examRepository.save(exam2);

            questionRepository.saveAll(Arrays.asList(
                    Question.builder().exam(exam2).skill(QuestionSkill.READING).questionText("The manager asked the team to finish the report ___ Friday.").options("{\"A\": \"in\", \"B\": \"on\", \"C\": \"by\", \"D\": \"at\"}").correctAnswer("C").explanation("By + mốc thời gian: trước thời điểm đó.").build(),
                    Question.builder().exam(exam2).skill(QuestionSkill.READING).questionText("Due to the bad weather, the flight was ___.").options("{\"A\": \"delayed\", \"B\": \"booked\", \"C\": \"arrived\", \"D\": \"departed\"}").correctAnswer("A").explanation("Chuyến bay bị hoãn (delayed) do thời tiết xấu.").build()
            ));
        }

        // 5. Seed Exam Histories (only if we have less than 4 to avoid infinite growth)
        if (examHistoryRepository.count() < 4) {
            log.info("Seeding exam histories...");
            List<Exam> exams = examRepository.findAll();
            if (exams.size() >= 2) {
                Exam ex1 = exams.get(0);
                Exam ex2 = exams.get(1);

                examHistoryRepository.saveAll(Arrays.asList(
                        ExamHistory.builder().user(student1).exam(ex1).score(10.0).correctAnswersCount(3).timeSpent(300).testedAt(LocalDateTime.now().minusDays(1)).build(),
                        ExamHistory.builder().user(student2).exam(ex1).score(6.67).correctAnswersCount(2).timeSpent(450).testedAt(LocalDateTime.now().minusHours(5)).build(),
                        ExamHistory.builder().user(student3).exam(ex2).score(5.0).correctAnswersCount(1).timeSpent(1200).testedAt(LocalDateTime.now().minusDays(2)).build(),
                        ExamHistory.builder().user(student1).exam(ex2).score(10.0).correctAnswersCount(2).timeSpent(800).testedAt(LocalDateTime.now().minusMinutes(30)).build()
                ));
            }
        }

        log.info("DatabaseSeeder check completed.");
    }

    private User seedUser(String email, String passwordHash, String fullName, Role role) {
        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            return existingUser.get();
        }
        User user = User.builder().email(email).passwordHash(passwordHash).fullName(fullName).role(role).status(UserStatus.ACTIVE).createdAt(LocalDateTime.now()).build();
        user = userRepository.save(user);
        log.info("Seeded user: {}", email);
        return user;
    }

    private User seedStudentUser(String email, String passwordHash, String fullName) {
        User user = seedUser(email, passwordHash, fullName, Role.STUDENT);

        if (!studentUserRepository.existsByUser_Id(user.getId())) {
            StudentUser student = StudentUser.builder()
                    .user(user)
                    .build();
            studentUserRepository.save(student);
        }

        return user;
    }
}
