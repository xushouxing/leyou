package com.leyou.service;

import com.leyou.common.utils.JsonUtils;
import com.leyou.config.LoginInterceptor;
import com.leyou.fenigClient.GoodsClient;
import com.leyou.pojo.Cart;
import com.leyou.pojo.Sku;
import com.leyou.pojo.UserInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {
    @Autowired
    private StringRedisTemplate template;
    @Autowired
    private GoodsClient goodsClient;
    private final static String CART_PRE="leyou:cart:uid:";
    public void addCart(Cart cart) {
        //拿到用户信息
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        String key=CART_PRE+userInfo.getId();
        BoundHashOperations<String, Object, Object> ops = template.boundHashOps(key);
        //获取数量
        Integer num = cart.getNum();
        //skuid
        Long skuId = cart.getSkuId();
        //商品是否存在
        Boolean boo=ops.hasKey(skuId.toString());
        if (boo){
            //存在数量增加
            Object o = ops.get(skuId.toString());
            cart = JsonUtils.parse(o.toString(), Cart.class);
            cart.setNum(cart.getNum()+num);
        }else {
            //从数据库查询出sku
            Sku sku = goodsClient.querySkuBySkuId(skuId);
            //将sku信息封装
            cart.setUserId(userInfo.getId());
            cart.setImage(StringUtils.isBlank(sku.getImages()) ? "" : StringUtils.split(sku.getImages(), ",")[0]);
            cart.setOwnSpec(sku.getOwnSpec());
            cart.setPrice(sku.getPrice());
            cart.setTitle(sku.getTitle());
        }
        // 将购物车数据写入redis
        ops.put(cart.getSkuId().toString(),JsonUtils.serialize(cart));
    }

    /**
     * 查询商品
     * @return
     */
    public List<Cart> getCart() {
        //从线程中拿到用户信息
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        String key=CART_PRE+userInfo.getId();
        //判断是否存在购物车
        if(!template.hasKey(key)){
            //不存在返回null
            return null;
        }
        BoundHashOperations<String, Object, Object> ops = template.boundHashOps(key);
        List<Object> carts = ops.values();
        //判断有无商品
        if (CollectionUtils.isEmpty(carts)){
            return null;
        }
        List<Cart> carts1 = carts.stream().map(o ->
            JsonUtils.parse(o.toString(), Cart.class)
        ).collect(Collectors.toList());
        return carts1;
    }

    public void updateCarts(Cart cart) {
        //从线程中拿到用户信息
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        String key=CART_PRE+userInfo.getId();
        BoundHashOperations<String, Object, Object> ops = template.boundHashOps(key);
        //获取数量
        Integer num = cart.getNum();
        //skuid
        Long skuId = cart.getSkuId();
        //存在数量增加
        Object o = ops.get(skuId.toString());
        cart = JsonUtils.parse(o.toString(), Cart.class);
        cart.setNum(cart.getNum()+num);
        // 写入购物车
        ops.put(cart.getSkuId().toString(), JsonUtils.serialize(cart));
    }

    /**
     * 删除商品
     * @param skuId
     */
    public void deleteCart(String skuId) {
        //从线程中拿到用户信息
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        String key=CART_PRE+userInfo.getId();
        BoundHashOperations<String, Object, Object> ops = template.boundHashOps(key);
        ops.delete(skuId);
    }
}
