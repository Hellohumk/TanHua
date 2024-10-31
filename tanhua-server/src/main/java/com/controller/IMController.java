package com.controller;

import com.annotation.NoAuthorization;
import com.pojo.vo.PageResult;
import com.service.IMService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("messages")
@Api(value = "IM消息中心", tags = "IM消息中心")
public class IMController {

    private static final Logger LOGGER = LoggerFactory.getLogger(IMController.class);

    @Autowired
    private IMService imService;

    @PostMapping("contacts")
    @ApiOperation(value = "添加联系人", notes = "添加联系人")
    public ResponseEntity<Void> contactUser(@RequestBody Map<String,
                Object> param) {
        try {
            Long userId = Long.valueOf(param.get("userId").toString());
            boolean result = this.imService.contactUser(userId);
            if (result) {
                return ResponseEntity.ok(null);
            }
        } catch (Exception e) {
            LOGGER.error("添加联系人失败! param = " + param, e);
        }
        return
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * 查询联系人列表
     *
     * @param page
     * @param pageSize
     * @param keyword
     * @return
     */
    @GetMapping("contacts")
    @ApiOperation(value = "查询联系人列表", notes = "查询联系人列表")
    public ResponseEntity<PageResult> queryContactsList(@RequestParam(value
            = "page", defaultValue = "1") Integer page,
                                                        @RequestParam(value = "pagesize", defaultValue = "10") Integer pageSize,
                                                        @RequestParam(value = "keyword", required = false) String keyword) {
        PageResult pageResult = this.imService.queryContactsList(page,
                pageSize, keyword);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 查询点赞列表
     *
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("likes")
    @ApiOperation(value = "查询点赞列表", notes = "查询点赞列表")
    public ResponseEntity<PageResult>
    queryMessageLikeList(@RequestParam(value = "page", defaultValue = "1")
                         Integer page,
                         @RequestParam(value = "pagesize", defaultValue = "10") Integer pageSize) {
        PageResult pageResult = this.imService.queryMessageLikeList(page,
                pageSize);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 查询喜欢列表
     *
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("loves")
    @ApiOperation(value = "查询喜欢列表", notes = "查询喜欢列表")
    public ResponseEntity<PageResult>
    queryMessageLoveList(@RequestParam(value = "page", defaultValue = "1")
                         Integer page,
                         @RequestParam(value = "pagesize", defaultValue = "10") Integer pageSize) {
        PageResult pageResult = this.imService.queryMessageLoveList(page,
                pageSize);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 查询评论列表
     *
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("comments")
    @ApiOperation(value = "查询评论列表", notes = "查询评论列表")
    public ResponseEntity<PageResult>
    queryMessageCommentList(@RequestParam(value = "page", defaultValue = "1")
                         Integer page,
                         @RequestParam(value = "pagesize", defaultValue = "10") Integer pageSize) {
        PageResult pageResult = this.imService.queryMessageCommentList(page,
                pageSize);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 查询公告列表
     *
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("announcements")
    @NoAuthorization //优化，无需进行token校验
    @ApiOperation(value = "查询公告列表", notes = "查询公告列表")
    public ResponseEntity<PageResult>
    queryMessageAnnouncementList(@RequestParam(value = "page", defaultValue =
            "1") Integer page,
                                 @RequestParam(value = "pagesize", defaultValue = "10") Integer pageSize) {
        PageResult pageResult =
                this.imService.queryMessageAnnouncementList(page, pageSize);
        return ResponseEntity.ok(pageResult);
    }


}
