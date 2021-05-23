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

package com.bitactor.framework.cloud.spring.boot.cluster.sender;

import com.bitactor.framework.cloud.spring.controller.sender.MsgSender;
import com.bitactor.framework.cloud.spring.rpc.bean.AppNameInfo;
import com.bitactor.framework.cloud.spring.rpc.extension.NetRPC;
import com.bitactor.framework.cloud.spring.rpc.handler.ConnectorRPCMessageHandler;
import com.bitactor.framework.cloud.spring.rpc.utils.AppNameUtils;
import com.bitactor.framework.cloud.spring.model.constants.ProtocolType;
import com.bitactor.framework.cloud.spring.model.utils.MessageUtil;
import com.bitactor.framework.cloud.spring.controller.session.SessionId;
import com.google.protobuf.GeneratedMessageV3;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 消息发送器
 *
 * @author WXH
 */
@Component
public class BackendMsgSender extends MsgSender {

    private static NetRPC netRPC;

    @Autowired
    public void setNetRPC(NetRPC netRPC) {
        BackendMsgSender.netRPC = netRPC;
        setSender(this);
    }

    @Override
    protected void sendMsgImpl(SessionId sessionId, int commandId, Object msg) throws Throwable {
        netRPC.rpc(sessionId.getSid(), ConnectorRPCMessageHandler.class).notify(sessionId, MessageUtil.checkObjType(msg.getClass()), commandId, MessageUtil.encode(msg));
    }

    @Override
    protected void broadcastAssignImpl(List<SessionId> sessionIds, int commandId, Object object) throws Throwable {
        AppNameInfo appNameInfo = AppNameUtils.parse(sessionIds.stream().findFirst().get().getSid());
        netRPC.rpc(appNameInfo.getGroup(), ConnectorRPCMessageHandler.class).broadcastAssign(sessionIds, MessageUtil.checkObjType(object.getClass()), commandId, MessageUtil.encode(object));
    }

    @Override
    protected void broadcastAllImpl(String group, int commandId, Object object) throws Throwable {
        netRPC.rpc(group, ConnectorRPCMessageHandler.class).broadcastAll(MessageUtil.checkObjType(object.getClass()), commandId, MessageUtil.encode(object));
    }
}
