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

package com.bitactor.framework.cloud.spring.boot.cluster.register;

import com.bitactor.framework.core.config.UrlProperties;
import com.bitactor.framework.core.constant.RPCConstants;
import com.bitactor.framework.core.logger.Logger;
import com.bitactor.framework.core.logger.LoggerFactory;
import com.bitactor.framework.core.registry.api.Registry;
import com.bitactor.framework.core.registry.api.support.AbstractRegistryFactory;
import com.bitactor.framework.core.registry.nacos.NacosRegistryFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author WXH
 */
@Component
public class RegistryManager {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationContext.class);
    private final ConcurrentHashMap<UrlProperties, Registry> REGISTRY_MAP = new ConcurrentHashMap<UrlProperties, Registry>();
    private final ReentrantLock lock = new ReentrantLock();

    public RegistryManager() {
    }

    private AbstractRegistryFactory getFactory(UrlProperties url) {
        if (url.getProtocol().equals(RPCConstants.NACOS_REGISTRY)) {
            return new NacosRegistryFactory();
        }
        return null;
    }

    /**
     * 获取注册中心注册器
     *
     * @param url
     * @return
     */
    public Registry getRegistry(UrlProperties url) {
        lock.lock();
        Registry registry = null;
        try {
            if (!REGISTRY_MAP.containsKey(url)) {
                registry = getFactory(url).getRegistry(url);
                REGISTRY_MAP.put(url, registry);
            }
            registry = REGISTRY_MAP.get(url);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Get registry error url :" + url.toFullString(), e);
        } finally {
            lock.unlock();
        }
        return registry;
    }
}
