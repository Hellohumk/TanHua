package com.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

/**
 * 一张用户一个表，存储自己发布的数据
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "quanzi_album")
public class Album implements Serializable {
    private static final long serialVersionUID = 432183095092216817L;
    private ObjectId id; //主键id
    private ObjectId publishId; //发布id
    private Long created; //发布时间
}
