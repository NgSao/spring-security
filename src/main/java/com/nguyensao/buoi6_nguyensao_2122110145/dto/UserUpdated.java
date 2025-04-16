package com.nguyensao.buoi6_nguyensao_2122110145.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdated {
    private Long id;
    private String email;
    private String role;
    private Boolean active;

}