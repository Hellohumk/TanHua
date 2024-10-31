package com.controller;


import com.pojo.dto.RecommendUserQueryParam;
import com.pojo.vo.NearUserVo;
import com.pojo.vo.PageResult;
import com.pojo.vo.TodayBest;
import com.service.TodayBestService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("todayBest/")
@Api(value = "今日佳人模块", tags = "今日佳人相关接口")
public class TodayBestController {

    @Autowired
    private TodayBestService todayBestService;
    /**
     * 查询今日佳人
     *
     * @return Todaybest 封装后的结果
     */
    @GetMapping
    @ApiOperation(value = "查询今日佳人", notes = "查询今日佳人的信息")
    @ApiResponses({
            @ApiResponse(code = 200, message = "查询成功", response = TodayBest.class),
            @ApiResponse(code = 500, message = "内部服务器错误")
    })
    public TodayBest queryTodayBest(@RequestHeader("Authorization") String
                                            token){
        return todayBestService.queryTodayBest(token);
    }

    //day09 查询今日佳人详情
    //TODO 怎么看这个都和上面他妈的相同，返回都是一个东西，怎么设计的？
    /**
     * 查询今日佳人
     *包和上面重复的，只能按文档来
     *
     * @param userId  今日佳人的id
     * @return
     */
    @GetMapping("{id}/personalInfo")
    @ApiOperation(value = "查询今日佳人详情", notes = "根据用户ID查询今日佳人的详细信息")
    @ApiResponses({
            @ApiResponse(code = 200, message = "查询成功", response = TodayBest.class),
            @ApiResponse(code = 500, message = "内部服务器错误")
    })
    public ResponseEntity<TodayBest> queryTodayBest(@PathVariable("id")
                                                    Long userId) {
        try {
            TodayBest todayBest =
                    this.todayBestService.queryTodayBest(userId);
            return ResponseEntity.ok(todayBest);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * 推荐朋友列表
     * @param queryParam
     * @param token
     * @return
     */
    @GetMapping("recommendation")
    @ApiOperation(value = "推荐朋友列表", notes = "根据查询参数和令牌获取推荐用户列表")
    public PageResult queryRecommendUserList(RecommendUserQueryParam
                                                     queryParam, @RequestHeader("Authorization") String token) {
        return todayBestService.queryRecommendUserList(queryParam,
                token);
    }


    @GetMapping("strangerQuestions")
    @ApiOperation(value = "查询问题", notes = "根据用户ID查询问题内容")
    public ResponseEntity<String> queryQuestion(@RequestParam("userId")
                                                Long userId) {
        try {
            //这里是拿到question类的txt
            String question = this.todayBestService.queryQuestion(userId);
            return ResponseEntity.ok(question);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    //回答别人问题
    @PostMapping("strangerQuestions")
    @ApiOperation(value = "回答问题", notes = "根据用户ID和回答内容回答问题")
    public ResponseEntity<Void> replyQuestion(@RequestBody Map<String,
                Object> param) {
        try {
            Long userId = Long.valueOf(param.get("userId").toString());//谁的问题
            String reply = param.get("reply").toString();//回答的内容
            Boolean result = this.todayBestService.replyQuestion(userId,
                    reply);
            if (result) {
                return ResponseEntity.ok(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }


    //day10 geo
    /**
     * 搜附近
     *
     * @param gender
     * @param distance
     * @return
     */
    @GetMapping("search")
    @ApiOperation(value = "搜附近", notes = "根据性别和距离查询附近的人")
    public ResponseEntity<List<NearUserVo>>
    queryNearUser(@RequestParam(value = "gender", required = false) String
                          gender,
                  @RequestParam(value = "distance", defaultValue = "2000") String distance)
    {
        try {
            List<NearUserVo> list =
                    this.todayBestService.queryNearUser(gender, distance);
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }


    //tanhua day10
    @GetMapping("cards")
    @ApiOperation(value = "查询卡片列表", notes = "查询卡片列表")
    @ApiResponses({
            @ApiResponse(code = 200, message = "查询成功", response = TodayBest.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "内部服务器错误")
    })
    public ResponseEntity<List<TodayBest>> queryCardsList() {
        try{
            List<TodayBest> list = todayBestService.queryCardList();
            if(list != null){
                return ResponseEntity.ok(list);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    //用户喜欢
    /**
     * 喜欢
     *
     * @param likeUserId
     * @return
     */
    @GetMapping("{id}/love")
    @ApiOperation(value = "喜欢用户", notes = "用户表示喜欢某个用户")
    @ApiResponses({
            @ApiResponse(code = 200, message = "喜欢成功", response = Void.class),
            @ApiResponse(code = 500, message = "内部服务器错误")
    })
    public ResponseEntity<Void> likeUser(@PathVariable("id") Long
                                                 likeUserId) {
        try {
            Boolean bool = this.todayBestService.disLikeUser(likeUserId);
            if(bool){
                return ResponseEntity.ok(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * 不喜欢
     *
     * @param likeUserId
     * @return
     */
    @GetMapping("{id}/unlove")
    @ApiOperation(value = "取消喜欢用户", notes = "用户取消喜欢某个用户")
    @ApiResponses({
            @ApiResponse(code = 200, message = "取消喜欢成功", response = Void.class),
            @ApiResponse(code = 500, message = "内部服务器错误")
    })
    public ResponseEntity<Void> disLikeUser(@PathVariable("id") Long
                                                    likeUserId) {
        try {
            Boolean bool = this.todayBestService.disLikeUser(likeUserId);
            if (bool) {
                return ResponseEntity.ok(null);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}
