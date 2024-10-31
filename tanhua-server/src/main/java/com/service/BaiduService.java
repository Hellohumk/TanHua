package com.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.dubbo.api.UserLocationApi;
import com.pojo.User;
import com.utils.UserThreadLocal;
import org.springframework.stereotype.Service;


/*
位置更新
 */
@Service
public class BaiduService {

    @Reference(version = "1.0.0")
    private UserLocationApi userLocationApi;
    public Boolean updateLocation(Double longitude, Double latitude, String address) {

        try {
            User user = UserThreadLocal.get();
            this.userLocationApi.updateUserLocation(user.getId(),
                    longitude, latitude, address);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
