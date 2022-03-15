# 前言
上传的第五版
做了一定的优化，将RocketMQ加入，默认使用RocketMQ，但没有删除RabittmQ的相关代码。以及补全和完善了注册和更改密码的代码逻辑，以及完成登陆接口的防止同一ip恶意多频率访问。并实现一个完成度相对简略的对于客户能否拥有秒杀资格的后台灵活配置功能。这一块的逻辑完成度不高的原因是，没有对应的每个用户的被筛选的数据，空有筛选的规则。项目中的加密部分换为国密SM3加密。
- [客户端前端服务器](https://github.com/weiran1999/seckill-front)
- [后台前端服务器](https://github.com/weiran1999/admin-manager)

# 简介
针对需求，本项目使用了基于Java微服务的解决方案，采用了SpringBoot框架、SpringCloud微服务架构、SpringCloud Gateway网关技术栈、SpringCloud alibaba技术栈Nacos、SpringCloud Netflix技术栈容灾和均衡负载和Feign通信、持久层MybatisPlus框架、中间件缓存Redis与相关框架、SpringBoot Admin技术栈、中间件消息队列RocketMQ等一系列技术栈(项目中也有RabbitMQ，只是被替换掉)，优化项目中的消息队列与缓存等技术，解决了秒杀系统设计与实现中，并发不安全的难题与数据库存储的瓶颈。微服务构架技术，则赋予了项目需要的容灾性和可扩展性，从而完成了一个具有高并发、高可用性能的秒杀系统以及灵活配置秒杀业务与策略的秒杀系统。并且拥有秒杀业务客户端和后台管理的前端服务器，实现了前后端分离。
## 项目模块
因为是用微服务架构构建的项目，很多地方需要一些微服务必须的组件。下面简单介绍一些项目模块。
- cloud-gateway
微服务网关模块，使用的是SpringCloud Gateway，这里的注册中心用的是Nacos。
网关承担的角色有：请求接入，作为所有API接口服务请求的接入点，比如这里所有模块都可以用网关的端口 8205/ 加上配置文件里的路由，可以直接访问下游的模块；中介策略，实现安全、验证、路由、过滤、流控等策略；等等。
- cloud-monitor
监控模块，使用SpringBoot Admin 技术栈，可以用来监控和管理我们所有微服务的 Spring Boot 项目。
- cloud-common
通用模块。负责一些通用的依赖管理和一些通用代码复用。
- cloud-manage
后台管理系统模块。后端提供接口给React框架下的后台前端服务器，实现前后端分离。
- cloud-uaa
用户认证中心模块，统一登陆，与客户注册功能。
- cloud-mission
主要秒杀业务模块。cloud-mission模块里的test包里，有TestJmeterController类专供Jmeter压测工具测试秒杀性能。React框架下的秒杀客户端前后端分离。

# 如何使用
- 首先将SQL导入自己的数据库，用户名root、密码123456即可。Mysql的表名得是SQL文件名。
- 启动Nacos，如果没有则先安装，安装后按网上文章博客启动。
- 启动本地的Redis，密码要配置为123456即可。如果本地没有安装Redis，则先安装。
- 如果使用RabbitMQ则启动本地的RocketMQ（RabbitMQ），用户名和密码才去默认即可。如果本地没有安装RocketMQ，则先安装。如果使用RocketMQ，则可以先下载RocketMQ与可视化软件，然后分别启动。
- 依次启动项目中的cloud-gateway、cloud-uaa、cloud-mission、cloud-manage模块，如果不用到后台管理系统可以不启动cloud-manage模块。
- 其中参数都可以了解后自行在项目里更改。
- cloud-monitor模块的SpringBoot Admin监控技术栈，使用只需要开启网关后访问http://localhost:8205/monitor 或者直接访问monitor端口。
- 启动后台前端服务器和客户端前端服务器。客户端有账号18077200000，密码123 后台系统有超级管理员账号super_admin 密码123和普通管理员账号admin 密码123。客户端端口为3000，后台系统端口为3001.

[一些自己收集的知识点和参考](./THINK.md)

# 秒杀的代码逻辑
- 关于秒杀的业务逻辑，用户访问，在uaa模块登入时，进行资格筛选，认证后。进入秒杀商品列表页面，点入秒杀商品详情后，点击立即秒杀，如果在规定时间内（按钮没有置灰），并且没有重复秒杀，则开启秒杀。
- 这里涉及到秒杀接口的URL加盐动态化，后端相关的秒杀代码，没有选择Redis的LUA脚本和Redisson分布式锁，因为项目中没有使用过多的Redis事务逻辑和Redis分布式逻辑。秒杀主要运用的是：Mysql加上乐观锁和Redis库存预热加载和Redis预减库存解决超卖，RabbitMQ(RocketMQ)消息队列使用串行化，保证项目的高可用和高并发。
- 秒杀的策略配置以及登陆初筛等功能，是由cloud-manage模块提供，持久层主要使用MyBatis完成。
- 在后台系统中，在商品列表里增加一个商品，则会分别在商品表和库存表中分别增加对应的信息，以及在Redis缓存中的商品缓存和库存缓存中增加，并且也会在后台秒杀库存页面中显示。并且在商品信息中有是否启用这个信息以及对应的控制，不启用的时候，客户端访问商品列表只会显示那些缓存中的启用的商品信息。
- 在后台中使用的SpringSecurity的JWT认证，而客户端使用的是自己写的Token加盐令牌的逻辑，每次客户端访问接口就需要前端服务器传递token给后端验证。其中的客户端的登陆和注册的密码，为了做到脱敏，都是前端服务器进行国密加密然后传输到后端存储。
- 后台系统中，简单实现一个对于用户是否能有资格进入秒杀系统的灵活配置，这里逻辑相对简略，此处的完成度不高。
- 后台管理系统的接口应该尊从微服务的规则，一个服务模块使用一个数据库，这里可用Feign来调用，即cloud-manage去调用cloud-mission模块的接口来调用。本项目目前使用MyBatis配置多数据源来调用资源。

# 解决的疑难杂症
- 当使用网关时，静态资源可以放网关下，如果是分别放下游服务静态资源包中，需要在代码中的js、css和img的路径加上/static/，和gateway配置中路径加上 /static/
- Swagger依赖版本可能会导致报错，doc.html页面功能需要另外加个依赖
- 使用MP框架时，想要切换主键生成策略，那么在切换之前，最好对数据库表执行"TRUNCATE TABLE 'table name'" 操作，不然会有影响。
- Druid 1.1.21以下的版本与MQ有冲突，高版本没有。1.1.22比较合适，更高版本的另外报错warn,连接失败。
- Feign调用接口如果要传递参数，必须要用@RequestParam注解
- Mysql Order是关键字，不能用来作为表名。
- RabbitMQ(若使用RocketMQ则暂时没有多实例容器工厂的问题) 开启多实例容器工厂配置来代替单实例配置后，在消费者中需要手动进行确认消费。并且比起使用单实例模式，一些地方可能出现并发问题，比如更新秒杀库存表。这里使用即时读取Redis库存数字，写入缓存库存数字来试着解决。
- Mybatis XML的很多,;) 符号的疏忽导致报错

# 未来展望
- 前端服务器可以用上cdn加速。
- Nginx对于Redis的分布式的一些配置未来也可以用上，Nginx均衡负载，集群分布式等，增加高可用的程度。
- 数据库的容灾，可以在云数据库厂商直接配置。主从结构，定时备份。也可以用容器构建。集群部署，主从分离，定时备份。
- 本身项目中秒杀模块也有注解加拦截器负责限流。关于限流、熔断等功能，还可以由网关来承载，这可能是未来改进的一个方向，项目中是以自定义注解加拦截器来限流。