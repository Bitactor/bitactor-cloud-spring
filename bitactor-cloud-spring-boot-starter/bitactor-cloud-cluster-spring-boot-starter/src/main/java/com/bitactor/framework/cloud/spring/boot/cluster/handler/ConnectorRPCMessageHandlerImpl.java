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

package com.bitactor.framework.cloud.spring.boot.cluster.handler;

import com.bitactor.framework.cloud.spring.controller.extension.ConnectorChannelHandler;
import com.bitactor.framework.cloud.spring.controller.session.SessionId;
import com.bitactor.framework.cloud.spring.core.BitactorApplicationProperties;
import com.bitactor.framework.cloud.spring.model.codec.MessageConnectorData;
import com.bitactor.framework.cloud.spring.model.constants.ProtocolType;
import com.bitactor.framework.cloud.spring.rpc.annotation.ServiceRPC;
import com.bitactor.framework.cloud.spring.rpc.handler.ConnectorRPCMessageHandler;
import com.bitactor.framework.core.logger.Logger;
import com.bitactor.framework.core.logger.LoggerFactory;
import com.bitactor.framework.core.net.api.Channel;
import io.netty.channel.ChannelFuture;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.List;

/**
 * @author WXH
 */
@ServiceRPC
public class ConnectorRPCMessageHandlerImpl implements ConnectorRPCMessageHandler {
    private Logger logger = LoggerFactory.getLogger(ConnectorRPCMessageHandlerImpl.class);
    @Autowired
    private BitactorApplicationProperties bitactorApplicationProperties;
    @Autowired(required = false)
    private ConnectorChannelHandler connectorChannelHandler;


    @Override
    public void broadcastAssign(List<SessionId> sessionIds, ProtocolType protoType, int commandId, byte[] msg) {
        List<Channel<ChannelFuture>> channels = connectorChannelHandler.getConnectorChannels(sessionIds);
        for (Channel<ChannelFuture> channel : channels) {
            channel.send(MessageConnectorData.builder(msg, protoType.valueInt(), commandId));
        }
    }

    @Override
    public void broadcastAll(ProtocolType protoType, int commandId, byte[] msg) {
        Collection<Channel<ChannelFuture>> channels = connectorChannelHandler.getConnectorChannels();
        for (Channel<ChannelFuture> channel : channels) {
            channel.send(MessageConnectorData.builder(msg, protoType.valueInt(), commandId));
        }
    }

    @Override
    public void notify(SessionId sessionId, ProtocolType protoType, int commandId, byte[] msg) {
        Channel<ChannelFuture> channel = connectorChannelHandler.getConnectorChannel(sessionId);
        channel.send(MessageConnectorData.builder(msg, protoType.valueInt(), commandId));
    }
}
