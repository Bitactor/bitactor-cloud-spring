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

package com.bitactor.framework.cloud.spring.boot.cluster.support;


import com.bitactor.framework.cloud.spring.boot.cluster.BitactorClusterProperties;
import com.bitactor.framework.cloud.spring.boot.cluster.config.SpringRegistryConfig;
import com.bitactor.framework.cloud.spring.boot.cluster.register.RegistryManager;
import com.bitactor.framework.cloud.spring.core.BitactorApplicationProperties;
import com.bitactor.framework.core.config.UrlProperties;
import com.bitactor.framework.core.constant.CommonConstants;
import com.bitactor.framework.core.constant.RPCConstants;
import com.bitactor.framework.core.registry.api.Registry;

import java.util.HashMap;
import java.util.Map;

/**
 * @author WXH
 */

public abstract class RegistrySupport {

    private BitactorApplicationProperties appProperties;
    private BitactorClusterProperties clusterProperties;
    private RegistryManager registryManager;

    public RegistrySupport(BitactorApplicationProperties appProperties, BitactorClusterProperties clusterProperties, RegistryManager registryManager) {
        this.appProperties = appProperties;
        this.clusterProperties = clusterProperties;
        this.registryManager = registryManager;
    }

    public BitactorApplicationProperties getAppProperties() {
        return appProperties;
    }

    public BitactorClusterProperties getClusterProperties() {
        return clusterProperties;
    }

    /**
     * 构建连接注册中心的url
     *
     * @return
     */
    private UrlProperties buildRegistryUrl() {
        SpringRegistryConfig registryConfig = getClusterProperties().getRegistry();
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(RPCConstants.SIDE_KEY, getSide());
        parameters.put(CommonConstants.NACOS_GROUP_KEY, getAppProperties().getProject());
        parameters.put(CommonConstants.BACKUP_KEY, registryConfig.getBackup());
        UrlProperties url = new UrlProperties(registryConfig.getProtocol(), registryConfig.getUsername(), registryConfig.getPassword(), registryConfig.getHost(), registryConfig.getPort(), parameters);
        url = url.setGroup(getAppProperties().getName());
        url = url.setAppId(getAppProperties().getId());
        url = url.addParameter(RPCConstants.REGISTRY_ROOT_KEY, getAppProperties().getName());
        return url;
    }

    protected String getAppId() {
        return getAppProperties().getSID();
    }

    protected Registry getRegistry() {
        return registryManager.getRegistry(buildRegistryUrl());
    }

    /**
     * 将开启的服务Url转换成注册中心的url
     *
     * @param original 原始传入的url
     */
    protected abstract UrlProperties transformRegistryUrl(UrlProperties original, String category);

    /**
     * 获取注册到中心的类型
     *
     * @return
     */
    protected abstract String getSide();
}
