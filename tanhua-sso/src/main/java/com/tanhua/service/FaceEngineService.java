package com.tanhua.service;


import com.arcsoft.face.EngineConfiguration;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.FaceInfo;
import com.arcsoft.face.FunctionConfiguration;
import com.arcsoft.face.enums.DetectMode;
import com.arcsoft.face.enums.ErrorInfo;
import com.arcsoft.face.enums.ImageFormat;
import com.arcsoft.face.toolkit.ImageFactory;
import com.arcsoft.face.toolkit.ImageInfo;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * 虹软人脸识别，完全看不懂，注意流程看文档就行
 */
@Slf4j
@Service
public class FaceEngineService {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(FaceEngineService.class);

    @Value("${config.arcface-sdk.app-id}")
    private String appid;
    @Value("${config.arcface-sdk.sdk-key}")
    private String sdkKey;
    @Value("${config.arcface-sdk.sdk-lib-path}")
    private String libPath;
    private FaceEngine faceEngine;



    @PostConstruct    //Bean初始化后立即执行
    public void init(){
        //INIT engine
        FaceEngine faceEngine = new FaceEngine(libPath);
        int activecode = faceEngine.activeOnline(appid,sdkKey);
        if(activecode != ErrorInfo.MOK.getValue() && activecode != ErrorInfo.MERR_ASF_ALREADY_ACTIVATED.getValue()){
            LOGGER.error("引擎启动失败");
            throw new RuntimeException("引擎启动失败");
        }

        //engine config
        EngineConfiguration engineConfiguration = new EngineConfiguration();
        engineConfiguration.setDetectMode(DetectMode.ASF_DETECT_MODE_IMAGE);

        //功能配置
        FunctionConfiguration functionConfiguration = new
                FunctionConfiguration();
        functionConfiguration.setSupportAge(true);
        functionConfiguration.setSupportFace3dAngle(true);
        functionConfiguration.setSupportFaceDetect(true);
        functionConfiguration.setSupportFaceRecognition(true);
        functionConfiguration.setSupportGender(true);
        functionConfiguration.setSupportLiveness(true);
        functionConfiguration.setSupportIRLiveness(true);
        engineConfiguration.setFunctionConfiguration(functionConfiguration);

        //初始化引擎
        int initCode = faceEngine.init(engineConfiguration);
        if (initCode != ErrorInfo.MOK.getValue()) {
            LOGGER.error("初始化引擎出错!");
            throw new RuntimeException("初始化引擎出错!");
        }
        this.faceEngine = faceEngine;

    }

    /**
     * 检查图片是否为人像
     * @param imageInfo
     * @return
     */
    public boolean checkIsPortrait(ImageInfo imageInfo){
        // 定义人脸列表
        List<FaceInfo> faceInfoList = new ArrayList<FaceInfo>();
        faceEngine.detectFaces(imageInfo.getImageData(),
                imageInfo.getWidth(), imageInfo.getHeight(), ImageFormat.CP_PAF_BGR24,
                faceInfoList);
        return !faceInfoList.isEmpty();
    }

    public boolean checkIsPortrait(byte[] imageData) {
        return this.checkIsPortrait(ImageFactory.getRGBData(imageData));
    }
    public boolean checkIsPortrait(File file) {
        return this.checkIsPortrait(ImageFactory.getRGBData(file));
    }

}
