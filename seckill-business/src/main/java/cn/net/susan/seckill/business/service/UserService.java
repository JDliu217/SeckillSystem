package cn.net.susan.seckill.business.service;

import cn.hutool.core.util.IdUtil;
import cn.net.susan.entity.auth.AuthUserEntity;
import cn.net.susan.entity.auth.CaptchaEntity;
import cn.net.susan.entity.auth.TokenEntity;
import cn.net.susan.exception.BusinessException;
import cn.net.susan.helper.UserTokenHelper;
import cn.net.susan.util.AssertUtil;
import cn.net.susan.util.PasswordUtil;
import cn.net.susan.util.RedisUtil;
import com.alibaba.fastjson.JSON;
import com.wf.captcha.ArithmeticCaptcha;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import static cn.net.susan.seckill.business.filter.JwtTokenFilter.parseUserEntity;
import static cn.net.susan.util.AssertUtil.ASSERT_ERROR_CODE;

/**
 * 用户Service
 *
 */
@Slf4j
@Service
public class UserService {

    private static final String REGISTER_USER_PREFIX = "registerUser:";
    private static final String CAPTCHA_PREFIX = "captcha:";
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private PasswordUtil passwordUtil;
    @Autowired
    private UserTokenHelper userTokenHelper;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Value("${seckill.api.captchaExpireSecond:60}")
    private int captchaExpireSecond;

    /**
     * 校验验证码是否正确
     *
     * @param uuid uuid
     * @param code 验证码
     */
    public void checkCode(String uuid, String code) {
        AssertUtil.isTrue(StringUtils.hasLength(uuid), "uuid不能为空");
        AssertUtil.isTrue(StringUtils.hasLength(code), "code不能为空");
        String redisCode = redisUtil.get(getCaptchaKey(uuid));
        AssertUtil.hasLength(redisCode, "验证码不正确，请换一个验证码");
        AssertUtil.isTrue(redisCode.trim().equals(code.trim()), "验证码错误");
    }

    /**
     * 删除缓存中的验证码
     *
     * @param uuid uuid
     */
    public void deleteCode(String uuid) {
        redisUtil.del(getCaptchaKey(uuid));
    }

    /**
     * 用户登录接口
     *
     * @param authUserEntity 用户信息
     * @return token信息
     */
    public TokenEntity login(AuthUserEntity authUserEntity) {
        checkCode(authUserEntity.getUuid(), authUserEntity.getCode());

        try {
            String username = authUserEntity.getUsername();
            String decodePassword = passwordUtil.decodeRsaPassword(authUserEntity);
            String json = redisUtil.get(REGISTER_USER_PREFIX + username);
            if (!StringUtils.hasLength(json)) {
                throw new BusinessException(ASSERT_ERROR_CODE, "该用户不存在");
            }
            AuthUserEntity userEntity = parseUserEntity(json);
            if (!passwordEncoder.matches(decodePassword, userEntity.getPassword())) {
                throw new BusinessException(ASSERT_ERROR_CODE, "密码错误");
            }
            String token = userTokenHelper.generateToken(authUserEntity.getUsername(), JSON.toJSONString(authUserEntity));
            redisUtil.del(getCaptchaKey(authUserEntity.getUuid()));
            return new TokenEntity(authUserEntity.getUsername(), token, Lists.newArrayList());
        } catch (Exception e) {
            log.info("登录失败：", e);
            if (e instanceof BusinessException) {
                throw e;
            }
            throw new BusinessException(ASSERT_ERROR_CODE, "用户名或密码错误");
        }
    }

    /**
     * 获取验证码
     *
     * @return
     */
    public CaptchaEntity getCode() {
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(111, 36);
        // 几位数运算，默认是两位
        captcha.setLen(2);
        // 获取运算的结果
        String result = "";
        try {
            result = new Double(Double.parseDouble(captcha.text())).intValue() + "";
        } catch (Exception e) {
            result = captcha.text();
        }
        String uuid = "C" + IdUtil.simpleUUID();
        // 保存验证码到Redis中
        redisUtil.set(getCaptchaKey(uuid), result, captchaExpireSecond);
        return new CaptchaEntity(uuid, captcha.toBase64());
    }


    private String getCaptchaKey(String uuid) {
        return String.format("%s%s", CAPTCHA_PREFIX, uuid);
    }

}
