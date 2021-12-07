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

package com.bitactor.framework.cloud.spring.boot.client;

import com.bitactor.framework.cloud.spring.boot.client.config.SpringClientConfig;
import com.bitactor.framework.cloud.spring.boot.client.extension.ClientAllReady;
import com.bitactor.framework.cloud.spring.boot.client.extension.ClientEntity;
import com.bitactor.framework.cloud.spring.boot.client.extension.ClientManager;
import com.bitactor.framework.cloud.spring.boot.client.net.ClientChannelManager;
import com.bitactor.framework.cloud.spring.boot.client.sender.ClientChannelNettySendPolicy;
import com.bitactor.framework.cloud.spring.core.BitactorApplicationProperties;
import com.bitactor.framework.cloud.spring.core.BitactorStarter;
import com.bitactor.framework.cloud.spring.core.constants.BitactorStarterType;
import com.bitactor.framework.core.config.UrlProperties;
import com.bitactor.framework.core.logger.Logger;
import com.bitactor.framework.core.logger.LoggerFactory;
import com.bitactor.framework.core.net.netty.client.NettyModeClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * @author WXH
 */
@Component
public class ClientStarter implements BitactorStarter {
    private final static Logger logger = LoggerFactory.getLogger(ClientStarter.class);

    @Autowired
    private BitactorClientProperties bitactorClientProperties;
    @Autowired
    private BitactorApplicationProperties bitactorApplicationProperties;
    @Autowired
    private ClientManager clientManager;
    @Autowired
    private ClientAllReady clientAllReady;
    @Autowired(required = false)
    private ClientChannelNettySendPolicy clientChannelNettySendPolicy;

    @Override
    public String type() {
        return BitactorStarterType.CLIENT;
    }

    @Override
    public void start(ContextRefreshedEvent contextRefreshedEvent) throws Throwable {
        SpringClientConfig clientConfig = bitactorClientProperties.getClient();
        UrlProperties urlProperties = clientConfig.toUrl();
        int count = clientManager.startCount();
        for (int i = 0; i < count; i++) {
            try {
                logger.info("begin build client");
                ClientEntity clientEntity = clientManager.buildNext();
                NettyModeClient client = new NettyModeClient(new ClientChannelManager(clientEntity, clientManager, clientChannelNettySendPolicy), urlProperties);
                clientEntity.setClient(client);
                client.threadStart().sync();
                clientEntity.init();
                clientManager.add(clientEntity);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
        clientAllReady.onAllReadyEvent();
    }

    @Override
    public void shutDown(ContextClosedEvent contextClosedEvent) throws Throwable {
        Collection<ClientEntity> all = clientManager.all();
        for (ClientEntity clientEntity : all) {
            clientEntity.doClose();
        }
    }
}
