package com.pojo.vo;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 最后展示出的评论VO
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "最后展示出的评论VO")
public class CommentsVO {

    private String id; //评论id
    private String avatar; //头像
    private String nickname; //昵称
    private String content; //评论
    private String createDate; //评论时间: 08:27
    private Integer likeCount; //点赞数
    private Integer hasLiked; //是否点赞（1是，0否）
}
