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

package com.bitactor.framework.cloud.spring.boot.cluster.config;


import com.alibaba.nacos.api.common.Constants;
import com.bitactor.framework.core.constant.RPCConstants;

/**
 * @author WXH
 */
public class SpringRegistryConfig {
    /**
     * 注册中心的主机地址
     */
    private String host;
    /**
     * 注册中心的端口
     */
    private Integer port;
    /**
     * 注册中心的账户名
     */
    private String username;
    /**
     * 注册中心的密码
     */
    private String password;
    /**
     * 注册中心类型，默认nacos
     */
    private String protocol = RPCConstants.DEFAULT_REGISTRY;
    /**
     * 注册的端,服务提供者，还是服务消费者
     */
    private String side;
    /**
     * 注册中心的备用节点
     */
    private String backup;
    /**
     * 默认分组
     */
    private String defGroup = Constants.DEFAULT_GROUP;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getSide() {
        return side;
    }

    public void setSide(String side) {
        this.side = side;
    }

    public String getBackup() {
        return backup;
    }

    public void setBackup(String backup) {
        this.backup = backup;
    }

    public String getDefGroup() {
        return defGroup;
    }

    public void setDefGroup(String defGroup) {
        this.defGroup = defGroup;
    }
}
