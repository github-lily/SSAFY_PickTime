package com.b101.pickTime.api.user.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserRegisterReq {
    @NotBlank(message = "Email must not be blank.")
    @Email(message = "Invalid email format.")
    private String username;

    @NotBlank(message = "Password must not be blank.")
    @Size(max = 30, message = "Password must be 30 characters or less.")
    private String password;

    @NotBlank(message = "Name must not be blank.")
    private String name;
}
