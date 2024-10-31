package com.dubbo.api;

import com.dubbo.pojo.vo.UserLocationVo;

import java.util.List;

public interface UserLocationApi {
    /**
     * 更新用户地理位置 insert
     *
     * @return
     */
    String updateUserLocation(Long userId, Double longitude, Double
            latitude, String address);

    /**
     * 查询用户地理位置
     *
     * @param userId
     * @return
     */
    UserLocationVo queryByUserId(Long userId);
    /**
     * 根据地理位置查询用户 ()附近的人
     *
     * @param longitude
     * @param latitude
     * @return
     */
    List<UserLocationVo> queryUserFromLocation(Double longitude, Double
            latitude, Integer range);
}
