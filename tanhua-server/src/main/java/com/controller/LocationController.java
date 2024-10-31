package com.controller;


import com.service.BaiduService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
//傻逼
@RequestMapping("baidu")
@Api(tags = "地址的存放")
public class LocationController {
    @Autowired
    private BaiduService baiduService;

    /**
     * 更新位置
     *
     * @param param
     * @return
     */
    @PostMapping("location")
    @ApiOperation("更新位置")
    public ResponseEntity<Void> updateLocation(@RequestBody Map<String,
                Object> param) {
        try{
            Double longitude =
                    Double.valueOf(param.get("longitude").toString());
            Double latitude =
                    Double.valueOf(param.get("latitude").toString());
            String address = param.get("addrStr").toString();

            //调
            Boolean bool = baiduService.updateLocation(longitude,latitude,address);

            if(bool){
                return ResponseEntity.ok(null);
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

    }

}
