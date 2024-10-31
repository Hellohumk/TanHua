package com.pojo;

import com.enums.SexEnum;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("用户详细信息")
public class UserInfo extends BasePojo {
    private Long id;
    private Long userId;
    private String logo;
    private String tags;
    private SexEnum sex;
    private Integer age;
    private String edu; //学历
    private String nickName;//nickname
    private String city; //城市
    private String birthday; //生日
    private String coverPic; // 封面图片
    private String industry; //行业
    private String income; //收入
    private String marriage; //婚姻状态

}
