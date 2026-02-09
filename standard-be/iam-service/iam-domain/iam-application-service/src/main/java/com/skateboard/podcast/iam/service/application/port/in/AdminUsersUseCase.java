package com.skateboard.podcast.iam.service.application.port.in;

import com.skateboard.podcast.domain.valueobject.UserStatus;
import com.skateboard.podcast.iam.service.application.dto.AdminUserUpdateCommand;
import com.skateboard.podcast.iam.service.application.dto.AdminUserView;

import java.util.List;
import java.util.UUID;

public interface AdminUsersUseCase {

    List<AdminUserView> list();

    AdminUserView update(UUID userId, AdminUserUpdateCommand command, UUID actorUserId);

    AdminUserView updateStatus(UUID userId, UserStatus status, UUID actorUserId);

    void delete(UUID userId, UUID actorUserId);
}
