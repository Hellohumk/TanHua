package com.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
公告返回VO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageAnnouncement {
    private String id;
    private String title;
    private String description;
    private String createDate;
}
