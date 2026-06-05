package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.controller.api.NotificationApi;
import com.sprint.mission.discodeit.dto.data.NotificationDto;
import com.sprint.mission.discodeit.entity.details.DiscodeitUserDetails;
import com.sprint.mission.discodeit.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController implements NotificationApi {
    private final NotificationService notificationService;

    @Override
    public ResponseEntity<List<NotificationDto>> getNotifications(DiscodeitUserDetails userDetails) {
        List<NotificationDto> notifications = notificationService.getNotifications(userDetails.getUserDto().id());

        return ResponseEntity.status(HttpStatus.OK).body(notifications);
    }

    @Override
    public ResponseEntity<Void> confirm(UUID notificationId, DiscodeitUserDetails userDetails) {
        notificationService.confirm(notificationId, userDetails.getUserDto().id());

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
