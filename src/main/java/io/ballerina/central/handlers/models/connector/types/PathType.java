/*
 * Copyright (c) 2023, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.central.handlers.models.connector.types;

import com.google.gson.annotations.Expose;

/**
 * Path parameter type model.
 */
public class PathType {
    @Expose
    public String name;
    @Expose
    public String typeName;
    @Expose
    public boolean isRestType;

    public PathType() {
    }

    public PathType(String name, String typeName) {
        this.name = name;
        this.typeName = typeName;
        this.isRestType = false;
    }

    public PathType(String name, String typeName, boolean isRestType) {
        this.name = name;
        this.typeName = typeName;
        this.isRestType = isRestType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public boolean isRestType() {
        return isRestType;
    }

    public void setRestType(boolean restType) {
        isRestType = restType;
    }
}
