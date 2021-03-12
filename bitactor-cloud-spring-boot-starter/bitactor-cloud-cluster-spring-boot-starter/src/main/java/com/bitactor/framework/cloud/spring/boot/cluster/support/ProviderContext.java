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

import com.bitactor.framework.cloud.spring.rpc.annotation.ServiceRPC;
import com.bitactor.framework.core.code.TClassWrapper;
import com.bitactor.framework.core.logger.Logger;
import com.bitactor.framework.core.logger.LoggerFactory;
import com.bitactor.framework.core.utils.lang.ConcurrentHashSet;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

/**
 * @author WXH
 */
@Component
public class ProviderContext {
    private static final Logger logger = LoggerFactory.getLogger(ProviderContext.class);
    protected final ConcurrentHashSet<TClassWrapper> providerService = new ConcurrentHashSet<TClassWrapper>();

    /**
     * 添加rpc服务的bean类
     *
     * @param bean
     */
    public void addProviderService(Object bean) {
        try {
            Object finalBean = AopProxyUtils.getSingletonTarget(bean);
            Class aClass = bean.getClass();
            if (finalBean != null) {
                aClass = finalBean.getClass();
            }
            if (aClass.isInterface()) {
                return;
            }
            if (AnnotationUtils.getAnnotation(aClass, ServiceRPC.class) != null) {
                // spring 的bean 可能是增强的代理bean,为了实现server 能够被Aspect 使用应传入代理的bean，但是class 必须为原始的class
                TClassWrapper tClassWrapper = new TClassWrapper(aClass);
                tClassWrapper.setProxyBean(bean);
                providerService.add(tClassWrapper);
                logger.info("Add rpc service : [" + aClass.getName() + "]");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new BeanCreationException("Add RPC service exception bean:" + bean.getClass().getName(), e);
        }
    }

    public ConcurrentHashSet<TClassWrapper> getProviderService() {
        return providerService;
    }
}
