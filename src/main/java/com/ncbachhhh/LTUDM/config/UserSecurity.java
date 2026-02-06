package com.ncbachhhh.LTUDM.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Slf4j
@Component("userSecurity")
public class UserSecurity {

//      Kiểm tra xem người dùng hiện tại có phải là chủ sở hữu tài khoản hay không
//      So sánh userId từ JWT token với userId trong request

    public boolean isOwner(Authentication authentication, String userId) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return false;
        }

        // Lấy JWT từ authentication
        Jwt jwt = (Jwt) authentication.getPrincipal();

        // Subject chứa userId
        String tokenUserId = jwt.getSubject();


        return userId.equals(tokenUserId);
    }
}
