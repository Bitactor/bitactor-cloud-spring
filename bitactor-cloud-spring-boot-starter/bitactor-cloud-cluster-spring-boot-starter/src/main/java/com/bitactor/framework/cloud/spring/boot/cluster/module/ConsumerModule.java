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
import com.bitactor.framework.cloud.spring.boot.cluster.config.SpringConsumerConfig;
import com.bitactor.framework.cloud.spring.boot.cluster.net.ConsumerNettyChannelInit;
import com.bitactor.framework.cloud.spring.boot.cluster.register.RegistryManager;
import com.bitactor.framework.cloud.spring.boot.cluster.sender.ConsumerChannelNettySendPolicy;
import com.bitactor.framework.cloud.spring.boot.cluster.support.ConsumerBoundApiEvent;
import com.bitactor.framework.cloud.spring.boot.cluster.support.RegistrySupport;
import com.bitactor.framework.cloud.spring.core.BitactorApplicationProperties;
import com.bitactor.framework.cloud.spring.rpc.bean.AppNameInfo;
import com.bitactor.framework.cloud.spring.rpc.extension.Consumer;
import com.bitactor.framework.cloud.spring.rpc.utils.AppNameUtils;
import com.bitactor.framework.core.Version;
import com.bitactor.framework.core.config.UrlProperties;
import com.bitactor.framework.core.constant.CommonConstants;
import com.bitactor.framework.core.constant.NetConstants;
import com.bitactor.framework.core.constant.RPCConstants;
import com.bitactor.framework.core.exception.NoSuchServiceException;
import com.bitactor.framework.core.logger.Logger;
import com.bitactor.framework.core.logger.LoggerFactory;
import com.bitactor.framework.core.registry.api.NotifyListener;
import com.bitactor.framework.core.rpc.api.filter.ParameterShallowCopyFilter;
import com.bitactor.framework.core.rpc.api.support.ModeClients;
import com.bitactor.framework.core.rpc.netty.ConsumerBound;
import com.bitactor.framework.core.utils.assist.ConfigUtils;
import com.bitactor.framework.core.utils.collection.CollectionUtils;
import com.bitactor.framework.core.utils.net.NetUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author WXH
 */
public class ConsumerModule extends RegistrySupport implements NotifyListener, Consumer {
    private final Logger logger = LoggerFactory.getLogger(ConsumerModule.class);
    private ConcurrentHashMap<String, ConsumerBound> customerBoundMap = new ConcurrentHashMap<String, ConsumerBound>();
    private UrlProperties subscribeUrl;
    private ConsumerBoundApiEvent boundApiEvent;
    private ConsumerChannelNettySendPolicy sendPolicy;
    private ConsumerNettyChannelInit consumerChannelInit;

    public ConsumerModule(BitactorApplicationProperties appProperties, BitactorClusterProperties backendProperties, RegistryManager registryManager, ConsumerChannelNettySendPolicy sendPolicy, ConsumerNettyChannelInit consumerChannelInit) {
        this(appProperties, backendProperties, registryManager, null, sendPolicy, consumerChannelInit);
    }

    public ConsumerModule(BitactorApplicationProperties appProperties
            , BitactorClusterProperties clusterProperties
            , RegistryManager registryManager
            , ConsumerBoundApiEvent boundApiEvent
            , ConsumerChannelNettySendPolicy sendPolicy
            , ConsumerNettyChannelInit consumerChannelInit) {
        super(appProperties, clusterProperties, registryManager);
        this.boundApiEvent = boundApiEvent;
        this.sendPolicy = sendPolicy;
        this.consumerChannelInit = consumerChannelInit;
    }


    public SpringConsumerConfig getConsumerConfig() {
        return Optional.ofNullable(getClusterProperties().getConsumer()).orElse(new SpringConsumerConfig());
    }


    public void reference() {
        if (!checkCanRef()) {
            return;
        }
        subscribeUrl = buildSubscribeUrl();
        getRegistry().subscribe(subscribeUrl, this);
    }

    /**
     * 是否存在此类api
     *
     * @param serverMark
     * @param service
     * @return
     */
    public boolean existApi(String serverMark, Class<?> service) {
        try {
            if (get(serverMark, service) != null) {
                return true;
            }
        } catch (Throwable throwable) {
            return false;
        }
        return false;
    }

    /**
     * 获取消费者对应接口的代理类
     *
     * @param appName 服务标记 可以是 url 中的 group,也可以是 groupAndId
     * @param service 服务接口
     * @param <T>     类型
     * @return 接口代理类
     * @throws Throwable 异常
     */
    @Override
    public <T> T get(String appName, Class<T> service) throws Throwable {
        AppNameInfo appNameInfo = AppNameUtils.parse(appName);
        ConsumerBound consumerBound = customerBoundMap.get(appNameInfo.getGroup());
        if (consumerBound == null) {
            throw new NoSuchServiceException("Can not find the RPC service,appName : " + appName + " service: " + service.getName());
        }
        if (appNameInfo.isAssign()) {
            return consumerBound.get(appNameInfo.getOriginal(), service);
        }
        return consumerBound.get(service);
    }

    public void shutdown() {
        for (ConsumerBound consumerBound : customerBoundMap.values()) {
            consumerBound.shutdown();
        }
        // 关闭注册器
        getRegistry().destroy();
    }

    /**
     * 检查是否能够进行服务引用
     *
     * @return
     */
    private boolean checkCanRef() {
        if (!customerBoundMap.isEmpty()) {
            return false;
        }
        return true;
    }

