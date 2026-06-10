package cn.net.susan.seckill.business.service;

import cn.net.susan.entity.seckill.UserSeckillProductTradeEntity;
import cn.net.susan.exception.BusinessException;
import cn.net.susan.helper.IdGenerateHelper;
import cn.net.susan.seckill.business.config.BusinessConfig;
import cn.net.susan.seckill.business.config.properties.OrderApiProperties;
import cn.net.susan.seckill.business.context.UserContext;
import cn.net.susan.seckill.business.entity.seckill.*;
import cn.net.susan.seckill.business.entity.user.UserEntity;
import cn.net.susan.seckill.business.mapper.seckill.SeckillOrderTradeMapper;
import cn.net.susan.seckill.business.mapper.seckill.SeckillProductMapper;
import cn.net.susan.seckill.business.util.HttpUtil;
import cn.net.susan.util.ApiResult;
import cn.net.susan.util.AssertUtil;
import cn.net.susan.util.FillUserUtil;
import cn.net.susan.util.OrderCodeUtil;
import cn.net.susan.util.RedisUtil;
import cn.net.susan.util.SignUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static cn.net.susan.seckill.business.util.BusinessKeyUtil.getProductOverKey;
import static cn.net.susan.seckill.business.util.BusinessKeyUtil.getProductStockKey;
import static cn.net.susan.seckill.business.util.BusinessKeyUtil.getUserSeckillProductKey;
import static cn.net.susan.seckill.business.util.ReTryUtil.retry;

/**
 * 秒杀商品 service
 *
 */
@Slf4j
@Service
public class UserSeckillProductService {

    private static final Map<Long, Boolean> LOCAL_LOW_IN_STOCK_MAP = new ConcurrentHashMap<>();
    private static final String TRUE_STRING = "true";

    @Autowired
    private UserService userService;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private SeckillProductMapper seckillProductMapper;
    @Autowired
    private ProductService productService;
    @Autowired
    private HttpUtil httpUtil;
    @Autowired
    private BusinessConfig businessConfig;
    @Autowired
    private SeckillOrderTradeMapper seckillOrderTradeMapper;
    @Autowired
    private IdGenerateHelper idGenerateHelper;
    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Value("${seckill.api.asyncCallApi:false}")
    private boolean asyncCallApi;

    @Value("${seckill.seckillProductTopic:SECKILL_PRODUCT_TOPIC}")
    private String seckillProductTopic;

    @Value("${seckill.orderTimeoutTopic:seckill.order.timeout.topic}")
    private String orderTimeoutTopic;

    /**
     * RocketMQ 延迟等级，默认 14 对应 10 分钟
     */
    @Value("${seckill.orderTimeoutDelayLevel:14}")
    private int orderTimeoutDelayLevel;

    /**
     * 用户秒杀商品方法
     *
     * @param userSeckillProductEntity 用户秒杀商品实体
     */
    public void doSeckillProduct(UserSeckillProductEntity userSeckillProductEntity) {
        try {
            //校验用户是否登录
            UserEntity currentUser = UserContext.getCurrentUser();
            if (Objects.isNull(currentUser)) {
                throw new BusinessException(HttpStatus.FORBIDDEN.value(), "请先登录");
            }
            //校验验证码
            userService.checkCode(userSeckillProductEntity.getUuid(), userSeckillProductEntity.getCode());
            //校验秒杀开始时间和结束时间
            checkTime(userSeckillProductEntity);

            //从本地缓存中校验库存
            Long seckillProductId = userSeckillProductEntity.getSeckillProductId();
            if (BooleanUtils.isTrue(LOCAL_LOW_IN_STOCK_MAP.get(seckillProductId))) {
                throw new BusinessException("该商品已售罄");
            }

            String productStockKey = getProductStockKey(seckillProductId);
            //检查商品是否存在
            String productValue = redisUtil.get(productStockKey);
            if (!StringUtils.hasLength(productValue)) {
                throw new BusinessException("该商品不存在");
            }

            //查询该用户是否秒杀过
            String userSeckillRecord = redisUtil.get(getUserSeckillProductKey(seckillProductId, currentUser.getUsername()));
            if (StringUtils.hasLength(userSeckillRecord)) {
                throw new BusinessException("您已经秒杀过该商品了，请勿重复秒杀");
            }

            //检查Redis中的售罄状态，防止部署到分布式环境中售罄状态的问题
            String overFlag = redisUtil.get(getProductOverKey(seckillProductId));
            if (TRUE_STRING.equals(overFlag)) {
                LOCAL_LOW_IN_STOCK_MAP.put(seckillProductId, true);
                throw new BusinessException("该商品已售罄");
            }

            //预扣减库存
            Long stock = redisUtil.decrement(productStockKey);
            //校验库存是否不足
            if (stock < 0) {
                LOCAL_LOW_IN_STOCK_MAP.put(seckillProductId, true);
                throw new BusinessException("该商品已售罄");
            }

            //发MQ消息真正扣减库存和下单
            userSeckillProductEntity.setUserName(currentUser.getUsername());
            send(seckillProductTopic, userSeckillProductEntity);
        } finally {
            userService.deleteCode(userSeckillProductEntity.getUuid());
        }
    }

