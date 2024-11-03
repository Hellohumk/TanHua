package com.tanhua.interceptor;

import com.annotation.NoAuthorization;
import com.tanhua.pojo.User;
import com.tanhua.service.UserService;
import com.utils.UserThreadLocal;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 这个和sky-take-out就一样了
 *
 * 注意这个no authorization
 */
@Component
public class TokenInterceptor implements HandlerInterceptor {

    @Autowired
    private UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response, Object handler)
            throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            NoAuthorization noAnnotation =
                    handlerMethod.getMethod().getAnnotation(NoAuthorization.class);
            if (noAnnotation != null) {
// 如果该方法被标记为无需验证token，直接返回即可
                return true;
            }
        }

        String token = request.getHeader("Authorization");
        if (StringUtils.isNotEmpty(token)) {
            User user = userService.queryUserByToken(token);//校验这个用户是否存入了token
            if (user != null) {
                return true;
            }
        }

        //token为空(未登录！) or 有token没查到redis（无效token！）
        response.setStatus(401);
        return false;
    }
}

