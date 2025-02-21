package com.lyc.interceptor;

import cn.hutool.core.bean.BeanUtil;
import com.lyc.dto.UserDTO;
import com.lyc.utils.JwtUtil;
import com.lyc.utils.RedisConstants;
import com.lyc.utils.UserHolder;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName LoginInterceptor
 * @Description TODO 登录校验拦截器
 * @Author LuKey_C
 * @Date 2025/2/20 13:45
 * @Version 1.0
 */
@Component
public class RefreshTokenInterceptor implements HandlerInterceptor {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("authorization");
        if (token.isBlank()){
            return true;
        }

        try {
            Claims claims = JwtUtil.parseToken(token);
            if (claims.isEmpty()){
                return true;
            }

            String userId = claims.get("id").toString();
            Boolean redisResult = redisTemplate.hasKey(RedisConstants.LOGIN_USER_KEY + userId);
            if (!Boolean.TRUE.equals(redisResult)) {
                return true;
            }

            UserDTO userDTO = BeanUtil.toBean(claims, UserDTO.class);
            UserHolder.saveUser(userDTO);

            redisTemplate.expire(RedisConstants.LOGIN_USER_KEY + userId,RedisConstants.LOGIN_USER_TTL, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserHolder.removeUser();
    }

}
