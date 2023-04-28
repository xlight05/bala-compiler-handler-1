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

import com.google.gson.annotations.Expose;

import java.util.List;
import java.util.Map;

/**
 * Connector model.
 */
public class Connector {
    @Expose
    public String orgName;
    @Expose
    public String moduleName;
    @Expose
    public String packageName;
    @Expose
    public String version;
    @Expose
    public String name;
    @Expose
    public String documentation;
    @Expose
    public Map<String, String> displayAnnotation;
    @Expose
    public List<Function> functions;

    public Connector(String orgName, String moduleName, String packageName, String version, String name,
                     String documentation, Map<String, String> displayAnnotation, List<Function> functions) {
        this.orgName = orgName;
        this.moduleName = moduleName;
        this.packageName = packageName;
        this.version = version;
        this.name = name;
        this.documentation = documentation;
        this.displayAnnotation = displayAnnotation;
        this.functions = functions;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public List<Function> getFunctions() {
        return functions;
    }

    public void setFunctions(List<Function> functions) {
        this.functions = functions;
    }
}
