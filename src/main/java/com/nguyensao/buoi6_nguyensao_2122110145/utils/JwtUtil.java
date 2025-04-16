package com.nguyensao.buoi6_nguyensao_2122110145.utils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import com.nguyensao.buoi6_nguyensao_2122110145.entity.User;
import com.nguyensao.buoi6_nguyensao_2122110145.exception.AppException;
import com.nguyensao.buoi6_nguyensao_2122110145.repository.UserRepository;

@Component
public class JwtUtil {

    private final UserRepository userRepository;
    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    public JwtUtil(UserRepository userRepository, JwtEncoder jwtEncoder, JwtDecoder jwtDecoder) {
        this.userRepository = userRepository;
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
    }

    @Value("${jwt.secret}")
    private String SECRET_KEY;
    public static final long EXPIRATION_TIME = (24 * 60 * 60) * 2;
    public static final long REFRESH_TOKEN_EXP = (24 * 60 * 60) * 12;
    public static final MacAlgorithm JWT_ALGORITHM = MacAlgorithm.HS512;

    public String generateToken(String email) {
        Instant now = Instant.now();
        Instant validity = now.plus(EXPIRATION_TIME, ChronoUnit.SECONDS);
        User user = userRepository.findByEmail(email).orElseThrow(() -> new AppException("User not found"));
        JwtClaimsSet claimsSet = JwtClaimsSet.builder()
                .issuedAt(now)
                .expiresAt(validity)
                .subject(email)
                .claim("role", user.getRole())
                .build();
        JwsHeader jwsHeader = JwsHeader.with(JWT_ALGORITHM).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claimsSet)).getTokenValue();
    }

    public String getSubjectFromToken(String token) {
        try {
            Jwt jwt = jwtDecoder.decode(token);
            return jwt.getSubject();
        } catch (Exception e) {
            throw new AppException("Không thể giải mã token: " + e.getMessage());
        }
    }

}