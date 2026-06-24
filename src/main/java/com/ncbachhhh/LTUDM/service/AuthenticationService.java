package com.ncbachhhh.LTUDM.service;

import com.ncbachhhh.LTUDM.dto.request.AuthenticationRequest;
import com.ncbachhhh.LTUDM.dto.request.LogoutRequest;
import com.ncbachhhh.LTUDM.dto.request.RefreshTokenRequest;
import com.ncbachhhh.LTUDM.dto.response.AuthenticationResponse;
import com.ncbachhhh.LTUDM.entity.InvalidatedToken;
import com.ncbachhhh.LTUDM.entity.User.User;
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
import lombok.RequiredArgsConstructor;
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
public class AuthenticationService {
    private static final String ISSUER = "LTUDM";
    private static final String ROLE_PREFIX = "ROLE_";

    private final UserRepository userRepository;
    private final InvalidatedTokenRepository invalidatedTokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-token-expiration:3600}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration:604800}")
    private long refreshTokenExpiration;

    // Đăng nhập bằng email/password, sau do cấp access/refresh token nếu user hợp lệ.
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new AppException(ErrorCode.WRONG_PASSWORD);
        }

        ensureActive(user);
        return buildTokenResponse(user);
    }

    // Refresh token theo có che rotate: verify refresh token cũ, revoke nó, rồi cấp cặp token mới.
    public AuthenticationResponse refreshToken(RefreshTokenRequest request) throws ParseException, JOSEException {
        SignedJWT refreshToken = verifyToken(request.getRefreshToken());
        revoke(refreshToken);

        User user = userRepository.findById(refreshToken.getJWTClaimsSet().getSubject())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        ensureActive(user);
        return buildTokenResponse(user);
    }

    // Logout bằng cách revoke token được gửi len; token invalid/expired thì coi như đã logout.
    public void logout(LogoutRequest request) throws ParseException, JOSEException {
        try {
            revoke(verifyToken(request.getToken()));
        } catch (AppException exception) {
            log.info("Token already expired or invalid");
        }
    }

    // Đóng gói access token và refresh token vào response chung của auth API.
    private AuthenticationResponse buildTokenResponse(User user) {
        return AuthenticationResponse.builder()
                .accessToken(generateToken(user.getId(), String.valueOf(user.getRole()), accessTokenExpiration))
                .refreshToken(generateToken(user.getId(), String.valueOf(user.getRole()), refreshTokenExpiration))
                .isAuthenticated(true)
                .build();
    }

    // Chặn user inactive/banned đăng nhập hoặc refresh token.
    private void ensureActive(User user) {
        if (!user.isActive()) {
            throw new AppException(ErrorCode.USER_BANNED);
        }
    }

    // Lưu jti của JWT vào bằng invalidated_tokens để token không dùng lại được.
    private void revoke(SignedJWT token) throws ParseException {
        invalidatedTokenRepository.save(InvalidatedToken.builder()
                .id(token.getJWTClaimsSet().getJWTID())
                .expiryTime(token.getJWTClaimsSet().getExpirationTime())
                .build());
    }

    // Parse và verify JWT bằng HS256, dong thời kiểm tra expiry và blacklist.
    private SignedJWT verifyToken(String token) throws ParseException, JOSEException {
        SignedJWT signedJwt = SignedJWT.parse(token);
        JWSVerifier verifier = new MACVerifier(secretKey.getBytes());

        if (!signedJwt.verify(verifier) || isExpired(signedJwt)) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }

        if (invalidatedTokenRepository.existsById(signedJwt.getJWTClaimsSet().getJWTID())) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }

        return signedJwt;
    }

    // JWT het han nếu không có exp hoặc exp nằm trước thời điểm hiện tại.
    private boolean isExpired(SignedJWT signedJwt) throws ParseException {
        Date expiration = signedJwt.getJWTClaimsSet().getExpirationTime();
        return expiration == null || expiration.before(new Date());
    }

    // Tạo JWT có subject là userId, scope là ROLE_<role>, và jti để phục vụ revoke.
    private String generateToken(String userId, String role, long expirationSeconds) {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(userId)
                .issuer(ISSUER)
                .issueTime(new Date())
                .expirationTime(Date.from(Instant.now().plus(expirationSeconds, ChronoUnit.SECONDS)))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", ROLE_PREFIX + role)
                .build();

        JWSObject jwsObject = new JWSObject(new JWSHeader(JWSAlgorithm.HS256), new Payload(claims.toJSONObject()));

        try {
            jwsObject.sign(new MACSigner(secretKey.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException exception) {
            log.error("Error while generating token", exception);
            throw new AppException(ErrorCode.INTERNAL_ERROR);
        }
    }
}
