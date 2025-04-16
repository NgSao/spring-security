package com.nguyensao.buoi6_nguyensao_2122110145.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserCreate {
    private String email;
    private String password;
    private String role;
    private Boolean active;
}