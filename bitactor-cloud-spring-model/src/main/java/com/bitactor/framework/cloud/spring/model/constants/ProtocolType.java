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

package com.bitactor.framework.cloud.spring.model.constants;

/**
 * @author WXH
 */
public enum ProtocolType {
    PROTO(0, "PROTO"),
    JSON(1, "JSON");
    private int type;
    private String des;

    private ProtocolType(int type, String des) {
        this.type = type;
        this.des = des;
    }

    public String valueStr() {
        return this.des;
    }

    public int valueInt() {
        return this.type;
    }

    public static ProtocolType valueOfInt(int intType) {
        ProtocolType protocolType = null;
        for (ProtocolType type : ProtocolType.values()) {
            if (type.valueInt() == intType) {
                protocolType = type;
                break;
            }
        }
        return protocolType;
    }
}
