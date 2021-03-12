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
import com.bitactor.framework.core.net.api.Channel;
import com.bitactor.framework.core.net.api.Client;
import org.springframework.context.annotation.Scope;

import java.util.concurrent.CompletableFuture;

/**
 * @author WXH
 */
@Scope("prototype")
public interface ClientEntity<T> {
    T getUid();

    void init();

    Channel getChannel();

    Client getClient();

    void setClient(Client client);

    void onActivity();

    void onReceived(MessageConnectorData connMessage);

    <R> R sendSync(Object req, Class<R> resp, RequestStage requestEvent);

    <R> CompletableFuture<R> sendAsync(Object req, Class<R> resp, RequestStage requestEvent);

    void onDestroy();

    void doClose();
}
