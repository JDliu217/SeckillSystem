package cn.net.susan.seckill.business.entity.product;

import cn.net.susan.entity.RequestConditionEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 */
@ApiModel("秒杀商品查询条件实体")
@Data
public class ESSeckillProductConditionEntity extends RequestConditionEntity {

    /**
     * ID
     */
    @ApiModelProperty("ID")
    private Long id;

    /**
     * 商品ID
     */
    @ApiModelProperty("商品ID")
    private Long productId;

    /**
     * 商品名称
     */
    @ApiModelProperty("商品名称")
    private String name;

    /**
     * 预扣库存
     */
    @ApiModelProperty("预扣库存")
    private Integer withHoldQuantity;

    /**
     * 实际剩余库存
     */
    @ApiModelProperty("实际剩余库存")
    private Integer remainQuantity;

    /**
     * 秒杀价格
     */
    @ApiModelProperty("秒杀价格")
    private BigDecimal price;

    /**
     * 秒杀开始时间
     */
    @ApiModelProperty("秒杀开始时间")
    private Date startTime;

    /**
     * 秒杀结束时间
     */
    @ApiModelProperty("秒杀结束时间")
    private Date endTime;
}
