package com.ncbachhhh.LTUDM.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component("userSecurity")
public class UserSecurity {
    public boolean isOwner(Authentication authentication, String userId) {
        if (authentication == null) {
            return false;
        }

        if (!(authentication.getPrincipal() instanceof Jwt jwt)) {
            return false;
        }

        return userId.equals(jwt.getSubject());
    }
}
