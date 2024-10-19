package com.tanhua.controller;


import com.alibaba.druid.util.StringUtils;
import com.tanhua.pojo.User;
import com.tanhua.pojo.vo.ErrorResult;
import com.tanhua.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("loginVerification")
    public ResponseEntity<Object> login(@RequestBody Map<String,Object> param){
        try{
            String mobile = param.get("phone").toString();
            String code = param.get("verificationCode").toString();
            String token = this.userService.login(mobile,code);

            Map<String,Object> result = new HashMap<>();

            if(! StringUtils.isEmpty(token)){
                //token不为空,则直接放行
                String[] ss = org.apache.commons.lang3.StringUtils.split(token,'|');
                String isNew = ss[0];
                String tokenStr = ss[1];//真token

                result.put("isNew",isNew);
                result.put("token",tokenStr);
                return ResponseEntity.ok(result);
            }
        }catch (Exception e){
            //TODO 这里捕获的是哪里的error，记得分析.不知道
            e.printStackTrace();
        }

        // 登录失败，验证码错误
        ErrorResult errorResult =
                ErrorResult.builder().errCode("000000").errMessage("验证码错误").build();
        return
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResult);
    }

    /**
     * 根据token查询用户数据
     *
     * @param token
     * @return
     */
    @GetMapping("{token}")
    public User queryUserByToken(@PathVariable("token") String token) {
        return this.userService.queryUserByToken(token);
    }

}
