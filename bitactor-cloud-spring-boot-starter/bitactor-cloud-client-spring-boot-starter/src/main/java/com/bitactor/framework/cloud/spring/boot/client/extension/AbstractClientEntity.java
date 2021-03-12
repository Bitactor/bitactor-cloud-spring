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

package com.bitactor.framework.cloud.spring.boot.client.extension;


import com.bitactor.framework.cloud.spring.model.codec.MessageConnectorData;
import com.bitactor.framework.cloud.spring.model.constants.ProtocolType;
import com.bitactor.framework.cloud.spring.model.utils.MessageUtil;
import com.bitactor.framework.core.logger.Logger;
import com.bitactor.framework.core.logger.LoggerFactory;
import com.bitactor.framework.core.threadpool.NamedThreadFactory;
import com.bitactor.framework.core.utils.lang.CycleAtomicInteger;

import java.util.Objects;
import java.util.concurrent.*;

/**
 * 抽象ClientEntity 实现收发消息逻辑
 * @author WXH
 */
public abstract class AbstractClientEntity<T> implements ClientEntity<T> {
    private Logger logger = LoggerFactory.getLogger(AbstractClientEntity.class);
    private int timeoutSec = 10;
    private CycleAtomicInteger msgId = new CycleAtomicInteger(MessageConnectorData.REQ_RESP_MIN_MSG_ID);
    // 异步请求的future
    private ConcurrentHashMap<Integer, CompletableFuture> responseFutures = new ConcurrentHashMap<>();
    // 响应消息的class
    private ConcurrentHashMap<Integer, Class<?>> futureRespClazz = new ConcurrentHashMap<>();

    private static final ScheduledExecutorService TIMEOUT_SERVICE = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("Req-Timeout", true));

    public AbstractClientEntity() {
    }

    public AbstractClientEntity(int timeoutSec) {
        this.timeoutSec = timeoutSec;
    }

    /**
     * 接受到消息
     *
     * @param message
     */
    private void onReceivedResp(int msgId, Object message) {
        // future 处理
        CompletableFuture future = responseFutures.remove(msgId);
        if (Objects.nonNull(future)) {
            future.complete(message);
        }
    }

    /**
     * 接受到消息
     *
     * @param message
     */
    public abstract void onReceivedNotice(ProtocolType protocolType, Object message);

    /**
     * 接受到未知消息
     *
     * @param message
     */
    public void onUnknown(Object message) {
        logger.info("接受到未知消息:{}", message);
    }

    @Override
    public void onReceived(MessageConnectorData connMessage) {
        try {
            ProtocolType protocolType = ProtocolType.valueOfInt(connMessage.getProtoType());
            Class<?> respClazz = futureRespClazz.get(connMessage.getMsgId());
            Object object = MessageUtil.decode(protocolType, connMessage.getMsgData(), respClazz);
            if (connMessage.getMsgId() == MessageConnectorData.NOTIFY_MSG_ID) {
                this.onReceivedNotice(protocolType, object);
            } else if (connMessage.getMsgId() >= MessageConnectorData.REQ_RESP_MIN_MSG_ID) {
                this.onReceivedResp(connMessage.getMsgId(), object);
            } else {
                this.onUnknown(object);
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    /**
     * 发送消息
     *
     * @param req
     * @param respClazz
     * @param <R>
     * @return
     */
    @Override
    public <R> CompletableFuture<R> sendAsync(Object req, Class<R> respClazz, RequestStage requestStage) {
        CompletableFuture<R> future = new CompletableFuture<>();
        int reqMsgId = msgId.next();
        try {
            requestStage.before();
            futureRespClazz.put(reqMsgId, respClazz);
            responseFutures.put(reqMsgId, future);
            MessageConnectorData msgData = buildData(req, reqMsgId);
            getChannel().send(msgData);
            // 超时处理
            TIMEOUT_SERVICE.schedule(() -> {
                if (Objects.nonNull(futureRespClazz.remove(reqMsgId)) || Objects.nonNull(responseFutures.remove(reqMsgId))) {
                    future.completeExceptionally(new TimeoutException("request:" + req.getClass().getSimpleName() + " timeout :" + timeoutSec + " sec"));
                }
            }, timeoutSec, TimeUnit.SECONDS);
        } catch (Exception e) {
            requestStage.exception(e);
            future.completeExceptionally(e);
        } finally {
            future.whenComplete((resp, cause) -> {
                if (resp == null && cause != null) {
                    requestStage.exception(cause);
                }
                requestStage.after();
            });
        }
        return future;
    }

    /**
     * 同步请求消息
     *
     * @param req
     * @param respClazz
     * @param requestStage
     * @param <R>
     * @return
     */
    @Override
    public <R> R sendSync(Object req, Class<R> respClazz, RequestStage requestStage) {
        CompletableFuture<R> future = sendAsync(req, respClazz, requestStage);
        try {
            return future.get(timeoutSec, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
            requestStage.exception(e);
        }
        return null;
    }



    /**
     * 构建协议消息
     *
     * @param req
     * @return
     * @throws Exception
     */
    private MessageConnectorData buildData(Object req, int reqMsgId) throws Exception {
        ProtocolType protocolType = MessageUtil.checkObjType(req.getClass());
        byte[] dataByte = MessageUtil.encode(protocolType,req);
        return MessageConnectorData.builder(dataByte, protocolType.valueInt(), reqMsgId, MessageUtil.getCommandId(req));
    }
}
