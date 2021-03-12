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

package com.bitactor.framework.cloud.spring.rpc.handler;


import com.bitactor.framework.cloud.spring.model.constants.ProtocolType;
import com.bitactor.framework.cloud.spring.controller.session.SessionId;
import com.bitactor.framework.core.rpc.api.annotation.Broadcast;
import com.bitactor.framework.core.rpc.api.annotation.NoWaitReturn;

import java.util.List;

/**
 * @author WXH
 */

public interface ConnectorRPCMessageHandler {

    /**
     * @param sessionIds
     * @param msg
     */
    @Broadcast
    void broadcastAssign(List<SessionId> sessionIds, ProtocolType protoType, int commandId, byte[] msg);

    /**
     * @param msg
     */
    @Broadcast
    void broadcastAll(ProtocolType protoType, int commandId, byte[] msg);

    /**
     * @param sessionId
     * @param msg
     */
    @NoWaitReturn
    void notify(SessionId sessionId, ProtocolType protoType, int commandId, byte[] msg);
}
