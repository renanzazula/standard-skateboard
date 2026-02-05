package com.skateboard.podcast.iam.service.dataaccess.crypto;


import com.skateboard.podcast.iam.service.application.port.out.PasswordHasher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BcryptPasswordHasherAdapter implements PasswordHasher {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    @Override
    public String hash(final String rawPassword) {
        return encoder.encode(rawPassword);
    }

    @Override
    public boolean matches(final String rawPassword, final String hash) {
        return encoder.matches(rawPassword, hash);
    }
}
