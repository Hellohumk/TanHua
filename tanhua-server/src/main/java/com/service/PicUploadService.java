package com.service;

import cn.hutool.core.date.DateTime;
import com.pojo.vo.PicUploadResult;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

//TODO OSS成型后补充

/**
 * 这个是从sso拷贝过来的，那为什么不tm都做成分布式互相调用，操死你的吗！
 *
 * 相对一套的，aliyunoss也要拷贝  pojo也要，我操死你啊
 */


@Service
public class PicUploadService {

    // 允许上传的格式
    private static final String[] IMAGE_TYPE = new String[]{".bmp", ".jpg",
            ".jpeg", ".gif", ".png"};

    private static final String HOME_PATH = "F:\\tanhuaPic";



    public PicUploadResult upload(MultipartFile uploadFile) {
        PicUploadResult picUploadResult = new PicUploadResult();

        //图片做校验，对后缀名
        boolean isLegal = false;
        for (String type : IMAGE_TYPE) {
            if
            (StringUtils.endsWithIgnoreCase(uploadFile.getOriginalFilename(),
                    type)) {
                isLegal = true;
                break;
            }
        }
        if (!isLegal) {
            picUploadResult.setStatus("error");
            return picUploadResult;
        }

        //file path
        String fileName = uploadFile.getOriginalFilename();
        String filePath = getFilePath(fileName);

        //TODO 这里后期换成aliyun
        try{

            String url = HOME_PATH + filePath;

            uploadFile.transferTo(new File(url));


        }catch(Exception e){
            //upload failed
            e.printStackTrace();
            picUploadResult.setStatus("error");
            return picUploadResult;
        }

        //success
        picUploadResult.setUid(String.valueOf(System.currentTimeMillis()));
        picUploadResult.setName(HOME_PATH + filePath);
        picUploadResult.setStatus("done");
        picUploadResult.setResponse("{\"status\": \"success\"}");

        return picUploadResult;

    }


    //不用UUID吗
    private String getFilePath(String sourceFileName) {
        DateTime dateTime = new DateTime();
        return "images/" + dateTime.toString("yyyy")
                + "/" + dateTime.toString("MM") + "/"
                + dateTime.toString("dd") + "/" +
                System.currentTimeMillis() +
                RandomUtils.nextInt(100, 9999) + "." +
                StringUtils.substringAfterLast(sourceFileName, ".");
    }



}
