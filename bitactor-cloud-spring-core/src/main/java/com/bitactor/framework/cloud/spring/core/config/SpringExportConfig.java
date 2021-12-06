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

package com.bitactor.framework.cloud.spring.core.config;


import com.bitactor.framework.core.config.UrlProperties;
import com.bitactor.framework.core.constant.NetConstants;
import com.bitactor.framework.core.utils.collection.CollectionUtils;
import com.bitactor.framework.core.utils.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author WXH
 */
public class SpringExportConfig {
    /**
     * 主机地址
     */
    private String host;

    /**
     * 暴露的端口
     */
    private Integer port;

    /**
     * netty 服务工作（worker）线程池 大小
     */
    private Integer ioThreads;

    /**
     * 最大连接数，0表示不限制，默认： 0
     */
    private Integer accepts;

    /**
     * 消息解码器类
     */
    private String codec;

    /**
     * 是否开启心跳，默认开启
     */
    private Boolean openHeartbeat;
    /**
     * 心跳超时毫秒数，默认60000ms，当开启心跳时若该值内没有接收到心跳消息，关闭指定连接
     */
    private Integer heartbeatTimeout;
    /**
     * 心跳周期,默认10000ms
     */
    private Long heartbeatPeriod;
    /**
     * 握手消息的数据获取的类集合
     */
    private Set<String> handShakeDataBoundClasses;
    /**
     * 网络协议，目前支持TCP,KCP
     */
    private String netProtocol;
    /**
     * 网络通道初始化处理类
     */
    private String channelInitClazz;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Integer getIoThreads() {
        return ioThreads;
    }

    public void setIoThreads(Integer ioThreads) {
        this.ioThreads = ioThreads;
    }

    public Integer getAccepts() {
        return accepts;
    }

    public void setAccepts(Integer accepts) {
        this.accepts = accepts;
    }

    public String getCodec() {
        return codec;
    }

    public void setCodec(String codec) {
        this.codec = codec;
    }

    public Boolean getOpenHeartbeat() {
        return openHeartbeat;
    }

    public void setOpenHeartbeat(Boolean openHeartbeat) {
        this.openHeartbeat = openHeartbeat;
    }

    public Integer getHeartbeatTimeout() {
        return heartbeatTimeout;
    }

    public void setHeartbeatTimeout(Integer heartbeatTimeout) {
        this.heartbeatTimeout = heartbeatTimeout;
    }

    public Long getHeartbeatPeriod() {
        return heartbeatPeriod;
    }

    public void setHeartbeatPeriod(Long heartbeatPeriod) {
        this.heartbeatPeriod = heartbeatPeriod;
    }

    public Set<String> getHandShakeDataBoundClasses() {
        return handShakeDataBoundClasses;
    }

    public void setHandShakeDataBoundClasses(Set<String> handShakeDataBoundClasses) {
        this.handShakeDataBoundClasses = handShakeDataBoundClasses;
    }

    public String getNetProtocol() {
        return netProtocol;
    }

    public void setNetProtocol(String netProtocol) {
        this.netProtocol = netProtocol;
    }

    public String getChannelInitClazz() {
        return channelInitClazz;
    }

    public void setChannelInitClazz(String channelInitClazz) {
        this.channelInitClazz = channelInitClazz;
    }

    public UrlProperties toUrl() {
        Map<String, String> parameters = new HashMap<String, String>();
        if (StringUtils.isNotEmpty(codec)) {
            parameters.put(NetConstants.CODEC_KEY, codec);
        }
        if (CollectionUtils.isNotEmpty(handShakeDataBoundClasses)) {
            parameters.put(NetConstants.HAND_SHAKE_DATA_BOUND_CLASS_KEY, StringUtils.join(handShakeDataBoundClasses, ","));
        }
        if (ioThreads != null && ioThreads > 0) {
            parameters.put(NetConstants.IO_THREADS_KEY, ioThreads.toString());
        }
        if (accepts != null && accepts > 0) {
            parameters.put(NetConstants.ACCEPTS_KEY, accepts.toString());
        }
        if (heartbeatTimeout != null && heartbeatTimeout > 1000) {
            parameters.put(NetConstants.HEARTBEAT_TIMEOUT_KEY, heartbeatTimeout.toString());
        }
        if (openHeartbeat != null) {
            parameters.put(NetConstants.HEARTBEAT_OPEN_KEY, openHeartbeat.toString());
        }
        if (heartbeatPeriod != null && heartbeatPeriod > 1000) {
            parameters.put(NetConstants.HEARTBEAT_PERIOD_KEY, heartbeatPeriod.toString());
        }
        if (channelInitClazz != null) {
            parameters.put(NetConstants.CHANNEL_INIT_CLASS_KEY, channelInitClazz);
        }
        UrlProperties url = new UrlProperties(NetConstants.DEFAULT_PROTOCOL, getHost(), getPort(), parameters);
        url = netProtocol == null ? url : url.addParameter(NetConstants.NET_PROTOCOL_KEY, netProtocol);
        return url;
    }
}
