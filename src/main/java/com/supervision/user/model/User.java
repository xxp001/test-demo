package com.supervision.user.model;

import lombok.Data;

@Data
public class User {
    private Integer id;

    private Integer roleId;

    private Integer departmentId;

    private String workCode;

    private String password;

    private String mobile;

    private Short userState;

    private String userRealname;
}