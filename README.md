

<h1 align="center">
 <a href="http://cloud.bitactor.com" title="Bitactor">bitactor</a>
</h1>

[![JDK](https://img.shields.io/badge/JDK-1.8%2B-green.svg)](https://www.oracle.com/technetwork/java/javase/downloads/index.html)

### 介绍

* Bitactor Cloud 是一款适配spring-boot的分布式集群游戏服务器框架，可快速开发出一个易维护、易扩展且稳定高的分布式游戏服务器，让开发者专注于业务功能的开发<br>
* 实现了基于dubbo面向接口的定制化RPC、基于spring的的自定义网络控制层、基于nacos的服务注册中心。<br>
* 适配springboot,并且继承了springboot的所有特性、达到了松耦合的效果，提高了系统的可重用性、可维护性以及可扩展性<br>
  **优点：**

* 使用简单，学习成本低
* 功能强大，非常容易写出性能优秀可扩展的分布式集群服务
* 适配性强，可以方便的使用支持springboot的第三方的优秀组件。

### 协议

#### 支持的协议

* TCP:常规的长连接协议
* KCP:[可靠的UDP协议](https://github.com/szhnet/kcp-netty)

#### bitactor协议格式

通用

| head     | packetType |
|:---------|:-----------|
| 2(short) | 1(byte)    |

MessageData 专有

| head     | packetType | protoType | msgId  | commandId | data    |
|:---------|:-----------|:----------|:-------|:----------|:--------|
| 2(short) | 1(byte)    | 1(byte)   | 4(int) | 4(int)    | n(byte) |

#### 协议名词解释

* head: 包头指定包体长度，不包括head的长度，head 长度可以通过配置修改
* packetType: 包类型，见下表
* protoType:**仅用于 MessageData 包** 指定序列化类型，支持Protobuf|Json
* msgId:**仅用于 MessageData 包**
  消息号，单次请求响应消息号相同，每增加一次请求消息号+1,推送消息的msg固定为0
* commandId:**仅用于 MessageData 包**
  序列化类的简单类名的hashCode,用于序列化&反序列化
* data:**仅用于 MessageData 包** 序列化类的byte[]

#### bitactor协议包类型

基于packetType协议包体分为5种

| 包类名            | packetType值 | 描述    |
|:-----------------|:-------------|:--------|
| MessageHandShake | 0x01         | 握手消息 |
| MessageAck       | 0x02         | 确认消息 |
| MessageHeartBeat | 0x03         | 心跳消息 |
| MessageData      | 0x04         | 数据消息 |
| MessageClose     | 0x10         | 关闭消息 |

### 安装

#### 打包

将工程拉取到本地 然后执行安装命令

```text
mvn clean install
```

#### 引用

```xml
<!--父工程引用springboot使其支持springboot 相关功能-->
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>2.2.2.RELEASE</version>
    <relativePath/>
</parent>
<!--添加框架依赖的库-->
<dependencies>
    <dependency>
        <groupId>com.i91aries.game</groupId>
        <artifactId>aries-spring-boot-starter</artifactId>
        <version>1.0.0</version>
    </dependency>
</dependencies>
```

### 框架服务类型

* 网关服务:对外暴露端口，为client 提供链接、分发消息到后端服务。
* 后端服务:后端业务服务，不对外暴露端口、处理来做网关服务的分发消息。
* 服务间消息通信: 通过nacos注册中心自动探索发现依托RPC连接访问。

### 简单启动示例

```java
@ServerMark(appName = "simple-game", group = "gameServer")
@EnableAriesGameConfiguration
@AriesController(gateway = "gameServer")
public class SimpleApplication {
    public static void main(String[] args) throws Throwable {
        SpringApplication.run(SimpleApplication.class, args);
    }
    /**
     * 登录授权
     *
     * @param request
     * @param session
     * @return
     */
    @AriesRequestMapping()
    public CommonResp auth(@ProtocolBody LoginAuthReq request, ClientNetSession session) {
        // do something
        return null;
    }
}
```

```yaml
# Spring配置
spring:
  # aries游戏框架配置
  aries:
    # 服务相关配置
    app:
      # 不配置id 将会将默认使用ip作为id
      # id: 1
    # 网关服务（gateway）对外暴露配置
    gateway:
      # 对外暴露的ip&域名
      host: 192.168.0.51
      # 对外暴露的端口,配置该值后将开启网关服
      port: 8899
      # net-protocol: KCP
      hand-shake-data-bound-classes: com.aries.game.gateway.frame.CustomHandShakeDataBound
    # RPC 该服务提供服务的配置
    provider:
      net-protocol: TCP
    # RPC 该服务的消费服务配置
    consumer:
      subscribe: "*"
      logger-delay: true
      router-adapter: com.aries.game.common.frame.CustomRouter
    # 注册中心配置 默认 nacos 支持 backup
    registry:
      host: 127.0.0.1
      port: 8848
```

#### 相关注解

* @ServerMark: appName =
  服务名称,group=集群组名，可以是非集群节点，但是也需要组名
* @EnableAriesGameConfiguration 开启游戏框架
* @AriesController: 标记消息控制器，用于注册消息接收器，gateway =
  指定接收的网关服的 “集群组名”，可以指定本服务或者其他网关服务
* @AriesRequestMapping:
  标记请求的处理方法，方法的类必须添加@AriesController注解，且方法参数的协议对象必须添加@ProtocolBody，
  protocol = 协议序列化类型，目前支持 protobuf和json
* ClientNetSession: 连接的session信息与指定的网关服的session保持一致,
  在注解AriesRequestMapping的方法上可以自动注入
* AbstractPlayer:抽象玩家类，如果在注解AriesRequestMapping的方法上需要注入该类的子实现类，需要在该服务实现PlayerManager接口

### 相关框架辅助

#### 自定义路由

实现自定义路由需要在配置中指定才能生效，
spring.aries.consumer.router-adapter=xxx.xxx.CustomRouter

```java
public class CustomRouter extends PollingRouterAdapter {

    @Override
    public Channel routerAdapter(List<AbstractClient> clients, RPCRequest request) {
        // 实现自定义路由并返回对应的channel
        // 默认路由规则（轮训）
        return super.routerAdapter(clients, request);
    }
}
```

#### 自定义握手消息处理器

实现自定义握手消息处理器需要在配置中指定才能生效，
spring.aries.gateway.hand-shake-data-bound-classes=xxx.xxx.CustomHandShakeDataBound<br>
仅支持网关服使用

```java
public class CustomHandShakeDataBound implements HandShakeDataBound {
    public void buildCustomHandShakeData(HandShakeData handShakeData, URL url) {
        // 在handShakeData中插入想要给客户端的相关自定义数据
    }
}
```

#### 服务生命周期

服务的生命周期钩子，在想要触发生命周期钩子的类上加上注解@Lifecycle(1000)**value值越大在多个生命周期实例下，优先级越高**,并在对应的方法上加上想要的生命周期钩子注解。

```java
@Lifecycle(1000)
public class Lifecycle {
    // 框架启动前
    @StartBefore
    public void beforeStartUp() throws Throwable {
        log.warn("Lifecycle do method [ beforeStartUp ]");
    }
    // 框架启动后
    @StartAfter
    public void afterStartUp() {
        log.warn("Lifecycle do method [ afterStartUp ]");
    }
    // 框架关闭前
    @ShutDownBefore
    public void shutDownBefore() throws Throwable {
        log.warn("Lifecycle do method [ shutDownBefore ]");
    }
    // 框架关闭后
    @ShutDownAfter
    public void shutDownAfter() {
        log.warn("Lifecycle do method [ shutDownAfter ]");
    }
}
```

#### 连接监听器

实现NettyChannelListener的接口，并注入spring的容器即可加上 **@Service** ,该监听器仅针对网关服的客户端连接销毁

```java
@Service
public class NettyChannelListenerImpl implements NettyChannelListener {
    @Override
    public void onDestroyEvent(NettyChannel channel) {
        // do something
    }
}
```

#### RPC使用

实现NettyChannelListener的接口，并注入spring的容器即可***加上@Service***,该监听器仅针对网关服的客户端连接销毁

```java
//在testRPC1和testRPC2的公共库中定义接口
public interface TestRPCService {
    /**
     * 打印
     * @param msg
     */
    void print(String msg);

    /**
     * 加法
     * @param p1
     * @param p2
     * @return
     */
    int addition(int p1,int p2);
}

//在testRPC2 的服务中实现rpc 接口
@ServiceRPC
public class TestRPCServiceImpl implements TestRPCService {

    @Override
    public void print(String msg) {
        System.out.println("received: " + msg);
    }

    @Override
    public int addition(int p1, int p2) {
        return p1+p2;
    }
}
//在testRPC1 的服务中调用rpc 接口

@ServerMark(appName = "game", group = "testRPC1")
@EnableAriesGameConfiguration
public class TestRPCApplication {
    // 指定调用的服务组名
    @ReferenceRPC("testRPC2")
    private TestRPCService testRPCService;
    public static void main(String[] args) throws Throwable {
        SpringApplication.run(TestRPCApplication.class, args);
        SpringUtils.getBean(TestRPCApplication.class).doSomething();
    }
    public void doSomething(){
        int r = testRPCService.addition(1,2);
        testRPCService.print("result: " + r);
    }
}

```

### 工程示例

请移步
[aries-game-sample-server](https://gitee.com/codergit/aries-game-sample-server)
