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

package com.bitactor.framework.cloud.spring.core;

import com.bitactor.framework.core.constant.CommonConstants;
import com.bitactor.framework.core.utils.lang.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.EmbeddedValueResolver;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;

import java.net.InetAddress;

/**
 * @author WXH
 */
@Configuration
@ConfigurationProperties(prefix = "spring.application")
public class BitactorApplicationProperties implements InitializingBean, BeanFactoryAware {
    @Nullable
    //@Value("${spring.application.name}")
    @NestedConfigurationProperty
    private String name;
    @NestedConfigurationProperty
    private String id;
    @Nullable
    @NestedConfigurationProperty
    private String project;

    private boolean connector;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSID() {
        return getName() + CommonConstants.SUB_SYMBOL + getId();
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public boolean isConnector(){
        return connector;
    }

    protected void setConnector(boolean connector) {
        this.connector = connector;
    }
    private EmbeddedValueResolver embeddedValueResolver;
    @Override
    public void afterPropertiesSet() throws Exception {
        this.setId(this.embeddedValueResolver.resolveStringValue(this.getId()));
        this.setName(this.embeddedValueResolver.resolveStringValue(this.getName()));
        this.setProject(this.embeddedValueResolver.resolveStringValue(this.getProject()));
        if (StringUtils.isEmpty(this.getId())) {
            this.setId(InetAddress.getLocalHost().getHostAddress());
        }
        this.setId(this.embeddedValueResolver.resolveStringValue(this.getId()));
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        if (beanFactory instanceof ConfigurableBeanFactory) {
            this.embeddedValueResolver = new EmbeddedValueResolver((ConfigurableBeanFactory) beanFactory);
        }
    }
}
