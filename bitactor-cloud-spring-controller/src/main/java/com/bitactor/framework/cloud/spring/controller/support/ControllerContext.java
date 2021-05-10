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

package com.bitactor.framework.cloud.spring.controller.support;

import com.alibaba.fastjson.JSON;
import com.bitactor.framework.cloud.spring.model.codec.MessageConnectorData;
import com.bitactor.framework.cloud.spring.model.constants.MsgErrorType;
import com.bitactor.framework.cloud.spring.model.constants.ProtocolType;
import com.bitactor.framework.cloud.spring.model.utils.MessageUtil;
import com.bitactor.framework.cloud.spring.core.BitactorApplicationProperties;
import com.bitactor.framework.cloud.spring.core.utils.SpringUtils;
import com.bitactor.framework.cloud.spring.controller.annotation.BitactorController;
import com.bitactor.framework.cloud.spring.controller.annotation.BitactorRequestMapping;
import com.bitactor.framework.cloud.spring.controller.annotation.ProtocolBody;
import com.bitactor.framework.cloud.spring.controller.bean.MappingInfo;
import com.bitactor.framework.cloud.spring.controller.bean.MappingReqParam;
import com.bitactor.framework.cloud.spring.controller.bean.MessageBytes;
import com.bitactor.framework.cloud.spring.controller.bean.conn.AbstractConnect;
import com.bitactor.framework.cloud.spring.controller.bean.conn.ConnectManager;
import com.bitactor.framework.cloud.spring.controller.extension.ConnectorMsgHandler;
import com.bitactor.framework.cloud.spring.controller.extension.CustomAuthHandler;
import com.bitactor.framework.cloud.spring.controller.extension.ErrorMessageHandler;
import com.bitactor.framework.cloud.spring.controller.invoker.ControllerInvoker;
import com.bitactor.framework.cloud.spring.controller.session.ClientNetSession;
import com.bitactor.framework.core.exception.ErrorSessionException;
import com.bitactor.framework.core.exception.NoMatchControllerException;
import com.bitactor.framework.core.exception.RepetitionException;
import com.bitactor.framework.core.logger.Logger;
import com.bitactor.framework.core.logger.LoggerFactory;
import com.bitactor.framework.core.net.api.Channel;
import com.bitactor.framework.core.utils.collection.CollectionUtils;
import com.google.protobuf.GeneratedMessageV3;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author WXH
 */
@Component
public class ControllerContext {
    private static final Logger logger = LoggerFactory.getLogger(ControllerContext.class);
    // json 的命令集合,KEY =commandId,value= appGroup
    protected final ConcurrentHashMap<Integer, MappingInfo> COMMAND_ID_BY_APP_GROUP_JSON = new ConcurrentHashMap<>();
    // protobuf的命令集合,KEY =commandId,value= appGroup
    protected final ConcurrentHashMap<Integer, MappingInfo> COMMAND_ID_BY_APP_GROUP_PROTO = new ConcurrentHashMap<>();


    protected final ConcurrentHashMap<Integer, ControllerInvoker> CONTROLLER_JSON_INVOKER_MAP = new ConcurrentHashMap<Integer, ControllerInvoker>();
    protected final ConcurrentHashMap<Integer, ControllerInvoker> CONTROLLER_PROTO_INVOKER_MAP = new ConcurrentHashMap<Integer, ControllerInvoker>();
    protected final Set<String> controllerList = new HashSet<>();

    @Autowired(required = false)
    private List<ConnectorMsgHandler> connectorMsgHandlers;

    @Autowired(required = false)
    private ConnectManager connectManager;
    @Autowired
    private BitactorApplicationProperties bitactorApplicationProperties;


    public BitactorApplicationProperties getBitactorApplicationProperties() {
        return bitactorApplicationProperties;
    }

    public Set<String> getControllerList() {
        return controllerList;
    }

