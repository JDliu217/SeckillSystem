package cn.net.susan.seckill.business.service;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.net.susan.entity.ResponsePageEntity;
import cn.net.susan.entity.seckill.ESSeckillProductEntity;
import cn.net.susan.entity.seckill.SeckillProductDetailEntity;
import cn.net.susan.exception.BusinessException;
import cn.net.susan.seckill.business.entity.product.ESSeckillProductConditionEntity;
import cn.net.susan.seckill.business.entity.seckill.SeckillProductDetailPageEntity;
import cn.net.susan.seckill.business.entity.user.UserEntity;
import cn.net.susan.seckill.business.util.JsonUtil;
import cn.net.susan.util.RedisUtil;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static cn.net.susan.constant.KeyConstant.SECKILL_PRODUCT_DETAIL_PFREFIX;
import static cn.net.susan.seckill.business.util.BusinessKeyUtil.getProductStockKey;

/**
 * 秒杀商品
 *
 */
@Slf4j
@Service
public class ProductService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Value("${seckill.api.seckillProductEsIndex:seckill-product-es-index-v1}")
    private String seckillProductEsIndex;
    @Autowired
    private RedisUtil redisUtil;

    /**
     * 搜索秒杀商品
     *
     * @param seckillProductConditionEntity 查询条件
     * @return 秒杀商品
     */
    public ResponsePageEntity<ESSeckillProductEntity> searchProductList(ESSeckillProductConditionEntity seckillProductConditionEntity) {
        try {
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.from(seckillProductConditionEntity.getPageBegin());
            searchSourceBuilder.size(seckillProductConditionEntity.getPageSize());
            if (StringUtils.hasLength(seckillProductConditionEntity.getName())) {
                searchSourceBuilder.query(QueryBuilders.matchQuery("name", seckillProductConditionEntity.getName()));
            }
            log.info("searchFromES请求参数:", searchSourceBuilder);
            ResponsePageEntity responsePageEntity = ResponsePageEntity.buildEmpty(seckillProductConditionEntity);
            List<ESSeckillProductEntity> productEntities = search(seckillProductEsIndex, searchSourceBuilder,
                    ESSeckillProductEntity.class, responsePageEntity);
            if (CollectionUtils.isEmpty(productEntities)) {
                return ResponsePageEntity.buildEmpty(seckillProductConditionEntity);
            }
            return ResponsePageEntity.build(seckillProductConditionEntity, responsePageEntity.getTotalCount(), productEntities);
        } catch (IOException e) {
            log.error("从ES中查询商品失败，原因：", e);
            return ResponsePageEntity.buildEmpty(seckillProductConditionEntity);
        }
    }

    /**
     * 获取商品信息
     *
     * @param id 秒杀商品ID
     * @return 商品信息
     */
    public SeckillProductDetailPageEntity getProductInfo(Long id) {
        String json = redisUtil.get(getKey(id.toString()));
        return JsonUtil.parseRedisEntity(json, SeckillProductDetailPageEntity.class);
    }

    /**
     * 获取秒杀商品详情
     *
     * @param id 秒杀商品ID
     * @return 商品详情
     */
    public SeckillProductDetailPageEntity getProductDetail(Long id) {
        SeckillProductDetailPageEntity seckillProductDetailEntity = getProductInfo(id);
        String stockValue = redisUtil.get(getProductStockKey(id));
        seckillProductDetailEntity.setWithHoldQuantity(Integer.parseInt(stockValue));
        calcSecondAndStatus(seckillProductDetailEntity);
        return seckillProductDetailEntity;
    }

    private void calcSecondAndStatus(SeckillProductDetailPageEntity seckillProductDetailEntity) {
        Date startTime = seckillProductDetailEntity.getStartTime();
        Date endTime = seckillProductDetailEntity.getEndTime();
        Date nowTime = new Date();

        //如果开始时间大于当前时间，说明秒杀还未开始
        if (startTime.after(nowTime)) {
            long betweenSecond = DateUtil.between(startTime, nowTime, DateUnit.SECOND);
            //秒杀前60秒开始倒计时
            if (betweenSecond <= 60) {
                seckillProductDetailEntity.setSeckillStatus(1);
                seckillProductDetailEntity.setRemainSeconds((int) betweenSecond);
            } else {
                seckillProductDetailEntity.setSeckillStatus(0);
            }
        } else {
            if (endTime.after(nowTime)) {
                seckillProductDetailEntity.setSeckillStatus(2);
            } else {
                seckillProductDetailEntity.setSeckillStatus(3);
            }
        }
    }

    private String getKey(String id) {
        return String.format("%s%s", SECKILL_PRODUCT_DETAIL_PFREFIX, id);
    }

    /**
     * 查询数据
     *
     * @param idxName            index
     * @param builder            查询参数
     * @param aClass             结果类对象
     * @param responsePageEntity 总记录数
     * @return java.util.List<T>
     */
    private <T> List<T> search(String idxName, SearchSourceBuilder builder, Class<T> aClass, ResponsePageEntity responsePageEntity) throws IOException {
        SearchRequest request = new SearchRequest(idxName);
        request.source(builder);
        SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        SearchHit[] hits = response.getHits().getHits();
        int total = (int) response.getHits().getTotalHits().value;
        responsePageEntity.setTotalCount(total);
        return Arrays.stream(hits).map(hit -> JSON.parseObject(hit.getSourceAsString(), aClass)).collect(Collectors.toList());
    }

    /**
     * 修改ES中商品库存
     *
     * @param id    秒杀商品ID
     * @param stock 库存
     * @throws IOException 异常
     */
    public void updateEsProductStock(Long id, Integer stock) {
        try {
            //构建修改请求
            UpdateRequest request = new UpdateRequest(seckillProductEsIndex, id.toString());
            request.doc("withHoldQuantity", stock);
            restHighLevelClient.update(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.info("updateEsProductStock失败：", e);
            throw new BusinessException("updateEsProductStock失败");
        }
    }
}
