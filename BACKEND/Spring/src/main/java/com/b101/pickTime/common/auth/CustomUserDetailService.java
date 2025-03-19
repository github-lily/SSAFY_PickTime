package com.b101.pickTime.common.auth;

import com.b101.pickTime.db.entity.User;
import com.b101.pickTime.db.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {
    private final UserRepository userRepository;
    
    // repository에서 유저 정보 가져옴
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User userData = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));;

        return new CustomUserDetails(userData.getUserId(), userData.getUsername(), userData.getPassword(), String.valueOf(userData.getRole()));
    }
}
