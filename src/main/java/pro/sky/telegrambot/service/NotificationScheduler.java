package pro.sky.telegrambot.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class NotificationScheduler {

    private static final Logger logger = LoggerFactory.getLogger(NotificationScheduler.class);

    private final NotificationTaskRepository repository;
    private final TelegramBot telegramBot;

    public NotificationScheduler(NotificationTaskRepository repository, TelegramBot telegramBot) {
        this.repository = repository;
        this.telegramBot = telegramBot;
    }

    @Scheduled(cron = "0 0/1 * * * *")
    public void sendNotifications() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        List<NotificationTask> tasks = repository.findAllByDateTime(now);

        for (NotificationTask task : tasks) {
            Long chatId = task.getChatId();
            String text = task.getMessage();

            SendResponse response = telegramBot.execute(new SendMessage(chatId, text));

            if (response.isOk()) {
                logger.info("Отправлено напоминание id={}", task.getId());
            } else {
                logger.error("Не удалось отправить id={}, код {}", task.getId(), response.errorCode());
            }
        }
    }
}