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


import com.bitactor.framework.cloud.spring.core.config.SpringExportConfig;
import com.bitactor.framework.core.config.UrlProperties;
import com.bitactor.framework.core.constant.CommonConstants;
import com.bitactor.framework.core.constant.NetConstants;
import com.bitactor.framework.core.constant.RPCConstants;
import com.bitactor.framework.core.utils.lang.StringUtils;
import com.bitactor.framework.core.utils.net.NetUtils;

import java.util.Objects;

/**
 * @author WXH
 */
public class SpringProviderConfig extends SpringExportConfig {
    /**
     * 消费者通道数
     */
    private Integer channelSize;

    public Integer getChannelSize() {
        return channelSize;
    }

    public void setChannelSize(Integer channelSize) {
        this.channelSize = channelSize;
    }

    public UrlProperties toUrl() {
        if (StringUtils.isEmpty(getHost())) {
            setHost(NetUtils.getLocalHost());
        }
        if (getPort() == null || getPort() <= 0) {
            setPort(NetUtils.getAvailablePort());
        }
        UrlProperties url = super.toUrl();
        if (url.getParameter(NetConstants.CODEC_KEY) == null) {
            url = url.addParameter(NetConstants.CODEC_KEY, RPCConstants.DEFAULT_RPC_CODEC);
        }
        if (Objects.nonNull(getChannelSize())) {
            url = url.addParameter(RPCConstants.CONSUMERS_CHANNEL_SIZE_KEY, Math.max(0,Math.min(getChannelSize(), CommonConstants.RUN_THREADS)));
        }
        return url;
    }
}
