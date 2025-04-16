package com.nguyensao.buoi6_nguyensao_2122110145.security;

import com.nguyensao.buoi6_nguyensao_2122110145.entity.UserProvider;
import com.nguyensao.buoi6_nguyensao_2122110145.repository.UserProviderRepository;
import com.nguyensao.buoi6_nguyensao_2122110145.repository.UserRepository;
import com.nguyensao.buoi6_nguyensao_2122110145.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final UserProviderRepository userProviderRepository;

    public OAuth2AuthenticationSuccessHandler(JwtUtil jwtUtil, UserRepository userRepository,
            UserProviderRepository userProviderRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.userProviderRepository = userProviderRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException {
        System.out.println(">>> OAuth2AuthenticationSuccessHandler được gọi");
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        String email = oauth2User.getAttribute("email");
        if (email == null) {
            System.err.println(">>> Lỗi: Không thể lấy email từ OAuth2 user");
            throw new IllegalStateException("Không thể lấy email từ OAuth2 user");
        }
        // Kiểm tra token trong cookie
        String token = null;
        if (request.getCookies() != null) {
            for (var cookie : request.getCookies()) {
                if (cookie.getName().equals("_tk")) {
                    token = cookie.getValue();
                    break;
                }
            }
        }
        // Nếu có token → kiểm tra liên kết
        if (token != null) {
            String jwtEmail = jwtUtil.getSubjectFromToken(token);
            // Kiểm tra xem liên kết đã tồn tại chưa
            var userOpt = userRepository.findByEmailWithProviders(jwtEmail);
            if (userOpt.isPresent()) {
                var user = userOpt.get();
                String provider = oauth2User.getAttribute("iss") != null ? "GOOGLE" : "FACEBOOK";
                boolean alreadyLinked = false;
                for (UserProvider up : user.getProviders()) {
                    if (up.getProvider().equals(provider)) {
                        if (!up.isActive()) {
                            up.setActive(true);
                            userProviderRepository.save(up);
                            alreadyLinked = false;
                            System.out.println(">>> Alosaone1 " + alreadyLinked);
                            break;
                        }
                        alreadyLinked = true;
                        System.out.println(">>> Alosaone2 " + alreadyLinked);
                    } else {
                        alreadyLinked = true;
                        break;
                    }
                    System.out.println(">>> Alosaone3 " + alreadyLinked);

                }
                System.out.println(">>> Alosaone " + alreadyLinked);

                if (alreadyLinked) {
                    System.out.println(">>> Tài khoản đã được liên kết với " + provider);
                    response.sendRedirect("http://localhost:8080/api/v1/auth/linked-already");
                    return;
                } else {
                    System.out.println(">>> Liên kết thành công với " + provider);
                    response.sendRedirect("http://localhost:8080/api/v1/auth/linked-success");
                    return;
                }
            }
        }

        System.out.println(">>> Tạo token cho email: " + email);
        try {
            String accessToken = jwtUtil.generateToken(email);
            System.out.println(">>> Token được tạo: " + accessToken);
            ResponseCookie cookie = ResponseCookie
                    .from("_tk", accessToken)
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .maxAge(3 * 60)
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
            response.sendRedirect("/api/v1/auth/success");
        } catch (Exception e) {
            System.err.println(">>> Lỗi khi tạo token: " + e.getMessage());
            throw new IllegalStateException("Lỗi khi tạo token: " + e.getMessage());
        }
    }

}