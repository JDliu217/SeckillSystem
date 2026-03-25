package cn.net.susan.seckill.business.filter;

import cn.net.susan.exception.BusinessException;
import cn.net.susan.helper.UserTokenHelper;
import cn.net.susan.seckill.business.context.UserContext;
import cn.net.susan.seckill.business.entity.user.UserEntity;
import cn.net.susan.util.AssertUtil;
import cn.net.susan.util.RedisUtil;
import cn.net.susan.util.SpringUtil;
import cn.net.susan.util.TokenUtil;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * token过滤器
 *
 */
@Slf4j
public class JwtTokenFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        String token = TokenUtil.getTokenForAuthorization(httpServletRequest);

        if (!StringUtils.hasLength(token)) {
            filterChain.doFilter(httpServletRequest, servletResponse);
            return;
        }

        UserTokenHelper userTokenHelper = SpringUtil.getBean(UserTokenHelper.class);
        RedisUtil redisUtil = SpringUtil.getBean(RedisUtil.class);
        String usernameFromToken = userTokenHelper.getUsernameFromToken(token);
        if(!StringUtils.hasLength(usernameFromToken)) {
            throw new BusinessException(HttpStatus.FORBIDDEN.value(), "token不存在或者已过期");
        }

        String redisToken = redisUtil.get(userTokenHelper.getTokenKey(usernameFromToken));
        if(!StringUtils.hasLength(redisToken)) {
            throw new BusinessException(HttpStatus.FORBIDDEN.value(), "请先登录");
        }

        redisToken = redisToken.replace("\"", "").trim();
        if(!token.equals(redisToken)) {
            throw new BusinessException(HttpStatus.FORBIDDEN.value(), "token错误");
        }

        String userJson = redisUtil.get(userTokenHelper.getUserKey(usernameFromToken));
        UserEntity userEntity = parseUserEntity(userJson);

        try {
            UserContext.setCurrentUser(userEntity);
            filterChain.doFilter(httpServletRequest, servletResponse);
        } finally {
            UserContext.remove();
        }
    }

    private static String parseUserJson(String userJson) {
        //去掉多余的双引号
        return userJson.substring(1).substring(0, userJson.length() - 1);
    }

    public static UserEntity parseUserEntity(String userJson) {
        String json = userJson;
        if(userJson.contains("\\")) {
            json = parseUserJson(json);
            json = StringEscapeUtils.unescapeJava(json);
            json = json.substring(0, json.length() - 1);
        }
        return JSON.parseObject(json, UserEntity.class);
    }
}
