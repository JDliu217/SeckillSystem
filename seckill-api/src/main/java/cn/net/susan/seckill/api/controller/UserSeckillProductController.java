package cn.net.susan.seckill.api.controller;

import cn.net.susan.seckill.business.entity.seckill.SeckillOrderTradeEntity;
import cn.net.susan.seckill.business.entity.seckill.UserSeckillProductEntity;
import cn.net.susan.seckill.business.service.UserSeckillProductService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 秒杀商品 controller
 *
 */
@Api(tags = "秒杀商品操作", description = "秒杀商品操作")
@RestController
@RequestMapping("")
@Validated
public class UserSeckillProductController {

    @Autowired
    private UserSeckillProductService userSeckillProductService;

    /**
     * 秒杀商品接口
     */
    @ApiOperation(notes = "秒杀商品接口", value = "秒杀商品接口")
    @PostMapping("/doSeckillProduct")
    public void doSeckillProduct(@RequestBody @Valid UserSeckillProductEntity userSeckillProductEntity) {
        userSeckillProductService.doSeckillProduct(userSeckillProductEntity);
    }

    /**
     * 查询秒杀结果
     */
    @ApiOperation(notes = "查询秒杀结果接口", value = "查询秒杀结果接口")
    @PostMapping("/getResult")
    public SeckillOrderTradeEntity getResult(@RequestBody @Valid UserSeckillProductEntity userSeckillProductEntity) {
        return userSeckillProductService.getResult(userSeckillProductEntity);
    }
}
