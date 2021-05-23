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

package com.bitactor.framework.cloud.spring.controller.handler;


import com.bitactor.framework.cloud.spring.model.constants.MsgErrorType;
import com.bitactor.framework.cloud.spring.model.constants.ProtocolType;
import com.bitactor.framework.cloud.spring.model.utils.MessageUtil;
import com.bitactor.framework.cloud.spring.controller.annotation.BitactorRequestMapping;
import com.bitactor.framework.cloud.spring.controller.bean.ControllerResp;
import com.bitactor.framework.cloud.spring.controller.bean.MappingReqParam;
import com.bitactor.framework.cloud.spring.controller.bean.MessageBytes;
import com.bitactor.framework.cloud.spring.controller.bean.conn.AbstractConnect;
import com.bitactor.framework.cloud.spring.controller.concurrent.ThreadAdapter;
import com.bitactor.framework.cloud.spring.controller.extension.ControllerInvokerHandler;
import com.bitactor.framework.cloud.spring.controller.invoker.ControllerInvoker;
import com.bitactor.framework.cloud.spring.controller.session.ClientNetSession;
import com.bitactor.framework.cloud.spring.controller.support.ControllerContext;
import com.bitactor.framework.core.exception.NoSuchPropertyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

/**
 * @author WXH
 */
@Component
public class ControllerInvokerHandlerImpl implements ControllerInvokerHandler {

    @Autowired
    private ControllerContext controllerContext;
    @Autowired(required = false)
    private ThreadAdapter adapter;

    @Override
    public ControllerResp request(ClientNetSession session, ProtocolType type, int msgId, int commandId, byte[] msg) throws Throwable {
        if (Objects.nonNull(adapter)) {
            CompletableFuture<ControllerResp> future = new CompletableFuture<>();
            adapter.doIt(session, () -> {
                ControllerResp request = doInvoker(session, type, msgId, commandId, msg);
                future.complete(request);
            });
            return future.get();
        } else {
            return doInvoker(session, type, msgId, commandId, msg);
        }
    }

    @Override
    public void request(ClientNetSession session, ProtocolType type, int msgId, int commandId, byte[] msg, BiConsumer<ControllerResp, ? super Throwable> action) throws Throwable {
        try {
            if (Objects.nonNull(adapter)) {
                CompletableFuture<ControllerResp> future = new CompletableFuture<>();
                adapter.doIt(session, () -> {
                    ControllerResp request = doInvoker(session, type, msgId, commandId, msg);
                    future.complete(request);
                });
                future.whenComplete(action);
            } else {
                action.accept(doInvoker(session, type, msgId, commandId, msg), null);
            }
        } catch (Exception e) {
            action.accept(null, e);
        }
    }

    private ControllerResp doInvoker(ClientNetSession session, ProtocolType type, int msgId, int commandId, byte[] msg) {
        AbstractConnect connect = null;
        MessageBytes messageBytes = null;
        Class<?> returnType = null;
        try {
            ControllerInvoker invoker = controllerContext.get(commandId, type);
            if (invoker == null) {
                throw new NoSuchPropertyException("Can not find commandId: [" + commandId + "]/[" + type + "] for the request");
            }
            Method method = invoker.getMethod();
            returnType = method.getReturnType();
            if (controllerContext.checkNeedAuth(session, invoker)) {
                messageBytes = controllerContext.buildErrorMessageByte(null, MsgErrorType.NEED_AUTH, session, type, msgId, commandId);
                return new ControllerResp(returnType, messageBytes);
            }
            Object instance = invoker.getInstance();
            BitactorRequestMapping mapping = invoker.getMapping();

            MappingReqParam param = controllerContext.buildRequestMethodArgs(session, method, mapping, msg);
            connect = param.getConnect();
            if (connect != null) {
                connect.tryLock();
            }
            Object result = method.invoke(instance, param.getArgs());
            messageBytes = new MessageBytes(msgId, invoker.getRespCommandID(), MessageUtil.encode(mapping.protocol(), result));
            return new ControllerResp(returnType, messageBytes);
        } catch (Throwable e) {
            messageBytes = controllerContext.buildErrorMessageByte(e.getCause(), MsgErrorType.SYS_ERROR, session, type, msgId, commandId);
            return new ControllerResp(returnType, messageBytes);
        } finally {
            if (connect != null) {
                // 执行最终的事件
                connect.doFinishEvent();
                // 释放锁
                connect.unLock();
            }
        }
    }
}
