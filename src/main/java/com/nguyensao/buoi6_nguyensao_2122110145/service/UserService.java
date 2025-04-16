package com.nguyensao.buoi6_nguyensao_2122110145.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.nguyensao.buoi6_nguyensao_2122110145.dto.OAuth2LinkRequest;
import com.nguyensao.buoi6_nguyensao_2122110145.dto.UserCreate;
import com.nguyensao.buoi6_nguyensao_2122110145.dto.UserDataResponse;
import com.nguyensao.buoi6_nguyensao_2122110145.dto.UserDto;
import com.nguyensao.buoi6_nguyensao_2122110145.dto.UserProviderDto;
import com.nguyensao.buoi6_nguyensao_2122110145.dto.UserRegister;
import com.nguyensao.buoi6_nguyensao_2122110145.dto.UserUpdated;
import com.nguyensao.buoi6_nguyensao_2122110145.entity.User;
import com.nguyensao.buoi6_nguyensao_2122110145.entity.UserProvider;
import com.nguyensao.buoi6_nguyensao_2122110145.exception.AppException;
import com.nguyensao.buoi6_nguyensao_2122110145.repository.UserProviderRepository;
import com.nguyensao.buoi6_nguyensao_2122110145.repository.UserRepository;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserProviderRepository userProviderRepository;

    public UserService(UserRepository userRepository, UserProviderRepository userProviderRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userProviderRepository = userProviderRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void register(UserRegister userDto) {
        if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            throw new AppException("Email không tồn tại");
        }
        User user = User.builder()
                .email(userDto.getEmail())
                .password(passwordEncoder.encode(userDto.getPassword()))
                .role(userDto.getRole().toUpperCase())
                .provider("LOCAL")
                .providerId(null)
                .active(true)
                .build();
        userRepository.save(user);
    }

    public List<UserDataResponse> getAllUsers() {
        List<User> users = userRepository.findAll();

        return users.stream()
                .map(user -> UserDataResponse.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .role(user.getRole())
                        .active(user.getActive())
                        .providerDtos(
                                user.getProviders().stream()
                                        .map(provider -> UserProviderDto.builder()
                                                .id(provider.getId())
                                                .provider(provider.getProvider())
                                                .providerUserId(provider.getProviderUserId())
                                                .build())
                                        .collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());
    }

    public UserDto createUser(UserCreate userCreate) {
        if (userRepository.findByEmail(userCreate.getEmail()).isPresent()) {
            throw new AppException("Email đã tồn tại");
        }
        User user = User.builder()
                .email(userCreate.getEmail())
                .password(passwordEncoder.encode(userCreate.getPassword()))
                .role(userCreate.getRole().toUpperCase())
                .active(userCreate.getActive())
                .build();
        userRepository.save(user);
        return new UserDto(user.getId(), user.getEmail(), user.getRole(), user.getActive());

    }

    public void updateUser(UserUpdated dto) {
        User user = userRepository.findById(dto.getId())
                .orElseThrow(() -> new AppException("Không tìm thấy người dùng với id: " + dto.getId()));
        if (!user.getEmail().equals(dto.getEmail())
                && userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new AppException("Email đã tồn tại");
        }
        user.setEmail(dto.getEmail());
        user.setRole(dto.getRole().toUpperCase());
        user.setActive(dto.getActive());
        userRepository.save(user);
    }

    private Map<String, Integer> otpAttempts = new HashMap<>();

    public void verifyOTP(String email, String code, String cookieCode) {
        int attempts = otpAttempts.getOrDefault(email, 0);
        if (cookieCode.isEmpty()) {
            throw new AppException("Không tim thấy mã OTP.");
        }
        if (cookieCode.equals(code)) {
            User user = userRepository.findByEmail(email).orElseThrow(() -> new AppException("User not found"));
            user.setActive(true);
            userRepository.save(user);
        } else {
            otpAttempts.put(email, attempts + 1);
            if (attempts >= 3) {
                throw new AppException("Số lần nhập quá nhiều. Vui lòng kích hoạt lại tài khoản.");
            }
            throw new AppException("Mã OTP không đúng. Vui lòng nhập lại");
        }
    }

    public void checkAndActivateUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException("Không tìm thấy người dùng với email: " + email));

        if (Boolean.FALSE.equals(user.getActive())) {
            throw new AppException("Tài khoản của bạn chưa được kích hoạt.");
        }

    }

    public void unlinkOAuth2Account(OAuth2LinkRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException("Không tìm thấy người dùng"));

        UserProvider provider = userProviderRepository.findById(request.getId())
                .orElseThrow(() -> new AppException("Không tìm thấy liên kết OAuth2"));

        // Optional: check xem provider này có thuộc về user đó không
        if (!provider.getUser().getId().equals(user.getId())) {
            throw new AppException("Liên kết không thuộc người dùng này");
        }
        userProviderRepository.delete(provider);
    }

}
