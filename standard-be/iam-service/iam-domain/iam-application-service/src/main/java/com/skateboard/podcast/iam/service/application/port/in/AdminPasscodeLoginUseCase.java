package com.skateboard.podcast.iam.service.application.port.in;

import com.skateboard.podcast.iam.service.application.dto.AuthResult;

public interface AdminPasscodeLoginUseCase {
    AuthResult login(String passcode, String deviceId, String deviceName);
}
