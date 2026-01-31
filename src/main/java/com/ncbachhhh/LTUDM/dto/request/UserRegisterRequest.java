package com.ncbachhhh.LTUDM.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRegisterRequest {
    private String email;
    private String username;
    private String password;
    private String display_name;
    private String avatar_url;
}
