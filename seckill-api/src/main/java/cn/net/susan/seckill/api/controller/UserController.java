package cn.net.susan.seckill.api.controller;

import cn.net.susan.seckill.business.context.UserContext;
import cn.net.susan.seckill.business.entity.user.UserEntity;
import io.swagger.annotations.Api;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * 用户操作
 *
 */
@Api(tags = "用户操作", description = "用户操作")
@RestController
@RequestMapping("")
@Validated
public class UserController {

    /**
     * 获取当前登录的用户信息
     */
    @GetMapping("/getUserInfo")
    public UserEntity getUserInfo() {
        UserEntity currentUser = UserContext.getCurrentUser();
        if (Objects.nonNull(currentUser)) {
            currentUser.setPassword(null);
        }
        return currentUser;
    }

}