package com.edu.espp.config;

import com.edu.espp.common.enums.LevelLesson;
import com.edu.espp.common.enums.TypeLesson;
import com.edu.espp.entity.Lesson;
import com.edu.espp.entity.LessonContent;
import com.edu.espp.entity.User;
import com.edu.espp.repository.LessonContentRepository;
import com.edu.espp.repository.LessonRepository;
import com.edu.espp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@Order(2)
@RequiredArgsConstructor
@Slf4j
public class LearningContentSeeder implements CommandLineRunner {

    private final LessonRepository lessonRepository;
    private final LessonContentRepository lessonContentRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void run(String... args) {
        User admin = userRepository.findByEmail("admin@espp.com").orElse(null);

        seedLesson(
                "A1 Greetings and Self Introduction",
                LevelLesson.A1,
                TypeLesson.VOCABULARY,
                "Từ vựng cơ bản dùng khi chào hỏi và giới thiệu bản thân.",
                admin,
                new ContentSeed("Nice to meet you", "/naɪs tə miːt juː/", "Rất vui được gặp bạn", 1,
                        "Dùng khi gặp ai đó lần đầu trong ngữ cảnh lịch sự hoặc thân thiện.",
                        "Nice to meet you, Lan."),
                new ContentSeed("Where are you from?", "/wer ɑːr juː frəm/", "Bạn đến từ đâu?", 2,
                        "Dùng để hỏi quê quán hoặc quốc gia của người đối diện.",
                        "Where are you from? I am from Vietnam."),
                new ContentSeed("My name is", "/maɪ neɪm ɪz/", "Tên của tôi là", 3,
                        "Cấu trúc đơn giản để giới thiệu tên của bản thân.",
                        "My name is Huy.")
        );

        seedLesson(
                "A1 Daily Vocabulary",
                LevelLesson.A1,
                TypeLesson.VOCABULARY,
                "Từ vựng hằng ngày giúp học viên giao tiếp trong các tình huống quen thuộc.",
                admin,
                new ContentSeed("breakfast", "/ˈbrekfəst/", "bữa sáng", 1,
                        "Danh từ chỉ bữa ăn đầu tiên trong ngày.",
                        "I have breakfast at seven o'clock."),
                new ContentSeed("commute", "/kəˈmjuːt/", "đi lại hằng ngày", 2,
                        "Dùng để nói việc di chuyển thường xuyên giữa nhà và nơi học hoặc làm.",
                        "I commute to school by bus."),
                new ContentSeed("schedule", "/ˈskedʒuːl/", "lịch trình", 3,
                        "Danh từ chỉ kế hoạch thời gian cho công việc hoặc học tập.",
                        "My schedule is busy today.")
        );

        seedLesson(
                "A2 Present Simple",
                LevelLesson.A2,
                TypeLesson.GRAMMAR,
                "Cách dùng thì hiện tại đơn để nói về thói quen, sự thật và lịch trình.",
                admin,
                new ContentSeed("Subject + V(s/es)", "", "Cấu trúc hiện tại đơn", 1,
                        "Thêm s hoặc es vào động từ khi chủ ngữ là he, she, it hoặc danh từ số ít.",
                        "She studies English every day."),
                new ContentSeed("Adverbs of frequency", "", "Trạng từ chỉ tần suất", 2,
                        "Các từ như always, usually, often, sometimes thường đứng trước động từ chính.",
                        "I usually review vocabulary at night."),
                new ContentSeed("Do/Does + subject + verb?", "", "Câu hỏi hiện tại đơn", 3,
                        "Dùng do hoặc does để tạo câu hỏi trong thì hiện tại đơn.",
                        "Does he speak English?")
        );

        seedLesson(
                "A2 Past Simple",
                LevelLesson.A2,
                TypeLesson.GRAMMAR,
                "Cách dùng thì quá khứ đơn để kể lại sự việc đã kết thúc trong quá khứ.",
                admin,
                new ContentSeed("regular verbs", "", "động từ có quy tắc", 1,
                        "Thêm ed vào động từ có quy tắc khi dùng thì quá khứ đơn.",
                        "I watched a movie last night."),
                new ContentSeed("irregular verbs", "", "động từ bất quy tắc", 2,
                        "Một số động từ có dạng quá khứ riêng và cần học thuộc.",
                        "She went to Da Nang last summer."),
                new ContentSeed("Did + subject + verb?", "", "câu hỏi quá khứ đơn", 3,
                        "Dùng did để đặt câu hỏi, động từ chính giữ nguyên mẫu.",
                        "Did you finish your homework?")
        );

        seedLesson(
                "B1 IPA Short and Long Vowels",
                LevelLesson.B1,
                TypeLesson.PRONUNCIATION,
                "Luyện phân biệt các cặp nguyên âm ngắn và dài thường gặp trong IPA.",
                admin,
                new ContentSeed("ship / sheep", "/ʃɪp/ - /ʃiːp/", "tàu / con cừu", 1,
                        "Cặp âm /ɪ/ và /iː/ khác nhau về độ dài và vị trí lưỡi.",
                        "The ship is near the sheep farm."),
                new ContentSeed("full / fool", "/fʊl/ - /fuːl/", "đầy / kẻ ngốc", 2,
                        "Cặp âm /ʊ/ và /uː/ cần phân biệt rõ để tránh nhầm nghĩa.",
                        "The cup is full."),
                new ContentSeed("bit / beat", "/bɪt/ - /biːt/", "một chút / đánh bại", 3,
                        "Âm dài thường kéo dài hơn và căng hơn âm ngắn.",
                        "Can you beat this level?")
        );

        seedLesson(
                "B1 Speaking Connectors",
                LevelLesson.B1,
                TypeLesson.GRAMMAR,
                "Cấu trúc nối ý giúp câu trả lời nói mạch lạc và tự nhiên hơn.",
                admin,
                new ContentSeed("In my opinion", "", "theo ý kiến của tôi", 1,
                        "Dùng để mở đầu khi đưa ra quan điểm cá nhân.",
                        "In my opinion, online learning is convenient."),
                new ContentSeed("For example", "", "ví dụ như", 2,
                        "Dùng để đưa ví dụ minh họa cho ý vừa nói.",
                        "For example, I use flashcards to remember words."),
                new ContentSeed("As a result", "", "kết quả là", 3,
                        "Dùng để nối nguyên nhân với kết quả.",
                        "I practiced every day. As a result, my pronunciation improved.")
        );

        seedLesson(
                "B2 Environment Vocabulary",
                LevelLesson.B2,
                TypeLesson.VOCABULARY,
                "Từ vựng học thuật về môi trường, biến đổi khí hậu và phát triển bền vững.",
                admin,
                new ContentSeed("sustainable", "/səˈsteɪnəbl/", "bền vững", 1,
                        "Tính từ chỉ điều gì đó có thể duy trì lâu dài mà không gây hại lớn.",
                        "We need sustainable energy solutions."),
                new ContentSeed("carbon footprint", "/ˈkɑːrbən ˈfʊtprɪnt/", "lượng khí thải carbon", 2,
                        "Chỉ tổng lượng khí nhà kính do một người, tổ chức hoặc hoạt động tạo ra.",
                        "Taking public transport can reduce your carbon footprint."),
                new ContentSeed("renewable energy", "/rɪˈnuːəbl ˈenərdʒi/", "năng lượng tái tạo", 3,
                        "Nguồn năng lượng đến từ tự nhiên và có thể tái tạo như mặt trời, gió.",
                        "Solar power is a form of renewable energy.")
        );

        seedLesson(
                "B2 Academic Writing Structures",
                LevelLesson.B2,
                TypeLesson.GRAMMAR,
                "Cấu trúc câu hữu ích khi viết đoạn văn học thuật và bài luận ngắn.",
                admin,
                new ContentSeed("It is widely believed that", "", "nhiều người tin rằng", 1,
                        "Dùng để giới thiệu một quan điểm phổ biến trong văn viết học thuật.",
                        "It is widely believed that education improves career opportunities."),
                new ContentSeed("This leads to", "", "điều này dẫn đến", 2,
                        "Dùng để diễn tả kết quả hoặc hệ quả của một vấn đề.",
                        "This leads to better communication among team members."),
                new ContentSeed("On the other hand", "", "mặt khác", 3,
                        "Dùng để trình bày ý đối lập hoặc góc nhìn khác.",
                        "On the other hand, technology can create distractions.")
        );

        log.info("Learning content seed completed.");
    }

    private void seedLesson(
            String title,
            LevelLesson level,
            TypeLesson type,
            String description,
            User createdBy,
            ContentSeed... contents
    ) {
        Lesson lesson = lessonRepository.findByExactTitle(title)
                .orElseGet(() -> lessonRepository.save(Lesson.builder()
                        .title(title)
                        .level(level)
                        .type(type)
                        .description(description)
                        .createdBy(createdBy)
                        .isPublished(true)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build()));

        for (ContentSeed content : contents) {
            if (!lessonContentRepository.existsContentInLesson(lesson.getId(), content.wordOrStructure())) {
                lessonContentRepository.save(LessonContent.builder()
                        .lesson(lesson)
                        .wordOrStructure(content.wordOrStructure())
                        .ipa(content.ipa())
                        .meaning(content.meaning())
                        .contentOrder(content.contentOrder())
                        .explanation(content.explanation())
                        .example(content.example())
                        .build());
            }
        }
    }

    private record ContentSeed(
            String wordOrStructure,
            String ipa,
            String meaning,
            Integer contentOrder,
            String explanation,
            String example
    ) {
    }
}
