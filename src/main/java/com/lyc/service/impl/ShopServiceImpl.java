package com.lyc.service.impl;

import cn.hutool.json.JSONUtil;
import com.lyc.dto.Result;
import com.lyc.entity.Shop;
import com.lyc.mapper.ShopMapper;
import com.lyc.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyc.utils.RedisConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ShopMapper shopMapper;

    @Override
    public Result queryById(Long id) {
        String shopJson = redisTemplate.opsForValue().get(RedisConstants.CACHE_SHOP_KEY + id);
        if (shopJson != null) {
            Shop shop = JSONUtil.toBean(shopJson, Shop.class);
            return Result.ok(shop);
        }

        Shop shop = getById(id);
        if (shop == null){
            return Result.fail("店铺不存在");
        }

        redisTemplate.opsForValue().set(RedisConstants.CACHE_SHOP_KEY + id,JSONUtil.toJsonStr(shop),RedisConstants.CACHE_SHOP_TTL, TimeUnit.MINUTES);

        return Result.ok(shop);
    }

    @Override
    @Transactional
    public Result updateShop(Shop shop) {
        Long id = shop.getId();
        if (id == null){
            return Result.fail("店铺ID不能为空");
        }
        //1.更新数据库
        shopMapper.updateById(shop);
        //2.删除缓存
        redisTemplate.delete(RedisConstants.CACHE_SHOP_KEY + id);

        return Result.ok();
    }
}
