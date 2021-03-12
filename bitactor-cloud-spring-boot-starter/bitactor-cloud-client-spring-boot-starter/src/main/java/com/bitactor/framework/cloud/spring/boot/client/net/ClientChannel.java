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


import com.bitactor.framework.cloud.spring.model.codec.MessageConnectorData;
import com.bitactor.framework.core.net.api.transport.message.MessageWrapper;
import com.bitactor.framework.core.net.netty.channel.NettyChannel;
import com.bitactor.framework.core.net.netty.channel.NettyChannelContext;

/**
 * @author WXH
 */
public class ClientChannel extends NettyChannel {
    private ClientChannelManager manager;

    public ClientChannel(NettyChannelContext channelContext, ClientChannelManager manager) {
        super(channelContext);
        this.manager = manager;
    }

    @Override
    public void onReceived(MessageWrapper message) {
        try {
            MessageConnectorData connMessage = (MessageConnectorData) message;
            manager.getEntity().onReceived(connMessage);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        manager.getEntity().onDestroy();
    }

    @Override
    public void onActivity() {
        manager.getEntity().onActivity();
    }


}
