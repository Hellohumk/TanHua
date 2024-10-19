package com.tanhua.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tanhua.mapper.UserMapper;
import com.tanhua.pojo.User;
import com.tanhua.pojo.vo.HuanXinUser;
import com.tanhua.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 、、TODO User login 好像正确和错误都会给token，错误相当于注册，后面把他改一下加一个registry
 *
 *  其次它要返回给前端的不止一个变量，两个 token 和 isNew来判断是否是新人
 *
 */
@Slf4j
@Service
public class UserService {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(UserService.class);
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private HuanXinService huanXinService;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    //TODO rOCKETmq 未找到
//    @Autowired
//    private RocketMQTemplate rocketMQTemplate;

    @Value("${jwt.secret}")
    private String key;

    //你返回两个量你就map啊，搞几把string
    public String login(String mobile, String code) {

        Boolean isNew = false;

        //code校验
        String rediskey = "CHECK_CODE_" + mobile;
        String c = redisTemplate.opsForValue().get(rediskey);
        if(c != code){
            return null;//验证码错误
        }

        //Mapper查是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<User>();
        queryWrapper.eq("mobile",mobile);
        //user唯一读取口，其余状态必然是走redis
        User user = userMapper.selectOne(queryWrapper);//查找对应用户

        //检验
        if(user == null){
            //no this user , registry! TODO 说的就是这一块，把他单独做一个registry好
            User u = new User();
            u.setMobile(mobile);

            //存储时采用MD5加密 原密码为secret + _123456
            user.setPassword(DigestUtils.md5Hex(key + "_123456"));

            //user唯一插入口
            userMapper.insert(u);
            user = u;
            isNew = true;

            //day05 环信注册
            huanXinService.register(user.getId());
        }

        //生成jwt
        Map<String, Object> claims = new HashMap<String, Object>();
        claims.put("mobile", mobile);
        claims.put("id", user.getId());
        String token = JwtUtil.createJWT(key,600000,claims);//10min

        //用户数据进入redis，but why？ key：TOKEN_ + token
        String redisTokenKey = "TOKEN_" + token;
        try{
            this.redisTemplate.opsForValue().set(redisTokenKey,
                    MAPPER.writeValueAsString(user), Duration.ofHours(1));
        }catch(Exception e){
            //redis 挂了
            e.printStackTrace();
        }

//rocket MQ
//        try{
//            //login sucess 消息发送到mq
//            Map<String, Object> msg = new HashMap<>();
//            msg.put("userId", user.getId());
//            msg.put("date", new Date());
//            this.rocketMQTemplate.convertAndSend("tanhua-sso-login", msg);
//        }catch(Exception e){
//            e.printStackTrace();
//        }

        return isNew + "|" + token;


    }

    /**
     * 从redis拿，登录后就有
     * @param token
     * @return
     */
    public User queryUserByToken(String token) {
        String redisKey = "TOKEN_" + token;
        String str = redisTemplate.opsForValue().get(redisKey);
        try{
            return MAPPER.readValue(str, new TypeReference<User>() {});
        }catch(Exception e){
            //这里应该是redis问题
            LOGGER.error("redis访问错误",e);
//            LOGGER.error("未从缓存中找到用户，用户可能没有登陆或服务器错误");
            return null;
        }
    }


}
