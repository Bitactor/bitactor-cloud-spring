

<h1 align="center">
 <a href="http://cloud.bitactor.com" title="Bitactor">bitactor</a>
</h1>


  [![JDK](https://img.shields.io/badge/JDK-1.8%2B-green.svg)](https://www.oracle.com/technetwork/java/javase/downloads/index.html)
  [![license](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0)

### 介绍

* Bitactor Cloud 是一款适配[Spring Boot](https://spring.io/projects/spring-boot)，网络层基于[Netty](https://netty.io/)的分布式NIO服务框架，可快速开发出一个易维护、易扩展且稳定高的分布式服务，让开发者专注于业务功能的开发<br>
* 实现了基于注解的消息控制层，让基于长连接网络的请求像 spring MVC 一样简单易用。<br>
* 参考[Apache Dubbo](https://github.com/apache/dubbo)实现的面向接口的定制化RPC，并基于nacos的服务注册中心。<br>
* 适配springboot,并且继承了springboot的所有特性、达到了松耦合的效果，提高了系统的可重用性、可维护性以及可扩展性<br>

### 协议

#### 支持的协议

* TCP   :   常规的长连接协议
* KCP   :   [可靠的UDP协议](https://github.com/szhnet/kcp-netty)
* WS    :   Websocket

#### Bitactor协议格式

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

### 开始使用

```xml
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.2.2.RELEASE</version>
        <relativePath/>
    </parent>
    <dependencies>
        <dependency>
             <groupId>com.bitactor.framework.cloud.spring.boot</groupId>
             <artifactId>bitactor-cloud-connector-spring-boot-starter</artifactId>
             <version>1.0.0</version>
        </dependency>
    </dependencies>
```

### 单服务启动示例

```java
@EnableBitactorClusterConfiguration
@SpringBootApplication()
@BitactorController(connector = "CONNECTOR")
public class SingleSimpleApplication {
    public static void main(String[] args) throws Throwable {
        SingleSimpleApplication.run(SimpleApplication.class, args);
    }
    /**
     * 登录授权
     *
     * @param request LoginAuthReq -> protobuf生成类
     * @param session 
     * @return CommonResp -> protobuf对象
     */
    @BitactorRequestMapping()
    public CommonResp auth(@ProtocolBody LoginAuthReq request, ClientNetSession session) {
        // do something
        return null;
    }
}
```

```yaml
spring:
  application:
    name: "CONNECTOR"
  # bitactor框架配置
  bitactor:
    # 网关服务（gateway）对外暴露配置
    connector:
      # 对外暴露的ip&域名
      host: 127.0.0.1
      # 对外暴露的端口,配置该值后将开启网关服
      port: 8899
      # TCP WS KCP
      net-protocol: TCP
      # ws-path: "/front"
      open-ws-ssl: true
```

#### 分布式服务
   分布式服务启用，仅需添加以下依赖即可，具体使用方法以及更多特性请参见 [官方文档](https://doc.bitactor.com/)
```xml
    <dependency>
        <groupId>com.bitactor.framework.cloud.spring.boot</groupId>
        <artifactId>bitactor-cloud-cluster-spring-boot-starter</artifactId>
        <version>1.0.0</version>
    </dependency>
```
