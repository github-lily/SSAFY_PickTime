package com.b101.pickTime.api.user.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoDto {

    private String username;
    private String name;
    private int level;

}
