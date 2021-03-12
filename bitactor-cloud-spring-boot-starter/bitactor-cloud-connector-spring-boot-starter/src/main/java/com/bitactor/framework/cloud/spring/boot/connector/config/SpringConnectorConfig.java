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

package com.bitactor.framework.cloud.spring.boot.connector.config;

import com.bitactor.framework.cloud.spring.core.config.SpringExportConfig;
import com.bitactor.framework.cloud.spring.model.constants.ConnectorConstants;
import com.bitactor.framework.core.config.UrlProperties;
import com.bitactor.framework.core.constant.NetConstants;
import com.bitactor.framework.core.utils.lang.StringUtils;
import com.bitactor.framework.core.utils.net.NetUtils;

import java.util.Objects;

/**
 * @author WXH
 */
public class SpringConnectorConfig extends SpringExportConfig {
    /**
     * byte[] 的大端小端模式，默认小端,java 是大端，c#小端
     */
    private Boolean bigEndian;
    /**
     * ip限制
     */
    private int ipLimit;
    /**
     * 是否开启channel 有序消息处理线程,如果该值true 则需要msgReceiveThreadPoolOpen
     */
    private Boolean msgReceiveOrderedQueueOpen;
    /**
     * websocket 路径 ws//127.0.0.1:443+路径
     */
    private String wsPath;
    /**
     * 开启ws ssl
     */
    private Boolean openWsSsl;

    public Boolean getBigEndian() {
        return bigEndian;
    }

    public void setBigEndian(Boolean bigEndian) {
        this.bigEndian = bigEndian;
    }


    public int getIpLimit() {
        return ipLimit;
    }

    public void setIpLimit(int ipLimit) {
        this.ipLimit = ipLimit;
    }

    public Boolean getMsgReceiveOrderedQueueOpen() {
        return msgReceiveOrderedQueueOpen;
    }

    public void setMsgReceiveOrderedQueueOpen(Boolean msgReceiveOrderedQueueOpen) {
        this.msgReceiveOrderedQueueOpen = msgReceiveOrderedQueueOpen;
    }

    public String getWsPath() {
        return wsPath;
    }

    public void setWsPath(String wsPath) {
        this.wsPath = wsPath;
    }

    public Boolean getOpenWsSsl() {
        return openWsSsl;
    }

    public void setOpenWsSsl(Boolean openWsSsl) {
        this.openWsSsl = openWsSsl;
    }

    public UrlProperties toUrl() {
        if (StringUtils.isEmpty(getHost())) {
            setHost(NetUtils.getLocalHost());
        }
        if (getPort() == null || getPort() <= 0) {
            setPort(NetUtils.getAvailablePort());
        }
        if (Objects.nonNull(getMsgReceiveThreadPoolOpen()) && getMsgReceiveThreadPoolOpen()) {
            setMsgReceiveOrderedQueueOpen(true);
        }
        if (Objects.isNull(getMsgReceiveThreadPoolOpen()) && Objects.isNull(getMsgReceiveOrderedQueueOpen())) {
            setMsgReceiveThreadPoolOpen(true);
            setMsgReceiveOrderedQueueOpen(true);
        }
        UrlProperties url = super.toUrl();
        if (url.getParameter(NetConstants.CODEC_KEY) == null) {
            url = url.addParameter(NetConstants.CODEC_KEY, ConnectorConstants.DEFAULT_CONNECTOR_CODEC);
        }
        if (!StringUtils.isEmpty(getWsPath())) {
            url = url.addParameter(NetConstants.WS_URL_PATH_KEY, getWsPath());
        }
        if (Objects.nonNull(getOpenWsSsl())) {
            url = url.addParameter(NetConstants.WS_OPEN_SSL, getOpenWsSsl());
        }
        if (msgReceiveOrderedQueueOpen != null) {
            url = url.addParameter(NetConstants.MSG_RECEIVE_ORDERED_QUEUE_OPEN_KEY, msgReceiveOrderedQueueOpen.toString());
        }
        url = url.addParameter(NetConstants.IP_LIMIT_NUM, ipLimit);
        url = bigEndian == null ? url.addParameter(NetConstants.BYTE_ODER_BIG_ENDIAN_KEY, false) : url.addParameter(NetConstants.BYTE_ODER_BIG_ENDIAN_KEY, getBigEndian());
        return url;
    }
}
