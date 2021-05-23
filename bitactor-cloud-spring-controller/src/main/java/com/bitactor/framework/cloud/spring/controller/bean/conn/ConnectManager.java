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

package com.bitactor.framework.cloud.spring.controller.bean.conn;

import java.util.Collection;

/**
 * 连接数据管理器,应该被实现且添加到spring bean中
 *
 * @author WXH
 */
public interface ConnectManager<U, T extends AbstractConnect<U>> {

    T get(U uid);

    boolean add(T connect);

    T remove(U uid);

    int onlineSize();

    Collection<T> all();
}
