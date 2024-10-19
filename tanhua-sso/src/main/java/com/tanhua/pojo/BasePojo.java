package com.tanhua.pojo;


import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;

import java.util.Date;

/**
 * pojo 参考即，所有pojo都必须有的东西
 */

public abstract class BasePojo {
    @TableField(fill = FieldFill.INSERT)//insert时自动填充,相当于AOP
    private Date created;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updated;



}
