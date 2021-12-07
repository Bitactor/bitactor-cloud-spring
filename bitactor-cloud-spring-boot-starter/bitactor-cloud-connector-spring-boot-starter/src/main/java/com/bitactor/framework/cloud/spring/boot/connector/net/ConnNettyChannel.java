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

package com.bitactor.framework.cloud.spring.boot.connector.net;


import com.bitactor.framework.cloud.spring.controller.extension.NettyChannelListener;
import com.bitactor.framework.cloud.spring.controller.session.ClientNetSession;
import com.bitactor.framework.cloud.spring.controller.support.ControllerContext;
import com.bitactor.framework.cloud.spring.core.utils.SpringUtils;
import com.bitactor.framework.cloud.spring.model.codec.MessageConnectorData;
import com.bitactor.framework.core.logger.Logger;
import com.bitactor.framework.core.logger.LoggerFactory;
import com.bitactor.framework.core.net.api.transport.message.MessageWrapper;
import com.bitactor.framework.core.net.netty.channel.ChannelNettySendPolicy;
import com.bitactor.framework.core.net.netty.channel.NettyChannel;
import com.bitactor.framework.core.net.netty.channel.NettyChannelContext;
import org.springframework.beans.BeansException;


/**
 * @author WXH
 */
public class ConnNettyChannel extends NettyChannel {
    private static final Logger logger = LoggerFactory.getLogger(ConnNettyChannel.class);
    private ClientNetSession session;

    private ControllerContext controllerContext;


    public ConnNettyChannel(NettyChannelContext channelContext, ClientNetSession session, ControllerContext controllerContext, ChannelNettySendPolicy sendPolicy) {
        super(channelContext, sendPolicy);
        this.session = session;
        this.controllerContext = controllerContext;
    }

    public ClientNetSession getSession() {
        return session;
    }

    @Override
    public void onReceived(MessageWrapper message) {
        if (!(message instanceof MessageConnectorData)) {
            logger.error("Front server received unknown message : " + message.getClass().getName() + " sessionId : " + session.getSessionId());
            return;
        }
        controllerContext.doConnectRequest(this, session, (MessageConnectorData) message);
    }

    @Override
    public void onActivity() {

    }


    @Override
    public void onDestroy() {
        NettyChannelListener listener = null;
        try {
            listener = SpringUtils.getBean(NettyChannelListener.class);
        } catch (BeansException ignored) {
        }
        if (listener != null) {
            listener.onDestroyEvent(this);
        }
        logger.debug("destroy connector with sessionId : " + session.getSessionId());
    }
}
