package com.lyc.service;

/**
 * @ClassName SmsService
 * @Description TODO
 * @Author LuKey_C
 * @Date 2025/2/20 12:44
 * @Version 1.0
 */
public interface SmsService {
    void sendCode(String phone,String code);
}
