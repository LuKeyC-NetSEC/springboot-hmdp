package com.lyc.service.impl;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.lyc.service.SmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @ClassName SmsServiceImpl
 * @Description TODO
 * @Author LuKey_C
 * @Date 2025/2/20 12:45
 * @Version 1.0
 */
@Service
public class SmsServiceImpl implements SmsService {

    @Autowired
    private Client client;

    @Override
    public void sendCode(String phone, String code) {
        SendSmsRequest request = new SendSmsRequest();
        request.setPhoneNumbers(phone);
        request.setSignName("阿里云短信测试");
        request.setTemplateCode("SMS_154950909");
        request.setTemplateParam("{\"code\":\"" + code + "\"}");
        try {
            client.sendSms(request);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