    private void checkTime(UserSeckillProductEntity userSeckillProductEntity) {
        SeckillProductDetailPageEntity productInfo = productService.getProductInfo(userSeckillProductEntity.getSeckillProductId());
        AssertUtil.notNull(productInfo, "该秒杀商品不存在");

        Date nowTime = new Date();
        AssertUtil.isTrue(!productInfo.getStartTime().after(nowTime), "该商品的秒杀活动还未开始");
        AssertUtil.isTrue(productInfo.getEndTime().after(nowTime), "该商品的秒杀活动已结束");
    }

    public SeckillOrderTradeEntity getResult(UserSeckillProductEntity userSeckillProductEntity) {
        //校验用户是否登录
        UserEntity currentUser = UserContext.getCurrentUser();
        if (Objects.isNull(currentUser)) {
            throw new BusinessException(HttpStatus.FORBIDDEN.value(), "请先登录");
        }
        String userSeckillProductKey = getUserSeckillProductKey(userSeckillProductEntity.getSeckillProductId(),
                currentUser.getUsername());
        String orderId = redisUtil.get(userSeckillProductKey);
        SeckillOrderTradeEntity seckillOrderTradeEntity = new SeckillOrderTradeEntity();
        if (StringUtils.hasLength(orderId)) {
            seckillOrderTradeEntity.setId(Long.parseLong(orderId));
        }
        return seckillOrderTradeEntity;
    }

    /**
     * 创建订单
     *
     * @param userSeckillProductEntity 用户秒杀商品实体
     */
    public void createOrder(UserSeckillProductEntity userSeckillProductEntity) {
        Long seckillProductId = userSeckillProductEntity.getSeckillProductId();
        SeckillProductEntity seckillProduct = seckillProductMapper.findById(seckillProductId);
        AssertUtil.notNull(seckillProduct, "该秒杀商品在系统中不存在");

        Boolean success = reduceStock(seckillProductId);
        if (BooleanUtils.isTrue(success)) {
            retry(() -> {
                SeckillProductEntity seckillProductEntity = seckillProductMapper.findById(seckillProductId);
                productService.updateEsProductStock(seckillProductId, seckillProductEntity.getWithHoldQuantity());
                createOrder(seckillProductId, userSeckillProductEntity);
                return userSeckillProductEntity;
            });
        } else {
            retry(() -> redisUtil.set(getProductOverKey(seckillProductId), TRUE_STRING));
        }
    }


    private Boolean reduceStock(Long seckillProductId) {
        return seckillProductMapper.reduceWithHoldStock(seckillProductId) > 0;
    }

    private void createOrder(Long seckillProductId, UserSeckillProductEntity userSeckillProductEntity) {
        UserSeckillProductTradeEntity userSeckillProductTradeEntity = createUserSeckillProductOrderEntity(userSeckillProductEntity);
        String userSeckillProductKey = getUserSeckillProductKey(seckillProductId, userSeckillProductEntity.getUserName());
        SignUtil.makeSign(userSeckillProductTradeEntity, businessConfig.getOrderApiProperties().getSecretKey());
        UserSeckillProductTradeEntity resultEntity = httpUtil.doPost(getCreateOrderUrl(),
                userSeckillProductTradeEntity,
                new ParameterizedTypeReference<ApiResult<UserSeckillProductTradeEntity>>() {
                });

        SeckillOrderTradeConditionEntity seckillOrderTradeConditionEntity = new SeckillOrderTradeConditionEntity();
        seckillOrderTradeConditionEntity.setUserId(resultEntity.getUserId());
        seckillOrderTradeConditionEntity.setSeckillProductId(resultEntity.getSeckillProductId());
        List<SeckillOrderTradeEntity> seckillOrderTradeEntities = seckillOrderTradeMapper.searchByCondition(seckillOrderTradeConditionEntity);

        SeckillOrderTradeEntity seckillOrderTradeEntity;
        if (CollectionUtils.isEmpty(seckillOrderTradeEntities)) {
            try {
                seckillOrderTradeEntity = createSeckillOrderTradeEntity(resultEntity);
                seckillOrderTradeMapper.insert(seckillOrderTradeEntity);
            } finally {
                FillUserUtil.clearCurrentUser();
            }
        } else {
            seckillOrderTradeEntity = seckillOrderTradeEntities.get(0);
        }

        if (!redisUtil.set(userSeckillProductKey, seckillOrderTradeEntity.getId().toString())) {
            throw new BusinessException("创建秒杀订单失败");
        }

        // 订单号写成之后前端就能轮询到了，再投一条延迟消息，到期没支付就自动关单并回补库存
        sendOrderTimeoutMessage(seckillOrderTradeEntity.getId(), seckillProductId);
    }

