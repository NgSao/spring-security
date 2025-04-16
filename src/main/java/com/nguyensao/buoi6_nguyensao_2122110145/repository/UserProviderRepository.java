package com.nguyensao.buoi6_nguyensao_2122110145.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nguyensao.buoi6_nguyensao_2122110145.entity.UserProvider;

@Repository
public interface UserProviderRepository extends JpaRepository<UserProvider, Long> {
    boolean existsByProviderAndProviderUserId(String provider, String providerUserId);

}
