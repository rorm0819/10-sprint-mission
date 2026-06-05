package com.sprint.mission.discodeit.event.listener;

import com.sprint.mission.discodeit.event.MessageCreatedEvent;
import com.sprint.mission.discodeit.event.RoleUpdatedEvent;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.NotificationService;
import com.sprint.mission.discodeit.service.ReadStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class NotificationRequiredEventListener {
    private final NotificationService notificationService;
    private final ReadStatusService readStatusService;
    private final ReadStatusRepository readStatusRepository;
    private final UserRepository userRepository;

    @TransactionalEventListener
    public void on(MessageCreatedEvent event) {
        readStatusRepository.findAllByChannelIdAndNotificationEnabledTrue(event.channelId())
                .stream()
                .filter(readStatus -> !readStatus.getUser().getId().equals(event.senderId()))
                .forEach(readStatus -> notificationService.create(
                        readStatus.getUser(),
                        event.senderUsername() + " (#" + event.channelName() + ")",
                        event.content()
                ));
    }

    @TransactionalEventListener
    public void on(RoleUpdatedEvent event) {
        notificationService.create(
                userRepository.findById(event.userId())
                        .orElseThrow(() -> UserNotFoundException.withId(event.userId())),
                "권한이 변경되었습니다.",
                event.oldRole() + " -> " + event.newRole()
        );
    }
}
