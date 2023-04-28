/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import java.util.List;

/**
 * Table type model.
 */
public class TableType extends Type {

    @Expose
    public Type rowType;

    @Expose
    public List<String> keys;

    @Expose
    public Type constraintType;

    public TableType(Type rowType, List<String> keys, Type constraintType) {
        this.typeName = "table";
        this.keys = keys;
        this.rowType = rowType;
        this.constraintType = constraintType;
    }

    public Type getRowType() {
        return rowType;
    }

    public void setRowType(Type rowType) {
        this.rowType = rowType;
    }

    public List<String> getKeys() {
        return keys;
    }

    public void setKeys(List<String> keys) {
        this.keys = keys;
    }

    public Type getConstraintType() {
        return constraintType;
    }

    public void setConstraintType(Type constraintType) {
        this.constraintType = constraintType;
    }
}
