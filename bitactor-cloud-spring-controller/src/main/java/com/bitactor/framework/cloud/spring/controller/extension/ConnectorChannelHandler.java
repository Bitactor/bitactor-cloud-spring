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

package com.bitactor.framework.cloud.spring.controller.extension;


import com.bitactor.framework.cloud.spring.controller.session.SessionId;
import com.bitactor.framework.core.net.api.Channel;

import java.util.Collection;
import java.util.List;

/**
 * @author WXH
 */
public interface ConnectorChannelHandler {
    /**
     * 获取集合中的所有存在的channel
     *
     * @param channelIds
     * @return
     */
    List<Channel> getConnectorChannels(List<SessionId> channelIds);
    /**
     * 返回一个通道实例
     *
     * @param sessionId sessionId
     * @return 通道实例
     */
    Channel getConnectorChannel(SessionId sessionId);

    /**
     * 获取通道集合
     *
     * @return
     */
    Collection<Channel> getConnectorChannels();
}
