package com.pojo.vo;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
/*
环信 contacts VO
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "Contacts", description = "环信 contacts VO")
public class Contacts {
    private Long id;
    private String userId;
    private String avatar;
    private String nickname;
    private String gender;
    private Integer age;
    private String city;
}
