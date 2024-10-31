package com.pojo.vo;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "VisitorsVo", description = "访客信息")
public class VisitorsVo {

    private Long id;
    private String avatar;
    private String nickname;
    private String gender;
    private Integer age;
    private String[] tags;
    private Integer fateValue;
}
