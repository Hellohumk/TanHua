package com.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

/**
 * 评论表
 *
 * !!!评论 点赞 和 喜欢 三个都是评论，通过commentType区分，
 * 点赞和喜欢只不过是特殊的评论
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "quanzi_comment")

public class Comment implements Serializable {
    private static final long serialVersionUID = -291788258125767614L;
    private ObjectId id;
    private ObjectId publishId; //发布id
    private Integer commentType; //评论类型，1-点赞，2-评论，3-喜欢
    private String content; //评论内容
    private Long userId; //评论人

    //昂与字段，谁给我自己的帖子点赞
    //发布人的用户id，对应上publish表中的userId
    private Long publishUserId; //发布人的用户id

    private Boolean isParent = false; //是否为父节点，默认是否
    private ObjectId parentId; // 父节点id
    private Long created; //发表时间
}
