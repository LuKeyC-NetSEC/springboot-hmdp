package com.lyc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lyc.dto.LoginFormDTO;
import com.lyc.dto.Result;
import com.lyc.entity.User;
import jakarta.servlet.http.HttpSession;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface IUserService extends IService<User> {

    Result sendCode(String phone, HttpSession session);

    Result login(LoginFormDTO loginForm, HttpSession session);

}
