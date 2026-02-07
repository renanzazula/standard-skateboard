package com.skateboard.podcast.iam.service.application.port.in;

import com.skateboard.podcast.domain.valueobject.Provider;
import com.skateboard.podcast.iam.service.application.dto.AuthResult;

public interface SocialLoginUseCase {
    AuthResult login(Provider provider, String token, String deviceId, String deviceName);
}
