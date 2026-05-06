package cn.net.susan.seckill.api.controller;

import cn.net.susan.entity.seckill.ESSeckillProductEntity;
import cn.net.susan.seckill.business.context.UserContext;
import cn.net.susan.seckill.business.entity.seckill.SeckillOrderTradeEntity;
import cn.net.susan.seckill.business.mapper.seckill.SeckillOrderTradeMapper;
import cn.net.susan.seckill.business.service.SeckillOrderTradeService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

/**
 * 订单操作
 *
 */
@Api(tags = "订单操作", description = "订单操作")
@RestController
@RequestMapping("")
@Validated
public class OrderController {

    @Autowired
    private SeckillOrderTradeService seckillOrderTradeService;


    /**
     * 跳转到秒杀订单详情页面
     *
     * @param id 秒杀订单ID
     * @return 秒杀订单详情页面
     */
    @GetMapping("/orderDetail/{id}")
    public ModelAndView goToOrderDetail(@PathVariable("id") Long id) {
        ModelAndView modelAndView = new ModelAndView("order_detail");
        modelAndView.addObject("userInfo", UserContext.getCurrentUser());
        SeckillOrderTradeEntity orderDetail = seckillOrderTradeService.getOrderDetail(id);
        modelAndView.addObject("orderDetail", orderDetail);
        return modelAndView;
    }
}
