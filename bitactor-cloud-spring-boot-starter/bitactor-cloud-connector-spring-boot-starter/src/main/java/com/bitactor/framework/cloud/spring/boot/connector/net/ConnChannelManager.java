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


import com.bitactor.framework.cloud.spring.controller.session.ClientNetSession;
import com.bitactor.framework.cloud.spring.controller.support.ControllerContext;
import com.bitactor.framework.core.logger.Logger;
import com.bitactor.framework.core.logger.LoggerFactory;
import com.bitactor.framework.core.net.api.Channel;
import com.bitactor.framework.core.net.api.ChannelContext;
import com.bitactor.framework.core.net.api.ChannelManager;
import com.bitactor.framework.core.net.netty.channel.NettyChannelContext;

/**
 * @author WXH
 */
public class ConnChannelManager implements ChannelManager {
    private static final Logger logger = LoggerFactory.getLogger(ConnChannelManager.class);

    private ControllerContext controllerContext;

    public ConnChannelManager(ControllerContext controllerContext) {
        this.controllerContext = controllerContext;
    }

    @Override
    public Channel registerChannel(ChannelContext channelContext) {
        ClientNetSession session = new ClientNetSession(((NettyChannelContext) channelContext).getContext().channel().id().toString(), controllerContext.getBitactorApplicationProperties().getSID());
        return new ConnNettyChannel((NettyChannelContext) channelContext, session, controllerContext);
    }

    @Override
    public Channel destroyChannel(String channelId) {
        return null;
    }

    @Override
    public void activityChannel(Channel channel) {
        // do nothing
    }

    @Override
    public void shutdownNotify() {
        // do nothing
    }
}
