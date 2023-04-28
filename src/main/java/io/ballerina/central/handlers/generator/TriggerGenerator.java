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

package io.ballerina.central.handlers.generator;

import io.ballerina.central.handlers.models.connector.Function;
import io.ballerina.central.handlers.models.connector.Type;
import io.ballerina.central.handlers.models.connector.types.ObjectType;
import io.ballerina.central.handlers.models.connector.types.UnionType;
import io.ballerina.central.handlers.models.trigger.ServiceType;
import io.ballerina.central.handlers.models.trigger.Trigger;
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.MethodDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ObjectTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.projects.Project;
import org.ballerinalang.docgen.Generator;
import org.ballerinalang.docgen.docs.BallerinaDocGenerator;
import org.ballerinalang.docgen.generator.model.ModuleDoc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Generator used to generate the Listener model.
 */
public class TriggerGenerator {

    public static final String TRIGGER_LISTENER_FILE_NAME = "listener.bal";
    public static final String TRIGGER_SERVICE_TYPES_FILE_NAME = "service_types.bal";
    public static final String TRIGGER_DATA_TYPES_FILE_NAME = "data_types.bal";
    public static final String TRIGGER_DISPATCHER_SERVICE_FILE_NAME = "dispatcher_service.bal";
    public static final String TRIGGER_LISTENER_CLASS_NAME = "Listener";
    public static final String INIT_FUNCTION_NAME = "init";
    public static final String TRIGGER_NAME = "Trigger";
    public static final String DEFAULT_LISTENER_PROTOCOL = "generic";
    public static final String DEFAULT_LISTEN_ON_PARAM_NAME = "listenOn";
    public static final String DEFAULT_LISTEN_ON_PARAM_TYPE = "union";
    public static final String DEFAULT_LISTEN_ON_LISTENER_TYPE = "Listener";

    public static List<Trigger> generateTriggerModel(Project project) throws IOException {

        List<Trigger> triggers = new ArrayList<>();
        Map<String, ModuleDoc> moduleDocMap = BallerinaDocGenerator.generateModuleDocMap(project);
        for (Map.Entry<String, ModuleDoc> moduleDoc : moduleDocMap.entrySet()) {
            if (checkValidTrigger(moduleDoc.getValue())) {
                SemanticModel semModel = moduleDoc.getValue().semanticModel;
                Optional<SyntaxTree> listenerFileList = moduleDoc.getValue().syntaxTreeMap.entrySet().stream()
                        .filter(e -> TRIGGER_LISTENER_FILE_NAME.equals(e.getKey()))
                        .map(Map.Entry::getValue)
                        .findFirst();
                List<Type> initParameterTypes = getInitParametersFromListener(listenerFileList, semModel);
                Map<String, String> triggerAnnotation = fetchDisplayAnnotationFromTrigger(listenerFileList);
                if (initParameterTypes.size() != 0) { //0 if display annotation is not specified
                    Optional<SyntaxTree> serviceTypeFile = moduleDoc.getValue().syntaxTreeMap.entrySet().stream()
                            .filter(e -> TRIGGER_SERVICE_TYPES_FILE_NAME.equals(e.getKey()))
                            .map(Map.Entry::getValue)
                            .findFirst();
                    triggers.add(getTriggerModelFromSyntaxTree(serviceTypeFile, semModel,
                            project.currentPackage().packageOrg().toString(),
                            project.currentPackage().packageName().toString(),
                            project.currentPackage().packageVersion().toString(),
                            moduleDoc.getKey(), initParameterTypes, triggerAnnotation));
                }
            }
        }
        return triggers;
    }

