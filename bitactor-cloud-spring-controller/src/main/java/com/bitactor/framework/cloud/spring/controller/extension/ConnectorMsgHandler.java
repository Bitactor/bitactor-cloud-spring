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
import com.bitactor.framework.cloud.spring.controller.session.ClientNetSession;
import com.bitactor.framework.core.net.api.Channel;

/**
 * @author WXH
 */
public interface ConnectorMsgHandler {
    /**
     * Controller 消息任务，如果返回是否需要继续往下处理
     *
     * @param session
     * @param type
     * @param msgId
     * @param commandId
     * @param msg
     * @return true 继续执行下一个
     * @throws Throwable
     */
    boolean request(Channel channel, ClientNetSession session, ProtocolType type, int msgId, int commandId, byte[] msg) throws Throwable;
}
