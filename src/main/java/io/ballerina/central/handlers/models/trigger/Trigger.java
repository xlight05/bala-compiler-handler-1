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

package io.ballerina.central.handlers.models.trigger;

import io.ballerina.central.handlers.models.connector.Type;
import com.google.gson.annotations.Expose;

import java.util.List;
import java.util.Map;

/**
 * Trigger model.
 */
public class Trigger {
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
    public List<ServiceType> serviceTypes;
    @Expose
    private List<Type> listenerParams;
    @Expose
    private String listenerProtocol;

    public Trigger(String orgName, String moduleName, String packageName, String version, String name,
                   String documentation, Map<String, String> displayAnnotation, List<ServiceType> serviceTypes,
                   List<Type> listenerParams, String listenerProtocol) {
        this.orgName = orgName;
        this.moduleName = moduleName;
        this.packageName = packageName;
        this.version = version;
        this.name = name;
        this.documentation = documentation;
        this.displayAnnotation = displayAnnotation;
        this.serviceTypes = serviceTypes;
        this.listenerParams = listenerParams;
        this.setListenerProtocol(listenerProtocol);
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

    public List<ServiceType> getServiceTypes() {
        return serviceTypes;
    }

    public void setServiceTypes(List<ServiceType> serviceTypes) {
        this.serviceTypes = serviceTypes;
    }

    public List<Type> getListenerParams() {

        return listenerParams;
    }

    public void setListenerParams(List<Type> listenerParams) {

        this.listenerParams = listenerParams;
    }

    public String getListenerProtocol() {

        return listenerProtocol;
    }

    public void setListenerProtocol(String listenerProtocol) {

        this.listenerProtocol = listenerProtocol;
    }
}
