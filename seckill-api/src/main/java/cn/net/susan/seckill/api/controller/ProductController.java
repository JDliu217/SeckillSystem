package cn.net.susan.seckill.api.controller;

import cn.net.susan.entity.ResponsePageEntity;
import cn.net.susan.entity.seckill.ESSeckillProductEntity;
import cn.net.susan.seckill.business.context.UserContext;
import cn.net.susan.seckill.business.entity.product.ESSeckillProductConditionEntity;
import cn.net.susan.seckill.business.service.ProductService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

/**
 * 商品操作
 *
 */
@Api(tags = "商品操作", description = "商品操作")
@RestController
@RequestMapping("")
@Validated
public class ProductController {

    @Autowired
    private ProductService productService;

    /**
     * 访问商品列表页面
     */
    @GetMapping("/productList")
    public ModelAndView goToProductList() {
        return new ModelAndView("product_list");
    }

    /**
     * 分页查询秒杀商品列表
     */
    @PostMapping("/searchProductList")
    public ResponsePageEntity<ESSeckillProductEntity> searchProductList(@RequestBody ESSeckillProductConditionEntity seckillProductConditionEntity) {
        return productService.searchProductList(seckillProductConditionEntity);
    }

    /**
     * 根据ID访问秒杀商品详情页面
     *
     * @param id 秒杀商品ID
     */
    @GetMapping("/productDetail/{id}")
    public ModelAndView goToProductDetail(@PathVariable("id") Long id) {
        ModelAndView modelAndView = new ModelAndView("product_detail");
        modelAndView.addObject("userInfo", UserContext.getCurrentUser());
        ESSeckillProductEntity productDetail = productService.getProductDetail(id);
        modelAndView.addObject("productDetail", productDetail);
        return modelAndView;
    }
}
