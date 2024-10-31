package com.pojo.vo;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
评论 点赞 喜欢 列表vo
 */


@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "MessageLike", description = "评论 点赞 喜欢 列表vo")
public class MessageLike {
    private String id;
    private String avatar;
    private String nickname;
    private String createDate;
}
