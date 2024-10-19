package com.controller;


import com.pojo.User;
import com.pojo.vo.HuanXinUser;
import com.utils.UserThreadLocal;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("huanxin")
public class HuanXinController {


    /**
     * 查询当前环信用户信息
     * @return
     */
    @GetMapping("user")
    public ResponseEntity<HuanXinUser> queryHuanXinUser(){
        User user = UserThreadLocal.get();
        HuanXinUser huanXinUser = new HuanXinUser();
        huanXinUser.setUsername(user.getId().toString());
        huanXinUser.setPassword(DigestUtils.md5Hex(user.getId() +
                "tanhua"));
        return ResponseEntity.ok(huanXinUser);
    }
}
