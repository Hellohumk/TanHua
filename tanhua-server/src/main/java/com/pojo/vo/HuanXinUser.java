package com.pojo.vo;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "HuanXinUser", description = "环信用户")
public class HuanXinUser {
    private String username;
    private String password;
}