    public static Trigger getTriggerModelFromSyntaxTree(Optional<SyntaxTree> serviceTypeST, SemanticModel semanticModel,
                                                        String orgName, String packageName, String version,
                                                        String moduleName, List<Type> initParameterTypes,
                                                        Map<String, String> displayAnnotation) {

        List<ServiceType> serviceTypes = new ArrayList<>();
        if (serviceTypeST.isEmpty()) {
            return null;
        }
        SyntaxTree syntaxTree = serviceTypeST.get();
        if (!syntaxTree.containsModulePart()) {
            return null;
        }
        ModulePartNode rootNode = syntaxTree.rootNode();
        for (Node rootMemberNode : rootNode.members()) {
            if (!SyntaxKind.TYPE_DEFINITION.equals(rootMemberNode.kind())) {
                continue;
            }
            TypeDefinitionNode typeDefinitionNode = (TypeDefinitionNode) rootMemberNode;
            String serviceTypeName = typeDefinitionNode.typeName().text();
            Map<String, String> serviceTypeAnnotation =
                    GeneratorUtils.getDisplayAnnotationFromMetadataNode(typeDefinitionNode.metadata());
            String serviceTypeDescription = GeneratorUtils.getDocFromMetadata(typeDefinitionNode.metadata());
            if (!(typeDefinitionNode.typeDescriptor() instanceof ObjectTypeDescriptorNode)) {
                continue;
            }
            List<Function> serviceFunctions = new ArrayList<>();
            NodeList<Node> serviceMembers = ((ObjectTypeDescriptorNode) typeDefinitionNode.typeDescriptor()).members();
            for (Node remoteFunctionNode : serviceMembers) {
                if (!(remoteFunctionNode instanceof MethodDeclarationNode)) {
                    continue;
                }
                NodeList<Token> qualifierList = ((MethodDeclarationNode) remoteFunctionNode).qualifierList();
                if ((Generator.containsToken(qualifierList, SyntaxKind.REMOTE_KEYWORD))) {
                    List<Type> remoteFunctionParameters = new ArrayList<>();
                    MethodDeclarationNode methodDeclarationNode = (MethodDeclarationNode) remoteFunctionNode;

                    String functionName = methodDeclarationNode.methodName().text();
                    Map<String, String> funcAnnotation =
                            GeneratorUtils.getDisplayAnnotationFromMetadataNode(
                                    methodDeclarationNode.metadata());

                    FunctionSignatureNode functionSignature = methodDeclarationNode.methodSignature();
                    remoteFunctionParameters.addAll(GeneratorUtils.getFunctionParameters(functionSignature.parameters(),
                            methodDeclarationNode.metadata(), semanticModel));

                    Type returnParam = null;
                    if (functionSignature.returnTypeDesc().isPresent()) {
                        returnParam = GeneratorUtils.getReturnParameter(functionSignature.returnTypeDesc().get(),
                                methodDeclarationNode.metadata(), semanticModel);
                    }

                    String[] qualifierArr = new String[qualifierList.size()];
                    for (int i = 0; i < qualifierList.size(); i++) {
                        qualifierArr[i] = qualifierList.get(i).toString().trim();
                    }

                    serviceFunctions.add(
                            new Function(functionName, remoteFunctionParameters, returnParam, funcAnnotation,
                                    qualifierArr, GeneratorUtils.getDocFromMetadata(methodDeclarationNode.metadata())));
                }
            }
            ServiceType serviceType = new ServiceType(serviceTypeName, serviceFunctions,
                    serviceTypeDescription, serviceTypeAnnotation);
            serviceTypes.add(serviceType);

        }
        //TODO: Add documentation and display annotations for triggers
        return new Trigger(orgName, moduleName, packageName, version, TRIGGER_NAME, "",
                displayAnnotation, serviceTypes, initParameterTypes,
                getTransportProtocolFromListenerParams(initParameterTypes));
    }

    public static String getTransportProtocolFromListenerParams(List<Type> initParameterTypes) {

        String transportProtocol = DEFAULT_LISTENER_PROTOCOL;
        //Check whether the asyncapi listenOn parameter structure is there
        for (Type parameterType : initParameterTypes) {
            if (DEFAULT_LISTEN_ON_PARAM_NAME.equals(parameterType.name)
                    && DEFAULT_LISTEN_ON_PARAM_TYPE.equals(parameterType.typeName)
                    && parameterType instanceof  UnionType) {
                for (Type unionType : ((UnionType) parameterType).members) {
                    if (unionType instanceof ObjectType && DEFAULT_LISTEN_ON_LISTENER_TYPE.equals(unionType.name)) {
                        transportProtocol = deriveTransportProtocolFromModuleName(unionType.typeInfo.moduleName);
                    }
                }
            }
        }
        return transportProtocol;
    }

    private static String deriveTransportProtocolFromModuleName(String moduleName) {
        //Define the mapping between listener's module name and transport
        return moduleName;
    }

