package com.tanhua.controller;

import com.tanhua.pojo.vo.ErrorResult;
import com.tanhua.service.UserInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("user")
@Slf4j
public class UserInfoController {
    @Autowired
    private UserInfoService userInfoService;
    /**
     * 完善个人信息
     *
     * @param param
     * @param token
     * @return
     */
    @RequestMapping("loginReginfo")
    @PostMapping
    public ResponseEntity<Object> saveUserInfo(@RequestBody Map<String,
                String> param, @RequestHeader("Authorization") String token) {
        try {
            Boolean saveUserInfo = this.userInfoService.saveUserInfo(param,
                    token);
            if (saveUserInfo) {
                return ResponseEntity.ok(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        ErrorResult errorResult = ErrorResult.builder()
                .errCode("000000")
                .errMessage("用户信息保存失败!")
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResult);
    }

    /**
     * 上传头像
     *
     * @param file
     * @param token
     * @return
     */
    @RequestMapping("loginReginfo/head")
    @PostMapping
    public ResponseEntity<Object> saveLogo(@RequestParam("headPhoto")
                                           MultipartFile file, @RequestHeader("Authorization") String token) {
        try {
            Boolean bool = this.userInfoService.saveLogo(file, token);
            if(bool){
                return ResponseEntity.ok(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        ErrorResult errorResult =
                ErrorResult.builder().errCode("000000").errMessage("图片非人像，请重新上传!").build();
        return
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResult);
    }



}
