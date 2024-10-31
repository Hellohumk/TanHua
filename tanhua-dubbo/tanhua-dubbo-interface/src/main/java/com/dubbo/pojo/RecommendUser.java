package com.dubbo.pojo;


import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "recommend_user")
@ApiModel(value = "推荐用户")
public class RecommendUser implements java.io.Serializable{
    private static final long serialVersionUID = -4296017160071130962L;
    // @Id
// private ObjectId id; //主键id
    @Indexed
    private Long userId; //推荐的用户id
    private Long toUserId; //用户id
    @Indexed
    private Double score; //推荐得分
    private String date; //日期
}
