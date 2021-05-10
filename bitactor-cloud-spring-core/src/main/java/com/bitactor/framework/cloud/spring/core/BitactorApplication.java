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


import com.bitactor.framework.cloud.spring.core.constants.BitactorStarterType;
import com.bitactor.framework.cloud.spring.core.invoker.MethodInvoker;
import com.bitactor.framework.cloud.spring.core.support.LifecycleContext;
import com.bitactor.framework.core.logger.Logger;
import com.bitactor.framework.core.logger.LoggerFactory;
import com.bitactor.framework.core.utils.collection.CollectionUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author WXH
 */
@Component
public class BitactorApplication implements ApplicationListener<ApplicationContextEvent>, ApplicationContextAware {
    private static final Logger logger = LoggerFactory.getLogger(BitactorApplication.class);
    private ApplicationContext applicationContext;
    @Autowired
    private LifecycleContext lifecycleContext;
    /**
     * 服务启动器
     */
    @Autowired(required = false)
    private List<BitactorStarter> starters;
    @Autowired
    private BitactorApplicationProperties bitactorApplicationProperties;

    private void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        try {
            doLifeCycle(lifecycleContext.getLifecycleStartBefore());

            start(contextRefreshedEvent);

            doLifeCycle(lifecycleContext.getLifecycleStartAfter());
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            ((AnnotationConfigApplicationContext) applicationContext).close();
        }
        logger.info("Start bitactor framework complete");
    }

    /**
     * 启动
     *
     * @param contextRefreshedEvent
     * @throws Throwable
     */
    private void start(ContextRefreshedEvent contextRefreshedEvent) throws Throwable {
        if (CollectionUtils.isEmpty(starters)) {
            return;
        }
        for (BitactorStarter starter : starters) {
            if (BitactorStarterType.CONNECTOR.equals(starter.type())) {
                bitactorApplicationProperties.setConnector(true);
            }
            starter.start(contextRefreshedEvent);
        }
    }

    private void onApplicationEvent(ContextClosedEvent contextClosedEvent) {
        try {
            doLifeCycle(lifecycleContext.getLifecycleShutDownBefore());

            shutDown(contextClosedEvent);

            doLifeCycle(lifecycleContext.getLifecycleShutDownAfter());
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        logger.info("Shut down bitactor framework complete");

    }

    /**
     * 关闭
     *
     * @param contextClosedEvent
     * @throws Throwable
     */
    private void shutDown(ContextClosedEvent contextClosedEvent) throws Throwable {
        if (CollectionUtils.isEmpty(starters)) {
            return;
        }
        for (BitactorStarter starter : starters) {
            starter.shutDown(contextClosedEvent);
        }
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }


    @Override
    public void onApplicationEvent(ApplicationContextEvent event) {
        if (event instanceof ContextRefreshedEvent) {
            onApplicationEvent((ContextRefreshedEvent) event);
        } else if (event instanceof ContextClosedEvent) {
            onApplicationEvent((ContextClosedEvent) event);
        }

    }

    /**
     * 执行生命周期
     *
     * @param lifecycle
     * @throws Throwable
     */
    public void doLifeCycle(List<MethodInvoker> lifecycle) throws Throwable {
        for (MethodInvoker methodInvoker : lifecycle) {
            if (methodInvoker.getInstance() == null) {
                Object bean = applicationContext.getBean(methodInvoker.getInstanceCls());
                if (bean == null) {
                    throw new NullPointerException("Can not find lifecycle MethodInvoker bean: " + methodInvoker.getInstanceCls());
                }
                methodInvoker.setInstance(bean);
            }
            Method method = methodInvoker.getMethod();
            method.setAccessible(true);
            method.invoke(methodInvoker.getInstance());
            method.setAccessible(false);
        }
    }

}
