package com.tanhua.controller;

import com.tanhua.service.HuanXinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
/*
处理环信相关业务-sso
 */


@RestController
@RequestMapping("user/huanxin")
public class HuanXinController {

    @Autowired
    private HuanXinService huanXinService;
    /**
     * 添加联系人
     *
     * @param userId
     * @param friendId
     * @return
     */
    @PostMapping("contacts/{owner_username}/{friend_username}")
    public ResponseEntity<Void>
    contactUsers(@PathVariable("owner_username") Long userId,
                 @PathVariable("friend_username") Long friendId) {
        try {
            boolean result = this.huanXinService.contactUsers(userId,
                    friendId);
            if (result) {
                return ResponseEntity.ok(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }


    //day09 回答问题
    /**
     * 发送系统消息
     *
     * @param target
     * @param msg
     * @param type
     * @return
     */
    @PostMapping("messages")
    public ResponseEntity<Void> sendMsg(@RequestParam("target") String
                                                target,
                                        @RequestParam("msg") String msg,
                                        @RequestParam(value = "type",
                                                defaultValue = "txt") String type) {
        try {
            boolean result = this.huanXinService.sendMsg(target, type,
                    msg);
            if (result) {
                return ResponseEntity.ok(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}
