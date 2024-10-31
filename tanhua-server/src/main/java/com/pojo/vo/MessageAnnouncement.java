package com.pojo.vo;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
公告返回VO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel   (value = "公告返回VO")
public class MessageAnnouncement {
    private String id;
    private String title;
    private String description;
    private String createDate;
}
