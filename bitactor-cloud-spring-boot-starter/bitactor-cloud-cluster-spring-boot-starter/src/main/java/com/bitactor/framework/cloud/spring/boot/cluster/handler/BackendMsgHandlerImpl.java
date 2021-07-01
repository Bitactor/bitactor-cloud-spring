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

package com.bitactor.framework.cloud.spring.boot.cluster.handler;

import com.bitactor.framework.cloud.spring.controller.bean.ControllerResp;
import com.bitactor.framework.cloud.spring.controller.bean.MappingInfo;
import com.bitactor.framework.cloud.spring.controller.bean.MessageBytes;
import com.bitactor.framework.cloud.spring.controller.extension.ConnectorMsgHandler;
import com.bitactor.framework.cloud.spring.controller.extension.ControllerInvokerHandler;
import com.bitactor.framework.cloud.spring.controller.session.ClientNetSession;
import com.bitactor.framework.cloud.spring.controller.support.ControllerContext;
import com.bitactor.framework.cloud.spring.model.codec.MessageConnectorData;
import com.bitactor.framework.cloud.spring.model.constants.MsgErrorType;
import com.bitactor.framework.cloud.spring.model.constants.ProtocolType;
import com.bitactor.framework.cloud.spring.rpc.handler.BackendRPCMessageHandler;
import com.bitactor.framework.cloud.spring.rpc.handler.ConsumerModuleHandler;
import com.bitactor.framework.core.exception.RpcException;
import com.bitactor.framework.core.logger.Logger;
import com.bitactor.framework.core.logger.LoggerFactory;
import com.bitactor.framework.core.net.api.Channel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author WXH
 */
@Component
public class BackendMsgHandlerImpl implements ConnectorMsgHandler {
    private static Logger logger = LoggerFactory.getLogger(BackendMsgHandlerImpl.class);

    @Autowired
    private ControllerInvokerHandler controllerInvokerHandler;
    @Autowired
    private ControllerContext controllerContext;
    @Autowired
    private ConsumerModuleHandler consumerModuleHandler;


    @Override
    public boolean request(Channel channel, ClientNetSession session, ProtocolType type, int msgId, int commandId, byte[] msg) throws Throwable {
        // 不存在可调用者，则不执行
        long start = System.currentTimeMillis();
        try {
            MappingInfo mappingInfo = controllerContext.getBackendMapping(commandId, type);
            BackendRPCMessageHandler backendRPCMessageHandler = consumerModuleHandler.getConsumer().get(mappingInfo.getFromGroup(), BackendRPCMessageHandler.class);
            if (mappingInfo.isAsync()) {
                // 异步请求远程Controller
                logger.debug("Start  async controller,channel:{},method:{} ->{} cmd: {}", channel.getChannelId(), mappingInfo.getMethodName(), mappingInfo.getFromGroup(), commandId);
                backendRPCMessageHandler.request(session, type, msgId, commandId, msg, (response, cause) -> {
                    try {
                        if (Objects.nonNull(cause)) {
                            logger.error("finish  async controller,channel:{},method:{} ->{} cmd: {}, RPC error: {}", channel.getChannelId(), mappingInfo.getMethodName(), mappingInfo.getFromGroup(), commandId, cause.getMessage());
                            throw cause;
                        }
                        doResponse(channel, type, response);
                        logger.debug("Finish async controller,channel:{},method:{} ->{} cmd:{} use:{}ms", channel.getChannelId(), mappingInfo.getMethodName(), mappingInfo.getFromGroup(), commandId, System.currentTimeMillis() - start);

                    } catch (Throwable throwable) {
                        doErrorResp(channel, session, type, msgId, commandId, throwable);
                    }
                });
            } else {
                // 同步请求远程Controller
                logger.debug("Start  sync controller,channel:{},method:{} -> {} cmd: {}", channel.getChannelId(), mappingInfo.getMethodName(), mappingInfo.getFromGroup(), commandId);
                ControllerResp response = backendRPCMessageHandler.request(session, type, msgId, commandId, msg);
                logger.debug("Finish sync controller,channel:{},method:{} -> {} cmd:{} use:{}ms", channel.getChannelId(), mappingInfo.getMethodName(), mappingInfo.getFromGroup(), commandId, System.currentTimeMillis() - start);
                doResponse(channel, type, response);
            }
        } catch (Throwable e) {
            e.printStackTrace();
            doErrorResp(channel, session, type, msgId, commandId, e);
            return false;
        } finally {
            long end = System.currentTimeMillis();
            logger.debug("BackendMsgHandler Local invoking use: " + (end - start) + "ms");
        }
        return false;
    }

    private void doErrorResp(Channel channel, ClientNetSession session, ProtocolType type, int msgId, int commandId, Throwable e) {
        MessageBytes response = controllerContext.buildErrorMessageByte(e, MsgErrorType.SYS_ERROR, session, type, msgId, commandId);
        if (response != null) {
            channel.send(MessageConnectorData.builder(response.getData(), type.valueInt(), response.getMsgId(), response.getCommandId()));
        }
    }

    private void doResponse(Channel channel, ProtocolType type, ControllerResp response) {
        if (response != null && response.getReturnType() != null && !void.class.equals(response.getReturnType())) {
            MessageBytes messageByte = response.getMessageBytes();
            if (messageByte != null) {
                channel.send(MessageConnectorData.builder(messageByte.getData(), type.valueInt(), messageByte.getMsgId(), messageByte.getCommandId()));
            } else {
                throw new RpcException("Receive rpc controller response messageByte null with has return type");
            }
        }
    }

}
