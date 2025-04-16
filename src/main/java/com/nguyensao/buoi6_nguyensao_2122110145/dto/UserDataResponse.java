package com.nguyensao.buoi6_nguyensao_2122110145.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDataResponse {
    private Long id;
    private String email;
    private String role;
    private Boolean active;
    private List<UserProviderDto> providerDtos;

}