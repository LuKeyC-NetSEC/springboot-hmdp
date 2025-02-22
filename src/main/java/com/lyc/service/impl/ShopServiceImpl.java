package com.lyc.service.impl;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
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
        // 缓存穿透
        // Shop shop = queryWithPassThrough(id);

        // 互斥锁解决缓存击穿
        Shop shop = queryWithMutex(id);
        if (shop == null){
            return Result.fail("店铺不存在");
        }
        return Result.ok(shop);
    }

    /**
     * 缓存击穿解决方案
     * @param id 店铺ID
     * @return Shop 店铺信息
     */
    public Shop queryWithMutex(Long id){
        //1.读取缓存
        String shopJson = redisTemplate.opsForValue().get(RedisConstants.CACHE_SHOP_KEY + id);
        //2.缓存命中对象直接返回
        if (StrUtil.isNotBlank(shopJson)) {
            return JSONUtil.toBean(shopJson, Shop.class);
        }
        //3.缓存命中空对象
        if (shopJson != null){
            //命中空对象 返回错误信息
            return null;
        }
        //4.未拿从缓存中拿到数据 进行缓存重建
        //4.1 获取互斥锁
        boolean isLock = tryLock(RedisConstants.LOCK_SHOP_KEY + id);
        Shop shop = null;
        try {
            //4.2 判断是否拿到锁
            if (!isLock){
                //4.3 失败则休眠
                Thread.sleep(50);
                return queryWithMutex(id);
            }
            //再次读取缓存 二次验证
            shopJson = redisTemplate.opsForValue().get(RedisConstants.CACHE_SHOP_KEY + id);
            if (StrUtil.isNotBlank(shopJson)) {
                return JSONUtil.toBean(shopJson, Shop.class);
            }
            if (shopJson != null){
                //命中空对象 返回错误信息
                return null;
            }
            //4.4 从数据库中获取数据
            shop = getById(id);
            if (shop == null){
                //4.4.1 数据不存在 将空值写入Redis 添加空对象
                redisTemplate.opsForValue().set(RedisConstants.CACHE_SHOP_KEY + id,"",RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);
                //返回错误信息
                return null;
            }
            //4.5 数据存在写入缓存
            redisTemplate.opsForValue().set(RedisConstants.CACHE_SHOP_KEY + id,JSONUtil.toJsonStr(shop),RedisConstants.CACHE_SHOP_TTL, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            unLock(RedisConstants.LOCK_SHOP_KEY + id);
        }

        return shop;
    }

    /**
     * 缓存穿透解决方案
     * @param id 店铺ID
     * @return Shop 店铺信息
     */
    public Shop queryWithPassThrough (Long id){
        String shopJson = redisTemplate.opsForValue().get(RedisConstants.CACHE_SHOP_KEY + id);
        if (StrUtil.isNotBlank(shopJson)) {
            return JSONUtil.toBean(shopJson, Shop.class);
        }
        if (shopJson != null){
            //命中空对象 返回错误信息
            return null;
        }

        Shop shop = getById(id);
        if (shop == null){
            //数据不存在 将空值写入Redis 添加空对象
            redisTemplate.opsForValue().set(RedisConstants.CACHE_SHOP_KEY + id,"",RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);
            //返回错误信息
            return null;
        }

        redisTemplate.opsForValue().set(RedisConstants.CACHE_SHOP_KEY + id,JSONUtil.toJsonStr(shop),RedisConstants.CACHE_SHOP_TTL, TimeUnit.MINUTES);

        return shop;
    }


    private boolean tryLock(String key){
        Boolean flag = redisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag);
    }

    private boolean unLock(String key){
        Boolean flag = redisTemplate.delete(key);
        return BooleanUtil.isTrue(flag);
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
