package com.pojo;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("问题")
public class Question extends BasePojo{

    private Long id;
    private Long userId;
    //问题内容
    private String txt;
}
