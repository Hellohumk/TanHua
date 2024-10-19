package com.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
评论 点赞 喜欢 列表vo
 */


@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageLike {
    private String id;
    private String avatar;
    private String nickname;
    private String createDate;
}
