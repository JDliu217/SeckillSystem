package cn.net.susan.seckill.api.controller;

import cn.net.susan.entity.seckill.ESSeckillProductEntity;
import cn.net.susan.seckill.business.context.UserContext;
import cn.net.susan.seckill.business.entity.seckill.SeckillOrderTradeEntity;
import cn.net.susan.seckill.business.mapper.seckill.SeckillOrderTradeMapper;
import cn.net.susan.seckill.business.service.SeckillOrderTradeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    /**
     * 支付回调
     *
     * <p>只把待支付的订单改成已支付；如果订单已经被超时任务关掉，这里改不动，
     * 按影响行数 0 静默返回，不抛异常——支付网关收到成功也会重试，重复回调无害。</p>
     *
     * @param orderId 秒杀订单ID
     */
    @ApiOperation(notes = "支付回调", value = "支付回调")
    @PostMapping("/payCallback")
    public void payCallback(@RequestParam("orderId") Long orderId) {
        seckillOrderTradeService.markPaid(orderId);
    }
}
