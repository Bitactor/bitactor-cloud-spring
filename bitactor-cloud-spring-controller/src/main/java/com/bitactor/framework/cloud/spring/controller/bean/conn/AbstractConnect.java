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

package com.bitactor.framework.cloud.spring.controller.bean.conn;


import com.bitactor.framework.cloud.spring.controller.session.ClientNetSession;

/**
 * @author WXH
 */
public abstract class AbstractConnect<T> {

    /**
     * 玩家
     *
     * @return
     */
    public abstract T getUid();

    /**
     * 获取玩家Session
     *
     * @return
     */
    public abstract ClientNetSession getSession();

    /**
     * 获取玩家Session
     *
     * @return
     */
    public abstract void setSession(ClientNetSession session);

    /**
     * 检查并更新Session
     *
     * @param session
     * @return
     */
    public boolean checkAndUpdateSession(ClientNetSession session) {
        ClientNetSession oldSession = getSession();
        if (oldSession == null) {
            return false;
        }
        if (!oldSession.getSessionId().equals(session.getSessionId())) {
            return false;
        }
        setSession(session);
        return true;
    }


    /**
     * 尝试加锁
     */
    public abstract void tryLock() throws Throwable;

    /**
     * 最终事件,
     *
     * @return
     */
    public abstract void doFinishEvent();

    /**
     * 释放锁
     */
    public abstract void unLock();
}
