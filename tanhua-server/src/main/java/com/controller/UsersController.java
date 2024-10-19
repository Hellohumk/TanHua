package com.controller;

import com.pojo.vo.UserInfoVo;
import com.service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("users")
public class UsersController {

    @Autowired
    private UsersService usersService;

    /**
     * 用户资料 - 读取
     *
     * @param userID
     * @param huanxinID
     * @return
     */
    @GetMapping
    public ResponseEntity<UserInfoVo> queryUserInfo(@RequestParam(value =
            "userID", required = false) String userID,
                                                    @RequestParam(value =
                                                            "huanxinID", required = false) String huanxinID) {
        try {
            UserInfoVo userInfoVo = usersService.queryUserInfo(userID, huanxinID);
            if (userInfoVo != null) {
                return ResponseEntity.ok(userInfoVo);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

    }

    /**
     * 更新用户信息 form
     * @param userInfoVo
     * @return
     */
    @PutMapping
    public ResponseEntity<Void> updateUserInfo(@RequestBody UserInfoVo userInfoVo){

        try {
                Boolean bool = usersService.updateUserInfo(userInfoVo);
            if (! bool) {
                return ResponseEntity.ok(null);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

    }
}
