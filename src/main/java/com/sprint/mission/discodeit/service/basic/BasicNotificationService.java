package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.data.NotificationDto;
import com.sprint.mission.discodeit.entity.Notification;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.notification.NotificationAccessDeniedException;
import com.sprint.mission.discodeit.exception.notification.NotificationNotFoundException;
import com.sprint.mission.discodeit.mapper.NotificationMapper;
import com.sprint.mission.discodeit.repository.NotificationRepository;
import com.sprint.mission.discodeit.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicNotificationService implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    @Override
    public NotificationDto create(User user, String title, String content) {
        Notification notification = new Notification(user, title, content);
        notificationRepository.save(notification);
        return notificationMapper.toDto(notification);
    }

    @Override
    public List<NotificationDto> getNotifications(UUID userId) {
        return notificationRepository.findAllByUserId(userId)
                .stream()
                .map(notificationMapper::toDto)
                .toList();
    }

    @Override
    public void confirm(UUID notificationId, UUID requestUserId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> NotificationNotFoundException.withId(notificationId));

        if (!notification.getUser().getId().equals(requestUserId)) {
            throw new NotificationAccessDeniedException();
        }

        notificationRepository.delete(notification);
    }
}
