package com.lyc.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyc.dto.LoginFormDTO;
import com.lyc.dto.Result;
import com.lyc.dto.UserDTO;
import com.lyc.entity.User;
import com.lyc.mapper.UserMapper;
import com.lyc.service.IUserService;
import com.lyc.service.SmsService;
import com.lyc.utils.JwtUtil;
import com.lyc.utils.RedisConstants;
import com.lyc.utils.RegexUtils;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.beans.BeanMap;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public Result sendCode(String phone, HttpSession session) {
        if (RegexUtils.isPhoneInvalid(phone)) {
            return Result.fail("手机号码格式不正确");
        }
        String code = RandomUtil.randomNumbers(6);

//        session.setAttribute("code", code);
        redisTemplate.opsForValue().set(RedisConstants.LOGIN_CODE_KEY + phone, code, RedisConstants.LOGIN_CODE_TTL, TimeUnit.MINUTES);

        log.debug("发送短信验证码成功，验证码：" + code);

        smsService.sendCode(phone, code);
        return Result.ok();
    }

    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        String phone = loginForm.getPhone();
        if (RegexUtils.isPhoneInvalid(phone)) {
            return Result.fail("手机号码格式不正确");
        }

        String code = loginForm.getCode();
//        String sessionCode = (String) session.getAttribute("code");
        String sessionCode = redisTemplate.opsForValue().get(RedisConstants.LOGIN_CODE_KEY + phone);
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

        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user, userDTO);

        Map<String, Object> map = BeanUtil.beanToMap(userDTO,new HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true)
                        .setFieldValueEditor((fieldName,fieldValue) -> fieldValue.toString()));
        String token = getJwtToken(map);
//        session.setAttribute("user", userDTO);
        return Result.ok(token);
    }

    private String getJwtToken(Map<String, Object> map) {
        String token = JwtUtil.createToken(map);
        String userId = map.get("id").toString();
        redisTemplate.opsForValue().set(RedisConstants.LOGIN_USER_KEY + userId ,token,RedisConstants.LOGIN_USER_TTL , TimeUnit.SECONDS);
        return token;
    }
}