    /**
     * 构建订阅url
     *
     * @return
     */
    private UrlProperties buildSubscribeUrl() {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(CommonConstants.PID_KEY, String.valueOf(ConfigUtils.getPid()));
        parameters.put(RPCConstants.SIDE_KEY, getSide());
        parameters.put(CommonConstants.VERSION_KEY, Version.getVersion());
        parameters.put(CommonConstants.TIMESTAMP_KEY, String.valueOf(System.currentTimeMillis()));
        parameters.put(RPCConstants.SERVER_DEFINITION_KEY, RPCConstants.SERVER_DEFINITION_BACKEND_CONSUMER);
        parameters.put(CommonConstants.NACOS_GROUP_KEY, getAppProperties().getProject());
        parameters.put(CommonConstants.GROUP_KEY, getAppProperties().getName());
        UrlProperties consumerUlr = getConsumerConfig().toUrl();
        return consumerUlr.addParameters(parameters);
    }

    @Override
    public void notifyAdd(List<UrlProperties> urls) {
        if (CollectionUtils.isEmpty(urls)) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[Consumer subscribe]:");
        for (UrlProperties url : urls) {
            try {
                sb.append(url.toFullString()).append("\n");
                // 通知对应类型服务无服务提供
                if (url.getProtocol().equals(CommonConstants.EMPTY_PROTOCOL)) {
                    logger.warn("[There are no services available on              ] : " + url.getGroup());
                    continue;
                }
                // 服务者是自己
                if (getAppProperties().getSID().equals(url.getGroupAndId())) {
                    continue;
                }
                ConsumerBound consumerBound = customerBoundMap.get(url.getGroup());
                if (consumerBound == null) {
                    // 若没有已经建立好对应group的 ConsumerBound 就新建一个，
                    // ConsumerBound 的 RouterAdapter、Filter 仅在第一次实例化ConsumerBound的时候添加，
                    // 也就是说RouterAdapter、Filter实在的实例取决于第一次订阅到的UrlProperties
                    consumerBound = new ConsumerBound(url.getGroup(), sendPolicy, consumerChannelInit) {
                        @Override
                        protected void shutdownNotify(ModeClients client) {
                            getRegistry().unregister(client.getUrl());
                        }
                    };
                    consumerBound.addRouterAdapter(subscribeUrl.getParameter(RPCConstants.ROUTER_ADAPTER_KEY, RPCConstants.DEFAULT_ROUTER_ADAPTER));
                    if (url.getParameter(RPCConstants.COPY_RPC_PARAMETERS_FILTER_KEY, RPCConstants.DEFAULT_RPC_COPY_PARAMETERS_FILTER)) {
                        consumerBound.addFilter(new ParameterShallowCopyFilter());
                    }
                    String[] filters = url.getParameter(RPCConstants.CUSTOM_RPC_FILTER_KEY, new String[0]);
                    consumerBound.addStrFilters(Arrays.asList(filters));
                    customerBoundMap.put(url.getGroup(), consumerBound);
                    // 注册消费者
                    doRegistryConsumer(url);
                }
                url = url.addParameter(NetConstants.LOGGER_DELAY_KEY, subscribeUrl.getParameter(NetConstants.LOGGER_DELAY_KEY, NetConstants.DEFAULT_LOGGER_DELAY));
                boolean changeUrl = consumerBound.addUrl(url);
                if (boundApiEvent != null && changeUrl) {
                    boundApiEvent.boundEvent(url);
                }
            } catch (Throwable throwable) {
                logger.error("Consumer subscribe add url failed : " + url.toFullString(), throwable);
            }
        }
        logger.info(sb.toString());

    }

    @Override
    public void notifySub(List<UrlProperties> urls) {
        if (CollectionUtils.isEmpty(urls)) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[Unsubscribe url]:");
        //Collection<UrlProperties> newsUrl = filterOldSub(urls);
        for (UrlProperties url : urls) {
            ConsumerBound consumerBound = customerBoundMap.get(url.getGroup());
            if (consumerBound != null) {
                if (consumerBound.closeClient(url.getGroupAndId())) {
                    sb.append(url.toFullString()).append("\n");
                }
            }
        }
        logger.info(sb.toString());
    }

    /**
     * 注册消费者
     *
     * @param original
     */
    private void doRegistryConsumer(UrlProperties original) {
        //getRegistry().register(transformRegistryUrl(original, RPCConstants.CONSUMERS_CATEGORY));
    }

    @Override
    protected UrlProperties transformRegistryUrl(UrlProperties original, String category) {
        List<String> removes = new ArrayList<String>();
        removes.add(RPCConstants.INTERFACE_KEY);
        removes.add(CommonConstants.APP_ID_KEY);
        original = original.setProtocol(RPCConstants.CONSUMER_PROTOCOL);
        original = original.setPort(0);
        original = original.removeParameters(removes);
        original = original.setHost(NetUtils.getLocalHost());
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(RPCConstants.SIDE_KEY, getSide());
        parameters.put(CommonConstants.PID_KEY, String.valueOf(ConfigUtils.getPid()));
        parameters.put(RPCConstants.CATEGORY_KEY, RPCConstants.CONSUMERS_CATEGORY);
        parameters.put(CommonConstants.TIMESTAMP_KEY, String.valueOf(System.currentTimeMillis()));
        parameters.put(CommonConstants.APPLICATION_KEY, getAppId());
        parameters.put(CommonConstants.NACOS_GROUP_KEY, getAppProperties().getProject());
        parameters.put(CommonConstants.GROUP_KEY, getAppProperties().getName());
        UrlProperties url = original.addParameters(parameters);
        return url;
    }

    @Override
    protected String getSide() {
        return RPCConstants.CONSUMER_SIDE;
    }


}
