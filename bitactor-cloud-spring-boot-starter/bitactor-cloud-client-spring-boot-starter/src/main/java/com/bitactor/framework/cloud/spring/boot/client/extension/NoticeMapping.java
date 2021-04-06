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

import com.bitactor.framework.cloud.spring.model.utils.MessageUtil;

/**
 * @author WXH
 */
public abstract class NoticeMapping {
    private int noticeId;
    private Class<?> noticeClazz;

    public NoticeMapping(Class<?> noticeClazz) {
        this.noticeId = MessageUtil.getCommandId(noticeClazz);
        this.noticeClazz = noticeClazz;
    }

    public NoticeMapping(int noticeId, Class<?> noticeClazz) {
        this.noticeId = noticeId;
        this.noticeClazz = noticeClazz;
    }

    public abstract void notice(Object notice);

    public int getNoticeId() {
        return noticeId;
    }

    public Class<?> getNoticeClazz() {
        return noticeClazz;
    }
}
