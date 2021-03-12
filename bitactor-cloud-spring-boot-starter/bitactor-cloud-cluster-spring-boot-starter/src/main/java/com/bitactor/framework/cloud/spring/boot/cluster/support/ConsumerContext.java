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

import com.alibaba.fastjson.JSON;
import com.bitactor.framework.cloud.spring.boot.cluster.module.ConsumerModule;
import com.bitactor.framework.cloud.spring.controller.bean.MappingInfo;
import com.bitactor.framework.cloud.spring.controller.support.ControllerContext;
import com.bitactor.framework.cloud.spring.core.BitactorApplicationProperties;
import com.bitactor.framework.cloud.spring.rpc.annotation.RefRPC;
import com.bitactor.framework.core.config.UrlProperties;
import com.bitactor.framework.core.constant.NetConstants;
import com.bitactor.framework.core.logger.Logger;
import com.bitactor.framework.core.logger.LoggerFactory;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.List;

/**
 * @author WXH
 */
@Component
public class ConsumerContext implements ApplicationContextAware {
    private static final Logger logger = LoggerFactory.getLogger(ConsumerContext.class);

    private ApplicationContext applicationContext;
    @Autowired
    private BitactorApplicationProperties bitactorApplicationProperties;

    @Autowired
    private ControllerContext controllerContext;

    /**
     * 检查并注入rpc的依赖
     */
    public void checkInjectRPCRef(ConsumerModule consumer) {
        List<String> referenceMap = Arrays.asList(applicationContext.getBeanDefinitionNames());
        for (String beanName : referenceMap) {
            Object bean = applicationContext.getBean(beanName);
            Object finalBean = AopProxyUtils.getSingletonTarget(bean);
            if (finalBean != null) {
                bean = finalBean;
            }
            Class objClz = bean.getClass();

            for (Field field : objClz.getDeclaredFields()) {
                //System.out.println("objClz: " + objClz.getSimpleName() + " field: " + field.getName());
                RefRPC reference = AnnotationUtils.getAnnotation(field, RefRPC.class);
                if (reference != null) {
                    if (consumer.existApi(reference.value(), field.getType())) {
                        field.setAccessible(true);
                        Object refRpc = null;
                        try {
                            refRpc = consumer.get(reference.value(), field.getType());
                        } catch (Throwable throwable) {
                            throwable.printStackTrace();
                        }
                        try {
                            field.set(bean, refRpc);
                            logger.info("inject rpc service field name :" + field.getName() + " bean name: " + beanName);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

    }

    /**
     * 初始化命令id
     *
     * @param url
     */
    public void initCommandId(UrlProperties url) {
        if (!bitactorApplicationProperties.isConnector()) {
            return;
        }
        List<String> list = url.getParameterList(NetConstants.CONTROLLER_COMMAND_ID_KEY);
        for (String str : list) {
            try {
                MappingInfo mappingInfo = JSON.parseObject(URLDecoder.decode(str, "UTF-8"), MappingInfo.class);
                if (mappingInfo != null) {
                    if (mappingInfo.getConnector().equals(bitactorApplicationProperties.getName())) {
                        controllerContext.boundCommandIdToAppGroup(mappingInfo);
                    }
                }
            } catch (IllegalArgumentException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
