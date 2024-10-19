package com.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mapper.UserInfoMapper;
import com.pojo.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 为什么不在sso加一个功能调，而是i这里再次访问数据库
 */


@Service
public class UserInfoService {
    @Autowired
    private UserInfoMapper userInfoMapper;

    public UserInfo queryById(Long id) {
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", id);
        return this.userInfoMapper.selectOne(queryWrapper);
    }

    /**
     * 根据参数查询用户info信息列表
     * @param queryWrapper
     * @return
     */
    public List<UserInfo> queryList(QueryWrapper<UserInfo> queryWrapper) {
        return userInfoMapper.selectList(queryWrapper);
    }

    public Boolean updateUserInfoByUserId(UserInfo userInfo) {
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userInfo.getUserId());
        return this.userInfoMapper.update(userInfo, queryWrapper) > 0;
    }
}