package com.tanhua.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

/*
day10 借助sso的upload来上传avatar
 */
public class UsersController {
    @Autowired
    private UserInfoController userInfoController;
    /**
     * 上传头像
     *
     * @param file
     * @param token
     * @return
     */
    @PostMapping("header")
    public ResponseEntity<Object> saveLogo(@RequestParam("headPhoto")
                                           MultipartFile file, @RequestHeader("Authorization") String token) {
        return this.userInfoController.saveLogo(file, token);
    }
}
