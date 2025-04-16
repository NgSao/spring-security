package com.nguyensao.buoi6_nguyensao_2122110145.security;

import java.util.Collections;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import com.nguyensao.buoi6_nguyensao_2122110145.entity.User;
import com.nguyensao.buoi6_nguyensao_2122110145.exception.AppException;
import com.nguyensao.buoi6_nguyensao_2122110145.repository.UserRepository;

@Component("userDetailsService")
public class UserDetailsCustom implements UserDetailsService {
    private final UserRepository userRepository;

    public UserDetailsCustom(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new AppException("User not found"));
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(user.getRole())));
    }

}
