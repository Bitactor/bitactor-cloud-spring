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

package com.bitactor.framework.cloud.spring.boot.cluster;


import com.bitactor.framework.cloud.spring.boot.cluster.module.ProviderModule;
import com.bitactor.framework.cloud.spring.boot.cluster.register.RegistryManager;
import com.bitactor.framework.cloud.spring.boot.cluster.support.ProviderContext;
import com.bitactor.framework.cloud.spring.controller.support.ControllerContext;
import com.bitactor.framework.cloud.spring.core.BitactorApplicationProperties;
import com.bitactor.framework.cloud.spring.core.BitactorStarter;
import com.bitactor.framework.cloud.spring.core.constants.BitactorStarterType;
import com.bitactor.framework.core.logger.Logger;
import com.bitactor.framework.core.logger.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * @author WXH
 */
@Component
public class ProviderStarter implements BitactorStarter {
    private final static Logger logger = LoggerFactory.getLogger(ProviderStarter.class);
    private ProviderModule provider;
    /**
     * 注册中心管理器
     */
    @Autowired
    private RegistryManager registryManager;
    @Autowired
    private ControllerContext controllerContext;
    @Autowired
    private ProviderContext providerContext;
    @Autowired
    private BitactorClusterProperties bitactorClusterProperties;
    @Autowired
    private BitactorApplicationProperties bitactorApplicationProperties;

    @Override
    public String type() {
        return BitactorStarterType.PROVIDER;
    }

    @Override
    public void start(ContextRefreshedEvent contextRefreshedEvent) throws Throwable {
        provider = new ProviderModule(bitactorApplicationProperties, bitactorClusterProperties, registryManager, providerContext.getProviderService(), controllerContext.getControllerList());
        provider.doExport();
        logger.info("Finish init provider....");
    }

    @Override
    public void shutDown(ContextClosedEvent contextClosedEvent) throws Throwable {
        if (provider != null) {
            provider.shutdown();
        }
    }
}
