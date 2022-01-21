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

package com.bitactor.framework.cloud.spring.boot.connector;

import com.bitactor.framework.cloud.spring.boot.connector.annotation.EnableBitactorConnector;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author WXH
 */
@Configuration
@ConditionalOnBean(annotation = EnableBitactorConnector.class)
@ConditionalOnProperty(prefix = "spring.bitactor.connector", value = "enabled", matchIfMissing = true)
@EnableConfigurationProperties(BitactorConnectorProperties.class)
@ComponentScan("com.bitactor.framework.cloud.spring")
public class BitactorConnectorAutoConfiguration{
}
