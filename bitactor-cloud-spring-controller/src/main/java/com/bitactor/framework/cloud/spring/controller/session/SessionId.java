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

package com.bitactor.framework.cloud.spring.controller.session;

/**
 * @author WXH
 */
public class SessionId {
    /**
     * session的唯一id
     */
    private String channelId;
    /**
     * 客户端与服务器直连的服务器标识
     */
    private String sid;

    public SessionId(){}
    public SessionId(String channelId, String sid) {
        this.channelId = channelId;
        this.sid = sid;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof SessionId)) {
            return false;
        }
        if (!obj.toString().equals(this.toString())) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return sid + "@" + channelId;
    }
}
