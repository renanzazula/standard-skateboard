package com.skateboard.podcast.iam.service.application.port.in;

import com.skateboard.podcast.iam.service.application.dto.AuthResult;

public interface RefreshUseCase {
    AuthResult refresh(String refreshToken, String deviceId);
}
