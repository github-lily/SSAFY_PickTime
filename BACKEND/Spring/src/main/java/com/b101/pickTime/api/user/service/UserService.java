package com.b101.pickTime.api.user.service;

import com.b101.pickTime.api.user.request.UserModiftReqDto;
import com.b101.pickTime.api.user.request.PasswordCheckReq;
import com.b101.pickTime.api.user.request.PasswordUpdateReq;
import com.b101.pickTime.api.user.request.UserRegisterReq;
import com.b101.pickTime.api.user.response.UserInfoDto;
import com.b101.pickTime.common.auth.CustomUserDetails;

public interface UserService {
    void createUser(UserRegisterReq userRegisterReq);
    boolean isExistUsername(String username);
    void checkPassword(PasswordCheckReq passwordCheckReq, CustomUserDetails customUserDetails);
    void modifyPassword(PasswordUpdateReq passwordUpdateReq, CustomUserDetails customUserDetails);

    UserInfoDto getUser(int userId);
    UserInfoDto modifyUser(int userId, UserModiftReqDto userModiftReqDto);
    void unactivateUser(int userId);
}
