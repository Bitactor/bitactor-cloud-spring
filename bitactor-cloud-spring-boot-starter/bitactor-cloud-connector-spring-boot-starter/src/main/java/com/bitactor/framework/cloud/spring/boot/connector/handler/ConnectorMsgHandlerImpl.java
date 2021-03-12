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

package com.bitactor.framework.cloud.spring.boot.connector.handler;

import com.bitactor.framework.cloud.spring.controller.bean.MessageBytes;
import com.bitactor.framework.cloud.spring.controller.extension.ConnectorMsgHandler;
import com.bitactor.framework.cloud.spring.controller.extension.ControllerInvokerHandler;
import com.bitactor.framework.cloud.spring.controller.session.ClientNetSession;
import com.bitactor.framework.cloud.spring.controller.support.ControllerContext;
import com.bitactor.framework.cloud.spring.model.codec.MessageConnectorData;
import com.bitactor.framework.cloud.spring.model.constants.MsgErrorType;
import com.bitactor.framework.cloud.spring.model.constants.ProtocolType;
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
public class ConnectorMsgHandlerImpl implements ConnectorMsgHandler {
    private static Logger logger = LoggerFactory.getLogger(ConnectorMsgHandlerImpl.class);

    @Autowired
    private ControllerInvokerHandler controllerInvokerHandler;
    @Autowired
    private ControllerContext controllerContext;


    @Override
    public boolean request(Channel channel, ClientNetSession session, ProtocolType type, int msgId, int commandId, byte[] msg) throws Throwable {
        // 不存在可调用者，则不执行
        if (!controllerContext.existInvoker(commandId, type)) {
            return true;
        }
        long start = System.currentTimeMillis();
        try {
            controllerInvokerHandler.request(session, type, msgId, commandId, msg, (resp, cause) -> {
                if (Objects.nonNull(cause)) {
                    exceptionHandler(channel, session, type, msgId, commandId, cause);
                } else {
                    if (resp != null && resp.getReturnType() != null && !void.class.equals(resp.getReturnType())) {
                        MessageBytes messageByte = resp.getMessageBytes();
                        channel.send(MessageConnectorData.builder(messageByte.getData(), type.valueInt(), messageByte.getMsgId(), messageByte.getCommandId()));

                    }
                }
            });

        } catch (Throwable cause) {
            exceptionHandler(channel, session, type, msgId, commandId, cause);
            return false;
        } finally {
            long end = System.currentTimeMillis();
            logger.debug("ConnectorMsgHandler Local invoking use: " + (end - start) + "ms");
        }
        return false;
    }

    private void exceptionHandler(Channel channel, ClientNetSession session, ProtocolType type, int msgId, int commandId, Throwable e) {
        e.printStackTrace();
        MessageBytes response = controllerContext.buildErrorMessageByte(e, MsgErrorType.SYS_ERROR, session, type, msgId, commandId);
        if (response != null) {
            channel.send(MessageConnectorData.builder(response.getData(), type.valueInt(), response.getMsgId(), response.getCommandId()));
        }
    }

}
