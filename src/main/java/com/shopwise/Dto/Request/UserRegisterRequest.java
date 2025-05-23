package com.shopwise.Dto.Request;

import lombok.Data;

@Data
public class UserRegisterRequest {
    private String name;
    private String email;
    private String password;
    private String confirmPassword;
    private String phone;
}
