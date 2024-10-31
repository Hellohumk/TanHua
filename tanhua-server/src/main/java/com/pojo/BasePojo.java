package com.pojo;


import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * pojo 参考即，所有pojo都必须有的东西
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "BasePojo", description = "所有pojo的父类")
public abstract class BasePojo {
    @TableField(fill = FieldFill.INSERT)//insert时自动填充,相当于AOP
    private Date created;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updated;



}
