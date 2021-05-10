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

package com.bitactor.framework.cloud.spring.core.support;

import com.bitactor.framework.cloud.spring.core.annotation.lifecycle.*;
import com.bitactor.framework.cloud.spring.core.invoker.MethodInvoker;
import com.bitactor.framework.core.logger.Logger;
import com.bitactor.framework.core.logger.LoggerFactory;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author WXH
 */
@Component
public class LifecycleContext {
    private static final Logger logger = LoggerFactory.getLogger(LifecycleContext.class);
    /**
     * 含有生命周期注解的bean
     */
    protected final List<MethodInvoker> lifecycleStartBefore = new ArrayList<>();
    protected final List<MethodInvoker> lifecycleStartAfter = new ArrayList<>();
    protected final List<MethodInvoker> lifecycleShutDownBefore = new ArrayList<>();
    protected final List<MethodInvoker> lifecycleShutDownAfter = new ArrayList<>();


    /**
     * 初始化生命周期的bean
     *
     * @param bean
     */
    public void addLifecycleBean(Object bean) {
        try {
            Object finalBean = AopProxyUtils.getSingletonTarget(bean);
            if (finalBean != null) {
                bean = finalBean;
            }
            Class aClass = bean.getClass();
            if (aClass.isInterface()) {
                return;
            }
            if (AnnotationUtils.getAnnotation(aClass, Lifecycle.class) != null) {
                for (Method method : aClass.getDeclaredMethods()) {
                    if (AnnotationUtils.getAnnotation(method, StartBefore.class) != null) {
                        lifecycleStartBefore.add(new MethodInvoker(aClass, method));
                        logger.info("Add lifecycle start before : [" + aClass.getName() + "]");
                    }
                    if (AnnotationUtils.getAnnotation(method, StartAfter.class) != null) {
                        lifecycleStartAfter.add(new MethodInvoker(aClass, method));
                        logger.info("Add lifecycle start after : [" + aClass.getName() + "]");
                    }
                    if (AnnotationUtils.getAnnotation(method, ShutDownBefore.class) != null) {
                        lifecycleShutDownBefore.add(new MethodInvoker(aClass, method));
                        logger.info("Add lifecycle shut down before : [" + aClass.getName() + "]");
                    }
                    if (AnnotationUtils.getAnnotation(method, ShutDownAfter.class) != null) {
                        lifecycleShutDownAfter.add(new MethodInvoker(aClass, method));
                        logger.info("Add lifecycle shut down after : [" + aClass.getName() + "]");
                    }
                }
                // 排序, 以order 值降序排序
                lifecycleStartBefore.sort((v1, v2) -> {
                    return v2.getObjAnnotation(Lifecycle.class).value() - v1.getObjAnnotation(Lifecycle.class).value();
                });
                lifecycleStartAfter.sort((v1, v2) -> {
                    return v2.getObjAnnotation(Lifecycle.class).value() - v1.getObjAnnotation(Lifecycle.class).value();
                });
                lifecycleShutDownBefore.sort((v1, v2) -> {
                    return v2.getObjAnnotation(Lifecycle.class).value() - v1.getObjAnnotation(Lifecycle.class).value();
                });
                lifecycleShutDownAfter.sort((v1, v2) -> {
                    return v2.getObjAnnotation(Lifecycle.class).value() - v1.getObjAnnotation(Lifecycle.class).value();
                });

            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new BeanCreationException("Add lifecycle exception bean:" + bean.getClass().getName(), e);
        }
    }

    public List<MethodInvoker> getLifecycleStartBefore() {
        return lifecycleStartBefore;
    }

    public List<MethodInvoker> getLifecycleStartAfter() {
        return lifecycleStartAfter;
    }

    public List<MethodInvoker> getLifecycleShutDownBefore() {
        return lifecycleShutDownBefore;
    }

    public List<MethodInvoker> getLifecycleShutDownAfter() {
        return lifecycleShutDownAfter;
    }
}
