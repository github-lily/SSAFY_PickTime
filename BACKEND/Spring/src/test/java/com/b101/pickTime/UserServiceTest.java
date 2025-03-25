package com.b101.pickTime;

import com.b101.pickTime.api.user.request.PasswordCheckReq;
import com.b101.pickTime.api.user.request.PasswordUpdateReq;
import com.b101.pickTime.api.user.request.UserModiftReqDto;
import com.b101.pickTime.api.user.request.UserRegisterReq;
import com.b101.pickTime.api.user.response.UserInfoDto;
import com.b101.pickTime.api.user.service.impl.UserServiceImpl;
import com.b101.pickTime.common.exception.exception.DuplicateEmailException;
import com.b101.pickTime.common.exception.exception.PasswordNotChangedException;
import com.b101.pickTime.common.exception.exception.PasswordNotMatchedException;
import com.b101.pickTime.db.entity.User;
import com.b101.pickTime.db.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createUser_ShouldThrowDuplicateEmailException_WhenUsernameExists() {
        // given
        UserRegisterReq req = new UserRegisterReq();
        req.setUsername("test@example.com");
        when(userRepository.existsByUsername(req.getUsername())).thenReturn(true);

        // when
        Executable action = () -> userService.createUser(req);

        // then
        assertThrows(DuplicateEmailException.class, action);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_ShouldSaveUser_WhenUsernameDoesNotExist() {
        // given
        UserRegisterReq req = new UserRegisterReq();
        req.setUsername("test@example.com");
        req.setPassword("password");
        req.setName("Test User");
        when(userRepository.existsByUsername(req.getUsername())).thenReturn(false);
        when(passwordEncoder.encode(req.getPassword())).thenReturn("encodedPassword");

        // when
        userService.createUser(req);

        // then
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void getUser_ShouldReturnUserInfoDto_WhenUserExists() {
        // given
        int userId = 1;
        UserInfoDto expectedUserInfo = new UserInfoDto("test@example.com", "Test User", 1);
        when(userRepository.getUserByUserId(userId)).thenReturn(expectedUserInfo);

        // when
        UserInfoDto actualUserInfo = userService.getUser(userId);

        // then
        assertEquals(expectedUserInfo, actualUserInfo);
    }

    @Test
    void modifyUser_ShouldUpdateUserDetails() {
        // given
        int userId = 1;
        UserModiftReqDto req = new UserModiftReqDto();
        req.setName("Updated Name");

        User user = new User();
        user.setName("Old Name");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        // when
        UserInfoDto result = userService.modifyUser(userId, req);

        // then
        assertEquals("Updated Name", result.getName());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void unactivateUser_ShouldSetUserInactive() {
        // given
        int userId = 1;
        User user = new User();
        user.setIsActive(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // when
        userService.unactivateUser(userId);

        // then
        assertFalse(user.getIsActive());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void checkPassword_ShouldThrowException_WhenPasswordDoesNotMatch() {
        // given
        int userId = 1;
        PasswordCheckReq req = new PasswordCheckReq();
        req.setPassword("wrongPassword");

        User user = new User();
        user.setPassword("encodedPassword");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(req.getPassword(), user.getPassword())).thenReturn(false);

        // when
        Executable action = () -> userService.checkPassword(req, userId);

        // then
        assertThrows(PasswordNotMatchedException.class, action);
    }

    @Test
    void modifyPassword_ShouldThrowException_WhenPasswordIsSame() {
        // given
        int userId = 1;
        PasswordUpdateReq req = new PasswordUpdateReq();
        req.setPassword("samePassword");

        User user = new User();
        user.setPassword("encodedPassword");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(req.getPassword(), user.getPassword())).thenReturn(true);

        // when
        Executable action = () -> userService.modifyPassword(req, userId);

        // then
        assertThrows(PasswordNotChangedException.class, action);
    }
}
