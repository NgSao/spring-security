package com.nguyensao.buoi6_nguyensao_2122110145.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OAuthResponse {
    private boolean userExists;
    private String googleLoginUrl;
    private String facebookLoginUrl;
}
