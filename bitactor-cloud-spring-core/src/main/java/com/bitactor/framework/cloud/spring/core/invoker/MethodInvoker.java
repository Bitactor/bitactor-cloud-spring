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

package com.bitactor.framework.cloud.spring.core.invoker;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * @author WXH
 */

public class MethodInvoker {
    private Class instanceCls;
    // 默认为空
    private Object instance;
    private Method method;

    public MethodInvoker(Class instanceCls, Method method) {
        this.instanceCls = instanceCls;
        this.method = method;
    }

    public Class getInstanceCls() {
        return instanceCls;
    }

    public Method getMethod() {
        return method;
    }

    public Object getInstance() {
        return instance;
    }
    public void setInstance(Object instance) {
        this.instance = instance;
    }

    /**
     * 获取实例对象的指定注解
     *
     * @param aClass
     * @param <A>
     * @return
     */
    public <A extends Annotation> A getObjAnnotation(Class<A> aClass) {
        return (A) instanceCls.getAnnotation(aClass);
    }

    /**
     * 获取实例对象的指定注解
     *
     * @param aClass
     * @param <A>
     * @return
     */
    public <A extends Annotation> A getMethodAnnotation(Class<A> aClass) {
        return this.method.getAnnotation(aClass);
    }

}
