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

package com.bitactor.framework.cloud.spring.rpc;

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
public class MsgSender {


    private static NetRPC netRPC;

    @Autowired
    public void setNetRPC(NetRPC netRPC) {
        MsgSender.netRPC = netRPC;
    }

    /**
     * 发送指定类型到消息到指定客户端
     *
     * @param sessionId
     * @param protoType
     * @param commandId
     * @param msg
     */
    public static void sendMsg(SessionId sessionId, ProtocolType protoType, int commandId, byte[] msg) {
        try {
            netRPC.rpc(sessionId.getSid(), ConnectorRPCMessageHandler.class).notify(sessionId, protoType, commandId, msg);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    /**
     * 发送指定协议类型的消息到指定客户端
     *
     * @param sessionId
     * @param protoType
     * @param object
     */
    public static void sendMsg(SessionId sessionId, ProtocolType protoType, Object object) {
        try {
            sendMsg(sessionId, protoType, object.getClass().getSimpleName().hashCode(), MessageUtil.encode(protoType, object));
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    /**
     * 发送protobuf 消息
     *
     * @param sessionId
     * @param object
     */
    public static void sendMsgProtoBuf(SessionId sessionId, GeneratedMessageV3 object) {
        try {
            sendMsg(sessionId, ProtocolType.PROTO, object.getClass().getSimpleName().hashCode(), object.toByteArray());
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    /**
     * 广播传入的指定session的client
     *
     * @param sessionIds
     * @param object
     */
    public static void broadcastAssign(List<SessionId> sessionIds, GeneratedMessageV3 object) {
        try {
            AppNameInfo appNameInfo = AppNameUtils.parse(sessionIds.stream().findFirst().get().getSid());
            netRPC.rpc(appNameInfo.getGroup(), ConnectorRPCMessageHandler.class).broadcastAssign(sessionIds, ProtocolType.PROTO, MessageUtil.parseCommandId(object), MessageUtil.encode(ProtocolType.PROTO, object));
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    /**
     * 广播指定组的所有client连接
     *
     * @param group
     * @param object
     */
    public static void broadcastAll(String group, GeneratedMessageV3 object) {
        try {
            netRPC.rpc(group, ConnectorRPCMessageHandler.class).broadcastAll(ProtocolType.PROTO, MessageUtil.parseCommandId(object), MessageUtil.encode(ProtocolType.PROTO, object));
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

}