    /**
     * 添加rpc服务的bean类
     *
     * @param bean
     */
    public void addControllerBean(Object bean) {
        try {
            Object finalBean = AopProxyUtils.getSingletonTarget(bean);
            if (finalBean != null) {
                bean = finalBean;
            }
            Class aClass = bean.getClass();
            if (aClass.isInterface()) {
                return;
            }
            BitactorController controllerAnnotation = AnnotationUtils.getAnnotation(aClass, BitactorController.class);
            if (controllerAnnotation != null) {
                logger.info("Mapping controller class [" + aClass + "]");
                Method[] methods = aClass.getMethods();
                for (Method method : methods) {
                    BitactorRequestMapping mapping = AnnotationUtils.getAnnotation(method, BitactorRequestMapping.class);
                    if (mapping != null) {
                        ControllerInvoker invoker = new ControllerInvoker(aClass, method, mapping, controllerAnnotation);
                        addControllerInvoker(invoker);
                        logger.info(String.format("Mapping controller commandId [%-10s] protocol : [%-8s] method [%s]"
                                , invoker.getRespCommand(), mapping.protocol().valueStr(), method.getName()));
                    }
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new BeanCreationException("Add controller bean exception bean:" + bean.getClass().getName(), e);
        }
    }

    private void addControllerInvoker(ControllerInvoker invoker) throws IllegalAccessException {
        //
        if (invoker.getReqProtoClass() == null || invoker.getRespProtoClass() == null) {
            throw new IllegalAccessException("Must have request and response proto object,may be you not add @ProtocolBody on the request param ,Illegal ControllerInvoker commandId : ["
                    + invoker.getReqCommandID() + "] method : "
                    + invoker.getMethod().getName() + " proto: "
                    + invoker.getMapping().protocol().valueStr());
        }
        String controllerStr = null;
        try {
            controllerStr = URLEncoder.encode(JSON.toJSONString(buildMappingInfo(invoker)), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        // protobuf 协议类验证返回值是否是protobuf
        if (invoker.getMapping().protocol().equals(ProtocolType.PROTO)) {
            if (!GeneratedMessageV3.class.isAssignableFrom(invoker.getMethod().getReturnType())) {
                logger.error("Mapping controller reqCommand [{}}] respCommand [{}] protocol : [{}] method [{}], is not a protobuf returnType: {}"
                        , invoker.getReqCommand(), invoker.getRespCommand(), invoker.getMapping().protocol().valueStr(), invoker.getMethod().getName(), invoker.getMethod().getReturnType());
                return;
            }
            if (!CONTROLLER_PROTO_INVOKER_MAP.containsKey(invoker.getReqCommandID())) {
                CONTROLLER_PROTO_INVOKER_MAP.put(invoker.getReqCommandID(), invoker);

                controllerList.add(controllerStr);
            } else {
                ControllerInvoker other = CONTROLLER_PROTO_INVOKER_MAP.get(invoker.getReqCommandID());
                throw new RepetitionException("Controller commandId repetition : \n["
                        + invoker.getReqCommandID() + "-"
                        + invoker.getReqCommand() + "] method : "
                        + invoker.getMethod().getName() + " proto: "
                        + invoker.getMapping().protocol().valueStr()
                        + "\n -----: \n["
                        + other.getReqCommandID() + "-"
                        + other.getReqCommand() + "] method : "
                        + other.getMethod().getName() + " proto: "
                        + other.getMapping().protocol().valueStr()
                );
            }
        } else if (invoker.getMapping().protocol().equals(ProtocolType.JSON)) {
            if (!CONTROLLER_JSON_INVOKER_MAP.containsKey(invoker.getReqCommandID())) {
                CONTROLLER_JSON_INVOKER_MAP.put(invoker.getReqCommandID(), invoker);
                controllerList.add(controllerStr);
            } else {
                ControllerInvoker other = CONTROLLER_JSON_INVOKER_MAP.get(invoker.getReqCommandID());
                throw new RepetitionException("Controller commandId repetition : \n["
                        + invoker.getReqCommand() + "] method : "
                        + invoker.getMethod().getName() + " proto: "
                        + invoker.getMapping().protocol().valueStr()
                        + "\n -----: \n["
                        + other.getReqCommand() + "] method : "
                        + other.getMethod().getName() + " proto: "
                        + other.getMapping().protocol().valueStr()
                );
            }
        } else {
            throw new IllegalAccessException("Illegal ControllerInvoker commandId : ["
                    + invoker.getReqCommandID() + "] method : "
                    + invoker.getMethod().getName() + " proto: "
                    + invoker.getMapping().protocol().valueStr());
        }

    }

    /**
     * 添加controller 对应的commandId 的appGroup PROTO 类型请求
     *
     * @param mappingInfo
     */
    public void boundCommandIdToAppGroup(MappingInfo mappingInfo) {
        if (mappingInfo == null) {
            return;
        }
        boundCommandIdToAppGroup(Arrays.asList(mappingInfo));
    }

    public void boundCommandIdToAppGroup(List<MappingInfo> mappingInfos) {
        if (CollectionUtils.isEmpty(mappingInfos)) {
            return;
        }
        for (MappingInfo mappingInfo : mappingInfos) {
            if (Objects.equals(ProtocolType.PROTO, mappingInfo.getProtocolType())) {
                COMMAND_ID_BY_APP_GROUP_PROTO.put(mappingInfo.getCmd(), mappingInfo);
            } else {
                COMMAND_ID_BY_APP_GROUP_JSON.put(mappingInfo.getCmd(), mappingInfo);
            }
            logger.info("Bound proto type:{} command id : {} ,relation: {} -> {} ,", mappingInfo.getProtocolType(), mappingInfo.getCmd(), mappingInfo.getFromGroup(), bitactorApplicationProperties.getSID());
        }
    }

    /**
     * 构建MappingInfo
     *
     * @param invoker
     * @return
     */
    public MappingInfo buildMappingInfo(ControllerInvoker invoker) {
        MappingInfo mappingInfo = new MappingInfo();
        mappingInfo.setAsync(invoker.async());
        mappingInfo.setCmd(invoker.getReqCommandID());
        mappingInfo.setProtocolType(invoker.getMapping().protocol());
        mappingInfo.setConnector(invoker.getController().connector());
        mappingInfo.setFromGroup(bitactorApplicationProperties.getName());
        mappingInfo.setMethodName(invoker.getMethod().getName());
        return mappingInfo;
    }

    /**
     * 是否存在 ControllerInvoker
     *
     * @param commandId
     * @param protocolType
     * @return
     */
    public boolean existInvoker(int commandId, ProtocolType protocolType) {
        try {
            return get(commandId, protocolType) != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取对应命令id 的控制器调用者
     *
     * @param commandId 命令id
     * @return ControllerInvoker
     */
    public ControllerInvoker get(int commandId, ProtocolType protocolType) {
        ControllerInvoker invoker = null;
        if (protocolType.equals(ProtocolType.PROTO)) {
            invoker = CONTROLLER_PROTO_INVOKER_MAP.get(commandId);
        } else if (protocolType.equals(ProtocolType.JSON)) {
            invoker = CONTROLLER_JSON_INVOKER_MAP.get(commandId);
        }
        if (Objects.isNull(invoker)) {
            throw new NoMatchControllerException("Can not match controller of command : " + commandId);
        }
        if (Objects.isNull(invoker.getInstance())) {
            Object bean = null;
            try {
                bean = SpringUtils.getBean(invoker.getInstanceCls());
            } catch (BeansException e) {
                throw new NullPointerException("Can not find ControllerInvoker bean: " + invoker.getInstanceCls());
            }
            invoker.setInstance(bean);
        }
        return invoker;
    }

    /**
     * 通过CommandId获取 AppGroup= appName
     *
     * @param commandId
     * @param protocolType
     * @return
     */
    public MappingInfo getBackendMapping(int commandId, ProtocolType protocolType) {
        MappingInfo mappingInfo = null;
        if (protocolType == ProtocolType.PROTO) {
            mappingInfo = COMMAND_ID_BY_APP_GROUP_PROTO.get(commandId);
        } else if (protocolType == ProtocolType.JSON) {
            mappingInfo = COMMAND_ID_BY_APP_GROUP_JSON.get(commandId);
        }
        return mappingInfo;
    }

    /**
     * 是否存在可调用的mapping
     *
     * @param commandId
     * @param protocolType
     * @return
     */
    public boolean existMapping(int commandId, ProtocolType protocolType) {
        if (existInvoker(commandId, protocolType)) {
            return true;
        }
        if (Objects.nonNull(getBackendMapping(commandId, protocolType))) {
            return true;
        }
        return false;
    }

    /**
     * 处理请求
     */
    public void doConnectRequest(Channel channel, ClientNetSession session, MessageConnectorData messageMvcData) {
        ProtocolType protocolType = null;
        try {
            protocolType = ProtocolType.valueOfInt(messageMvcData.getProtoType());
            if (!existMapping(messageMvcData.getCommandId(), protocolType)) {
                logger.error("Server received controller message but can not find app group by command id  : " + messageMvcData.getCommandId());
                MessageBytes response = buildErrorMessageByte(null, MsgErrorType.SYS_ERROR, session, protocolType, messageMvcData.getMsgId(), messageMvcData.getCommandId());
                if (response != null) {
                    channel.send(MessageConnectorData.builder(response.getData(), messageMvcData.getProtoType(), response.getMsgId(), response.getCommandId()));
                }
                return;
            }
            for (ConnectorMsgHandler connectorMsgHandler : connectorMsgHandlers) {
                boolean doNext = connectorMsgHandler.request(channel, session, ProtocolType.valueOfInt(messageMvcData.getProtoType()), messageMvcData.getMsgId(),
                        messageMvcData.getCommandId(), messageMvcData.getMsgData());
                if (!doNext) {
                    break;
                }
            }

        } catch (Throwable e) {
            e.printStackTrace();
            MessageBytes response = buildErrorMessageByte(e, MsgErrorType.SYS_ERROR, session, protocolType, messageMvcData.getMsgId(), messageMvcData.getCommandId());
            if (response != null) {
                channel.send(MessageConnectorData.builder(response.getData(), messageMvcData.getProtoType(), response.getMsgId(), response.getCommandId()));
            }
        }
    }

    /**
     * 构建错误响应
     *
     * @param exception
     * @param msgErrorType
     * @param session
     * @param type
     * @param msgId
     * @param commandId
     * @return
     */
    public MessageBytes buildErrorMessageByte(Throwable exception, int msgErrorType, ClientNetSession session, ProtocolType type, int msgId, int commandId) {
        ErrorMessageHandler handler = null;
        try {
            handler = SpringUtils.getBean(ErrorMessageHandler.class);
        } catch (BeansException ignored) {
        }
        return Optional.ofNullable(handler).orElse(new ErrorMessageHandler() {
            @Override
            public MessageBytes getErrorMessageByte(Throwable exception, int msgErrorType, ClientNetSession session, ProtocolType type, int msgId, int commandId) {
                return null;
            }
        }).getErrorMessageByte(exception, msgErrorType, session, type, msgId, commandId);
    }

    /**
     * 检查权限
     *
     * @param session
     * @return
     */
    public boolean checkNeedAuth(ClientNetSession session) {
        CustomAuthHandler handler = null;
        try {
            handler = SpringUtils.getBean(CustomAuthHandler.class);
        } catch (BeansException ignored) {
        }
        return Optional.ofNullable(handler).orElse(new CustomAuthHandler() {
            @Override
            public boolean checkNeedAuth(ClientNetSession session) {
                return false;
            }
        }).checkNeedAuth(session);
    }

    /**
     * 构建 BitactorRequestMapping 方法参数
     *
     * @param session
     * @param method
     * @param mapping
     * @param msg
     * @return
     * @throws Throwable
     */
    public MappingReqParam buildRequestMethodArgs(ClientNetSession session, Method method, BitactorRequestMapping mapping, byte[] msg) throws Exception {
        MappingReqParam reqParam = new MappingReqParam();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        Class<?>[] parameterTypes = method.getParameterTypes();
        int paramLength = parameterTypes.length;
        Object[] args = new Object[paramLength];
        for (int i = 0; i < paramLength; i++) {
            Annotation[] annotations = parameterAnnotations[i];
            if (annotations != null && annotations.length > 0) {
                for (Annotation annotation : annotations) {
                    if (annotation instanceof ProtocolBody) {
                        args[i] = MessageUtil.decode(mapping.protocol(), msg, parameterTypes[i]);
                        break;
                    }
                }
            }
            if (parameterTypes[i] == ClientNetSession.class) {
                args[i] = session;
            } else if (ClassUtils.isAssignable(AbstractConnect.class, parameterTypes[i])) {
                if (connectManager == null) {
                    throw new ErrorSessionException("Connect not match session");
                }
                AbstractConnect connect = connectManager.get(session.getUid());
                if (connect.checkAndUpdateSession(session)) {
                    reqParam.setConnect(connect);
                    args[i] = connect;
                } else {
                    throw new ErrorSessionException("Connect not match session");
                }
            }
        }
        reqParam.setArgs(args);
        return reqParam;
    }

    public ConcurrentHashMap<Integer, ControllerInvoker> getControllerJsonInvoker() {
        return CONTROLLER_JSON_INVOKER_MAP;
    }

    public ConcurrentHashMap<Integer, ControllerInvoker> getControllerProtoInvoker() {
        return CONTROLLER_PROTO_INVOKER_MAP;
    }
}
