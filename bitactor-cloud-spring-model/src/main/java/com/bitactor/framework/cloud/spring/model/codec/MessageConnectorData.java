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

package com.bitactor.framework.cloud.spring.model.codec;


import com.bitactor.framework.core.constant.NetConstants;
import com.bitactor.framework.core.net.api.transport.message.MessageData;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;

/**
 * @author WXH
 */
public class MessageConnectorData extends MessageData {
    private int msgId;
    private int commandId;
    private int protoType;
    private byte[] msgData;
    public static final int NOTIFY_MSG_ID = 0;
    public static final int REQ_RESP_MIN_MSG_ID = 100;

    private MessageConnectorData(int msgId, byte[] data, byte[] msgData, int commandId, int protoType) {
        super(data);
        this.msgId = msgId;
        this.msgData = msgData;
        this.commandId = commandId;
        this.protoType = protoType;
    }

    public int getMsgId() {
        return msgId;
    }

    public int getCommandId() {
        return commandId;
    }

    public int getProtoType() {
        return protoType;
    }

    public byte[] getMsgData() {
        return msgData;
    }

    /**
     * 构建一个 MessageFrontData
     */
    public static MessageConnectorData builder(byte[] messageData, int protoType, int commandId) {
        return builder(messageData, protoType, NOTIFY_MSG_ID, commandId);
    }

    /**
     * 构建一个 MessageFrontData
     */
    public static MessageConnectorData builder(byte[] messageData, int protoType, int msgId, int commandId) {
        ByteBuf dataBuf = Unpooled.buffer(messageData.length + NetConstants.BYTES_4_LENGTH * 2 + NetConstants.BYTES_1_LENGTH);
        dataBuf.writeByte(protoType);
        dataBuf.writeInt(msgId);
        dataBuf.writeInt(commandId);
        dataBuf.writeBytes(messageData);
        return new MessageConnectorData(msgId, ByteBufUtil.getBytes(dataBuf), messageData, commandId, protoType);
    }

    /**
     * 构建一个 MessageFrontData
     */
    public static MessageConnectorData builder(byte[] data, byte[] messageData, int protoType, int msgId, int commandId) {
        return new MessageConnectorData(msgId, data, messageData, commandId, protoType);
    }
}
