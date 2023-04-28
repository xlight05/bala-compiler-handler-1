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

import java.util.ArrayList;
import java.util.List;

/**
 * Intersection type model.
 */
public class IntersectionType extends Type {

    @Expose
    public List<Type> members;

    public IntersectionType() {
        this.typeName = "intersection";
        this.members = new ArrayList<>();
    }

    public List<Type> getMembers() {
        return members;
    }

    public void setMembers(List<Type> members) {
        this.members = members;
    }
}
