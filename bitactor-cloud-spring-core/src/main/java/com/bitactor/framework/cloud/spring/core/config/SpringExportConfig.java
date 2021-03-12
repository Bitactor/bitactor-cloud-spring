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
     * 网络连接的处理器的线程池名称
     */
    private String threadPool;

    /**
     * 线程数量仅限【fixed】固定线程池
     */
    private Integer threads;

    /**
     * io 线程大小 仅限【fixed】
     */
    private Integer ioThreads;

    /**
     * 线程池队列长度
     */
    private Integer queues;

    /**
     * 最大连接数
     */
    private Integer accepts;

    /**
     * 消息解码器类
     */
    private String codec;
    /**
     * @deprecated 接收消息体的大小
     */
    private Integer payload;
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
     * 是否开启数据消息处理线程池
     */
    private Boolean msgReceiveThreadPoolOpen;
    /**
     * 握手消息的数据获取的类集合
     */
    private Set<String> handShakeDataBoundClasses;
    /**
     * 网络协议，目前支持TCP,KCP
     */
    private String netProtocol;

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

    public String getThreadPool() {
        return threadPool;
    }

    public void setThreadPool(String threadPool) {
        this.threadPool = threadPool;
    }

    public Integer getThreads() {
        return threads;
    }

    public void setThreads(Integer threads) {
        this.threads = threads;
    }

    public Integer getIoThreads() {
        return ioThreads;
    }

    public void setIoThreads(Integer ioThreads) {
        this.ioThreads = ioThreads;
    }

    public Integer getQueues() {
        return queues;
    }

    public void setQueues(Integer queues) {
        this.queues = queues;
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

    public Integer getPayload() {
        return payload;
    }

    public void setPayload(Integer payload) {
        this.payload = payload;
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

    public Boolean getMsgReceiveThreadPoolOpen() {
        return msgReceiveThreadPoolOpen;
    }

    public void setMsgReceiveThreadPoolOpen(Boolean msgReceiveThreadPoolOpen) {
        this.msgReceiveThreadPoolOpen = msgReceiveThreadPoolOpen;
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

    public UrlProperties toUrl() {
        Map<String, String> parameters = new HashMap<String, String>();
        if (StringUtils.isNotEmpty(threadPool)) {
            parameters.put(NetConstants.THREAD_POOL_KEY, threadPool);
        }
        if (StringUtils.isNotEmpty(codec)) {
            parameters.put(NetConstants.CODEC_KEY, codec);
        }
        if (CollectionUtils.isNotEmpty(handShakeDataBoundClasses)) {
            parameters.put(NetConstants.HAND_SHAKE_DATA_BOUND_CLASS_KEY, StringUtils.join(handShakeDataBoundClasses, ","));
        }
        if (threads != null && threads > 0) {
            parameters.put(NetConstants.THREADS_KEY, threads.toString());
        }
        if (ioThreads != null && ioThreads > 0) {
            parameters.put(NetConstants.IO_THREADS_KEY, ioThreads.toString());
        }
        if (queues != null && queues > 0) {
            parameters.put(NetConstants.QUEUES_KEY, queues.toString());
        }
        if (accepts != null && accepts > 0) {
            parameters.put(NetConstants.ACCEPTS_KEY, accepts.toString());
        }
        if (payload != null && payload > 0) {
            parameters.put(NetConstants.PAYLOAD_KEY, payload.toString());
        }
        if (heartbeatTimeout != null && heartbeatTimeout > 1000) {
            parameters.put(NetConstants.HEARTBEAT_TIMEOUT_KEY, heartbeatTimeout.toString());
        }
        if (openHeartbeat != null) {
            parameters.put(NetConstants.HEARTBEAT_OPEN_KEY, openHeartbeat.toString());
        }
        if (msgReceiveThreadPoolOpen != null) {
            parameters.put(NetConstants.MSG_RECEIVE_THREAD_POOL_OPEN_KEY, msgReceiveThreadPoolOpen.toString());
        }
        if (heartbeatPeriod != null && heartbeatPeriod > 1000) {
            parameters.put(NetConstants.HEARTBEAT_PERIOD_KEY, heartbeatPeriod.toString());
        }
        UrlProperties url = new UrlProperties(NetConstants.DEFAULT_PROTOCOL, getHost(), getPort(), parameters);
        url = netProtocol == null ? url : url.addParameter(NetConstants.NET_PROTOCOL_KEY, netProtocol);
        return url;
    }
}
