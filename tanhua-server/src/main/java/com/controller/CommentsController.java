package com.controller;

import com.pojo.vo.PageResult;
import com.service.CommentsService;
import com.service.MovementsService;
import com.service.QuanZiMQService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/*
加入day08 sendMsg mq 20241012
 */

@RestController
@RequestMapping("comments")
@Api(value="评论模块")
public class CommentsController {
    @Autowired
    private CommentsService commentsService;

    @Autowired
    private MovementsService movementsService;

    @Autowired
    private QuanZiMQService quanZiMQService;


    /**
     * 分页查询评论列表
     * @param publishId
     * @param page
     * @param pagesize
     * @return
     */
    @GetMapping()
    @ApiOperation(value = "分页查询评论列表", notes = "根据动态ID分页查询评论")
    @ApiResponses({
            @ApiResponse(code = 200, message = "查询成功", response = PageResult.class),
            @ApiResponse(code = 500, message = "内部服务器错误")
    })
    public ResponseEntity<PageResult> queryCommentsList(@RequestParam("movementId") String publishId,
                                                        @RequestParam(value = "page", defaultValue = "1") Integer page,
                                                        @RequestParam(value = "pagesize", defaultValue = "10") Integer pagesize){
        try {
            PageResult pageResult =
                    this.commentsService.queryCommentsList(publishId, page, pagesize);
            return ResponseEntity.ok(pageResult);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();


    }
    /**
    * 保存评论
    */
    @PostMapping
    @ApiOperation(value = "保存评论", notes = "保存用户对动态的评论")
    @ApiResponses({
            @ApiResponse(code = 200, message = "保存成功"),
            @ApiResponse(code = 500, message = "内部服务器错误")
    })
    public ResponseEntity<Void> saveComments(@RequestBody
                                             Map<String,String> param) {
        try {
            String publishId = param.get("movementId");
            String content = param.get("comment");
            Boolean result = this.commentsService.saveComments(publishId,
                    content);
            if (result) {

                //发送消息
                this.quanZiMQService.sendCommentPublishMsg(publishId);

                return ResponseEntity.ok(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

//TODO day04 点赞好像有问题和cancel，评论点赞咋搞的 包有问题啊
    //有一个解释是：20241006
    //这里传进来的是commentId ，点赞数通过redis记录，前缀和publish——comment——like相同，但是后面id不同（mongo id全局唯一性）

    //评论点赞：

    /**
     * 点赞
     * @param publishId
     * @return
     */
    @GetMapping("/{id}/like")
    @ApiOperation(value = "点赞评论", notes = "用户对评论的点赞")
    @ApiResponses({
            @ApiResponse(code = 200, message = "点赞成功", response = Long.class),
            @ApiResponse(code = 500, message = "内部服务器错误")
    })
    public ResponseEntity<Long> likeComment(@PathVariable("id") String publishId){
        try{
            Long likeCount = movementsService.likeComment(publishId);
            if (null != likeCount) {
                return ResponseEntity.ok(likeCount);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * 取消点赞
     *
     * @param publishId
     * @return
     */
    @GetMapping("/{id}/dislike")
    @ApiOperation(value = "取消点赞评论", notes = "用户取消对评论的点赞")
    @ApiResponses({
            @ApiResponse(code = 200, message = "取消点赞成功", response = Long.class),
            @ApiResponse(code = 500, message = "内部服务器错误")
    })
    public ResponseEntity<Long> disLikeComment(@PathVariable("id") String
                                                       publishId) {
        try {
            Long likeCount =
                    this.movementsService.cancelLikeComment(publishId);
            if (null != likeCount) {
                return ResponseEntity.ok(likeCount);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }


}









