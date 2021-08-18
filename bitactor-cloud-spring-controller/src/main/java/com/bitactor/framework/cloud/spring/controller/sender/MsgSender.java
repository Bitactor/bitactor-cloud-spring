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

package com.bitactor.framework.cloud.spring.controller.sender;

import com.bitactor.framework.cloud.spring.controller.session.SessionId;
import com.bitactor.framework.cloud.spring.model.utils.MessageUtil;

import java.util.List;

/**
 * @author WXH
 */
public abstract class MsgSender {
    protected static MsgSender sender;

    protected void setSender(MsgSender sender) {
        MsgSender.sender = sender;
    }

    /**
     * 发送指定类型到消息到指定客户端
     *
     * @param sessionId
     * @param commandId
     * @param msg
     */
    protected abstract void sendMsgImpl(SessionId sessionId, int commandId, Object msg) throws Throwable;

    /**
     * 广播传入的指定session的client
     *
     * @param sessionIds
     * @param object
     */
    protected abstract void broadcastAssignImpl(List<SessionId> sessionIds, int commandId, Object object) throws Throwable;

    /**
     * 广播指定组的所有client连接
     *
     * @param group
     * @param object
     */
    protected abstract void broadcastAllImpl(String group, int commandId, Object object) throws Throwable;

    /**
     * 发送指定类型到消息到指定客户端
     *
     * @param sessionId
     * @param msg
     */
    public static void sendMsg(SessionId sessionId, Object msg) {
        try {
            sender.sendMsgImpl(sessionId, MessageUtil.getCommandId(msg), msg);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    /**
     * 发送指定类型到消息到指定客户端
     *
     * @param sessionId
     * @param commandId
     * @param msg
     */
    public static void sendMsg(SessionId sessionId, int commandId, Object msg) {
        try {
            sender.sendMsgImpl(sessionId, commandId, msg);
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
    public static void broadcastAssign(List<SessionId> sessionIds, int commandId, Object object) {
        try {
            sender.broadcastAssignImpl(sessionIds, commandId, object);
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
    public static void broadcastAssign(List<SessionId> sessionIds, Object object) {
        try {
            sender.broadcastAssignImpl(sessionIds, MessageUtil.getCommandId(object), object);
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
    public static void broadcastAll(String group, int commandId, Object object) {
        try {
            sender.broadcastAllImpl(group, commandId, object);
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
    public static void broadcastAll(String group, Object object) {
        try {
            sender.broadcastAllImpl(group, MessageUtil.getCommandId(object), object);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
}