    /**
     * 投递订单超时延迟消息
     *
     * <p>用同步发送，失败只记日志不抛：订单已经建好了，不能因为消息发不出去就让整条消费失败重投，
     * 那样会把库存再扣一次。没投出去的由扫表任务兜底。</p>
     *
     * @param orderId          秒杀订单ID
     * @param seckillProductId 秒杀商品ID
     */
    private void sendOrderTimeoutMessage(Long orderId, Long seckillProductId) {
        try {
            SeckillOrderTimeoutMessage message = new SeckillOrderTimeoutMessage(orderId, seckillProductId);
            rocketMQTemplate.syncSend(orderTimeoutTopic,
                    MessageBuilder.withPayload(message).build(), 3000L, orderTimeoutDelayLevel);
            log.info("订单超时消息已投递，orderId={}, delayLevel={}", orderId, orderTimeoutDelayLevel);
        } catch (Exception e) {
            log.error("订单超时消息投递失败，等待扫表兜底，orderId={}", orderId, e);
        }
    }


    private SeckillOrderTradeEntity createSeckillOrderTradeEntity(UserSeckillProductTradeEntity resultEntity) {
        SeckillOrderTradeEntity seckillOrderTradeEntity = new SeckillOrderTradeEntity();
        seckillOrderTradeEntity.setUserId(resultEntity.getUserId());
        seckillOrderTradeEntity.setUserName(resultEntity.getUserName());
        seckillOrderTradeEntity.setProductId(resultEntity.getProductId());
        seckillOrderTradeEntity.setProductName(resultEntity.getProductName());
        seckillOrderTradeEntity.setCode(resultEntity.getCode());
        seckillOrderTradeEntity.setTradeId(resultEntity.getTradeId());
        seckillOrderTradeEntity.setPaymentAmount(resultEntity.getPaymentAmount());
        seckillOrderTradeEntity.setTotalAmount(resultEntity.getTotalAmount());
        seckillOrderTradeEntity.setPrice(resultEntity.getPrice());
        seckillOrderTradeEntity.setCostPrice(resultEntity.getCostPrice());
        seckillOrderTradeEntity.setOrderStatus(1);
        seckillOrderTradeEntity.setPayStatus(1);
        seckillOrderTradeEntity.setQuantity(1);
        seckillOrderTradeEntity.setModel(resultEntity.getModel());
        seckillOrderTradeEntity.setOrderTime(resultEntity.getOrderTime());
        seckillOrderTradeEntity.setSeckillProductId(resultEntity.getSeckillProductId());
        seckillOrderTradeEntity.setCreateUserId(resultEntity.getUserId());
        seckillOrderTradeEntity.setCreateUserName(resultEntity.getUserName());
        seckillOrderTradeEntity.setCreateTime(new Date());
        seckillOrderTradeEntity.setId(idGenerateHelper.nextId());
        return seckillOrderTradeEntity;
    }

    private String getCreateOrderUrl() {
        OrderApiProperties orderApiProperties = businessConfig.getOrderApiProperties();
        return String.format("%s%s", orderApiProperties.getHost(), orderApiProperties.getCreateOrderUrl());
    }

    private UserSeckillProductTradeEntity createUserSeckillProductOrderEntity(UserSeckillProductEntity userSeckillProductEntity) {
        Long seckillProductId = userSeckillProductEntity.getSeckillProductId();
        UserSeckillProductTradeEntity userSeckillProductOrderEntity = new UserSeckillProductTradeEntity();
        userSeckillProductOrderEntity.setCode(OrderCodeUtil.generateOrderCode());
        userSeckillProductOrderEntity.setUserName(userSeckillProductEntity.getUserName());
        userSeckillProductOrderEntity.setSeckillProductId(seckillProductId);

        SeckillProductEntity seckillProductEntity = seckillProductMapper.findById(seckillProductId);
        AssertUtil.notNull(seckillProductEntity, String.format("秒杀商品[%s]不存在", seckillProductId));

        userSeckillProductOrderEntity.setProductId(seckillProductEntity.getProductId());
        userSeckillProductOrderEntity.setModel(seckillProductEntity.getModel());
        userSeckillProductOrderEntity.setProductName(seckillProductEntity.getName());
        userSeckillProductOrderEntity.setPrice(seckillProductEntity.getPrice());
        userSeckillProductOrderEntity.setQuantity(1);
        userSeckillProductOrderEntity.setTotalAmount(seckillProductEntity.getPrice());
        userSeckillProductOrderEntity.setPaymentAmount(seckillProductEntity.getPrice());
        userSeckillProductOrderEntity.setOrderTime(new Date());

        return userSeckillProductOrderEntity;
    }

    /**
     * 发生MQ消息
     *
     * @param topic   主题
     * @param message 消息
     */
    public void send(String topic, Object message) {
        try {
            rocketMQTemplate.asyncSend(topic, message, new SendCallback() {
                @Override
                public void onSuccess(SendResult sendResult) {
                    log.info("消息发送成功, topic:{},message:{}", topic, message);
                }

                @Override
                public void onException(Throwable throwable) {
                    log.error("消息发送失败, topic:{},throwable:{}", topic, throwable);
                }
            });

        } catch (Exception e) {
            log.error("消息发送失败，原因：", e);
        }
    }
}
