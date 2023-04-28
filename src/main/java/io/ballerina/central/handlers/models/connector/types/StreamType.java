/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.ballerina.central.handlers.models.connector.Type;
import com.google.gson.annotations.Expose;

/**
 * Stream type model.
 */
public class StreamType extends Type {
    @Expose
    public Type leftTypeParam;
    @Expose
    public Type rightTypeParam;

    public StreamType(Type leftTypeParam, Type rightTypeParam) {
        this.typeName = "stream";
        this.leftTypeParam = leftTypeParam;
        this.rightTypeParam = rightTypeParam;
    }

    public Type getLeftTypeParam() {
        return leftTypeParam;
    }

    public void setLeftTypeParam(Type leftTypeParam) {
        this.leftTypeParam = leftTypeParam;
    }

    public Type getRightTypeParam() {
        return rightTypeParam;
    }

    public void setRightTypeParam(Type rightTypeParam) {
        this.rightTypeParam = rightTypeParam;
    }
}
