package com.nguyensao.buoi6_nguyensao_2122110145.controller;

import com.nguyensao.buoi6_nguyensao_2122110145.service.EmailService;
import com.nguyensao.buoi6_nguyensao_2122110145.service.TokenBlacklistService;
import com.nguyensao.buoi6_nguyensao_2122110145.service.UserService;
import com.nguyensao.buoi6_nguyensao_2122110145.utils.GenerateOTP;
import com.nguyensao.buoi6_nguyensao_2122110145.utils.JwtUtil;

import jakarta.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.nguyensao.buoi6_nguyensao_2122110145.dto.LoginResponse;
import com.nguyensao.buoi6_nguyensao_2122110145.dto.OAuth2LinkRequest;
import com.nguyensao.buoi6_nguyensao_2122110145.dto.OtpDto;
import com.nguyensao.buoi6_nguyensao_2122110145.dto.UserCreate;
import com.nguyensao.buoi6_nguyensao_2122110145.dto.UserDataResponse;
import com.nguyensao.buoi6_nguyensao_2122110145.dto.UserDto;
import com.nguyensao.buoi6_nguyensao_2122110145.dto.UserLogin;
import com.nguyensao.buoi6_nguyensao_2122110145.dto.UserRegister;
import com.nguyensao.buoi6_nguyensao_2122110145.dto.UserUpdated;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

        private final AuthenticationManagerBuilder authenticationManagerBuilder;
        private final UserService userService;
        private final EmailService emailService;
        private final JwtUtil jwtUtil;
        private final TokenBlacklistService tokenBlacklistService;

        public AuthController(AuthenticationManagerBuilder authenticationManagerBuilder, UserService userService,
                        EmailService emailService, JwtUtil jwtUtil, TokenBlacklistService tokenBlacklistService) {
                this.authenticationManagerBuilder = authenticationManagerBuilder;
                this.userService = userService;
                this.emailService = emailService;
                this.jwtUtil = jwtUtil;
                this.tokenBlacklistService = tokenBlacklistService;

        }

        @PostMapping("/login")
        public ResponseEntity<LoginResponse> login(@RequestBody UserLogin request) {
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                                request.getEmail(), request.getPassword());
                Authentication authentication = authenticationManagerBuilder.getObject()
                                .authenticate(authenticationToken);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                String access_token = jwtUtil.generateToken(request.getEmail());
                LoginResponse loginResponse = new LoginResponse();
                loginResponse.setAccessToken(access_token);
                loginResponse.setEmail(request.getEmail());
                ResponseCookie cookie = ResponseCookie
                                .from("_tk", access_token)
                                .httpOnly(true)
                                .secure(true)
                                .path("/")
                                .maxAge(3 * 60).build();
                return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString())
                                .body(loginResponse);

        }

        @GetMapping("/logout")
        public ResponseEntity<String> logout(@CookieValue(value = "_tk", defaultValue = "") String token) {
                if (token.isEmpty()) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                        .body("Vui lòng đăng nhập trước khi đăng xuất.");
                }

                tokenBlacklistService.blacklist(token);
                ResponseCookie cookie = ResponseCookie
                                .from("_tk", null)
                                .httpOnly(true)
                                .secure(true)
                                .path("/")
                                .maxAge(0)
                                .build();
                return ResponseEntity.ok()
                                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                                .body("Đăng xuất thành công. Token đã bị thu hồi.");
        }

        @PostMapping("/register")
        public ResponseEntity<String> register(@RequestBody @Valid UserRegister userRegister) {
                userService.register(userRegister);
                String verificationCode = GenerateOTP.generate();
                emailService.sendVerificationEmail(userRegister.getEmail(), verificationCode);
                ResponseCookie cookie = ResponseCookie
                                .from("_otp", verificationCode)
                                .httpOnly(true)
                                .secure(true)
                                .path("/")
                                .maxAge(3 * 60).build();
                return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString())
                                .body("Đăng kí tài khoản thành công.");
        }

        @PostMapping("/activate")
        public ResponseEntity<String> activateUser(@RequestBody OtpDto request,
                        @CookieValue(value = "_otp", defaultValue = "") String cookieCode) {
                userService.verifyOTP(request.getEmail(), request.getOtp(), cookieCode);
                ResponseCookie cookie = ResponseCookie
                                .from("_otp", null)
                                .httpOnly(true)
                                .secure(true)
                                .path("/")
                                .maxAge(0)
                                .build();
                return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString())
                                .body("Xác thực thành công! Tài khoản đã được kích hoạt.");
        }

        @GetMapping("/users")
        public ResponseEntity<List<UserDataResponse>> getAllUsers() {
                return ResponseEntity.ok().body(userService.getAllUsers());
        }

        @PostMapping("/users")
        public ResponseEntity<UserDto> createUser(UserCreate userCreate) {
                return ResponseEntity.ok().body(userService.createUser(userCreate));
        }

        @PostMapping("/users/update")
        public ResponseEntity<String> updateUser(@RequestBody UserUpdated userCreate) {
                userService.updateUser(userCreate);
                return ResponseEntity.ok().body("Cập nhật người dùng thành công!");
        }

        @GetMapping("/test")
        public ResponseEntity<String> Test() {
                return ResponseEntity.ok().body("Test user success!");
        }

        @GetMapping("/success")
        public ResponseEntity<String> oauth2Success() {
                return ResponseEntity.ok("Đăng nhập OAuth2 thành công!");
        }

        @GetMapping("/linked-already")
        public ResponseEntity<String> alreadyLinked() {
                return ResponseEntity.ok("Tài khoản đã được liên kết trước đó!");
        }

        @GetMapping("/linked-success")
        public ResponseEntity<String> linkedSuccess() {
                return ResponseEntity.ok("Liên kết tài khoản thành công!");
        }

        @PostMapping("/unlinked")
        public ResponseEntity<String> unLink(@RequestBody OAuth2LinkRequest request) {
                userService.unlinkOAuth2Account(request);
                return ResponseEntity.ok("Tài khoản đã được hủy liên kết!");
        }

}