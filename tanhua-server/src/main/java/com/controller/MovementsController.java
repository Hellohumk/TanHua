package com.controller;

import com.pojo.vo.Movements;
import com.pojo.vo.PageResult;
import com.pojo.vo.VisitorsVo;
import com.service.MovementsService;
import com.service.QuanZiMQService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

//dat08 改动，为每一个业务加上调用MQ发消息

@RestController
@RequestMapping("movements")
public class MovementsController {

    @Autowired
    private MovementsService movementsService;

    @Autowired
    private QuanZiMQService quanZiMQService;


    /**
     * 发送动态
     *
     * @param textContent
     * @param location
     * @param multipartFile
     * @param token
     * @return
     */
    @PostMapping()
    public ResponseEntity<Void> savePublish(@RequestParam(value =
            "textContent", required = false) String textContent,
                                            @RequestParam(value =
                                                    "longitude",required = false) String longtitude,
                                            @RequestParam(value =
                                                    "latitude",required = false) String latitude,
                                            @RequestParam(value =
                                                    "location", required = false) String location,
                                            @RequestParam(value =
                                                    "imageContent", required = false) MultipartFile[] multipartFile,
                                            @RequestHeader("Authorization")
                                            String token) {
        try {
            String publishId = movementsService.savePublish(textContent,longtitude,latitude,
                    location, multipartFile, token);
            if(publishId != null){
                //MQ sendMsg
                this.quanZiMQService.publishMsg(publishId);

                return ResponseEntity.ok(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * 查询好友动态
     *
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping
    public PageResult queryPublishList(@RequestParam(value = "page",
            defaultValue = "1") Integer page,
                                       @RequestParam(value = "pagesize",
                                               defaultValue = "10") Integer pageSize) {
        return this.movementsService.queryPublishList(page, pageSize,false);
    }

    /**
     * 查询推荐动态
     *
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("recommend")
    public PageResult queryRecommendPublishList(@RequestParam(value =
            "page", defaultValue = "1") Integer page,
                                                @RequestParam(value = "pagesize",
                                                        defaultValue = "10") Integer pageSize) {
        return this.movementsService.queryPublishList(page, pageSize,
                true);
    }

    /**
     * 点赞
     *
     * @param publishId
     * @return
     */
    @GetMapping("{id}/like")
    public ResponseEntity<Long> likeComment(@PathVariable("id") String
                                                    publishId) {
        try{
            Long likeCount = movementsService.likeComment(publishId);
            if(likeCount != null){

                //发送点赞消息
                this.quanZiMQService.likePublishMsg(publishId);

                //返回总喜欢数，返回不为空即成功
                return ResponseEntity.ok(likeCount);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

    }

    /**
     * 取消点赞
     *
     * @param publishId
     * @return
     */
    @GetMapping("/{id}/dislike")
    public ResponseEntity<Long> disLikeComment(@PathVariable("id") String
                                                       publishId) {
        try {
            Long likeCount =
                    this.movementsService.cancelLikeComment(publishId);
            if (null != likeCount) {

                //发送取消点赞消息
                this.quanZiMQService.disLikePublishMsg(publishId);

                return ResponseEntity.ok(likeCount);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     *
     * 喜欢
     * @param publishId
     * @return
     */
    @GetMapping("/{id}/love")
    public ResponseEntity<Long> loveComment(@PathVariable("id") String publishId){
        try{
            Long loveCount = movementsService.loveComment(publishId);
            if(loveCount != null){

                //发送喜欢消息
                this.quanZiMQService.lovePublishMsg(publishId);

                return ResponseEntity.ok(loveCount);
            }

        }catch(Exception e){
            e.printStackTrace();
        }
        //failed
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

    }

    public ResponseEntity<Long> disLoveComment(@PathVariable("id") String publishId) {
        try {
            Long loveCount = this.movementsService.cancelLoveComment(publishId);
            if (null != loveCount) {

                //发送取消喜欢消息
                this.quanZiMQService.disLovePublishMsg(publishId);

                return ResponseEntity.ok(loveCount);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

    }

    /**
     * 查询单条动态信息
     *
     * @param publishId
     * @return
     */
    @GetMapping("/{id}")
    public ResponseEntity<Movements> queryById(@PathVariable("id") String
                                                       publishId) {
        try {
            Movements movements =
                    this.movementsService.queryById(publishId);
            if(null != movements){

                this.quanZiMQService.queryPublishMsg(publishId);

                return ResponseEntity.ok(movements);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }


    //day09
    /**
     * 谁看过我
     *
     * @return
     */
    @GetMapping("visitors")
    public ResponseEntity<List<VisitorsVo>> queryVisitorsList(){
        try {
            List<VisitorsVo> list =
                    this.movementsService.queryVisitorsList();
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * 分页查询自己发布的publish
     * @param page
     * @param pageSize
     * @param userId
//     * @return  PageResult<Movement>
     */
    @GetMapping("all")
    public ResponseEntity<PageResult> queryAlbumList(@RequestParam(value =
            "page", defaultValue = "1") Integer page,
                                                     @RequestParam(value =
                                                             "pagesize", defaultValue = "10") Integer pageSize,
                                                     @RequestParam(value =
                                                             "userId") Long userId) {
        try {
            PageResult pageResult =
                    this.movementsService.queryAlbumList(userId, page, pageSize);
            return ResponseEntity.ok(pageResult);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //failed
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }





}

