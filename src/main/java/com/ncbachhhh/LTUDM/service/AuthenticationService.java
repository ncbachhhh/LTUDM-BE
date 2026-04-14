package com.ncbachhhh.LTUDM.service;

import com.ncbachhhh.LTUDM.dto.request.AuthenticationRequest;
import com.ncbachhhh.LTUDM.dto.request.IntrospectRequest;
import com.ncbachhhh.LTUDM.dto.request.LogoutRequest;
import com.ncbachhhh.LTUDM.dto.request.RefreshTokenRequest;
import com.ncbachhhh.LTUDM.dto.response.AuthenticationResponse;
import com.ncbachhhh.LTUDM.dto.response.IntrospectResponse;
import com.ncbachhhh.LTUDM.entity.InvalidatedToken;
import com.ncbachhhh.LTUDM.exception.AppException;
import com.ncbachhhh.LTUDM.exception.ErrorCode;
import com.ncbachhhh.LTUDM.repository.InvalidatedTokenRepository;
import com.ncbachhhh.LTUDM.repository.UserRepository;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {
    UserRepository userRepository;
    InvalidatedTokenRepository invalidatedTokenRepository;
    PasswordEncoder passwordEncoder;

    @NonFinal
    @Value("${jwt.secret}")
    protected String SECRET_KEY;

    @NonFinal
    @Value("${jwt.access-token-expiration:3600}")
    protected long ACCESS_TOKEN_EXPIRATION;

    @NonFinal
    @Value("${jwt.refresh-token-expiration:604800}")
    protected long REFRESH_TOKEN_EXPIRATION;

    // Kiểm tra token còn hợp lệ hay không
    public IntrospectResponse introspect(IntrospectRequest request) throws ParseException, JOSEException {
        boolean isValid = true;
        try {
            verifyToken(request.getToken());
        } catch (AppException e) {
            isValid = false;
        }

        return IntrospectResponse.builder()
                .valid(isValid)
                .build();
    }

    // Xác thực email và mật khẩu để cấp access token và refresh token
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new AppException(ErrorCode.WRONG_PASSWORD);
        }
        if (!user.isActive()) {
            throw new AppException(ErrorCode.USER_BANNED);
        }

        String accessToken = generateToken(user.getId(), String.valueOf(user.getRole()), ACCESS_TOKEN_EXPIRATION);
        String refreshToken = generateToken(user.getId(), String.valueOf(user.getRole()), REFRESH_TOKEN_EXPIRATION);

        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .isAuthenticated(true)
                .build();
    }

    // Xác thực refresh token cũ, thu hồi nó và cấp cặp token mới
    public AuthenticationResponse refreshToken(RefreshTokenRequest request) throws ParseException, JOSEException {
        SignedJWT signedJWT = verifyToken(request.getRefreshToken());

        invalidatedTokenRepository.save(InvalidatedToken.builder()
                .id(signedJWT.getJWTClaimsSet().getJWTID())
                .expiryTime(signedJWT.getJWTClaimsSet().getExpirationTime())
                .build());

        var user = userRepository.findById(signedJWT.getJWTClaimsSet().getSubject())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (!user.isActive()) {
            throw new AppException(ErrorCode.USER_BANNED);
        }

        return AuthenticationResponse.builder()
                .accessToken(generateToken(user.getId(), String.valueOf(user.getRole()), ACCESS_TOKEN_EXPIRATION))
                .refreshToken(generateToken(user.getId(), String.valueOf(user.getRole()), REFRESH_TOKEN_EXPIRATION))
                .isAuthenticated(true)
                .build();
    }

    // Đăng xuất bằng cách đưa token hiện tại vào danh sách đã thu hồi
    public void logout(LogoutRequest request) throws ParseException, JOSEException {
        try {
            SignedJWT signedJWT = verifyToken(request.getToken());
            invalidatedTokenRepository.save(InvalidatedToken.builder()
                    .id(signedJWT.getJWTClaimsSet().getJWTID())
                    .expiryTime(signedJWT.getJWTClaimsSet().getExpirationTime())
                    .build());
        } catch (AppException e) {
            log.info("Token already expired or invalid");
        }
    }

    // Kiểm tra chữ ký, hạn dùng và trạng thái thu hồi của token
    private SignedJWT verifyToken(String token) throws ParseException, JOSEException {
        JWSVerifier verifier = new MACVerifier(SECRET_KEY.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);

        if (!signedJWT.verify(verifier) || signedJWT.getJWTClaimsSet().getExpirationTime().before(new Date())) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }
        if (invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID())) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }

        return signedJWT;
    }

    // Tạo JWT chứa thông tin user và quyền truy cập
    private String generateToken(String userId, String role, long expirationSeconds) {
        JWSObject jwsObject = new JWSObject(
                new JWSHeader(JWSAlgorithm.HS256),
                new Payload(new JWTClaimsSet.Builder()
                        .subject(userId)
                        .issuer("LTUDM")
                        .issueTime(new Date())
                        .expirationTime(new Date(Instant.now().plus(expirationSeconds, ChronoUnit.SECONDS).toEpochMilli()))
                        .jwtID(UUID.randomUUID().toString())
                        .claim("scope", "ROLE_" + role)
                        .build().toJSONObject())
        );

        try {
            jwsObject.sign(new MACSigner(SECRET_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Error while generating token: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
