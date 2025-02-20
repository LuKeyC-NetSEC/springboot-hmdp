package com.lyc.interceptor;

import com.lyc.dto.UserDTO;
import com.lyc.entity.User;
import com.lyc.utils.UserHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * @ClassName LoginInterceptor
 * @Description TODO 登录校验拦截器
 * @Author LuKey_C
 * @Date 2025/2/20 13:45
 * @Version 1.0
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");
        if (user == null){
            response.setStatus(401);
            return false;
        }
        UserHolder.saveUser(user);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        UserHolder.removeUser();
    }
}
