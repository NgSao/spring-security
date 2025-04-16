package com.nguyensao.buoi6_nguyensao_2122110145.dto;

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
public class UserProviderDto {

    private Long id;

    private String provider;

    private String providerUserId;

}
