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

package com.bitactor.framework.cloud.spring.boot.client.config;


import com.bitactor.framework.cloud.spring.core.config.SpringExportConfig;
import com.bitactor.framework.cloud.spring.model.constants.ConnectorConstants;
import com.bitactor.framework.core.config.UrlProperties;
import com.bitactor.framework.core.constant.NetConstants;
import com.bitactor.framework.core.utils.lang.StringUtils;

import java.util.Objects;

/**
 * @author WXH
 */
public class SpringClientConfig extends SpringExportConfig {
    /**
     * byte[] 的大端小端模式，默认小端,java 是大端，c#小端
     */
    private Boolean bigEndian;
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
        url = bigEndian == null ? url.addParameter(NetConstants.BYTE_ODER_BIG_ENDIAN_KEY, false) : url.addParameter(NetConstants.BYTE_ODER_BIG_ENDIAN_KEY, getBigEndian());
        return url;
    }
}
