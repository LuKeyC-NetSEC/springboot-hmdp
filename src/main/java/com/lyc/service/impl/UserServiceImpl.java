package com.lyc.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyc.dto.LoginFormDTO;
import com.lyc.dto.Result;
import com.lyc.entity.User;
import com.lyc.mapper.UserMapper;
import com.lyc.service.IUserService;
import com.lyc.service.SmsService;
import com.lyc.utils.RegexUtils;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SmsService smsService;

    @Override
    public Result sendCode(String phone, HttpSession session) {
        if (RegexUtils.isPhoneInvalid(phone)) {
            return Result.fail("手机号码格式不正确");
        }
        String code = RandomUtil.randomNumbers(6);

        session.setAttribute("code", code);

        log.debug("发送短信验证码成功，验证码：" + code);

        smsService.sendCode(phone,code);
        return Result.ok();
    }

    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        String phone = loginForm.getPhone();
        if (RegexUtils.isPhoneInvalid(phone)) {
            return Result.fail("手机号码格式不正确");
        }

        String code = loginForm.getCode();
        String sessionCode = (String) session.getAttribute("code");
        if (!code.equals(sessionCode) && !code.isEmpty()) {
            return Result.fail("验证码错误");
        }

        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getPhone, phone);
        User user = userMapper.selectOne(queryWrapper);

        if (user == null) {
            user = new User();
            user.setPhone(phone);
            user.setNickName("user_" + RandomUtil.randomString(10));
            userMapper.insert(user);
        }

        String password = loginForm.getPassword();

        if (password != null) {
            LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(User::getPhone, phone);
            lambdaQueryWrapper.eq(User::getPassword, password);
            user = userMapper.selectOne(lambdaQueryWrapper);
        }

        if (user == null) {
            return Result.fail("手机号或密码不正确");
        }

        session.setAttribute("user",user);
        return Result.ok();
    }
}
