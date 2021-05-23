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

package com.bitactor.framework.cloud.spring.controller.annotation;


import com.bitactor.framework.cloud.spring.model.constants.ProtocolType;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface BitactorRequestMapping {
    ProtocolType protocol() default ProtocolType.PROTO;

    /**
     * 是否异步
     *
     * @return
     */
    boolean async() default false;
    /**
     * 是否需要授权
     *
     * @return
     */
    boolean auth() default false;

    /**
     * 请求消息的序列化id，默认序列化对象名的hashcode
     *
     * @return
     */
    int reqCmd() default 0;

    /**
     * 响应消息的序列化id，默认序列化对象名的hashcode
     *
     * @return
     */
    int respCmd() default 0;
}
