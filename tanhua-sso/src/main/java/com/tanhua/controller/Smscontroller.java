package com.tanhua.controller;

import com.tanhua.service.SmsService;
import com.tanhua.pojo.vo.ErrorResult;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static java.lang.String.*;

@RestController
@RequestMapping("user")
@Slf4j
public class Smscontroller {

//slf4j 提供的日志记录器对象
    private static final Logger LOGGER = LoggerFactory.getLogger(Smscontroller.class);

    @Autowired
    private SmsService smsService;

    /**
     * 发送验证码
     */
    @PostMapping
    //RespinseEntity 变量自己点进去看，记得有父类
    public ResponseEntity<Object> sendCheckCode(@RequestBody Map<String,Object> param) {
        //当返回的code不属于3 and 1 时返回这个
        ErrorResult.ErrorResultBuilder resultBuilder = ErrorResult.builder().errCode("000000").errMessage("发送短信验证码失败");
        try {
            String phone = valueOf(param.get("phone"));
            Map<String,Object> sendCheckCode = this.smsService.sendCheckCode(phone);
            int code = ((Integer) sendCheckCode.get("code")).intValue();
            if(code == 3){
                //发送success
                return ResponseEntity.ok(null);
            }else if(code == 1){
                //不是failed，而是没有发送 验证码这个请求的原因，这个好绕好煞笔
                resultBuilder.errCode("000001").errMessage(sendCheckCode.get("msg").toString());
            }
        }catch(Exception e){
            LOGGER.error("发送短信验码失败",e);
        }

        //相当于返回500
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resultBuilder.build());
    }
}
