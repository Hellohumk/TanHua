package com.tanhua.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tanhua.enums.SexEnum;
import com.tanhua.pojo.User;
import com.tanhua.pojo.UserInfo;
import com.tanhua.mapper.UserInfoMapper;
import com.tanhua.pojo.vo.PicUploadResult;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
public class UserInfoService {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(UserInfoService.class);

    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private UserService userService;
    @Autowired
    private FaceEngineService faceEngineService;
    @Autowired
    private PicUploadService picUploadService;

    public Boolean saveUserInfo(Map<String,String> param,String token){
        //相当于登陆一次后，user就从redis查，无需interceptor了
        User user = this.userService.queryUserByToken(token);
        if (user == null) {
            return false;
        }

        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(user.getId());
        //拿回是个gender
        userInfo.setSex(StringUtils.equals(param.get("gender"),"man") ? SexEnum.MAN : SexEnum.WOMAN);
        userInfo.setNickName(param.get("nickname"));
        userInfo.setBirthday(param.get("birthday"));
        userInfo.setCity(param.get("city"));

        //save UserInfo
        userInfoMapper.insert(userInfo);

        return true;
    }

    //为什么不把upload单独抽出来做一个nmd的controller，然后调两次，什么二比逻辑
    public Boolean saveLogo(MultipartFile file,String token){
        User user = this.userService.queryUserByToken(token);
        if (user == null) {
            return false;
        }

        try{


        }catch(Exception e){
            e.printStackTrace();
            LOGGER.error("人脸检测出错！");
            return false;
        }
        //upload
        PicUploadResult uploadResult = picUploadService.upload(file);

        UserInfo userInfo = new UserInfo();
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<UserInfo>();
        queryWrapper.eq("user_id",user.getId());
        userInfoMapper.update(userInfo,queryWrapper);

        return true;

    }


}
