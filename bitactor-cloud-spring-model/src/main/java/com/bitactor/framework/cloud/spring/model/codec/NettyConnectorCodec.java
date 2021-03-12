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


import com.bitactor.framework.core.config.UrlProperties;
import com.bitactor.framework.core.net.api.transport.message.MessageWrapper;
import com.bitactor.framework.core.net.netty.codec.NettyDefaultCodec;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;

/**
 * netty下的默认编码解码器
 *
 * @author WXH
 */
public class NettyConnectorCodec extends NettyDefaultCodec {
    public NettyConnectorCodec(UrlProperties url) {
        super(url);
    }

    public MessageWrapper buildMessageData(byte[] data) {
        ByteBuf buf = Unpooled.buffer(data.length);
        buf.writeBytes(data);
        int protoType = buf.readByte();
        int msgId = buf.readInt();
        int commandId = buf.readInt();
        byte[] messageData = ByteBufUtil.getBytes(buf, buf.readerIndex(), buf.readableBytes());
        return MessageConnectorData.builder(data, messageData,protoType,msgId, commandId);
    }
}