    public static List<Type> getInitParametersFromListener(Optional<SyntaxTree> listenerFile, SemanticModel model) {

        List<Type> listenerInitParameters = new ArrayList<>();
        if (listenerFile == null || listenerFile.isEmpty()) {
            return listenerInitParameters;
        }
        SyntaxTree listenerFileST = listenerFile.get();
        if (!listenerFileST.containsModulePart()) {
            return listenerInitParameters;
        }
        ModulePartNode rootNode = listenerFileST.rootNode();
        for (Node memberNode : rootNode.members()) {
            if (!SyntaxKind.CLASS_DEFINITION.equals(memberNode.kind())) {
                continue;
            }
            ClassDefinitionNode classDefinitionNode = (ClassDefinitionNode) memberNode;
            if (classDefinitionNode.visibilityQualifier().isPresent() &&
                    classDefinitionNode.visibilityQualifier().get().kind().equals(SyntaxKind.PUBLIC_KEYWORD)
                    && TRIGGER_LISTENER_CLASS_NAME.equals(classDefinitionNode.className().text())) {
                //Unused Variable
//                Map<String, String> listenerAnnotation =
//                        GeneratorUtils.getDisplayAnnotationFromMetadataNode(classDefinitionNode.metadata());
                // Validate Trigger
                for (Node classMembers : classDefinitionNode.members()) {
                    if (classMembers instanceof FunctionDefinitionNode &&
                            (Generator.containsToken(((FunctionDefinitionNode) classMembers).qualifierList(),
                                    SyntaxKind.PUBLIC_KEYWORD)) && INIT_FUNCTION_NAME.equals(
                            ((FunctionDefinitionNode) classMembers).functionName().text())
                    ) {
                        FunctionDefinitionNode initFunctionDefNode = ((FunctionDefinitionNode) classMembers);
                        listenerInitParameters.addAll(GeneratorUtils
                                .getFunctionParameters(initFunctionDefNode.functionSignature().parameters(),
                                        initFunctionDefNode.metadata(), model));
                    }
                }
            }
        }
        return listenerInitParameters;
    }

    public static Map<String, String> fetchDisplayAnnotationFromTrigger(Optional<SyntaxTree> listenerFile) {

        Map<String, String> listenerAnnotation = Collections.emptyMap();
        if (listenerFile != null && listenerFile.isPresent()) {
            SyntaxTree listenerFileST = listenerFile.get();
            if (listenerFileST.containsModulePart()) {
                ModulePartNode rootNode = listenerFileST.rootNode();
                for (Node memberNode : rootNode.members()) {
                    if (SyntaxKind.CLASS_DEFINITION.equals(memberNode.kind())) {
                        ClassDefinitionNode classDefinitionNode = (ClassDefinitionNode) memberNode;
                        if (classDefinitionNode.visibilityQualifier().isPresent()
                                &&
                                classDefinitionNode.visibilityQualifier().get().kind().equals(SyntaxKind.PUBLIC_KEYWORD)
                                && TRIGGER_LISTENER_CLASS_NAME.equals(classDefinitionNode.className().text())) {
                            listenerAnnotation =
                                    GeneratorUtils.getDisplayAnnotationFromMetadataNode(classDefinitionNode.metadata());
                        }
                    }
                }
            }
        }
        return listenerAnnotation;
    }

    private static boolean checkValidTrigger(ModuleDoc moduleDoc) {

        boolean hasDataTypes = false;
        boolean hasServiceTypes = false;
        boolean hasDispatcherService = false;
        boolean hasListener = false;

        for (String fileName : moduleDoc.syntaxTreeMap.keySet()) {
            if (TRIGGER_DATA_TYPES_FILE_NAME.equals(fileName)) {
                hasDataTypes = true;
            } else if (TRIGGER_SERVICE_TYPES_FILE_NAME.equals(fileName)) {
                hasServiceTypes = true;
            } else if (TRIGGER_DISPATCHER_SERVICE_FILE_NAME.equals(fileName)) {
                hasDispatcherService = true;
            } else if (TRIGGER_LISTENER_FILE_NAME.equals(fileName)) {
                hasListener = true;
            }
        }
        return hasDataTypes && hasServiceTypes && hasDispatcherService && hasListener;
    }
}

