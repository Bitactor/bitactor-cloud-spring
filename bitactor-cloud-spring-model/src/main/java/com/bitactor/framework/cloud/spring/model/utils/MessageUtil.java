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

package com.bitactor.framework.cloud.spring.model.utils;

import com.alibaba.fastjson.JSON;
import com.bitactor.framework.cloud.spring.model.constants.ProtocolType;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author WXH
 */
public class MessageUtil {

    public static final String UNKNOWN_COMMAND = "-1_UNKNOWN";
    public static final int UNKNOWN_COMMAND_ID = -1;

    /**
     * class 协议类型
     */
    private static final ConcurrentHashMap<Class<?>, ProtocolType> protoClassType = new ConcurrentHashMap<>();

    /**
     * protobuf Decode 方法缓存
     */
    private static final ConcurrentHashMap<Class<?>, Method> protoDecodeCache = new ConcurrentHashMap<>();
    /**
     * protobuf Encode 方法缓存
     */
    private static final ConcurrentHashMap<Class<?>, Method> protoEncodeCache = new ConcurrentHashMap<>();

    /**
     * 获取缓存的protobuf 的 Decode方法
     *
     * @param protoClass
     * @return
     * @throws NoSuchMethodException
     */
    private static Method getProtoDecodeMethod(Class<?> protoClass) throws NoSuchMethodException {
        Method method = protoDecodeCache.get(protoClass);
        if (Objects.isNull(method)) {
            method = protoClass.getMethod("parseFrom", byte[].class);
            if (method != null) {
                protoDecodeCache.put(protoClass, method);
            }
        }
        return method;
    }

    /**
     * 获取缓存的protobuf 的 Encode方法
     *
     * @param protoClass
     * @return
     * @throws NoSuchMethodException
     */
    private static Method getProtoEncodeMethod(Class<?> protoClass) throws NoSuchMethodException {
        Method method = protoEncodeCache.get(protoClass);
        if (Objects.isNull(method)) {
            method = protoClass.getMethod("toByteArray");
            if (method != null) {
                protoEncodeCache.put(protoClass, method);
            }
        }
        return method;
    }

    public static int getCommandId(Object object) {
        if (object == null) {
            return UNKNOWN_COMMAND_ID;
        }
        return getCommandId(object.getClass());
    }

    public static int getCommandId(Class clazz) {
        if (clazz == null) {
            return UNKNOWN_COMMAND_ID;
        }
        return clazz.getSimpleName().hashCode();
    }

    public static String getCommand(Object object) {
        if (object == null) {
            return UNKNOWN_COMMAND;
        }
        return object.getClass().getSimpleName();
    }

    public static String getCommand(Class clazz) {
        if (clazz == null) {
            return UNKNOWN_COMMAND;
        }
        return clazz.getSimpleName();
    }


    public static Object decode(ProtocolType protocolType, byte[] bytes, Class<?> parameterType) throws Exception {
        Object decodeObj = null;
        if (protocolType.equals(ProtocolType.PROTO)) {
            Method method = getProtoDecodeMethod(parameterType);
            Object[] args = new Object[1];
            args[0] = bytes;
            decodeObj = method.invoke(null, args);
        } else if (protocolType.equals(ProtocolType.JSON)) {
            decodeObj = JSON.parseObject(bytes, parameterType);
        } else {
            throw new IllegalAccessException("Illegal protocol type");
        }
        return decodeObj;
    }


    public static byte[] encode(ProtocolType protocolType, Object obj) throws Exception {
        if (obj == null) {
            return new byte[0];
        }
        byte[] bytes = new byte[0];
        if (protocolType.equals(ProtocolType.PROTO)) {
            Method method = getProtoEncodeMethod(obj.getClass());
            bytes = (byte[]) method.invoke(obj);
        } else if (protocolType.equals(ProtocolType.JSON)) {
            bytes = JSON.toJSONBytes(obj);
        } else {
            throw new IllegalAccessException("Illegal protocol type");
        }
        return bytes;
    }


    /**
     * 检查传入的class 的ProtocolType类型
     *
     * @param clazz
     * @return
     * @throws Exception
     */
    public static ProtocolType checkObjType(Class<?> clazz) throws Exception {
        ProtocolType type = protoClassType.get(clazz);
        if (Objects.nonNull(type)) {
            return type;
        } else {
            try {
                Method methodP = clazz.getMethod("parseFrom", byte[].class);
                Method methodT = clazz.getMethod("toByteArray");
                if (Objects.nonNull(methodP) && Objects.nonNull(methodT)) {
                    type = ProtocolType.PROTO;
                    protoEncodeCache.put(clazz, methodT);
                    protoDecodeCache.put(clazz, methodP);
                } else {
                    type = ProtocolType.JSON;
                }
                protoClassType.put(clazz, type);
            } catch (Exception ignored) {
                type = ProtocolType.JSON;
            }
        }

        return type;
    }

    public static int parseCommandId(Object object) {
        return object.getClass().getSimpleName().hashCode();
    }

}
