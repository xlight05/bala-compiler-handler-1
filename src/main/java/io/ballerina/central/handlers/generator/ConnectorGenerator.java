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

import io.ballerina.central.handlers.models.connector.Connector;
import io.ballerina.central.handlers.models.connector.Function;
import io.ballerina.central.handlers.models.connector.Type;
import io.ballerina.central.handlers.models.connector.types.PathType;
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.projects.Project;
import org.ballerinalang.docgen.Generator;
import org.ballerinalang.docgen.docs.BallerinaDocGenerator;
import org.ballerinalang.docgen.generator.model.ModuleDoc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Generator used to generate the Connector model.
 */
public class ConnectorGenerator {

    public static List<Connector> generateConnectorModel(Project project) throws IOException {
        List<Connector> connectors = new ArrayList<>();
        Map<String, ModuleDoc> moduleDocMap = BallerinaDocGenerator.generateModuleDocMap(project);
        for (Map.Entry<String, ModuleDoc> moduleDoc : moduleDocMap.entrySet()) {
            SemanticModel model = moduleDoc.getValue().semanticModel;
            for (Map.Entry<String, SyntaxTree> syntaxTreeMapEntry : moduleDoc.getValue().syntaxTreeMap.entrySet()) {
                connectors.addAll(getConnectorModelFromSyntaxTree(syntaxTreeMapEntry.getValue(), model,
                        project.currentPackage().packageOrg().toString(),
                        project.currentPackage().packageName().toString(),
                        project.currentPackage().packageVersion().toString(), moduleDoc.getKey()));
            }
        }
        return connectors;
    }

    /**
     * Check if there are connectors with duplicate names.
     *
     * @param connectors List of connectors
     * @return boolean
     */
    public static boolean containsDuplicateNames(List<Connector> connectors) {
        List<String> connectorNames = new ArrayList<>();
        for (Connector connector : connectors) {
            if (connectorNames.contains(connector.getName())) {
                return true;
            } else {
                connectorNames.add(connector.getName());
            }
        }
        return false;
    }

    public static List<Connector> getConnectorModelFromSyntaxTree(SyntaxTree syntaxTree, SemanticModel semanticModel,
                                                                  String orgName, String packageName, String version,
                                                                  String moduleName) {
        Type.clearVisitedTypeMap();
        List<Connector> connectorsList = new ArrayList<>();
        if (!syntaxTree.containsModulePart()) {
            return connectorsList;
        }
        ModulePartNode modulePartNode = syntaxTree.rootNode();
        for (Node node : modulePartNode.members()) {
            if (node.kind() != SyntaxKind.CLASS_DEFINITION) {
                continue;
            }
            ClassDefinitionNode classDefinition = (ClassDefinitionNode) node;
            if (classDefinition.visibilityQualifier().isEmpty() || !classDefinition.visibilityQualifier().get()
                    .kind().equals(SyntaxKind.PUBLIC_KEYWORD) || !Generator
                    .containsToken(classDefinition.classTypeQualifiers(), SyntaxKind.CLIENT_KEYWORD)) {
                continue;
            }
            String connectorName = classDefinition.className().text();
            String description = GeneratorUtils.getDocFromMetadata(classDefinition.metadata());
            Map<String, String> connectorAnnotation =
                    GeneratorUtils.getDisplayAnnotationFromMetadataNode(classDefinition.metadata());
            List<Function> functions = new ArrayList<>();
            for (Node member : classDefinition.members()) {
                if (!(member instanceof FunctionDefinitionNode)) {
                    continue;
                }
                NodeList<Token> qualifierList = ((FunctionDefinitionNode) member).qualifierList();
                if ((Generator.containsToken(qualifierList, SyntaxKind.PUBLIC_KEYWORD) ||
                        Generator.containsToken(qualifierList, SyntaxKind.REMOTE_KEYWORD) ||
                        Generator.containsToken(qualifierList, SyntaxKind.RESOURCE_KEYWORD))) {
                    FunctionDefinitionNode functionDefinition = (FunctionDefinitionNode) member;
                    List<PathType> pathParams = new ArrayList<>();
                    List<Type> queryParams = new ArrayList<>();

                    String functionName = functionDefinition.functionName().text();
                    Map<String, String> funcAnnotation =
                            GeneratorUtils.getDisplayAnnotationFromMetadataNode(functionDefinition.metadata());

                    FunctionSignatureNode functionSignature = functionDefinition.functionSignature();
                    pathParams.addAll(GeneratorUtils.getPathParameters(functionDefinition.relativeResourcePath()));
                    queryParams.addAll(GeneratorUtils.getFunctionParameters(functionSignature.parameters(),
                            functionDefinition.metadata(), semanticModel));

                    Type returnParam = null;
                    if (functionSignature.returnTypeDesc().isPresent()) {
                        returnParam = GeneratorUtils.getReturnParameter(functionSignature.returnTypeDesc().get(),
                                functionDefinition.metadata(), semanticModel);
                    }

                    String[] qualifierArr = new String[qualifierList.size()];
                    for (int i = 0; i < qualifierList.size(); i++) {
                        qualifierArr[i] = qualifierList.get(i).toString().trim();
                    }

                    functions.add(new Function(functionName, pathParams, queryParams, returnParam, funcAnnotation,
                            qualifierArr, GeneratorUtils.getDocFromMetadata(functionDefinition.metadata())));
                }
            }
            connectorsList.add(new Connector(orgName, moduleName, packageName, version, connectorName,
                    description, connectorAnnotation, functions));
        }
        return connectorsList;
    }

}
