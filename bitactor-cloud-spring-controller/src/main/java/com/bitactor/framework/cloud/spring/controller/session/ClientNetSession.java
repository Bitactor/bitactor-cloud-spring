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


import com.bitactor.framework.cloud.spring.controller.bean.online.OnlineInfo;

import java.util.HashMap;

/**
 * @author WXH
 */
public class ClientNetSession {
    private HashMap<String, Object> param = new HashMap<String, Object>();
    private SessionId sessionId;
    private long uid;
    private String userId;

    public ClientNetSession(String sessionId, String sid) {
        this.sessionId = new SessionId(sessionId, sid);
    }

    public HashMap<String, Object> getParam() {
        return param;
    }

    public void setParam(HashMap<String, Object> param) {
        this.param = param;
    }

    public SessionId getSessionId() {
        return sessionId;
    }

    public void setSessionId(SessionId sessionId) {
        this.sessionId = sessionId;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public void addParam(String key, Object value) {
        param.put(key, value);
        // TODO SpringUtils
        /*try {
            SpringUtils.getBean(OnlineManager.class).update(build());
        } catch (BeansException ignored) {
        }*/
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public <T> T getParamInstance(String key) {
        try {
            return (T) (param.get(key));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public <T> T removeParam(String key) {
        try {
            return (T) (param.remove(key));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("sessionId: ").append(sessionId).append(", ");
        sb.append("uid: ").append(uid).append(", ");
        sb.append("param: ").append(param);
        return sb.toString();
    }
    public OnlineInfo build() {
        OnlineInfo onlineInfo = new OnlineInfo();
        onlineInfo.setId(getUserId());
        onlineInfo.setSessionId(getSessionId());
        onlineInfo.setParams(new HashMap<>(getParam()));
        return onlineInfo;
    }
}
