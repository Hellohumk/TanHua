package com.pojo.vo;

import io.swagger.annotations.ApiModel;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@ApiModel(value = "错误结果")
public class ErrorResult {

    private String errCode;
    private String errMessage;
}
