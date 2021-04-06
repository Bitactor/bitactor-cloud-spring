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
 *
 * @author WXH
 */
public abstract class AbstractClientEntity<T> implements ClientEntity<T> {
    private Logger logger = LoggerFactory.getLogger(AbstractClientEntity.class);
    private int timeoutSec = 10;
    private CycleAtomicInteger msgId = new CycleAtomicInteger(MessageConnectorData.REQ_RESP_MIN_MSG_ID);
    // 异步请求的future
    private ConcurrentHashMap<Integer, ResponseMapping<?>> responseMapping = new ConcurrentHashMap<>();
    // 响应消息的class
    private ConcurrentHashMap<Integer, NoticeMapping> noticeMapping = new ConcurrentHashMap<>();

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
        ResponseMapping remove = responseMapping.remove(msgId);
        if (Objects.nonNull(remove) && Objects.nonNull(remove.getFuture())) {
            remove.getFuture().complete(message);
        }
    }

    public void addNoticeMapping(NoticeMapping notice) {
        noticeMapping.put(notice.getNoticeId(), notice);
    }

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
            ResponseMapping<?> responseMapping = this.responseMapping.get(connMessage.getMsgId());

            // 响应类型
            if (Objects.nonNull(responseMapping)) {
                Class<?> respClazz = responseMapping.getRespClazz();
                Object object = MessageUtil.decode(protocolType, connMessage.getMsgData(), respClazz);
                this.onReceivedResp(connMessage.getMsgId(), object);
                return;
            }
            // 通知类型
            if (connMessage.getMsgId() == MessageConnectorData.NOTIFY_MSG_ID && noticeMapping.containsKey(connMessage.getCommandId())) {
                NoticeMapping noticeMapping = this.noticeMapping.get(connMessage.getCommandId());
                Object object = MessageUtil.decode(protocolType, connMessage.getMsgData(), noticeMapping.getNoticeClazz());
                noticeMapping.notice(object);
                return;
            }
            // 未知类型
            this.onUnknown(connMessage);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            this.onUnknown(connMessage);
        }
    }

    /**
     * 发送异步消息
     *
     * @param req
     * @param respClazz
     * @param <R>
     * @return
     */
    @Override
    public <R> CompletableFuture<R> sendAsync(Object req, Class<R> respClazz, RequestStage requestStage) {
        return sendAsync(MessageUtil.getCommandId(req), req, respClazz, requestStage);
    }

    /**
     * 发送异步消息
     *
     * @param reqCmd
     * @param req
     * @param respClazz
     * @param requestStage
     * @param <R>
     * @return
     */
    @Override
    public <R> CompletableFuture<R> sendAsync(int reqCmd, Object req, Class<R> respClazz, RequestStage requestStage) {
        CompletableFuture<R> future = new CompletableFuture<>();
        int reqMsgId = msgId.next();
        try {
            requestStage.before();
            responseMapping.put(reqMsgId, new ResponseMapping<>(future, respClazz));
            MessageConnectorData msgData = buildData(req, reqMsgId, reqCmd);
            getChannel().send(msgData);
            // 超时处理
            TIMEOUT_SERVICE.schedule(() -> {
                if (Objects.nonNull(responseMapping.remove(reqMsgId))) {
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
     * 仅发送消息
     *
     * @param req
     */
    @Override
    public boolean justSend(Object req) {
        return justSend(MessageUtil.getCommandId(req), req);
    }

    /**
     * 仅发送消息
     *
     * @param reqCmd
     * @param req
     */
    @Override
    public boolean justSend(int reqCmd, Object req) {
        boolean status = false;
        try {
            int reqMsgId = msgId.next();
            MessageConnectorData msgData = buildData(req, reqMsgId, reqCmd);
            getChannel().send(msgData);
            status = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return status;
    }

    /**
     * 发送同步消息
     *
     * @param req
     * @param respClazz
     * @param requestStage
     * @param <R>
     * @return
     */
    @Override
    public <R> R sendSync(Object req, Class<R> respClazz, RequestStage requestStage) {
        return sendSync(MessageUtil.getCommandId(req), req, respClazz, requestStage);
    }

    /**
     * 发送同步消息
     *
     * @param reqCmd
     * @param req
     * @param respClazz
     * @param requestStage
     * @param <R>
     * @return
     */
    @Override
    public <R> R sendSync(int reqCmd, Object req, Class<R> respClazz, RequestStage requestStage) {
        CompletableFuture<R> future = sendAsync(reqCmd, req, respClazz, requestStage);
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
    private MessageConnectorData buildData(Object req, int reqMsgId, int reqCmd) throws Exception {
        ProtocolType protocolType = MessageUtil.checkObjType(req.getClass());
        byte[] dataByte = MessageUtil.encode(protocolType, req);
        return MessageConnectorData.builder(dataByte, protocolType.valueInt(), reqMsgId, reqCmd);
    }
}
