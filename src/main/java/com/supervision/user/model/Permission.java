package com.supervision.user.model;

import lombok.Data;

@Data
public class Permission {
    private Integer id;

    private String permission;

    private String permissionDesc;
}