package com.ncbachhhh.LTUDM.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UserCreationRequest {
    private String email;
    private String username;
    private String password;
    private String display_name;
    private String avatar_url;
}
