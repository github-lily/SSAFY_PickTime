package com.b101.pickTime.api.user.service.impl;


import com.b101.pickTime.api.user.request.PasswordCheckReq;
import com.b101.pickTime.api.user.request.UserRegisterReq;
import com.b101.pickTime.api.user.service.UserService;
import com.b101.pickTime.common.auth.CustomUserDetails;
import com.b101.pickTime.common.exception.exception.DuplicateEmailException;
import com.b101.pickTime.common.exception.exception.PasswordNotMatchedException;
import com.b101.pickTime.db.entity.User;
import com.b101.pickTime.db.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    // 실구현체(BCryptPasswordEncoder)가 아닌 인터페이스(PasswordEncoder)를 주입해야 함
    private final PasswordEncoder passwordEncoder;

    public void createUser(UserRegisterReq userRegisterReq) {
        String username = userRegisterReq.getUsername();
        // 이미 존재하는 이메일인지 확인
        if(isExistUsername(username)) {
            throw new DuplicateEmailException();
        }
        
        User user = User.builder()
                .username(userRegisterReq.getUsername())
                .password(passwordEncoder.encode(userRegisterReq.getPassword()))
                .name(userRegisterReq.getName())
//                .level(1)
//                .role(Role.ROLE_USER)
//                .isActive(true)
                .build();

        userRepository.save(user);
    }
    public void checkPassword(PasswordCheckReq passwordCheckReq, CustomUserDetails customUserDetails) {
        // DB에서 조회하여 비교 <= customerUserDetails에서 바로 비교 X
        User user = userRepository.findById(customUserDetails.getUserId()).orElseThrow();
        if (!passwordEncoder.matches(passwordCheckReq.getPassword(), user.getPassword())) {
            throw new PasswordNotMatchedException("password is not matched");
        }
    }
    public boolean isExistUsername(String username) {
        return userRepository.existsByUsername(username);
    }
}
