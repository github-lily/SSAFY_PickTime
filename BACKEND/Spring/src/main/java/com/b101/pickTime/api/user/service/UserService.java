package com.b101.pickTime.api.user.service;

import com.b101.pickTime.api.user.request.UserRegisterReq;

public interface UserService {
    void createUser(UserRegisterReq userRegisterReq);
    boolean isExistUsername(String username);
}
