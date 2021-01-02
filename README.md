# Touch-Air-Mall--分布式基础篇

* 微服务架构图

  ![image-20201209145750230](https://img2020.cnblogs.com/blog/1875400/202101/1875400-20210102142412765-2143147266.png)

* 项目描述

  * 前后分离开发，分为内网部署和外网部署，外网就是面向公众访问,部署前端项目，内网部署是整个后台的服务集群，公众是通过客户端完成相应功能，比如登录注册等需要通过客户端，向后台服务 发送请求
  * 完整的请求流程：通过任意客户端发请求来到Nginx集群，Nginx把请求转交给后台服务，先将请求服务交给Api网关，Api网关为SpringCloud GateWay，网关可以根据当前请求，动态路由到指定的服务，例如要调用商品服务，购物车服务，还是检索服务，如果路由过来后，某一个服务众多，网关会负载均衡的调用服务，当某些服务出现问题，会在网关级别对服务做统一的熔断或者降级，使用Spring Cloud alibaba提供的Sentinel，当然网关还有其他工能，如认证授权，是否合法，限流，限制瞬时流量，降级。当请求通过网关到达服务后，进行处理，都是Spring Boot的一个个微服务，服务与服务之间会相互调用，下订单时调用商品服务，有些请求需要登录以后才会处理，所以有一个基于OAuth 2的认证中心，OAuth 2的社交登录，整个应用的安全和权限控制用SpringSecurity来进行控制
  * 特别是这些服务要保存一些数据或缓存，缓存使用的是redis集群，分片集群加哨兵集群，持久化使用的是mysql集群。可以读写分离，或分库分表。服务与服务之间，利用消息队列进行异步解耦，完成分布式事务的最终一致性，RabbitMQ做消息队列，检索用ElasticSearch，有些服务运行期间，存取图片视频等，利用阿里云对象存储服务（OSS）。这些是整个服务关于数据存储的解决方案
  * 项目上线后，为了快速定位项目中可能出现的一些问题，使用ELK对日志进行处理，用LogStash收集业务里面各种日志，存储到ES中，用Kibana从ES中检索到日志信息，快速度定位线上问题所在
  * 在分布式系统中，每一个服务都可能部署在每一台机器，而且服务与服务之间要相互调用，就得知道彼此都在哪里，将所有服务注册到服务中心，别的服务可以通过注册中心发现其他服务的注册所在位置，使用Spring Cloud Alibaba Nacos来作为服务的注册中心，同样每一个服务配置众多，后来要集中管理这些配置，实现改一处配置，其他服务都要修改掉，使用Spring Cloud Alibaba Nacos来作为服务的配置中心，所有服务可以动态的从配置中心中获取配置，包括服务在调用期间可能出现的问题，比如下订单服务调用商品服务，商品服务调用库存服务，可能某一个链路出现问题，我们要追踪某一个调用链哪里出现问题，该怎么解决等等，使用Spring Cloud Sleuth＋Zipkin 把每一个服务的信息交给交给开源的Prometheus进行聚合分析，再由Grafana进行可视化展示，Altermananger实时得到服务的报警信息，以邮件和手机短信方式通知开发运维人员
  * 提供持续继承和持续部署，开发人员可以将修改后的代码提交给GitHub，运维人员通过自动化工具Jenkins Pipeline，从GitHub获取代码，将它打包成Docker镜像，最后通过Kuberneters 集成整个Docker服务，将服务以Docker容器的方式运行



## 前期准备

### docker安装mysql

* `conf/my.cnf`

  ```
  [mysqld]
  init_connect='SET collation_connection = utf8_unicode_ci'
  init_connect='SET NAMES utf8'
  character-set-server=utf8
  collation-server=utf8_unicode_ci
  skip-character-set-client-handshake
  skip-name-resolve
  
  [client]
  default-character-set=utf8
  
  [mysql]
  default-character-set=utf8
  
  ```

* 启动容器

  ```shell
  docker run --name mysql3306  -p 3306:3306  -v /var/touchAirMallVolume/mysql/data:/var/lib/mysql  -v /var/touchAirMallVolume/mysql/conf/my.cnf:/etc/mysql/my.cnf -e MYSQL_ROOT_PASSWORD=123456 -d mysql:5.7.31
  ```

  ```
  docker run --name mysql  -p 3306:3306 --restart=always -v /var/local/mysql/data:/var/lib/mysql  -v /var/local/mysql/conf/my.cnf:/etc/mysql/my.cnf -e MYSQL_ROOT_PASSWORD=123456 -d mysql:5.7.31
  
  docker run -p 6379:6379 --name redis  --restart=always \
  -v /var/local/redis/data:/data \
  -v /var/local/redis/conf/redis.conf:/etc/redis/redis.conf \
  -d redis redis-server /etc/redis/redis.conf
  
  ```

  

### docker安装redis

* `conf/redis.conf`

  
  
* 启动容器

  ```shell
  docker run -p 6379:6379 --name redis6379  \
  -v /var/touchAirMallVolume/redis/data:/data \
  -v /var/touchAirMallVolume/redis/conf/redis.conf:/etc/redis/redis.conf \
  -d redis redis-server /etc/redis/redis.conf
  ```

* 验证redis

  ```shell
  docker exec -it redis6379 redis-cli
  ```

  ![image-20201203164153643](https://img2020.cnblogs.com/blog/1875400/202101/1875400-20210102142415067-559124026.png)

* 默认redis是不持久化的，存在内存中

  修改`redis.conf` 开启持久化

  ```shell
  vim /var/touchAirMallVolume/redis/conf/redis.conf
  
  #添加以下内容
  appendonly yes
  ```

  重启redis容器

## 项目快速搭建

* 使用人人开源项目，快速搭建前后端分离项目

  [人人开源项目地址](https://gitee.com/renrenio)

  `renren-fast`、`renren-fast-vue`、`renren-generator`

* 使用逆向工程，快速生成基本CRUD代码

  ![image-20201209150125172](https://img2020.cnblogs.com/blog/1875400/202101/1875400-20210102142415308-745288054.png)

## 分布式组件

### SpringCloud Alibaba

#### 简介

* Spring Cloud alibaba 为分布式应用开发提供了一站式解决方案。它包含开发分布式应用程序所需的所有组件，使您可以轻松地使用 Spring Cloud 开发应用程序

  使用阿里巴巴的 Spring Cloud，你只需要添加一些注释和少量配置，就可以将 Spring Cloud 应用程序连接到阿里巴巴的分布式解决方案上，并使用阿里巴巴的中间件构建一个分布式应用系统

#### 特性

* `Flow control and service degradation`（流量控制和服务降级）
* `Service registration and discovery`（服务注册和发现）
* `Distributed Configuration`（分布式配置）
* `Event-driven`（事件驱动）
* `Message Bus`（消息总线）：使用 Spring Cloud Bus RocketMQ 的分布式系统的链接节点
* `Distributed Transaction`（分布式事务 Seata）
* `Dubbo RPC`

#### 优劣势比较

* SpringCloud的几大痛点：
  * SpringCloud部分组件停止维护和更新（Eureka、Hystrix等）
  * SpringCloud部分环境搭建复杂，没有完善的可视化界面，需要大量的二次开发和定制
  * SpringCloud配置复杂、难以上手，部分配置差别难以区分和合理应用
* SpringCloud Alibaba的优势：
  * 阿里使用过的组件经历了考验，性能强悍，设计合理，现在开源出来大家用成套的产品搭配完善的可视化界面给开发运维带来了极大的便利，搭建简单、学习曲线低
* 结合SpringCloud Alibaba 我们最终的技术搭配方案：
  * SpringCloud Alibaba - Nacos：注册中心（服务发现/注册）
  * SpringCloud Alibaba - Nacos：配置中心 （动态配置管理）
  * SpringCloud - Ribbon ：负载均衡
  * SpringCloud - Feign/OpenFeign : 声明式 HTTP 客户端（调用远程服务）
  * SpringCloud Alibaba - Sentinel ：服务容错（限流、降级、熔断）
  * SpringCloud - GateWay : API网关（webflux编程模式）
  * SpringcCloud - Sleuth : 调用链监控
  * SpringCloud Alibaba - Seata：分布式事务解决方案



### SpringCloud Alibaba Nacos 注册中心

#### 简介

* Nacos 致力于帮助您发现、配置和管理微服务，Nacos 提供了一组简单易用的特性集，帮助您快速实现动态服务发现、服务配置、服务元数据及流量管理

  Nacos 帮助您更敏捷和容易地构建、交付和管理微服务平台。 Nacos 是构建以“服务”为中心的现代应用架构 (例如微服务范式、云原生范式) 的服务基础设施

#### 下载安装

* [官网下载地址](https://github.com/alibaba/nacos/releases)

* docker安装

  ```shell
  docker pull nacos/nacos-server
  
  docker run --env MODE=standalone --name nacos -d -p 8848:8848 nacos/nacos-server
  ```

  测试是否安装成功

  ```
  浏览器输入：ip:8848/nacos
  登录账号密码：nacos nacos
  ```

#### 将微服务注册到nacos中

* 第一步：修改pom.xml文件，引入 Nacos Discovery Starter

  ```xml
  <!--nacos 服务注册与发现-->
  <dependency>
  	<groupId>com.alibaba.cloud</groupId>
      <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
  </dependency>
  ```

* 第二步：在微服务的yml配置文件中，配置上nacos server的地址，并**指定应用名称**

  ```yaml
  spring:
    datasource:
      username: root
      password: 123456
      url: jdbc:mysql://192.168.83.133:3306/touch_air_mall_pms
      driver-class-name: com.mysql.cj.jdbc.Driver
    cloud:
      nacos:
        discovery:
          server-addr: 192.168.83.133:8848
    application:
      name: touch-air-mall-product
  ```

  ![image-20201204171638379](https://img2020.cnblogs.com/blog/1875400/202101/1875400-20210102142415575-1841824468.png)

* 第三步：在启动类添加注解 @EnableDiscoveryClient 开启服务注册与发现功能

  ![image-20201204171814761](https://img2020.cnblogs.com/blog/1875400/202101/1875400-20210102142415932-698261652.png)

* 第四步：启动应用，观察nacos服务列表是否已注册进服务中心

  > 注意：每一个应用都应该有名字，这样才能成功注册进去

  ![image-20201204171955653](https://img2020.cnblogs.com/blog/1875400/202101/1875400-20210102142416230-501658450.png)
  
  >注意：项目中部分微服务的 配置文件在nacos中，可自行修改

### SpringCloud OpenFeign远程调用

#### Feign声明式远程调用

* Fegin是一个声明式的HTTP客户端，它的目的就是让远程调用更加简单。Fegin提供了HTTP请求的模板，通过编写简单的接口和插入注解，就可以定义好HTTP请求的参数、格式、地址等信息

* Fegin整合了Ribbon（负载均衡）和Hystrix（服务熔断），可以让我们不再需要显示地使用这两个组件
* SpringCloud Fegin在NetFlix Fegin的基础上扩展了对SpringMVC注解的支持，在其实现下，我们只需要创建一个接口并用注解的方式来配置它，即可完成对服务提供方的接口绑定。简化了SpringCloud Ribbon自选封装服务调用客户端的开发量

#### 使用

* 第一步：引入依赖（对应版本）

  ```xml
   <!--openfeign-->
  <dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-starter-openfeign</artifactId>
      <version>2.2.3.RELEASE</version>
  </dependency>
  ```

* 第二步：开启feign功能

  ```java
  @EnableDiscoveryClient
  ```

  ![image-20201207165509849](https://img2020.cnblogs.com/blog/1875400/202101/1875400-20210102142416511-587296807.png)

* 第三步：声明远程接口

  ![image-20201207165430261](https://img2020.cnblogs.com/blog/1875400/202101/1875400-20210102142416837-1535436048.png)

* 测试

  * 正常启动 用户服务8000和优惠券服务7000

    ![image-20201207170202495](https://img2020.cnblogs.com/blog/1875400/202101/1875400-20210102142417110-564347043.png)

  * 优惠券服务7000宕机

    ![image-20201207170337167](https://img2020.cnblogs.com/blog/1875400/202101/1875400-20210102142417320-1804422581.png)





#### fegin远程调用的处理逻辑（*）

* ```
  1、 CouponFeignService.saveSpuBounds(spuBoundTO)
    1.1、@RequestBody 将这个对象转为json
    1.2、找到coupon服务，给 /coupon/spubounds/save 发送请求
         将上一步转的json 放在请求体位置，发送请求
    1.3、对方服务收到请求，请求体里有json数据
        @RequestBody SpuBoundsEntity spuBounds：将请求体中的json 转为 SpuBoundsEntity 这个类型
  2、只有json 数据模型是兼容的，双方服务无需使用同一个to
  ```

* 两种不同的请求处理

  ```
  feign 请求的两种写法
   1. 让所有请求过网关
  	1.1 @FeignClient("touch-air-mall-gateway"):给网关服务发请求
  	1.2 /api/product/skuinfo/info/{skuId}
       
   2. 直接指定具体某个微服务处理
  	2.1 @FeignClient("touch-air-mall-product")：给商品服务发请求
  	2.2 /product/skuinfo/info/{skuId}
  ```

  

### SpringCloud Alibaba Nacos 配置中心

#### 官方文档示例

* [官网文档](https://github.com/alibaba/spring-cloud-alibaba/blob/master/spring-cloud-alibaba-examples/nacos-example/nacos-config-example/readme.md)

* 第一步：引入依赖

  ```xml
  <!--nacos 配置中心做配置管理-->
  <dependency>
      <groupId>com.alibaba.cloud</groupId>
      <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
  </dependency>
  ```

* 第二步：在应用的 `/src/main/resources/bootstrap.properties` 配置文件中配置 Nacos Config 元数据

  `bootstrap.properties`文件内容会优先于 `yml` 文件被加载

  ```
  spring.application.name=touch-air-mall-coupon
  spring.cloud.nacos.config.server-addr=192.168.83.133
  ```

  ![image-20201207171409989](https://img2020.cnblogs.com/blog/1875400/202101/1875400-20210102142417528-583512420.png)

* 观察启动类 新增配置的默认ID（默认当前应用的名称 `application name ＋ properties`）

  ![image-20201207172543860](https://img2020.cnblogs.com/blog/1875400/202101/1875400-20210102142417806-1851675865.png)

  ![image-20201207172629031](https://img2020.cnblogs.com/blog/1875400/202101/1875400-20210102142418067-1089345107.png)

* 测试

  * 第一次加载

  ```java
   @Value("${coupon.user.name}")
      private String name;
      @Value("${coupon.user.age}")
      private int age;
      @RequestMapping("/nacos/config")
      public R testConfig(){
          return R.ok().put("name", name).put( "age", age);
      }
  ```

  ![image-20201207172729718](https://img2020.cnblogs.com/blog/1875400/202101/1875400-20210102142418249-101511945.png)

  * 在线修改 nacos 配置中心的配置

    ![image-20201207172845883](https://img2020.cnblogs.com/blog/1875400/202101/1875400-20210102142418546-388143985.png)

    必须重启，再次请求接口

    ![image-20201207173002500](https://img2020.cnblogs.com/blog/1875400/202101/1875400-20210102142418720-567266150.png)

* 动态获取 加上注解 @RefreshScope

  无需重启，动态刷新配置

  ![image-20201207173353758](https://img2020.cnblogs.com/blog/1875400/202101/1875400-20210102142418929-1384385638.png)



#### 命名空间和配置分组

* 每个微服务可以创建自己的命名空间，其次可以使用配置分组区分环境：dev、test、prod等

##### 命名空间

* 用于进行租户粒度的配置隔离。不同的命名空间下，可以存在相同的Group或Data ID的配置。NameSpace的常用场景之一是不同环境的配置的区分隔离，例如开发测试环境和生产环境的资源（如配置、服务）隔离等

  * public（保留空间）：默认新增的所有配置都在public空间

  * 利用命名空间来做环境隔离：开发、测试、生产。注意：在bootstrap.properties中，配置使用哪个命名空间![image-20201208095157234](https://img2020.cnblogs.com/blog/1875400/202101/1875400-20210102142419139-2029152856.png)

    ![image-20201208095211756](https://img2020.cnblogs.com/blog/1875400/202101/1875400-20210102142419352-1937390665.png)

  * 每一个微服务之间互相隔离配置，每一个微服务都创建自己的命名空间，只加载自己命名空间下的所有配置

##### 配置集

* 所有配置的集合

##### 配置集ID

* 类似配置文件名

  ![image-20201208102509326](https://img2020.cnblogs.com/blog/1875400/202101/1875400-20210102142419587-107286490.png)

##### 配置分组

* 默认所有的配置集都属于：DEFAULT_GROUP

  ![image-20201208103514818](https://img2020.cnblogs.com/blog/1875400/202101/1875400-20210102142419787-142530976.png)

  ![image-20201208103436993](https://img2020.cnblogs.com/blog/1875400/202101/1875400-20210102142419997-528357132.png)

#### 同时加载多个配置集

* 随着服务增长，配置文件会越来越多，不易于维护。所有通常按类拆分成多个配置文件

  只需要在`bootstrap.properties`中说明加载配置中心的哪些配置文件即可

  ```
  spring.cloud.nacos.config.extension-configs[0].data-id=datasource.yaml
  spring.cloud.nacos.config.extension-configs[0].group=dev
  spring.cloud.nacos.config.extension-configs[0].refresh=true
  spring.cloud.nacos.config.extension-configs[1].data-id=mybatis.yaml
  spring.cloud.nacos.config.extension-configs[1].group=dev
  spring.cloud.nacos.config.extension-configs[1].refresh=true
  spring.cloud.nacos.config.extension-configs[2].data-id=other.yaml
  spring.cloud.nacos.config.extension-configs[2].group=dev
  spring.cloud.nacos.config.extension-configs[2].refresh=true
  ```

  ![image-20201208110535615](https://img2020.cnblogs.com/blog/1875400/202101/1875400-20210102142420246-1410754132.png)

  ![image-20201208110554545](https://img2020.cnblogs.com/blog/1875400/202101/1875400-20210102142420459-266374861.png)

  ![image-20201208110636690](https://img2020.cnblogs.com/blog/1875400/202101/1875400-20210102142420741-970452406.png)



### SpringCloud GateWay网关

#### 简介

* 网关作为流量的入口，常用功能包括路由转发、权限校验、限流控制等。而SpringCloud gateway作为SpringCloud官方推出的第二代网关框架，取代了Zuul网关

  | 组件                | RPS(每秒处理请求) |
  | ------------------- | ----------------- |
  | SpringCloud Gateway | 32213.38          |
  | Zuul                | 20800.13          |
  | Linkerd             | 28050.76          |

  网关提供API全托管服务，丰富的API管理功能，辅助企业管理大规模的API，以降低管理成本和安全风险，包括协议适配、协议转发、安全策略、防刷、流量、监控日志等功能

  SpringCloud Gateway 旨在提供一种简单有效的方式来对API进行路由，并为他们提供切面，例如：安全性、监控指标和弹性等

* 网关使用前后对比

  ![image-20201208113124245](https://img2020.cnblogs.com/blog/1875400/202101/1875400-20210102142421063-2014737955.png)

  ![image-20201208113201925](https://img2020.cnblogs.com/blog/1875400/202101/1875400-20210102142421446-946040766.png)

#### 官方文档

* [Gateway网关](https://docs.spring.io/spring-cloud-gateway/docs/2.2.5.RELEASE/reference/html/)

#### 核心理念

##### 路由/断言/过滤

* web请求，通过一些匹配条件，定位到真正的服务节点。并在这个转发过程的前后，进行一些精细化控制。perdicate就是我们的匹配条件。而Filter就可以理解为一个无所不能的拦截器，有了这两个元素，再加上目标uri，就可以实现一个具体的路由了

  ![image-20201208134939497](https://img2020.cnblogs.com/blog/1875400/202101/1875400-20210102142421728-1631431304.png)

  Gateway流程：客户端向Spring Cloud Gateway发出请求。然后在Gateway Handler Mapping 中找到与请求相匹配的路由，将其发送到Gateway Web Handler

  Handler再通过指定的过滤器链来将请求发送到我们实际的服务执行业务逻辑，然后返回

  过滤器之间用虚线分开是因为过滤器可能会在发送代理请求之前（“pre”）或之后（“post”）执行业务逻辑

  Filter在“pre”类型的过滤器可以做参数校验、权限校验、流量监控、日志输出、协议转换等，在“post”类型的过滤器中可以做响应内容、响应头的修改，日志的输出，流量监控等有非常重要的作用



#### 网关启动测试

* 第一步：引入依赖

  ```
  <dependency>
  	<groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-starter-gateway</artifactId>
  </dependency>
  ```

* 第二步：编写配置文件

  * `application.properties` 服务名称和nacos注册中心地址

    ```
    spring.cloud.nacos.discovery.server-addr=192.168.83.133:8848
    spring.application.name=touch-air-mall-gateway
    ```

  * 启动类添加注解，开启服务注册与发现

    `@EnableDiscoveryClient`

  * `bootstrap.properties` 

    ```
    #默认加载的配置文件 应用名.properties
    spring.application.name=touch-air-mall-gateway
    #配置中心地址
    spring.cloud.nacos.config.server-addr=192.168.83.133
    #命名空间
    spring.cloud.nacos.config.namespace=fa94e939-311c-427a-915a-a6c37bc403ae
    
    spring.cloud.nacos.config.extension-configs[0].data-id=touch-air-mall-gateway.yaml
    spring.cloud.nacos.config.extension-configs[0].group=DEFAULT_GROUP
    spring.cloud.nacos.config.extension-configs[0].refresh=true
    ```

  * nacos配置中心，新增命名空间 `touch-air-mall-gateway` ,生成的id 对应上面配置文件的`namespace`

    ![image-20201208171218166](https://img2020.cnblogs.com/blog/1875400/202101/1875400-20210102142421929-1893256781.png)

    ![image-20201208171144287](https://img2020.cnblogs.com/blog/1875400/202101/1875400-20210102142422130-21745002.png)

  * 启动服务，查看情况

    ![image-20201208171332895](https://img2020.cnblogs.com/blog/1875400/202101/1875400-20210102142422425-855436611.png)



#### 网关路由转发测试

* `application.yml` ,编写路由规则，详情参考官方文档

  请求路径带参数：`url`，并且当值等于 baidu 时，转发到百度

  ​										 当值等于 qq 时，转发到 qq

  ```
  spring:
    cloud:
      gateway:
        routes:
          - id: test_route
            uri: https://www.baidu.com
            predicates:
              - Query=url,baidu
          - id: qq_route
            uri: https://www.qq.com
            predicates:
              - Query=url,qq
  ```

* 重启

  ![image-20201208171633308](https://img2020.cnblogs.com/blog/1875400/202101/1875400-20210102142422809-1821563459.png)

  ![image-20201208171704341](https://img2020.cnblogs.com/blog/1875400/202101/1875400-20210102142423061-1119369968.png)





## 对象存储OSS

### SpringCloud Alibaba-OSS

#### 简介

* 对象存储服务（Object Storage Service,OSS）是一种海量、安全、低成本、高可靠的云储存服务，适合存放任意类型的文件。容量和处理能力弹性扩展，多种存储类型供选择，全面优化存储成本
* 您可以通过本文档提供的简单的REST接口，在任何时间、任何地点、任何互联网设备上进行上传和下载数据。基于OSS，您可以搭建出各种多媒体分享网站、网盘、个人和企业数据备份等基于大规模数据的服务

#### 使用

* 阿里云官网开通OSS服务

  [OSS云存储服务控制台](https://oss.console.aliyun.com/overview)

  [API文档](https://help.aliyun.com/document_detail/31947.html?spm=5176.8465980.0.dexternal.4e701450t2QdXz)

  [Aliyun Spring Boot OSS Simple](https://github.com/alibaba/aliyun-spring-boot/tree/master/aliyun-spring-boot-samples/aliyun-oss-spring-boot-sample)

* 创建bucket

  ![image-20201216142709111](https://img2020.cnblogs.com/blog/1875400/202101/1875400-20210102142423294-1008599511.png)

* 设置子账户，获得`accessKeyId`和`accessKeySecret`并给子账户添加权限

  ![image-20201216143156319](https://img2020.cnblogs.com/blog/1875400/202101/1875400-20210102142423588-722339256.png)

  ![image-20201216143339357](https://img2020.cnblogs.com/blog/1875400/202101/1875400-20210102142423934-2061755950.png)

#### 上传方式

* 1：普通上传方式

  用户将文件上传至应用服务器，应用服务器拿到文件流，通过java代码将文件流数据上传到文件存储服务器，流数据经过后台一层处理，在高并发场景下，安全但性能差

  ![image-20201216112734303](https://img2020.cnblogs.com/blog/1875400/202101/1875400-20210102142424187-715665300.png)

* 2：服务端签名后直传

  操作对象存储的账号密码信息还是存储在后端服务中

  用户/前端上传之前先向服务器请求上传策略，服务器利用阿里云存储服务的账号密码生成一个防伪的签名，签名中包含访问OSS的授权令牌、以及具体的存储位置等信息，返回给前端

  前端带着这个防伪的令牌签名和要上传的文件，直接上传到OSS

  ![image-20201216113057702](https://img2020.cnblogs.com/blog/1875400/202101/1875400-20210102142424486-1906363615.png)

  [服务器签名后直传](https://help.aliyun.com/document_detail/31926.html?spm=a2c4g.11186623.6.1715.67635cb1ovaImp)

  [签名后直传Java实现](https://help.aliyun.com/document_detail/91868.html?spm=a2c4g.11186623.2.18.4c36366cyqmnYA#concept-ahk-rfz-2fb)

  ![image-20201216164459365](https://img2020.cnblogs.com/blog/1875400/202101/1875400-20210102142424819-1739301912.png)

  服务器端加密，前端直传，需要配置跨域

  ![image-20201217101418830](https://img2020.cnblogs.com/blog/1875400/202101/1875400-20210102142425059-537187874.png)

## 基本概念

### 三级分类

#### 递归树形数据获取

* 第一步：商品三级分类实体类`CategoryEntity`中添加字段

  ```
  //建表忽略字段
  @TableField(exist = false)
  private List<CategoryEntity> children;
  ```

* `CategoryServiceImpl`具体实现

   `Java8` 新特性，`Stream`流（前提准备）

  ```
   @Override
      public List<CategoryEntity> listWithTree() {
          // 1、查询出所有分类
          List<CategoryEntity> categoryEntities = baseMapper.selectList(null);
          // 2、组装成父子的树形结构
          // 2.1、找出所有的一级分类
          // 2.2、递归查出子类
          List<CategoryEntity> level1Menu = categoryEntities.stream().filter(categoryEntity ->
                  categoryEntity.getParentCid() == 0
          ).map(menu-> {
              menu.setChildren(getChildrens(menu, categoryEntities));
              return menu;
          }).sorted((menu1,menu2)->{
              return (menu1.getSort()==null?0:menu1.getSort()) - (menu2.getSort()==null?0:menu2.getSort());
          }).collect(Collectors.toList());
          return level1Menu;
      }
  
      /**
       * 递归查找所有菜单的子菜单
       */
      public List<CategoryEntity> getChildrens(CategoryEntity root,List<CategoryEntity> all){
          List<CategoryEntity> children = all.stream().filter(categoryEntity -> {
              return categoryEntity.getParentCid().equals(root.getCatId());
          }).map(categoryEntity -> {
              categoryEntity.setChildren(getChildrens(categoryEntity, all));
              return categoryEntity;
          }).sorted((menu1,menu2)->{
              return (menu1.getSort()==null?0:menu1.getSort()) - (menu2.getSort()==null?0:menu2.getSort());
          }).collect(Collectors.toList());
          return children;
      }
  ```

  

#### 配置网关路由与路径重写

##### 官方文档

[Gateway Filter](https://docs.spring.io/spring-cloud-gateway/docs/2.2.5.RELEASE/reference/html/#gatewayfilter-factories)

##### MallGateway配置

* `application.yml`文件

  ```
  spring:
    cloud:
      gateway:
        routes:
          - id: test_route
            uri: https://www.baidu.com
            predicates:
              - Query=url,baidu
          - id: qq_route
            uri: https://www.qq.com
            predicates:
              - Query=url,qq
          - id: admin_route
            uri: lb://renren-fast
            predicates:
              - Path= /api/**
            filters:
              - RewritePath= /api/(?<segment>/?.*),/renren-fast/$\{segment}
  ```

* 配置说明

  注意是：`admin_route`

  前端项目统一请求都带有 `/api` 前缀

  `lb:renren-fast`:LoadBalance 负载均衡

  满足`predicates`:http://localhost:9527/api/captcha.jpg  ---> http://renren-fast:8080/api/captcha.jpg

  但是默认的请求是：http://localhost:8080/renren-fast/captcha.jpg

  使用Gateway的Filter 重写路径（重写规则参考官方文档）

#### 网关统一配置跨域

##### 跨域

* **跨域**：指的是浏览器不能执行其他网站的脚本。它是由浏览器的**同源策源**造成的，**是浏览器对`javascript`施加的安全限制**

* **同源策略**：是指协议、域名、端口都要相同，其中有一个不同都会产生跨域

  | URL                                                        | 说明                   | 是否允许通信                     |
  | ---------------------------------------------------------- | ---------------------- | -------------------------------- |
  | http://www.a.com/a.js<br />http://www.a.com/b.js           | 同一域名下             | 允许                             |
  | http://www.a.com/ab/a.js<br />http://www.a.com/script/b.js | 同一域名不同文件夹下   | 允许                             |
  | http://www.a.com:8000/a.js<br />http://www.a.com/b.js      | 同一域名，不同端口     | 不允许                           |
  | http://www.a.com/a.js<br />https://www.a.com/b.js          | 同一域名不同协议       | 不允许                           |
  | http://www.a.com/a.js<br />http://70.32.92.74/b.js         | 域名和域名对应ip       | 不允许                           |
  | http://www.a.com/a.js<br />http://script.a.com/b.js        | 主域相同，子域不同     | 不允许                           |
  | http://www.a.com/a.js<br />http://a.com/b.js               | 同一域名，不同二级域名 | 不允许（cookie这种也不允许访问） |
  | http://www.a.com/a.js<br />http://www.a.com/b.js           | 不同域名               | 不允许                           |



##### 跨域流程

* 非简单请求（PUT、DELETE）等，需要先发送预检请求

  * 1：预检请求，OPTIONS
  * 2：响应允许跨域
  * 3：发送真实请求
  * 4：响应数据

  ![image-20201210094713223](https://img2020.cnblogs.com/blog/1875400/202101/1875400-20210102142425322-1509630594.png)



##### 解决跨域

* 第一种：使用`nginx`部署为同一域

  ![image-20201210095127167](https://img2020.cnblogs.com/blog/1875400/202101/1875400-20210102142425605-1269514032.png)

* 第二种：配置当次请求允许跨域
  * 添加响应头
    * Access-Control-Allow-Origin：支持哪些来源的请求跨域
    * Access-Control-Allow-Methods：支持哪些方法跨域
    * Access-Control-Allow-Credentials：跨域请求默认不包含cookie，设置为true可以包含cookie
    * Access-Control-Expose-Headers：跨域请求暴露的字段
      * CORS请求时，XMLHttpRequest对象的getResponseHeader()方法只能拿到6个基本字段：Cache-Control、Content-Language、Content-Type、Expires、Last-Modified、Pragma如果想拿到其他字段，就必须在Access-Control-Expose-Headers里面指定
    * Access-Control-Max-Age：表明该响应的有效时间为多少秒。在有效时间内，浏览器无须为同一请求再次发起预检请求。请注意：浏览器自身维护了一个最大有效时间，如果该首部字段的值超过了最大有效时间，将不会生效



* `MallCorsConfiguration` 网关服务添加跨域配置类

  ```java
  @Configuration
  public class MallCorsConfiguration {
      @Bean
      public CorsWebFilter corsWebFilter(){
          UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
          CorsConfiguration corsConfiguration = new CorsConfiguration();
          //跨域配置
          corsConfiguration.addAllowedHeader("*");
          corsConfiguration.addAllowedOrigin("*");
          corsConfiguration.addAllowedMethod("*");
          corsConfiguration.setAllowCredentials(true);
  
          urlBasedCorsConfigurationSource.registerCorsConfiguration("/**",corsConfiguration);
          return new CorsWebFilter(urlBasedCorsConfigurationSource);
      }
  }
  ```

  ![image-20201210104828913](https://img2020.cnblogs.com/blog/1875400/202101/1875400-20210102142425850-979972517.png)

  > 注意：这里需要注释掉`renren-fast`中的跨域配置，统一由网关进行配置

#### 树形结构页面展示

##### renren-fast-vue 快速搭建前端项目



### SPU与SKU

#### SPU

##### Standard Product Unit（标准化产品单元）

* 是商品信息聚合的最小单位，是一组可复用、易检索的标准化信息的集合，该集合**描述了一个产品的特性**

#### SKU

##### Stock Keeping Unit （库存量单位）

* 即库存进出计量的基本单元，可以是以件、盒、托盘等为单位。SKU这是对于大型连锁超市DC（配送中心）物流管理的一个必要的方法。现在已经被引申为产品统一编号的简称，每种产品均对应有唯一的SKU号



### 基本属性【规格参数】与销售属性

* 每个分类下的商品共享规格参数，与销售属性。只是有些商品不一定要用这个分类下全部的属性：
  * 属性是以三级分类组织起来的
  * 规格参数中有些是可以提供检索的
  * 规格参数也是基本属性，他们具有自己的分组
  * 属性的分组也是以三级分类组织起来的
  * 属性名确定的，但是值是每一个商品不同来决定的

#### 属性分组-规格参数-销售属性-三级分类

* 数据库关联关系图

  ![image-20201218153129161](https://img2020.cnblogs.com/blog/1875400/202101/1875400-20210102142426118-1782464715.png)

#### SPU-SKU-属性表

* 数据库关联关系图

  ![image-20201218153242683](https://img2020.cnblogs.com/blog/1875400/202101/1875400-20210102142426493-264476964.png)



## 接口编写

### HTTP请求模板

####  WebStorm

* `File-Settings-Editor-Live Templates`

  ![image-20201228141021336](https://img2020.cnblogs.com/blog/1875400/202101/1875400-20210102142426978-1933137602.png)

  

### JSR303数据校验

#### JSR303

* 给Bean添加校验注解：`javax.validation.constraints`,并定义自己的message提示

* 开启校验功能`@Valid`

  效果：校验错误以后会有默认的响应

* 给校验的bean后紧跟一个`BindingResult`,就可以获得到校验的结果
* 每个方法都这样做，太麻烦且代码冗余

#### 分组校验

* 1. 给校验注解标注什么情况需要进行校验

  ![image-20201217171250931](https://img2020.cnblogs.com/blog/1875400/202101/1875400-20210102142427270-1140454901.png)

* 2. `controller`添加注解 `@Validated{(AddGroup.class, UpdateGroup.class)}`
  3. **默认没有指定分组的校验注解，在分组校验的情况下 不生效**

#### 自定义校验

* 编写一个自定义的校验注解  

  ![image-20201218141235704](https://img2020.cnblogs.com/blog/1875400/202101/1875400-20210102142427580-698767844.png)

  可以指定多个不同的校验器，适配不同类型的校验

  ![image-20201218141416574](https://img2020.cnblogs.com/blog/1875400/202101/1875400-20210102142427879-898310838.png)

* 编写一个自定义的校验器

  ![image-20201218141449974](https://img2020.cnblogs.com/blog/1875400/202101/1875400-20210102142428213-804721939.png)

* 关联自定义的校验器和校验注解

### 全局异常处理

* 集中处理所有异常

  ```
  @RestControllerAdvice 等价于 @ControllerAdvice和@ResponseBody
  处理全局异常并以json格式返回
  ```

  ![image-20201228142142467](https://img2020.cnblogs.com/blog/1875400/202101/1875400-20210102142428539-244826788.png)

* x系统错误码和错误信息定义类

  * 1. 错误码定义规则为5位数字

    2. 前两位表示业务场景，最后三位表示错误码 

       例如：10001 10：通用 001：系统未知异常

    3. 维护错误码后需要维护错误描述，将他们定义为枚举形式

       错误码列表：

       10：通用

       11：商品

       12：订单

       13：购物车

       14：物流

### 接口文档地址

[在线接口文档地址](https://easydoc.xyz/s/78237135/ZUqEdvA4/HgVjlzWV)

### Object划分

#### PO（persistant object）持久对象

* PO就是对应数据库中某个表中的一条记录，多个记录可以用PO的集合。PO中应该不包含任何数据库的操作

#### DO（Domain Object）领域对象

* 就是从现实世界中抽象出来的有形或者无形的业务实体

#### TO（Transfer Object）数据传输对象

* 不同的应用程序之间传输的对象

#### DTO（Data Transfer Object）数据传输对象

* 这个概念来源于J2EE的设计模式，原来的目的是为了EJB的分布式应用提供粗粒度的数据实体，以减少分布式调用的次数，从而提高分布式调用的性能和降低网络负载，但在这里，泛指用于展示层与服务层之间的数据传输对象

#### VO（View Object）视图对象

* 通常用于业务层之间的数据传递，和PO一样也是仅仅包含数据而已。但应是抽象出的业务对象，可以和表对应，也可以不，这根据业务的需要。用`new`关键字创建，由`GC`回收
* 接收页面传递来的数据，封装对象
* 将业务处理完成的对象，封装成页面要用的数据

#### BO（Business Object）业务对象

* 从业务模型的角度看，见`UML`元件领域模型中的领域对象。封装业务逻辑的`java`对象，通过调用DAO方法，结合PO、VO进行业务操作。business object：业务对象 主要作用是把业务逻辑封装为一个对象。这个对象可以包括一个或多个对象。比如一个简历，有教育经历、工作经历、社会关系等等。我们可以把教育经历对应一个PO,工作经历对应一个PO,社会关系对应一个PO，建立一个简历的BO对象去处理简历，每个BO都包含这些PO。这样处理业务逻辑时，我们就可以针对BO去处理

#### POJO（Plain Ordinary Java Object）简单无规则Java对象

* 传统意义的Java对象，就是说在一些 Object/Relation Mapping工具中，能够做到维护数据库表记录的persisent object 完全符合 Java Bean 规范的纯java对象，没有增加别的属性和方法，我的理解就是最基本的java Bean，只有属性字段以及setter和getter方法
* POJO 是DO/DTO/BO/VO的统称

#### DAO（Data Access Object）数据访问对象

* 是一个`sun`的标准`j2ee`设计模式，这个模式中有个接口就是DAO，它负责持久层的操作，为业务层接口。此对象用于访问数据库。通常和PO结合使用，DAO中包含了各种数据库的操作方法。通过它的方法，结合PO对数据库进行相关的操作，夹在业务逻辑与数据库资源中间，配合VO，提供数据库的CRUD操作





## 分布式基础篇总结

### 分布式基础概念

* 微服务、注册中心、配置中心、远程调用、Feign、网关

### 基础开发

* SpringBoot 2.0 、SpringCloud、Mybatis-Plus、Vue组件化、阿里云对象存储

### 环境

* VMWare、Linux、Docker、MySQL、Redis、逆向工程&人人开源 

### 开发规范

* 数据校验JSR303、全局异常处理、全局统一返回、全局跨域处理 
* 枚举状态、业务状态码、VO与TO与PO划分、逻辑删除 
* Lombok：@Data、@Slf4
