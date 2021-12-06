/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bitactor.framework.cloud.spring.boot.cluster.config;


import com.bitactor.framework.core.config.UrlProperties;
import com.bitactor.framework.core.constant.NetConstants;
import com.bitactor.framework.core.constant.RPCConstants;
import com.bitactor.framework.core.utils.lang.StringUtils;
import com.bitactor.framework.core.utils.net.NetUtils;

import java.util.Optional;

/**
 * @author WXH
 */
public class SpringConsumerConfig {
    /**
     * 主机地址
     */
    private String host = NetUtils.getLocalHost();
    /**
     * 端口
     */
    private Integer port = 0;
    /**
     * 订阅对应的服务类型
     * [*]星号代表所有
     */
    private String subscribe;
    /**
     * 路由器类路径名，配置后可在调用同集群内的提供者时通过自定义路由器来选择提供者
     */
    private String routerAdapter;
    /**
     * 是否开启订阅服务的延迟打印，在心跳开启的情况下才会打印
     */
    private Boolean loggerDelay;
    /**
     * 网络通道初始化处理类
     */
    private String channelInitClazz;

    public Boolean getLoggerDelay() {
        return loggerDelay;
    }

    public void setLoggerDelay(Boolean loggerDelay) {
        this.loggerDelay = loggerDelay;
    }

    public String getSubscribe() {
        return Optional.ofNullable(subscribe).orElse("*");
    }

    public void setSubscribe(String subscribe) {
        this.subscribe = subscribe;
    }

    public String getRouterAdapter() {
        return routerAdapter;
    }

    public void setRouterAdapter(String routerAdapter) {
        this.routerAdapter = routerAdapter;
    }

    public String getChannelInitClazz() {
        return channelInitClazz;
    }

    public void setChannelInitClazz(String channelInitClazz) {
        this.channelInitClazz = channelInitClazz;
    }

    public UrlProperties toUrl() {
        UrlProperties url = new UrlProperties(NetConstants.DEFAULT_PROTOCOL, host, port, getSubscribe());
        if (!StringUtils.isEmpty(routerAdapter)) {
            url = url.addParameter(RPCConstants.ROUTER_ADAPTER_KEY, routerAdapter);
        }
        if (!StringUtils.isEmpty(channelInitClazz)) {
            url = url.addParameter(NetConstants.CHANNEL_INIT_CLASS_KEY, channelInitClazz);
        }
        url = getLoggerDelay() != null ? url.addParameter(NetConstants.LOGGER_DELAY_KEY, getLoggerDelay()) : url;
        return url;
    }
}
