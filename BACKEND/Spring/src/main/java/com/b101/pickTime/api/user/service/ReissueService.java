package com.b101.pickTime.api.user.service;

import jakarta.servlet.http.HttpServletRequest;

public interface ReissueService {
    // refresh 확인하여 access토큰 재발급
    public String reissue(HttpServletRequest request);
}
