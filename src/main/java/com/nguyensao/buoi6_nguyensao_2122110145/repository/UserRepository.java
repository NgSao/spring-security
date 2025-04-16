package com.nguyensao.buoi6_nguyensao_2122110145.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import com.nguyensao.buoi6_nguyensao_2122110145.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByProviderAndProviderId(String provider, String providerId);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.providers WHERE u.email = :email")
    Optional<User> findByEmailWithProviders(@Param("email") String email);
}
