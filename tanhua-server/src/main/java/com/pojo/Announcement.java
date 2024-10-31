package com.pojo;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("公告")
public class Announcement extends BasePojo {
    private Long id;
    private String title;
    private String description;
}
