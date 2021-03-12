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

import com.bitactor.framework.cloud.spring.boot.cluster.config.SpringConsumerConfig;
import com.bitactor.framework.cloud.spring.boot.cluster.config.SpringProviderConfig;
import com.bitactor.framework.cloud.spring.boot.cluster.config.SpringRegistryConfig;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.EmbeddedValueResolver;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.context.annotation.Configuration;

/**
 * @author WXH
 */
@Configuration
@ConfigurationProperties(prefix = "spring.bitactor.cluster")
public class BitactorClusterProperties implements InitializingBean, BeanFactoryAware {
    private EmbeddedValueResolver embeddedValueResolver;

    @NestedConfigurationProperty
    private SpringRegistryConfig registry = new SpringRegistryConfig();
    @NestedConfigurationProperty
    private SpringProviderConfig provider = new SpringProviderConfig();
    @NestedConfigurationProperty
    private SpringConsumerConfig consumer = new SpringConsumerConfig();

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        if (beanFactory instanceof ConfigurableBeanFactory) {
            this.embeddedValueResolver = new EmbeddedValueResolver((ConfigurableBeanFactory) beanFactory);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // TODO  对string 的配置进行 resolveStringValue
    }

    public SpringRegistryConfig getRegistry() {
        return registry;
    }

    public void setRegistry(SpringRegistryConfig registry) {
        this.registry = registry;
    }

    public SpringProviderConfig getProvider() {
        return provider;
    }

    public void setProvider(SpringProviderConfig provider) {
        this.provider = provider;
    }

    public SpringConsumerConfig getConsumer() {
        return consumer;
    }

    public void setConsumer(SpringConsumerConfig consumer) {
        this.consumer = consumer;
    }
}
