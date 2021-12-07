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

package com.bitactor.framework.cloud.spring.boot.client.net;


import com.bitactor.framework.cloud.spring.boot.client.extension.ClientEntity;
import com.bitactor.framework.cloud.spring.boot.client.extension.ClientManager;
import com.bitactor.framework.cloud.spring.boot.client.sender.ClientChannelNettySendPolicy;
import com.bitactor.framework.core.net.api.Channel;
import com.bitactor.framework.core.net.api.ChannelContext;
import com.bitactor.framework.core.net.api.ChannelManager;
import com.bitactor.framework.core.net.netty.channel.NettyChannelContext;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author WXH
 */
public class ClientChannelManager implements ChannelManager {

    private final static ConcurrentHashMap<String, Channel> channels = new ConcurrentHashMap<>();


    private ClientEntity entity;
    private ClientManager clientManager;
    private ClientChannelNettySendPolicy sendPolicy;

    public ClientChannelManager(ClientEntity entity, ClientManager clientManager, ClientChannelNettySendPolicy sendPolicy) {
        this.entity = entity;
        this.clientManager = clientManager;
        this.sendPolicy = sendPolicy;
    }

    public ClientEntity getEntity() {
        return entity;
    }

    @Override
    public Channel registerChannel(ChannelContext channelContext) {
        Channel channel = new ClientChannel((NettyChannelContext) channelContext, this, sendPolicy);
        channels.put(channel.getChannelId(), channel);
        return channel;
    }

    @Override
    public Channel destroyChannel(String channelId) {
        this.clientManager.remove(entity.getUid());
        return channels.remove(channelId);
    }

    @Override
    public void activityChannel(Channel channel) {
        // do nothing
    }

    @Override
    public void shutdownNotify() {
        // doNothing
    }
}
