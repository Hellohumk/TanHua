package com.controller;

import com.pojo.vo.UserInfoVo;
import com.service.UsersService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("users")
@Api(value = "用户资料模块", tags = "用户资料相关接口")
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
    @ApiOperation(value = "查询用户信息", notes = "根据用户ID和换信ID查询用户信息")
    @ApiResponses({
            @ApiResponse(code = 200, message = "查询成功", response = UserInfoVo.class),
            @ApiResponse(code = 500, message = "内部服务器错误")
    })
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
    @ApiOperation(value = "更新用户信息", notes = "通过表单更新用户信息")
    @ApiResponses({
            @ApiResponse(code = 200, message = "更新成功", response = Void.class),
            @ApiResponse(code = 500, message = "内部服务器错误")
    })
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
