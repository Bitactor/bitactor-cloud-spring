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

package com.bitactor.framework.cloud.spring.boot.connector.module;


import com.bitactor.framework.cloud.spring.boot.connector.BitactorConnectorProperties;
import com.bitactor.framework.cloud.spring.boot.connector.config.SpringConnectorConfig;
import com.bitactor.framework.cloud.spring.core.BitactorApplicationProperties;
import com.bitactor.framework.core.config.UrlProperties;
import com.bitactor.framework.core.constant.NetConstants;
import com.bitactor.framework.core.constant.RPCConstants;
import com.bitactor.framework.core.net.api.Channel;
import com.bitactor.framework.core.net.api.ChannelManager;
import com.bitactor.framework.core.net.api.transport.AbstractServer;
import com.bitactor.framework.core.net.netty.server.NettyModeServer;
import io.netty.channel.ChannelFuture;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * @author WXH
 */
public class ConnectorModule {
    private BitactorConnectorProperties properties;
    private BitactorApplicationProperties appProperties;
    private ChannelManager<ChannelFuture> channelManager;
    private AbstractServer<ChannelFuture> server;

    public ConnectorModule(BitactorConnectorProperties properties, BitactorApplicationProperties appProperties, ChannelManager<ChannelFuture> channelManager) {
        this.properties = properties;
        this.appProperties = appProperties;
        this.channelManager = channelManager;
    }

    public SpringConnectorConfig getConnectorConfig() {
        return Optional.ofNullable(getProperties().getConnector()).orElse(new SpringConnectorConfig());
    }

    public BitactorConnectorProperties getProperties() {
        return properties;
    }

    public BitactorApplicationProperties getAppProperties() {
        return appProperties;
    }

    public ChannelManager<ChannelFuture> getChannelManager() {
        return channelManager;
    }

    public void exportConnector() throws Throwable {
        checkCanExport();
        UrlProperties exportUlr = buildUrl();
        server = new NettyModeServer(channelManager, exportUlr);
        server.threadStart().sync();
    }

    private UrlProperties buildUrl() {
        UrlProperties url = getConnectorConfig().toUrl();
        url = url.setGroup(appProperties.getName());
        url = url.setAppId(appProperties.getId());
        url = url.addParameter(RPCConstants.SERVER_DEFINITION_KEY, RPCConstants.SERVER_DEFINITION_CONNECTOR);
        if (!url.hasParameter(NetConstants.BYTE_ODER_BIG_ENDIAN_KEY)) {
            url = url.addParameter(NetConstants.BYTE_ODER_BIG_ENDIAN_KEY, false);
        }
        return url;
    }

    public void shutdown() {
        if (server != null) {
            server.close();
        }
    }

    /**
     * 检测是否可以暴露服务
     */
    private void checkCanExport() {
        if (channelManager == null) {
            throw new NullPointerException("channelManager null ");
        }
    }

    /**
     * 获取集合中的所有存在的channel
     *
     * @param channelIds
     * @return
     */
    public List<Channel<ChannelFuture>> getChannels(List<String> channelIds) {
        return server.getChannels(channelIds);
    }

    /**
     * 返回一个通道实例
     *
     * @param channelId 通道id
     * @return 通道实例
     */
    public Channel<ChannelFuture> getChannel(String channelId) {
        return server.getChannel(channelId);
    }

    /**
     * 获取通道集合
     *
     * @return
     */
    public Collection<Channel<ChannelFuture>> getChannels() {
        return server.getChannels();
    }
}
