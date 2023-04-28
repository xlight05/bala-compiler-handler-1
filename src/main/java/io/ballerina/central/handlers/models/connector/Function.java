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
package io.ballerina.central.handlers.models.connector;

import io.ballerina.central.handlers.models.connector.types.PathType;
import com.google.gson.annotations.Expose;

import java.util.List;
import java.util.Map;

/**
 * Function model.
 */
public class Function {
    @Expose
    public String name;
    @Expose
    public List<PathType> pathParams;
    @Expose
    public List<Type> parameters;
    @Expose
    public Type returnType;
    @Expose
    public String[] qualifiers;
    @Expose
    public String documentation;
    @Expose
    public Map<String, String> displayAnnotation;

    public Function(String name, List<Type> queryParams, Type returnType, Map<String, String> displayAnnotation,
                    String[] qualifiers, String documentation) {
        this.name = name;
        this.parameters = queryParams;
        this.returnType = returnType;
        this.displayAnnotation = displayAnnotation;
        this.qualifiers = qualifiers;
        this.documentation = documentation;
    }

    public Function(String name, List<PathType> pathParams, List<Type> queryParams, Type returnType,
                    Map<String, String> displayAnnotation, String[] qualifiers, String documentation) {
        this.name = name;
        this.pathParams = pathParams;
        this.parameters = queryParams;
        this.returnType = returnType;
        this.displayAnnotation = displayAnnotation;
        this.qualifiers = qualifiers;
        this.documentation = documentation;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<PathType> getPathParams() {
        return pathParams;
    }

    public void setPathParams(List<PathType> pathParams) {
        this.pathParams = pathParams;
    }

    public List<Type> getParameters() {
        return parameters;
    }

    public void setParameters(List<Type> parameters) {
        this.parameters = parameters;
    }

    public Type getReturnType() {
        return returnType;
    }

    public void setReturnType(Type returnType) {
        this.returnType = returnType;
    }

    public String[] getQualifiers() {
        return qualifiers;
    }

    public void setQualifiers(String[] qualifiers) {
        this.qualifiers = qualifiers;
    }

    public String getDocumentation() {
        return documentation;
    }

    public void setDocumentation(String documentation) {
        this.documentation = documentation;
    }

    public Map<String, String> getDisplayAnnotation() {
        return displayAnnotation;
    }

    public void setDisplayAnnotation(Map<String, String> displayAnnotation) {
        this.displayAnnotation = displayAnnotation;
    }
}
