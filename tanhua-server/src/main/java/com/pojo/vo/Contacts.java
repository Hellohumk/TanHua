package com.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
/*
环信 contacts VO
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Contacts {
    private Long id;
    private String userId;
    private String avatar;
    private String nickname;
    private String gender;
    private Integer age;
    private String city;
}
