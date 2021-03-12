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

package com.bitactor.framework.cloud.spring.core.listener;

import com.bitactor.framework.cloud.spring.core.constants.BannerConstants;
import com.bitactor.framework.cloud.spring.core.constants.BitactorLogo;
import com.bitactor.framework.core.Version;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.Banner.Mode;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.logging.LoggingApplicationListener;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.Order;

/**
 * @author WXH
 */
@Order(LoggingApplicationListener.DEFAULT_ORDER)
public class StartBannerApplicationListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {
    private static final Log logger = LogFactory.getLog(StartBannerApplicationListener.class);

    private static Mode BANNER_MODE = Mode.CONSOLE;

    public static void setBANNER_MODE(Mode bANNER_MODE) {
        BANNER_MODE = bANNER_MODE;
    }

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        if (BANNER_MODE == Mode.OFF) {
            return;
        }
        String bannerText = this.buildBannerText();
        if (BANNER_MODE == Mode.CONSOLE) {
            System.out.print(bannerText);
        } else if (BANNER_MODE == Mode.LOG) {
            logger.info(bannerText);
        }
    }

    private String buildBannerText() {
        StringBuilder bannerTextBuilder = new StringBuilder();
        bannerTextBuilder.append(BannerConstants.LINE_SEPARATOR).append(BitactorLogo.logo)
                .append(" :: bitactor ::        (v").append(Version.getVersion()).append(")")
                .append(BannerConstants.LINE_SEPARATOR);
        return bannerTextBuilder.toString();
    }
}
