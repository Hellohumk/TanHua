package com.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("用户")
public class User extends BasePojo {

    private Long id;
    private String mobile;//mobile

    @JsonIgnore //json序列化时忽略
    private String password;
}
