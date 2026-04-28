package cn.net.susan.seckill.api.controller;

import cn.net.susan.entity.auth.AuthUserEntity;
import cn.net.susan.entity.auth.CaptchaEntity;
import cn.net.susan.entity.auth.TokenEntity;
import cn.net.susan.seckill.business.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;

/**
 * 登录 controller
 *
 */
@Api(tags = "登录操作", description = "登录操作")
@RestController
@RequestMapping("")
@Validated
public class LoginController {

    @Autowired
    private UserService userService;

    /**
     * 用户登录
     *
     * @param authUserEntity 用户实体
     * @return 影响行数
     */
    @ApiOperation(notes = "用户登录", value = "用户登录")
    @PostMapping("/doLogin")
    public TokenEntity doLogin(@Valid @RequestBody AuthUserEntity authUserEntity) {
        return userService.login(authUserEntity);
    }

    /**
     * 访问登录页面
     * @return
     */
    @ApiOperation(notes = "访问登录页面", value = "访问登录页面")
    @GetMapping("/login")
    @ResponseBody
    public ModelAndView login() {
        return new ModelAndView("login");
    }

    /**
     * 获取验证码
     * @return
     */
    @ApiOperation("获取验证码")
    @GetMapping(value = "/code")
    public CaptchaEntity getCode() {
        return userService.getCode();
    }
}
