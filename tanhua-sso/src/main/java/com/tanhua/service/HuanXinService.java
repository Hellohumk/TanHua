package com.tanhua.service;

import cn.hutool.crypto.digest.DigestUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tanhua.config.HuanxinConfig;
import com.tanhua.pojo.vo.HuanXinUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
/*
环信具体业务
 */

@Service
public class HuanXinService {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    @Autowired
    private HuanXinTokenService huanXinTokenService;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private HuanxinConfig huanXinConfig;

    /**
     * 注册环信用户
     * @param userId
     * @return
     */
    public boolean register(Long userId){
        String targetUrl = huanXinConfig.getUrl()
                + huanXinConfig.getOrgName() + "/"
                + huanXinConfig.getAppName() + "/users";

        String token = huanXinTokenService.getToken();

        try{
            //用户id做用户名
            //密码（默认）为 userId + tanhua字符串拼接后MD5加密
            HuanXinUser huanXinUser = new HuanXinUser(String.valueOf(userId), DigestUtil.md5Hex(userId + "tanhua"));

            //设置body （用户信息 存在List然后放进body）
            //asList： Array -> List ,传入一个对象则是把这个对象作为一个单独的元素塞入长度为一的list中
            String body = MAPPER.writeValueAsString(Arrays.asList(huanXinUser));

            // 请求头
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "application/json");
            headers.add("Authorization", "Bearer " + token);
            //组装成entity
            HttpEntity<String> httpEntity = new HttpEntity<>(body,
                    headers);
            //发送
            ResponseEntity<String> responseEntity =
                    this.restTemplate.postForEntity(targetUrl, httpEntity, String.class);

            //200 发送成功 给true 否则全给false
            return responseEntity.getStatusCodeValue() == 200;

        }catch(Exception e){
            //failed
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 添加好友
     *
     * @param userId
     * @param friendId
     * @return
     */
    public boolean contactUsers(Long userId, Long friendId) {
        //添加联系人的第三方api的url
        String targetUrl = this.huanXinConfig.getUrl()
                + this.huanXinConfig.getOrgName() + "/"
                + this.huanXinConfig.getAppName() + "/users/" +
                userId + "/contacts/users/" + friendId;
        try {
            String token = this.huanXinTokenService.getToken();
// 请求头
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer " + token);
            HttpEntity<String> httpEntity = new HttpEntity<>(headers);
            ResponseEntity<String> responseEntity =
                    this.restTemplate.postForEntity(targetUrl, httpEntity, String.class);
            return responseEntity.getStatusCodeValue() == 200;
        } catch (Exception e) {
            e.printStackTrace();
        }
// 添加失败
        return false;
    }

    public boolean sendMsg(String target, String type, String msg) {
        try{
            //拿进入环信的token
            String token = this.huanXinTokenService.getToken();
// 请求头
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer " + token);//TODO 为什么加 Bearer

            //请求体信息拼接
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("target_type", "users");
            paramMap.put("target", Arrays.asList(target));
            Map<String, Object> msgMap = new HashMap<>();
            msgMap.put("type", type);
            msgMap.put("msg", msg);
            paramMap.put("msg", msgMap);

            //表示消息发送者;无此字段Server会默认设置为“from”:“admin”，有from字段但值为空串(“”)时请求失败
            //TODO why？
            msgMap.put("from", type);


            HttpEntity<String> httpEntity = new HttpEntity<>
                    (MAPPER.writeValueAsString(paramMap), headers);

            String targetUrl =  huanXinConfig.getUrl()
                    + huanXinConfig.getOrgName() + "/"
                    + huanXinConfig.getAppName() + "/"
                    + "messages";

            ResponseEntity<String> responseEntity =
                    this.restTemplate.postForEntity(targetUrl, httpEntity, String.class);
            return responseEntity.getStatusCodeValue() == 200;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
