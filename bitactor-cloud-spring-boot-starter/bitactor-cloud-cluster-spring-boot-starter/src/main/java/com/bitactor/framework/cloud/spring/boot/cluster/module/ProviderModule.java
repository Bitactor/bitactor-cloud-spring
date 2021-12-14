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

package com.bitactor.framework.cloud.spring.boot.cluster.module;


import com.bitactor.framework.cloud.spring.boot.cluster.BitactorClusterProperties;
import com.bitactor.framework.cloud.spring.boot.cluster.config.SpringProviderConfig;
import com.bitactor.framework.cloud.spring.boot.cluster.net.ProviderNettyChannelInit;
import com.bitactor.framework.cloud.spring.boot.cluster.register.RegistryManager;
import com.bitactor.framework.cloud.spring.boot.cluster.sender.ProviderChannelNettySendPolicy;
import com.bitactor.framework.cloud.spring.boot.cluster.support.RegistrySupport;
import com.bitactor.framework.cloud.spring.core.BitactorApplicationProperties;
import com.bitactor.framework.core.Version;
import com.bitactor.framework.core.code.TClassWrapper;
import com.bitactor.framework.core.config.UrlProperties;
import com.bitactor.framework.core.constant.CommonConstants;
import com.bitactor.framework.core.constant.NetConstants;
import com.bitactor.framework.core.constant.RPCConstants;
import com.bitactor.framework.core.logger.Logger;
import com.bitactor.framework.core.logger.LoggerFactory;
import com.bitactor.framework.core.rpc.api.support.AbstractExport;
import com.bitactor.framework.core.rpc.netty.ProviderExport;
import com.bitactor.framework.core.utils.assist.ConfigUtils;
import com.bitactor.framework.core.utils.collection.CollectionUtils;
import com.bitactor.framework.core.utils.lang.StringUtils;
import io.netty.channel.ChannelFuture;

import java.util.*;
import java.util.concurrent.TimeoutException;


/**
 * @author WXH
 */
public class ProviderModule extends RegistrySupport {
    private static final Logger logger = LoggerFactory.getLogger(ProviderModule.class);
    private AbstractExport<ChannelFuture> export;
    private Set<TClassWrapper> serviceWrapper;
    private Set<String> controllerList;
    private ProviderChannelNettySendPolicy sendPolicy;
    private ProviderNettyChannelInit providerChannelInit;

    public ProviderModule(BitactorApplicationProperties appProperties
            , BitactorClusterProperties clusterProperties
            , RegistryManager registryManager
            , Set<TClassWrapper> serviceWrapper
            , Set<String> controllerList
            , ProviderChannelNettySendPolicy sendPolicy
            , ProviderNettyChannelInit providerChannelInit) {
        super(appProperties, clusterProperties, registryManager);
        this.serviceWrapper = serviceWrapper;
        this.controllerList = controllerList;
        this.sendPolicy = sendPolicy;
        this.providerChannelInit = providerChannelInit;
    }


    public void doExport() throws Throwable {
        checkCanExport();
        export = new ProviderExport(sendPolicy, providerChannelInit);
        UrlProperties providerUrl = buildProviderUrl();
        export.addUrl(providerUrl);
        if (CollectionUtils.isEmpty(serviceWrapper)) {
            throw new IllegalAccessException("provider services can not be empty.");
        }
        for (TClassWrapper serviceWrapper : serviceWrapper) {
            // bean 可能是代理对象
            export.addServiceProxyBean(serviceWrapper.getBean(), serviceWrapper.getTClass());
        }
        export.exportLocal();
        if (export.isExport()) {
            getRegistry().register(transformRegistryUrl(export.getUrl(), RPCConstants.PROVIDERS_CATEGORY));
        } else {
            throw new TimeoutException("Registry failed by  server not export ,service application : " + getAppProperties().getName());
        }
    }

    public void shutdown() {
        getRegistry().destroy();
        this.export.shutdown();
    }

    /**
     * 将开启的服务Url转换成注册中心的url
     *
     * @param original 原始传入的url
     * @param category 类别 [消费者，提供者]
     * @return UrlProperties url
     */
    protected UrlProperties transformRegistryUrl(UrlProperties original, String category) {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(CommonConstants.PID_KEY, String.valueOf(ConfigUtils.getPid()));
        parameters.put(RPCConstants.SIDE_KEY, getSide());
        parameters.put(CommonConstants.TIMESTAMP_KEY, String.valueOf(System.currentTimeMillis()));
        parameters.put(RPCConstants.CATEGORY_KEY, RPCConstants.PROVIDERS_CATEGORY);
        parameters.put(CommonConstants.VERSION_KEY, Version.getVersion());
        parameters.put(CommonConstants.APPLICATION_KEY, getAppId());
        parameters.put(CommonConstants.GROUP_KEY, getAppProperties().getName());
        return original.addParameters(parameters);
    }

    @Override
    protected String getSide() {
        return RPCConstants.PROVIDER_SIDE;
    }

    /**
     * 构建基础的注册中心url,服务暴露后的url在此基础上会增加一些数据
     *
     * @return url
     * @see ProviderExport
     */
    private UrlProperties buildProviderUrl() {
        UrlProperties url = Optional.ofNullable(getClusterProperties().getProvider()).orElse(new SpringProviderConfig()).toUrl();
        url = url.setGroup(getAppProperties().getName());
        url = url.setAppId(getAppProperties().getId());
        url = url.addParameter(CommonConstants.VERSION_KEY, Version.getVersion());
        url = url.addParameter(RPCConstants.SERVER_DEFINITION_KEY, RPCConstants.SERVER_DEFINITION_BACKEND_PROVIDER);
        if (!CollectionUtils.isEmpty(controllerList)) {
            url = url.setParameterList(NetConstants.CONTROLLER_COMMAND_ID_KEY, new ArrayList<>(controllerList));
        }
        // 如果有自定义协议组则更新协议组
        String group = getAppProperties().getName();
        if (!StringUtils.isEmpty(group)) {
            url = url.addParameter(CommonConstants.GROUP_KEY, group);
        }
        String projectName = getAppProperties().getProject();
        if (!StringUtils.isEmpty(projectName)) {
            url = url.addParameter(CommonConstants.NACOS_GROUP_KEY, projectName);
        }
        return url;
    }


    private void checkCanExport() throws IllegalAccessException {
    }
}
