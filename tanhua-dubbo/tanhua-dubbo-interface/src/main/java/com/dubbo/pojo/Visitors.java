package com.dubbo.pojo;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "visitors")
@ApiModel(value = "Visitors", description = "来访记录")
public class Visitors implements java.io.Serializable{
    private static final long serialVersionUID = 2811682148052386573L;
    private ObjectId id;
    private Long userId; //被偷看用户id
    private Long visitorUserId; //来访用户id
    private String from; //来源，如首页、圈子等
    private Long date; //来访时间
    private Double score; //得分

}
