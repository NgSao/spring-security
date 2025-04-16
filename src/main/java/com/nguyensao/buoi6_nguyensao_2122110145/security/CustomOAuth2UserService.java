
package com.nguyensao.buoi6_nguyensao_2122110145.security;

import com.nguyensao.buoi6_nguyensao_2122110145.entity.User;
import com.nguyensao.buoi6_nguyensao_2122110145.entity.UserProvider;
import com.nguyensao.buoi6_nguyensao_2122110145.exception.AppException;
import com.nguyensao.buoi6_nguyensao_2122110145.repository.UserProviderRepository;
import com.nguyensao.buoi6_nguyensao_2122110145.repository.UserRepository;
import com.nguyensao.buoi6_nguyensao_2122110145.utils.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Collections;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final UserProviderRepository userProviderRepository;
    private final OidcUserService oidcUserService;
    private final JwtUtil jwtUtil;

    public CustomOAuth2UserService(UserRepository userRepository, UserProviderRepository userProviderRepository,
            JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.userProviderRepository = userProviderRepository;
        this.oidcUserService = new OidcUserService();
        this.jwtUtil = jwtUtil;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        System.out.println(">>> CustomOAuth2UserService loadUser (OAuth2) được gọi");
        OAuth2User oauth2User = super.loadUser(userRequest);
        return processUser(userRequest, oauth2User);
    }

    public OidcUser loadUser(OidcUserRequest userRequest) {
        System.out.println(">>> CustomOAuth2UserService loadUser (OIDC) được gọi");
        OidcUser oidcUser = oidcUserService.loadUser(userRequest);
        return (OidcUser) processUser(userRequest, oidcUser);
    }

    private OAuth2User processUser(OAuth2UserRequest userRequest, OAuth2User oauth2User) {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String providerId;
        String email;

        if ("google".equals(registrationId)) {
            providerId = oauth2User.getAttribute("sub");
            email = oauth2User.getAttribute("email");
            if (email == null)
                throw new AppException("Không thể lấy email từ Google");
        } else if ("facebook".equals(registrationId)) {
            providerId = oauth2User.getAttribute("id");
            email = oauth2User.getAttribute("email");
            if (email == null)
                throw new AppException("Không thể lấy email từ Facebook");
        } else {
            throw new AppException("Nhà cung cấp OAuth2 không được hỗ trợ: " + registrationId);
        }

        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                    .getRequest();
            String token = null;

            if (request.getCookies() != null) {
                for (var cookie : request.getCookies()) {
                    if (cookie.getName().equals("_tk")) {
                        token = cookie.getValue();
                        break;
                    }
                }
            }
            // System.out.println(">>> BATDAUNE ");

            if (token != null) {
                // Có token → lấy email hiện tại từ token
                String jwtEmail = jwtUtil.getSubjectFromToken(token);
                var existingUser = userRepository.findByEmail(jwtEmail);
                if (existingUser.isPresent()) {
                    User user = existingUser.get();
                    String provider = registrationId.toUpperCase();
                    boolean alreadyLinked = userProviderRepository.existsByProviderAndProviderUserId(provider,
                            providerId);

                    if (!alreadyLinked) {
                        UserProvider userProvider = UserProvider.builder()
                                .user(user)
                                .provider(provider)
                                .providerUserId(providerId)
                                .active(false)
                                .build();
                        userProviderRepository.save(userProvider);
                        // System.out.println(">>> Đã thêm liên kết nhà cung cấp " + provider);
                    } else {
                        // System.out.println(">>> Nhà cung cấp " + provider + " đã liên kết, không thêm
                        // lại.");

                    }

                    return buildOAuth2User(user, oauth2User);
                }
            }

            // Không có token hoặc không tìm thấy user → tạo user mới
            User user = userRepository.findByEmail(email)
                    .orElseGet(() -> {
                        System.out.println(">>> Người dùng chưa tồn tại, tạo mới: " + email);
                        User newUser = User.builder()
                                .email(email)
                                .password("")
                                .role("USER")
                                .provider(registrationId.toUpperCase())
                                .providerId(providerId)
                                .active(true)
                                .build();
                        User savedUser = userRepository.saveAndFlush(newUser);
                        UserProvider userProvider = UserProvider.builder()
                                .user(savedUser)
                                .provider(registrationId.toUpperCase())
                                .providerUserId(providerId)
                                .active(true)
                                .build();
                        userProviderRepository.save(userProvider);
                        return savedUser;
                    });

            return buildOAuth2User(user, oauth2User);

        } catch (Exception e) {
            // System.err.println(">>> Lỗi khi xử lý người dùng: " + e.getMessage());
            throw new AppException("Lỗi khi xử lý người dùng: " + e.getMessage());
        }
    }

    private OAuth2User buildOAuth2User(User user, OAuth2User oauth2User) {
        if (oauth2User instanceof OidcUser) {
            OidcUser oidcUser = (OidcUser) oauth2User;
            return new DefaultOidcUser(
                    Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getRole())),
                    oidcUser.getIdToken(),
                    oidcUser.getUserInfo(),
                    "email");
        } else {
            return new org.springframework.security.oauth2.core.user.DefaultOAuth2User(
                    Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getRole())),
                    oauth2User.getAttributes(),
                    "email");
        }
    }
}
