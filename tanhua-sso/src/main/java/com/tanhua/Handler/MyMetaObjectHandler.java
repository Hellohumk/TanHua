package com.tanhua.Handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Date;

/*
MP 自动填充
 */

@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void updateFill(MetaObject metaObject) {
       setFieldValByName("updated",new Date(),metaObject);

    }

    // metaObject是一个用于表示对象元数据的类。即动态的获得对应的对象
    //这里你传进来什么对象，他就能拿到对应对象（表）的属性，然后定向修改。
    @Override
    public void insertFill(MetaObject metaObject) {
        Object created = getFieldValByName("created",metaObject);
        if(created == null){
            //empty,can be fill
            setFieldValByName("created",new Date(),metaObject);
        }

        Object updated = getFieldValByName("updated",metaObject);
        if(updated == null){
            setFieldValByName("updated",new Date(),metaObject);
        }
    }

}
