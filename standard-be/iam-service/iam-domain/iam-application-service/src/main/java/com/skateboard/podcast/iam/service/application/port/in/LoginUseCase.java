package com.skateboard.podcast.iam.service.application.port.in;

import com.skateboard.podcast.iam.service.application.dto.AuthResult;

public interface LoginUseCase {
    AuthResult login(String email, String password, String deviceId, String deviceName);
}
