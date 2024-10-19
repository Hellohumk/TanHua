package com.service;

/*
day 10 用户信息查询
 */

import com.enums.SexEnum;
import com.pojo.User;
import com.pojo.UserInfo;
import com.pojo.vo.UserInfoVo;
import com.utils.UserThreadLocal;
import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UsersService {

    @Autowired
    private UserInfoService userInfoService;

    public UserInfoVo queryUserInfo(String userID, String huanxinID) {
        User user = UserThreadLocal.get();

        //这tm什么意思 这是三个分岔口 userId可能有三个值
        Long userId = user.getId();
        //这是什么意思，环信id互通？
        if (StringUtils.isNotBlank(userID)) {
            userId = Long.valueOf(userID);
        } else if (StringUtils.isNotBlank(huanxinID)) {
            userId = Long.valueOf(huanxinID);
        }

        UserInfo userInfo = this.userInfoService.queryById(userId);
        if (null == userInfo) {
            return null;
        }

        //组装
        UserInfoVo userInfoVo = new UserInfoVo();
        userInfoVo.setAge(userInfo.getAge() != null ? userInfo.getAge().toString() : null);
        userInfoVo.setAvatar(userInfo.getLogo());
        userInfoVo.setBirthday(userInfo.getBirthday());
        userInfoVo.setEducation(userInfo.getEdu());
        userInfoVo.setCity(userInfo.getCity());
        userInfoVo.setGender(userInfo.getSex().name().toLowerCase());
        userInfoVo.setId(userInfo.getUserId());
        userInfoVo.setMarriage(userInfo.getMarriage().equals("已婚") ? 1 : 0);
        userInfoVo.setIncome(userInfo.getIncome());
        userInfoVo.setNickname(userInfo.getNickName());
        userInfoVo.setProfession(userInfo.getIndustry());

        return userInfoVo;
    }

    /**
     * 简单的update 借助userinfoservice 走 mapper
     * @param userInfoVo
     * @return
     */
    public Boolean updateUserInfo(UserInfoVo userInfoVo) {

        User user = UserThreadLocal.get();
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(user.getId());
        userInfo.setAge(Integer.valueOf(userInfoVo.getAge()));
        userInfo.setSex(StringUtils.equalsIgnoreCase(userInfoVo.getGender(),
                "man") ? SexEnum.MAN : SexEnum.WOMAN);
        userInfo.setBirthday(userInfoVo.getBirthday());
        userInfo.setCity(userInfoVo.getCity());
        userInfo.setEdu(userInfoVo.getEducation());
        userInfo.setIncome(StringUtils.replaceAll(userInfoVo.getIncome(),
                "K", ""));
        userInfo.setIndustry(userInfoVo.getProfession());
        userInfo.setMarriage(userInfoVo.getMarriage() == 1 ? "已婚" : "未婚");
        return this.userInfoService.updateUserInfoByUserId(userInfo);


    }
}
