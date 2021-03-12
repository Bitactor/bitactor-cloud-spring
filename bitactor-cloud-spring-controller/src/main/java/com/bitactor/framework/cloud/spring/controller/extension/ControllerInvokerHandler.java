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

package com.bitactor.framework.cloud.spring.controller.extension;


import com.bitactor.framework.cloud.spring.model.constants.ProtocolType;
import com.bitactor.framework.cloud.spring.controller.bean.ControllerResp;
import com.bitactor.framework.cloud.spring.controller.session.ClientNetSession;

import java.util.function.BiConsumer;

/**
 * @author WXH
 */
public interface ControllerInvokerHandler {

    /**
     * 同步请求后端服务器，等待消息响应
     *
     * @param session
     * @return
     */
    ControllerResp request(ClientNetSession session, ProtocolType type, int msgId, int commandId, byte[] msg) throws Throwable;

    void request(ClientNetSession session, ProtocolType type, int msgId, int commandId, byte[] msg, BiConsumer<ControllerResp, ? super Throwable> action) throws Throwable;
}
