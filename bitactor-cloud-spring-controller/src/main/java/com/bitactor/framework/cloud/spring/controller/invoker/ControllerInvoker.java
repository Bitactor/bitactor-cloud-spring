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

package com.bitactor.framework.cloud.spring.controller.invoker;


import com.bitactor.framework.cloud.spring.model.utils.MessageUtil;
import com.bitactor.framework.cloud.spring.controller.annotation.BitactorController;
import com.bitactor.framework.cloud.spring.controller.annotation.BitactorRequestMapping;
import com.bitactor.framework.cloud.spring.controller.annotation.ProtocolBody;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * @author WXH
 */

public class ControllerInvoker {
    private Class instanceCls;
    private Class reqProtoClass;
    private Class respProtoClass;
    private Object instance;
    private Method method;
    private BitactorRequestMapping mapping;

    private BitactorController controller;

    public ControllerInvoker(Class instanceCls, Method method, BitactorRequestMapping mapping, BitactorController controller) {
        this.instanceCls = instanceCls;
        this.method = method;
        this.mapping = mapping;
        this.controller = controller;
        this.reqProtoClass = getRequestProtoClass(method);
        this.respProtoClass = getResponseProtoClass(method);
    }

    public static Class getRequestProtoClass(Method method) {
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        Class<?>[] parameterTypes = method.getParameterTypes();
        Class<?> reqClass = null;
        int paramLength = parameterTypes.length;
        for (int i = 0; i < paramLength; i++) {
            Annotation[] annotations = parameterAnnotations[i];
            if (annotations != null && annotations.length > 0) {
                for (Annotation annotation : annotations) {
                    if (annotation instanceof ProtocolBody) {
                        reqClass = parameterTypes[i];
                        break;
                    }
                }
            }
        }
        return reqClass;
    }

    public static Class getResponseProtoClass(Method method) {
        return method.getReturnType();
    }

    public Class getReqProtoClass() {
        return reqProtoClass;
    }

    public Class getRespProtoClass() {
        return respProtoClass;
    }

    public int getReqCommandID() {
        return mapping.reqCmd() != 0 ? mapping.reqCmd() : MessageUtil.getCommandId(getReqProtoClass());
    }

    public int getRespCommandID() {
        return mapping.respCmd() != 0 ? mapping.respCmd() : MessageUtil.getCommandId(getRespProtoClass());
    }

    public String getReqCommand() {
        return MessageUtil.getCommand(getReqProtoClass());
    }

    public String getRespCommand() {
        return MessageUtil.getCommand(getRespProtoClass());
    }

    public BitactorController getController() {
        return controller;
    }

    public Class getInstanceCls() {
        return instanceCls;
    }

    public Method getMethod() {
        return method;
    }

    public BitactorRequestMapping getMapping() {
        return mapping;
    }

    public Object getInstance() {
        return instance;
    }

    public void setInstance(Object instance) {
        this.instance = instance;
    }

    /**
     * 是否是异步执行，controller 优先于 mapping
     *
     * @return
     */
    public boolean async() {
        if (getController().async()) {
            return true;
        }
        return getMapping().async();
    }

    /**
     * 是否是需要授权，controller 优先于 mapping
     *
     * @return
     */
    public boolean auth() {
        if (getController().auth()) {
            return true;
        }
        return getMapping().auth();
    }

}
