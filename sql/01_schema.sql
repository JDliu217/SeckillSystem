create database seckill;
use seckill;

DROP TABLE IF EXISTS `seckill_product`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `seckill_product` (
  `id` bigint NOT NULL COMMENT 'ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `with_hold_quantity` int NOT NULL COMMENT '预扣库存',
  `remain_quantity` int NOT NULL COMMENT '实际剩余库存',
  `price` decimal(10,2) NOT NULL COMMENT '秒杀价格',
  `create_user_id` bigint NOT NULL COMMENT '创建人ID',
  `create_user_name` varchar(30) NOT NULL COMMENT '创建人名称',
  `create_time` datetime(3) NOT NULL COMMENT '创建日期',
  `update_user_id` bigint DEFAULT NULL COMMENT '修改人ID',
  `update_user_name` varchar(30) DEFAULT NULL COMMENT '修改人名称',
  `update_time` datetime(3) DEFAULT NULL COMMENT '修改时间',
  `is_del` tinyint(1) DEFAULT '0' COMMENT '是否删除 1：已删除 0：未删除',
  `start_time` datetime(3) NOT NULL COMMENT '秒杀开始时间',
  `end_time` datetime(3) NOT NULL COMMENT '秒杀结束时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='秒杀商品表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `seckill_product`
--

LOCK TABLES `seckill_product` WRITE;
/*!40000 ALTER TABLE `seckill_product` DISABLE KEYS */;
-- 演示数据：秒杀时间窗口使用相对当前时间的表达式，导入后 4 件商品立即处于「进行中」状态，
-- 无需手动改时间即可演示。（create_time 为 3~7 天前，start_time 为 1 小时前，end_time 为 1~3 天后）
INSERT INTO `seckill_product` VALUES
(1810188012633329664,1809866827210194944,30,30,79.00,13,'admin',DATE_ADD(NOW(), INTERVAL -7 DAY),13,'admin',DATE_ADD(NOW(), INTERVAL -6 DAY),0,DATE_ADD(NOW(), INTERVAL -1 HOUR),DATE_ADD(NOW(), INTERVAL 2 DAY)),
(1810193536536535044,1810193364796563456,25,25,2999.00,13,'admin',DATE_ADD(NOW(), INTERVAL -7 DAY),13,'admin',DATE_ADD(NOW(), INTERVAL -5 DAY),0,DATE_ADD(NOW(), INTERVAL -1 HOUR),DATE_ADD(NOW(), INTERVAL 2 DAY)),
(1810241124927193089,1792868154511069184,10,10,50.00,13,'admin',DATE_ADD(NOW(), INTERVAL -7 DAY),NULL,NULL,NULL,0,DATE_ADD(NOW(), INTERVAL -1 HOUR),DATE_ADD(NOW(), INTERVAL 3 DAY)),
(1813577212945072128,1809866827210194944,100,100,99.00,13,'admin',DATE_ADD(NOW(), INTERVAL -3 DAY),NULL,NULL,NULL,0,DATE_ADD(NOW(), INTERVAL -1 HOUR),DATE_ADD(NOW(), INTERVAL 1 DAY));
/*!40000 ALTER TABLE `seckill_product` ENABLE KEYS */;
UNLOCK TABLES;
