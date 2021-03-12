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

package com.bitactor.framework.cloud.spring.rpc.extension;

/**
 * @author WXH
 */
public interface NetRPC {
    /**
     * 获取消费者对应接口的代理类
     *
     * @param serverMark 服务标记 可以是 url 中的group,也可以是groupAndId
     * @param service    服务接口
     * @param <T>        类型
     * @return 接口代理类
     * @throws Throwable 异常
     */
    <T> T rpc(String serverMark, Class<T> service) throws Throwable;
}
