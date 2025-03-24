package com.b101.pickTime.api.user.service;

import com.b101.pickTime.api.user.request.UserModiftReqDto;
import com.b101.pickTime.api.user.request.UserRegisterReq;
import com.b101.pickTime.api.user.response.UserInfoDto;

public interface UserService {
    public void createUser(UserRegisterReq userRegisterReq);
    public UserInfoDto getUser(int userId);
    public UserInfoDto modifyUser(int userId, UserModiftReqDto userModiftReqDto);

    public void unactivateUser(int userId);
}
