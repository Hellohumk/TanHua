package com.dubbo.pojo;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.List;

/**
 * 发布表
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "quanzi_publish")
@ApiModel(value = "发布表",description="发布表(publish详细信息)")
public class Publish implements Serializable {

    private static final long serialVersionUID = 8732308321082804771L;
    private ObjectId id; //主键id

    //DAY08
    private Long pid;//推荐引擎使用的uid

    private Long userId;
    private String text; //文字
    private List<String> medias; //媒体数据，图片或小视频 url
    private Integer seeType; // 谁可以看，1-公开，2-私密，3-部分可见，4-不给谁看
    private List<Long> seeList; //部分可见的列表
    private List<Long> notSeeList; //不给谁看的列表
    private String longitude; //经度
    private String latitude; //纬度
    private String locationName; //位置名称
    private Long created; //发布时间
}
