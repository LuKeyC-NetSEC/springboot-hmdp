package com.lyc.service.impl;

import cn.hutool.json.JSONUtil;
import com.lyc.dto.Result;
import com.lyc.entity.ShopType;
import com.lyc.mapper.ShopTypeMapper;
import com.lyc.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyc.utils.RedisConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public Result queryTypeList() {
        List<String> shopTypes = redisTemplate.opsForList().range(RedisConstants.CACHE_SHOP_TYPE_KEY, 0, -1);
        if (!shopTypes.isEmpty()){
            List<ShopType> result = new ArrayList<>();
            for (String type : shopTypes) {
                ShopType shopType = JSONUtil.toBean(type,ShopType.class);
                result.add(shopType);
            }
            return Result.ok(result);
        }

        List<ShopType> result = query().orderByAsc("sort").list();
        if (result.isEmpty()){
            return Result.fail("店铺类型不存在");
        }
        for (ShopType shopType : result) {
            String shopTypeJson = JSONUtil.toJsonStr(shopType);
            shopTypes.add(shopTypeJson);
        }

        redisTemplate.opsForList().rightPushAll(RedisConstants.CACHE_SHOP_TYPE_KEY, shopTypes);
        redisTemplate.expire(RedisConstants.CACHE_SHOP_TYPE_KEY, Duration.ofHours(1));


        return Result.ok(result);
    }
}
