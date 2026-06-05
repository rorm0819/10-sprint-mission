package com.sprint.mission.discodeit.event;

import java.util.UUID;

public record MessageCreatedEvent(
        UUID messageId,
        UUID channelId,
        UUID senderId,
        String channelName,
        String senderUsername,
        String content
) {
}
