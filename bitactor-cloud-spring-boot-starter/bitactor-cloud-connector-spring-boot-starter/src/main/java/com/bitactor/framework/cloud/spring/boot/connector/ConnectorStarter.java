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

package com.bitactor.framework.cloud.spring.boot.connector;


import com.bitactor.framework.cloud.spring.boot.connector.module.ConnectorModule;
import com.bitactor.framework.cloud.spring.boot.connector.net.ConnNettyChannelInit;
import com.bitactor.framework.cloud.spring.boot.connector.net.ConnChannelManager;
import com.bitactor.framework.cloud.spring.boot.connector.sender.ConnectorChannelNettySendPolicy;
import com.bitactor.framework.cloud.spring.controller.extension.ConnectorChannelHandler;
import com.bitactor.framework.cloud.spring.controller.session.SessionId;
import com.bitactor.framework.cloud.spring.controller.support.ControllerContext;
import com.bitactor.framework.cloud.spring.core.BitactorApplicationProperties;
import com.bitactor.framework.cloud.spring.core.BitactorStarter;
import com.bitactor.framework.cloud.spring.core.constants.BitactorStarterType;
import com.bitactor.framework.core.logger.Logger;
import com.bitactor.framework.core.logger.LoggerFactory;
import com.bitactor.framework.core.net.api.Channel;
import com.bitactor.framework.core.utils.collection.CollectionUtils;
import com.bitactor.framework.core.utils.lang.StringUtils;
import io.netty.channel.ChannelFuture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author WXH
 */
@Order(100)
@Component
public class ConnectorStarter implements BitactorStarter, ConnectorChannelHandler {
    private final static Logger logger = LoggerFactory.getLogger(ConnectorStarter.class);
    private ConnectorModule connector;
    @Autowired
    private ControllerContext controllerContext;
    @Autowired
    private BitactorConnectorProperties bitactorConnectorProperties;
    @Autowired
    private BitactorApplicationProperties bitactorApplicationProperties;
    @Autowired(required = false)
    private ConnectorChannelNettySendPolicy connectorChannelNettySendPolicy;
    @Autowired(required = false)
    private ConnNettyChannelInit connChannelInit;

    @Override
    public String type() {
        return BitactorStarterType.CONNECTOR;
    }

    @Override
    public void start(ContextRefreshedEvent contextRefreshedEvent) throws Throwable {
        connector = new ConnectorModule(bitactorConnectorProperties, bitactorApplicationProperties, new ConnChannelManager(controllerContext, connectorChannelNettySendPolicy), connChannelInit);
        connector.exportConnector();
        logger.info("Finish init gateway....");
    }

    @Override
    public void shutDown(ContextClosedEvent contextClosedEvent) throws Throwable {
        if (connector != null) {
            connector.shutdown();
        }
    }

    /**
     * 获取集合中的所有存在的channel
     *
     * @param channelIds
     * @return
     */
    public List<Channel<ChannelFuture>> getConnectorChannels(List<SessionId> channelIds) {
        if (CollectionUtils.isEmpty(channelIds)) {
            return Collections.emptyList();
        }
        if (connector == null) {
            return Collections.emptyList();
        }
        ArrayList<Channel<ChannelFuture>> channels = new ArrayList<>();
        for (SessionId sessionId : channelIds) {
            Channel<ChannelFuture> channel = getConnectorChannel(sessionId);
            if (channel != null) {
                channels.add(channel);
            }
        }
        return channels;
    }

    /**
     * 返回一个通道实例
     *
     * @param sessionId sessionId
     * @return 通道实例
     */
    @Override
    public Channel<ChannelFuture> getConnectorChannel(SessionId sessionId) {
        if (sessionId == null) {
            return null;
        }
        if (StringUtils.isEmpty(sessionId.getChannelId())) {
            return null;
        }
        if (connector == null) {
            return null;
        }
        return connector.getChannel(sessionId.getChannelId());
    }

    /**
     * 获取通道集合
     *
     * @return
     */
    @Override
    public Collection<Channel<ChannelFuture>> getConnectorChannels() {
        if (connector == null) {
            return Collections.emptyList();
        }
        return connector.getChannels();
    }
}
