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

package com.bitactor.framework.cloud.spring.boot.connector.sender;

import com.bitactor.framework.cloud.spring.controller.extension.ConnectorChannelHandler;
import com.bitactor.framework.cloud.spring.controller.sender.MsgSender;
import com.bitactor.framework.cloud.spring.controller.session.SessionId;
import com.bitactor.framework.cloud.spring.model.codec.MessageConnectorData;
import com.bitactor.framework.cloud.spring.model.utils.MessageUtil;
import com.bitactor.framework.core.logger.Logger;
import com.bitactor.framework.core.logger.LoggerFactory;
import com.bitactor.framework.core.net.api.Channel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * 消息发送器
 *
 * @author WXH
 */
@Component()
public class ConnectorMsgSender extends MsgSender {
    private Logger logger = LoggerFactory.getLogger(ConnectorMsgSender.class);
    private ConnectorChannelHandler connectorChannelHandler;

    @Autowired
    public void setConnectorChannelHandler(ConnectorChannelHandler connectorChannelHandler) {
        this.connectorChannelHandler = connectorChannelHandler;
        // 优先级最低
        if (Objects.isNull(MsgSender.sender)) {
            setSender(this);
        }
    }

    @Override
    protected void sendMsgImpl(SessionId sessionId, int commandId, Object msg) throws Throwable {
        Channel channel = connectorChannelHandler.getConnectorChannel(sessionId);
        channel.send(MessageConnectorData.builder(MessageUtil.encode(msg), MessageUtil.checkObjType(msg.getClass()).valueInt(), commandId));
    }

    @Override
    protected void broadcastAssignImpl(List<SessionId> sessionIds, int commandId, Object object) throws Throwable {
        List<Channel> channels = connectorChannelHandler.getConnectorChannels(sessionIds);
        for (Channel channel : channels) {
            channel.send(MessageConnectorData.builder(MessageUtil.encode(object), MessageUtil.checkObjType(object.getClass()).valueInt(), commandId));
        }
    }

    @Override
    protected void broadcastAllImpl(String group, int commandId, Object object) throws Throwable {
        logger.error("Bean ConnectorMsgSender nonsupport this method");
    }
}
