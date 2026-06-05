package com.sprint.mission.discodeit.controller.api;

import com.sprint.mission.discodeit.dto.data.NotificationDto;
import com.sprint.mission.discodeit.entity.details.DiscodeitUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

public interface NotificationApi {

    ResponseEntity<List<NotificationDto>> getNotifications(@AuthenticationPrincipal DiscodeitUserDetails userDetails);

    ResponseEntity<Void> confirm(@PathVariable UUID notificationId,
                                 @AuthenticationPrincipal DiscodeitUserDetails userDetails);
}
