# 商城简介

* 项目由业务集群系统+后台管理系统构成，打通了分布式开发及全栈开发技能，包含前后分离全栈开发、Restful接口、数据校验、网关、注册发现、配置中心、熔断、限流、降级、链路追踪、性能监控、压力测试、系统预警、集群部署、持续集成、持续部署…
* 一个完整的电商项目，采用现阶段流行的技术来实现，是一个非常好的练手项目，工作中或平时积累的技术的一次完美融合
* 持续更新中

## 分布式基础_全栈开发篇

* 使用SpringBoot+Vue+element-ui+逆向工程搭建全套后台管理系统，基于Docker环境，通过前后分离方式，以商品系统为例，进行全栈开发

## 分布式高级_微服务架构篇

* 开发整个商城系统，掌握微服务的全套方案。使用SpringBoot+SpringCloud并配套SpringCloud Alibaba系列，引入全套微服务治理方案：Nacos注册中心/配置中心、Sentinel流量保护系统、Seata分布式事务&RabbitMQ柔性事务方案、SpringCloud-Gateway网关、Feign远程调用、Sleuth+Zipkin链路追踪系统、Spring Cache缓存、SpringSession跨子域Session同步方案、基于ElasticSearch7全文检索、异步编排与线程池、压力测试调优、Redisson分布式锁、分布式信号量等

## 高可用集群_架构提升篇

* 基于kubernetes集群，整合kubesphere可视化界面，搭建全套系统环境。使用集群化部署，包括Redis Cluster集群，MySQL主从与分库分表(使用ShardingSphere完成)集群，RabbitMQ镜像队列集群，ElasticSearch高可用集群。基于kubesphere整合Jenkins全可视化CICD，全套Pipeline流水线编写，参数化构建+手动确认模式保证



## 组织架构

### 项目组织

```
touch-air-mall
├── doc 项目相关资料
	├── img -- 图片样例
	├── markdown -- 项目笔记
	├── nacos -- nacos相关配置导出
	├── nginx -- nginx相关配置
	├── sql -- 数据库文件
	├── tool -- 开发常用工具
├── mall-common -- 工具类及通用代码
├── mall-coupon -- 优惠卷服务
├── mall-gateway -- 统一配置网关
├── mall-order -- 订单服务
├── mall-product -- 商品服务
├── mall-search -- 检索服务
├── mall-third-part -- 第三方服务
├── mall-ware -- 仓储服务
├── mall-member -- 会员服务
├── renren-generator -- 人人开源项目的代码生成器
└── renren-fast -- 人人admin管理后台
```

## 部分功能演示

### 商品服务

#### 三级分类

* 树形展示

* 树形拖拽保存

  ![](https://img2020.cnblogs.com/blog/1875400/202101/1875400-20210118153441497-1034412913.gif)



#### 品牌管理

* 整合oss

  ![](https://img2020.cnblogs.com/blog/1875400/202101/1875400-20210118153515829-57897371.gif)

#### 属性分组

* 父子组件交互

* 级联选择器

  ![](https://img2020.cnblogs.com/blog/1875400/202101/1875400-20210118153438343-1604661780.gif)



#### 商品新增发布

* 新增商品，保存spu、sku等基本信息，发布商品，数据同步到Es中，后续检索服务使用

  ![](https://img2020.cnblogs.com/blog/1875400/202101/1875400-20210118153522541-1748463792.gif)

  查看ES中是否成功写入

  ![image-20210118145913086](https://img2020.cnblogs.com/blog/1875400/202101/1875400-20210118153435945-1014183994.png)



### 商城业务

#### 商城首页

* nginx 域名访问--负载均衡到网关

* 动静分离--静态资源直接存放在nginx中

* 缓存--优化三级分类获取

  ![](https://img2020.cnblogs.com/blog/1875400/202101/1875400-20210118153531924-1052346150.gif)

#### 商城检索

* 两种方式进入

  * 第一种：进入商城首页 点击分类--手机
  * 第二种：浏览器输入地址：http://search.mall.com/list.html

* 检索条件

  * 1、全文检索：skuTitle -> keyword
  * 2、排序：saleCount（销量）、hotScore（热度评分）、skuPrice（价格）
  * 3、过滤：hasStock（是否有货）、skuPrice（价格区间）、brandId（品牌id 可以多选）、catalogId
  * 4、聚合：attrs（属性）

  * 5、面包屑导航
  * 6、条件删除&条件筛选联动

  ![](https://img2020.cnblogs.com/blog/1875400/202101/1875400-20210118153537549-895122574.gif)



## 技术选型

### 后端技术

| 技术               | 说明         | 官网                                                  |
| ------------------ | ------------ | ----------------------------------------------------- |
| SpringBoot         | 容器+MVC框架 | https://spring.io/projects/spring-boot                |
| SpringCloud        | 微服务架构   | https://spring.io/projects/spring-cloud               |
| SpringCloudAlibaba | 一系列组件   | https://spring.io/projects/spring-cloud-alibaba       |
| MyBatis-Plus       | ORM框架      | [https://mp.baomidou.com](https://mp.baomidou.com/)   |
| Elasticsearch      | 搜索引擎     | https://github.com/elastic/elasticsearch              |
| RabbitMQ           | 消息队列     | [https://www.rabbitmq.com](https://www.rabbitmq.com/) |
| Springsession      | 分布式缓存   | https://projects.spring.io/spring-session             |
| Redisson           | 分布式锁     | https://github.com/redisson/redisson                  |
| Docker             | 应用容器引擎 | [https://www.docker.com](https://www.docker.com/)     |
| OSS                | 对象云存储   | https://github.com/aliyun/aliyun-oss-java-sdk         |

## 架构图

### 系统架构图

![image-20210115085713740](https://img2020.cnblogs.com/blog/1875400/202101/1875400-20210115090846775-1599985037.png)

### 业务架构图

![image-20210115085746142](https://img2020.cnblogs.com/blog/1875400/202101/1875400-20210115090843187-1478313045.png)