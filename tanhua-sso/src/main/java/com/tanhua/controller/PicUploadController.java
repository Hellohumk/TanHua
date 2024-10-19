package com.tanhua.controller;

import com.tanhua.pojo.vo.PicUploadResult;
import com.tanhua.service.PicUploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("pic/upload")
public class PicUploadController {

    //TODO 这里上传的本地
    @Autowired
    private PicUploadService picUploadService;
    @PostMapping
    @ResponseBody
    public PicUploadResult upload(@RequestParam("file") MultipartFile
                                          multipartFile) {
        return this.picUploadService.upload(multipartFile);
    }

}
