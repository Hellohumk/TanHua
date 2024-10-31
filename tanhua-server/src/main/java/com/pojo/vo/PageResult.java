package com.pojo.vo;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "分页结果")
public class PageResult {
    private Integer counts;//总记录数
    private Integer pagesize;//页大小
    private Integer pages;//总页数
    private Integer page;//当前页码
    private List<?> items; //列表
}
