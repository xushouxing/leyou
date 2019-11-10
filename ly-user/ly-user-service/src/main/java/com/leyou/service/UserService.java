package com.leyou.service;

import com.leyou.User;
import com.leyou.common.exception.LyException;
import com.leyou.common.myenum.ExceptionEnum;
import com.leyou.common.utils.CodecUtils;
import com.leyou.common.utils.NumberUtils;
import com.leyou.mapper.UserMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private AmqpTemplate amqpTemplate;
    static final String KEY_PREFIX = "user:code:phone:";

    static final Logger logger = LoggerFactory.getLogger(UserService.class);
    public Boolean checkUserData(String data, Integer type) {
        User user=new User();
        switch (type){
            case 1:
               user.setUsername(data);
               break;
            case 2:
                user.setPhone(data);
                break;
            default:
                return null;
        }
        return this.userMapper.selectCount(user) == 0;
    }

    public void sendVerifyCode(String phone) {
            String code = NumberUtils.generateCode(6);
        try {
            Map<String,String> msg=new HashMap<>();
            msg.put("code",code);
            msg.put("phone",phone);
            amqpTemplate.convertAndSend("leyou.sms.exchange", "sms.verify.code", msg);
            stringRedisTemplate.opsForValue().set(KEY_PREFIX+phone,code,5,TimeUnit.MINUTES);
        } catch (AmqpException e) {
            logger.error("发送短信失败。phone：{}， code：{}", phone, code);
            throw new LyException(ExceptionEnum.CODE_SEND_FAIL);
        }
    }

    public void register(User user, String code) {
        String key=KEY_PREFIX + user.getPhone();
        String code1 = stringRedisTemplate.opsForValue().get(key);
        if(!StringUtils.equals(code1,code)){
            throw new LyException(ExceptionEnum.CODE_NOT_SAVE);
        }
        //获取盐
        String salt = CodecUtils.generateSalt();
        //密码加密
        String md5Hex = CodecUtils.md5Hex(user.getPassword(), salt);
        //创建时间
        Date date=new Date();
        user.setCreated(date);
        user.setPassword(md5Hex);
        user.setSalt(salt);
        user.setId(null);
        int i = userMapper.insertSelective(user);
        if (i==1){
            //注册成功删除key
            stringRedisTemplate.delete(key);
        }
    }

    public User queryUser(String username, String password) {
        // 查询
        User record = new User();
        record.setUsername(username);
        User user = this.userMapper.selectOne(record);
        // 校验用户名
        if (user == null) {
            return null;
        }
        // 校验密码
        if (!user.getPassword().equals(CodecUtils.md5Hex(password, user.getSalt()))) {
            return null;
        }
        // 用户名密码都正确
        return user;
    }
}
