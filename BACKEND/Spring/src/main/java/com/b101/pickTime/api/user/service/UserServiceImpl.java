package com.b101.pickTime.api.user.service;


import com.b101.pickTime.api.user.request.UserRegisterReq;
import com.b101.pickTime.common.exception.exception.DuplicateEmailException;
import com.b101.pickTime.db.entity.User;
import com.b101.pickTime.db.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{
    private final UserRepository userRepository;
    // 실구현체(BCryptPasswordEncoder)가 아닌 인터페이스(PasswordEncoder)를 주입해야 함
    private final PasswordEncoder passwordEncoder;

    public void createUser(UserRegisterReq userRegisterReq) {
        String username = userRegisterReq.getUsername();
        // 이미 존재하는 이메일인지 확인
        if(userRepository.existsByUsername(username)) {
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
}
