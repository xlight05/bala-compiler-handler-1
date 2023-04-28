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

import io.ballerina.central.handlers.models.connector.types.ArrayType;
import io.ballerina.central.handlers.models.connector.types.EnumType;
import io.ballerina.central.handlers.models.connector.types.ErrorType;
import io.ballerina.central.handlers.models.connector.types.InclusionType;
import io.ballerina.central.handlers.models.connector.types.IntersectionType;
import io.ballerina.central.handlers.models.connector.types.MapType;
import io.ballerina.central.handlers.models.connector.types.ObjectType;
import io.ballerina.central.handlers.models.connector.types.PrimitiveType;
import io.ballerina.central.handlers.models.connector.types.RecordType;
import io.ballerina.central.handlers.models.connector.types.StreamType;
import io.ballerina.central.handlers.models.connector.types.TableType;
import io.ballerina.central.handlers.models.connector.types.UnionType;
import com.google.gson.annotations.Expose;
import io.ballerina.compiler.api.ModuleID;
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.impl.symbols.BallerinaIntersectionTypeSymbol;
import io.ballerina.compiler.api.impl.symbols.BallerinaRecordTypeSymbol;
import io.ballerina.compiler.api.impl.symbols.BallerinaUnionTypeSymbol;
import io.ballerina.compiler.api.symbols.ArrayTypeSymbol;
import io.ballerina.compiler.api.symbols.ErrorTypeSymbol;
import io.ballerina.compiler.api.symbols.IntersectionTypeSymbol;
import io.ballerina.compiler.api.symbols.MapTypeSymbol;
import io.ballerina.compiler.api.symbols.ObjectTypeSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.StreamTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TableTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.compiler.syntax.tree.ArrayTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.IntersectionTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.MapTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.OptionalTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.RecordFieldNode;
import io.ballerina.compiler.syntax.tree.RecordTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.StreamTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.StreamTypeParamsNode;
import io.ballerina.compiler.syntax.tree.TableTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.UnionTypeDescriptorNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Type model.
 */
public class Type {
    @Expose
    public String name;
    @Expose
    public String typeName;
    @Expose
    public boolean optional;
    @Expose
    public TypeInfo typeInfo;
    @Expose
    public boolean defaultable;
    @Expose
    public String defaultValue;

    @Expose
    public Map<String, String> displayAnnotation;
    @Expose
    public String documentation;

    public Type() {
    }

    public Type(String name, String typeName, boolean optional, TypeInfo typeInfo, boolean defaultable,
                String defaultValue, Map<String, String> displayAnnotation, String documentation) {
        this.name = name;
        this.typeName = typeName;
        this.optional = optional;
        this.typeInfo = typeInfo;
        this.defaultable = defaultable;
        this.defaultValue = defaultValue;
        this.displayAnnotation = displayAnnotation;
        this.documentation = documentation;
    }

    private static Map<String, VisitedType> visitedTypeMap = new HashMap<>();

    public static void clearVisitedTypeMap() {
        visitedTypeMap.clear();
    }

    public static Type fromSyntaxNode(Node node, SemanticModel semanticModel) {
        Type type = null;
        if (node instanceof SimpleNameReferenceNode || node instanceof QualifiedNameReferenceNode) {
            Optional<Symbol> optSymbol = null;
            try {
                optSymbol = semanticModel.symbol(node);
            } catch (NullPointerException ignored) {
            }
            if (optSymbol != null && optSymbol.isPresent()) {
                Symbol symbol = optSymbol.get();
                type = fromSemanticSymbol(symbol);
                clearVisitedTypeMap();
            }
        } else if (node instanceof BuiltinSimpleNameReferenceNode) {
            BuiltinSimpleNameReferenceNode builtinSimpleNameReferenceNode = (BuiltinSimpleNameReferenceNode) node;
            type = new PrimitiveType(builtinSimpleNameReferenceNode.name().text());
        } else if (node instanceof OptionalTypeDescriptorNode) {
            OptionalTypeDescriptorNode optionalTypeDescriptorNode = (OptionalTypeDescriptorNode) node;
            type = fromSyntaxNode(optionalTypeDescriptorNode.typeDescriptor(), semanticModel);
            type.optional = true;
            // todo: check syntax tree compatible version for ErrorTypeDescriptorNode
//        } else if (node instanceof ErrorTypeDescriptorNode) {
//            ErrorTypeDescriptorNode errorType = (ErrorTypeDescriptorNode) node;
//            type = new PrimitiveType(errorType.errorKeywordToken().text());
        } else if (node instanceof UnionTypeDescriptorNode) {
            UnionType unionType = new UnionType();
            flattenUnionNode(node, semanticModel, unionType.members);
            type = unionType;
        } else if (node instanceof IntersectionTypeDescriptorNode) {
            IntersectionType intersectionType = new IntersectionType();
            flattenIntersectionNode(node, semanticModel, intersectionType.members);
            type = intersectionType;
        } else if (node instanceof ArrayTypeDescriptorNode) {
            ArrayTypeDescriptorNode arrayTypeDescriptorNode = (ArrayTypeDescriptorNode) node;
            type = new ArrayType(fromSyntaxNode(arrayTypeDescriptorNode.memberTypeDesc(), semanticModel));
        } else if (node instanceof StreamTypeDescriptorNode) {
            StreamTypeDescriptorNode streamNode = (StreamTypeDescriptorNode) node;
            StreamTypeParamsNode streamParams = streamNode.streamTypeParamsNode().isPresent() ?
                    (StreamTypeParamsNode) streamNode.streamTypeParamsNode().get() : null;
            Type leftParam = null, rightParam = null;
            if (streamParams != null) {
                leftParam = fromSyntaxNode(streamParams.leftTypeDescNode(), semanticModel);
                if (streamParams.rightTypeDescNode().isPresent()) {
                    rightParam = fromSyntaxNode(streamParams.rightTypeDescNode().get(), semanticModel);
                }
            }
            type = new StreamType(leftParam, rightParam);
        } else if (node instanceof RecordTypeDescriptorNode) {
            RecordTypeDescriptorNode recordNode = (RecordTypeDescriptorNode) node;
            List<Type> fields = new ArrayList<>();
            recordNode.fields().forEach(node1 -> fields.add(fromSyntaxNode(node1, semanticModel)));
            Type restType = recordNode.recordRestDescriptor().isPresent() ?
                    fromSyntaxNode(recordNode.recordRestDescriptor().get().typeName(), semanticModel) : null;
            type = new RecordType(fields, restType);
        } else if (node instanceof RecordFieldNode) {
            RecordFieldNode recordField = (RecordFieldNode) node;
            type = fromSyntaxNode(recordField.typeName(), semanticModel);
            type.name = recordField.fieldName().text();
        } else if (node instanceof MapTypeDescriptorNode) {
            MapTypeDescriptorNode mapNode = (MapTypeDescriptorNode) node;
            type = new MapType(fromSyntaxNode(mapNode.mapTypeParamsNode().typeNode(), semanticModel));
        } else if (node instanceof TableTypeDescriptorNode) {
            TableTypeDescriptorNode tableTypeNode = (TableTypeDescriptorNode) node;
            Optional<Symbol> optSymbol = null;
            TableTypeSymbol tableTypeSymbol = null;
            Node keyConstraint = null;
            List<String> keySpecifiers = null;
            try {
                optSymbol = semanticModel.symbol(tableTypeNode);
            } catch (NullPointerException ignored) {
            }
            if (optSymbol != null && optSymbol.isPresent()) {
                tableTypeSymbol = (TableTypeSymbol) optSymbol.get();
            }
            if (tableTypeNode.keyConstraintNode().isPresent()) {
                keyConstraint = tableTypeNode.keyConstraintNode().get();
            }
            if (tableTypeSymbol != null) {
                keySpecifiers = tableTypeSymbol.keySpecifiers();
            }
            type = new TableType(fromSyntaxNode(tableTypeNode.rowTypeParameterNode(), semanticModel),
                    keySpecifiers, fromSyntaxNode(keyConstraint, semanticModel));
        } else {
            type = new PrimitiveType(node.toSourceCode());
        }
        return type;
    }

    public static void flattenUnionNode(Node node, SemanticModel semanticModel, List<Type> fields) {
        if (node instanceof UnionTypeDescriptorNode) {
            UnionTypeDescriptorNode unionTypeNode = (UnionTypeDescriptorNode) node;
            flattenUnionNode(unionTypeNode.leftTypeDesc(), semanticModel, fields);
            flattenUnionNode(unionTypeNode.rightTypeDesc(), semanticModel, fields);
            return;
        }
        fields.add(fromSyntaxNode(node, semanticModel));
    }

    public static void flattenIntersectionNode(Node node, SemanticModel semanticModel, List<Type> fields) {
        if (node instanceof IntersectionTypeDescriptorNode) {
            IntersectionTypeDescriptorNode intersectionTypeNode = (IntersectionTypeDescriptorNode) node;
            flattenUnionNode(intersectionTypeNode.leftTypeDesc(), semanticModel, fields);
            flattenUnionNode(intersectionTypeNode.rightTypeDesc(), semanticModel, fields);
            return;
        }
        fields.add(fromSyntaxNode(node, semanticModel));
    }

    public static VisitedType getVisitedType(String typeName) {
        if (visitedTypeMap.containsKey(typeName)) {
            return visitedTypeMap.get(typeName);
        }
        return null;
    }

    public static void completeVisitedTypeEntry(String typeName, Type typeNode) {
        VisitedType visitedType = visitedTypeMap.get(typeName);
        visitedType.setCompleted(true);
        visitedType.setTypeNode(typeNode);
    }

    public static Type fromSemanticSymbol(Symbol symbol) {
        Type type = null;
        if (symbol instanceof TypeReferenceTypeSymbol) {
            TypeReferenceTypeSymbol typeReferenceTypeSymbol = (TypeReferenceTypeSymbol) symbol;
            type = getEnumType(typeReferenceTypeSymbol, symbol);
        } else if (symbol instanceof RecordTypeSymbol) {
            RecordTypeSymbol recordTypeSymbol = (RecordTypeSymbol) symbol;
            String typeName = ((BallerinaRecordTypeSymbol) recordTypeSymbol).getBType().toString();
            VisitedType visitedType = getVisitedType(typeName);
            if (visitedType != null) {
                return geAlreadyVisitedType(symbol, typeName, visitedType, false);
            } else {
                if (typeName.contains("record {")) {
                    type = getRecordType(recordTypeSymbol);
                } else {
                    visitedTypeMap.put(typeName, new VisitedType());
                    type = getRecordType(recordTypeSymbol);
                    completeVisitedTypeEntry(typeName, type);
                }
            }
        } else if (symbol instanceof ArrayTypeSymbol) {
            ArrayTypeSymbol arrayTypeSymbol = (ArrayTypeSymbol) symbol;
            type = new ArrayType(fromSemanticSymbol(arrayTypeSymbol.memberTypeDescriptor()));
        } else if (symbol instanceof MapTypeSymbol) {
            MapTypeSymbol mapTypeSymbol = (MapTypeSymbol) symbol;
            type = new MapType(fromSemanticSymbol(mapTypeSymbol.typeParam()));
        } else if (symbol instanceof TableTypeSymbol) {
            TableTypeSymbol tableTypeSymbol = (TableTypeSymbol) symbol;
            TypeSymbol keyConstraint = null;
            if (tableTypeSymbol.keyConstraintTypeParameter().isPresent()) {
                keyConstraint = tableTypeSymbol.keyConstraintTypeParameter().get();
            }
            type = new TableType(fromSemanticSymbol(tableTypeSymbol.rowTypeParameter()),
                    tableTypeSymbol.keySpecifiers(), fromSemanticSymbol(keyConstraint));
        } else if (symbol instanceof UnionTypeSymbol) {
            UnionTypeSymbol unionSymbol = (UnionTypeSymbol) symbol;
            String typeName = ((BallerinaUnionTypeSymbol) unionSymbol).getBType().toString();
            VisitedType visitedType = getVisitedType(typeName);
            if (visitedType != null) {
                return geAlreadyVisitedType(symbol, typeName, visitedType, true);
            } else {
                visitedTypeMap.put(typeName, new VisitedType());
                type = getUnionType(unionSymbol);
                completeVisitedTypeEntry(typeName, type);
            }
        } else if (symbol instanceof ErrorTypeSymbol) {
            ErrorTypeSymbol errSymbol = (ErrorTypeSymbol) symbol;
            ErrorType errType = new ErrorType();
            if (errSymbol.detailTypeDescriptor() instanceof TypeReferenceTypeSymbol) {
                errType.detailType = fromSemanticSymbol(errSymbol.detailTypeDescriptor());
            }
            type = errType;
        } else if (symbol instanceof IntersectionTypeSymbol) {
            IntersectionTypeSymbol intersectionTypeSymbol = (IntersectionTypeSymbol) symbol;
            String typeName = ((BallerinaIntersectionTypeSymbol) intersectionTypeSymbol).getBType().toString();
            VisitedType visitedType = getVisitedType(typeName);
            if (visitedType != null) {
                return geAlreadyVisitedType(symbol, typeName, visitedType, false);
            } else {
                visitedTypeMap.put(typeName, new VisitedType());
                type = getIntersectionType(intersectionTypeSymbol);
                completeVisitedTypeEntry(typeName, type);
            }
        } else if (symbol instanceof StreamTypeSymbol) {
            StreamTypeSymbol streamTypeSymbol = (StreamTypeSymbol) symbol;
            type = fromSemanticSymbol(streamTypeSymbol.typeParameter());
        } else if (symbol instanceof ObjectTypeSymbol) {
            ObjectTypeSymbol objectTypeSymbol = (ObjectTypeSymbol) symbol;
            ObjectType objectType = new ObjectType();
            objectTypeSymbol.fieldDescriptors().forEach((typeName, typeSymbol) -> {
                Type semanticSymbol = fromSemanticSymbol(typeSymbol);
                if (semanticSymbol != null) {
                    objectType.fields.add(semanticSymbol);
                }
            });
            objectTypeSymbol.typeInclusions().forEach(typeSymbol -> {
                Type semanticSymbol = fromSemanticSymbol(typeSymbol);
                if (semanticSymbol != null) {
                    objectType.fields.add(new InclusionType(semanticSymbol));
                }
            });
            type = objectType;
        } else if (symbol instanceof TypeSymbol) {
            String typeName = ((TypeSymbol) symbol).signature();
            if (typeName.startsWith("\"") && typeName.endsWith("\"")) {
                typeName = typeName.substring(1, typeName.length() - 1);
            }
            type = new PrimitiveType(typeName);
        }
        return type;
    }

    private static Type geAlreadyVisitedType(Symbol symbol, String typeName, VisitedType visitedType,
                                             boolean getClone) {
        if (visitedType.isCompleted()) {
            Type existingType = visitedType.getTypeNode();
            if (getClone) {
                Type type = new Type(existingType.getName(), existingType.getTypeName(), existingType.isOptional(),
                        existingType.getTypeInfo(), existingType.isDefaultable(), existingType.getDefaultValue(),
                        existingType.getDisplayAnnotation(), existingType.getDocumentation());
                return type;
            }
            return existingType;
        } else {
            Type type = new Type();
            setTypeInfo(typeName, symbol, type);
            return type;
        }
    }

    private static Type getIntersectionType(IntersectionTypeSymbol intersectionTypeSymbol) {
        Type type;
        IntersectionType intersectionType = new IntersectionType();
        intersectionTypeSymbol.memberTypeDescriptors().forEach(typeSymbol -> {
            Type semanticSymbol = fromSemanticSymbol(typeSymbol);
            if (semanticSymbol != null) {
                intersectionType.members.add(semanticSymbol);
            }
        });

        type = intersectionType;
        return type;
    }

    private static Type getUnionType(UnionTypeSymbol unionSymbol) {
        Type type;
        UnionType unionType = new UnionType();
        unionSymbol.memberTypeDescriptors().forEach(typeSymbol -> {
            Type semanticSymbol = fromSemanticSymbol(typeSymbol);
            if (semanticSymbol != null) {
                unionType.members.add(semanticSymbol);
            }
        });
        if (unionType.members.stream().allMatch(type1 -> type1 instanceof ErrorType)) {
            ErrorType errType = new ErrorType();
            errType.isErrorUnion = true;
            errType.errorUnion = unionType;
            type = errType;
        } else {
            type = unionType;
        }
        return type;
    }

    private static Type getRecordType(RecordTypeSymbol recordTypeSymbol) {
        Type type;
        List<Type> fields = new ArrayList<>();
        recordTypeSymbol.fieldDescriptors().forEach((name, field) -> {
            Type subType = fromSemanticSymbol(field.typeDescriptor());
            if (subType != null) {
                subType.setName(name);
                subType.setOptional(field.isOptional());
                subType.setDefaultable(field.hasDefaultValue());
                fields.add(subType);
            }
        });
        Type restType = recordTypeSymbol.restTypeDescriptor().isPresent() ?
                fromSemanticSymbol(recordTypeSymbol.restTypeDescriptor().get()) : null;
        type = new RecordType(fields, restType);
        return type;
    }

    private static Type getEnumType(TypeReferenceTypeSymbol typeReferenceTypeSymbol, Symbol symbol) {
        Type type;
        if (typeReferenceTypeSymbol.definition().kind().equals(SymbolKind.ENUM)) {
            List<Type> fields = new ArrayList<>();
            ((UnionTypeSymbol) typeReferenceTypeSymbol.typeDescriptor()).memberTypeDescriptors()
                    .forEach(typeSymbol -> {
                        Type semanticSymbol = fromSemanticSymbol(typeSymbol);
                        if (semanticSymbol != null) {
                            fields.add(semanticSymbol);
                        }
                    });
            type = new EnumType(fields);
        } else {
            type = fromSemanticSymbol(typeReferenceTypeSymbol.typeDescriptor());
        }
        setTypeInfo(typeReferenceTypeSymbol.getName().isPresent() ? typeReferenceTypeSymbol.getName().get()
                : null, symbol, type);
        return type;
    }

    private static void setTypeInfo(String  typeName, Symbol symbol, Type type) {
        if (type != null && symbol.getName().isPresent() && symbol.getModule().isPresent()) {
            ModuleID moduleID = symbol.getModule().get().id();
            type.typeInfo = new TypeInfo(symbol.getName().get(), moduleID.orgName(), moduleID.moduleName(),
                    null, moduleID.version());
            type.name = typeName;
        }
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

    public boolean isOptional() {
        return optional;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    public TypeInfo getTypeInfo() {
        return typeInfo;
    }

    public void setTypeInfo(TypeInfo typeInfo) {
        this.typeInfo = typeInfo;
    }

    public boolean isDefaultable() {
        return defaultable;
    }

    public void setDefaultable(boolean defaultable) {
        this.defaultable = defaultable;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Map<String, String> getDisplayAnnotation() {
        return displayAnnotation;
    }

    public void setDisplayAnnotation(Map<String, String> displayAnnotation) {
        this.displayAnnotation = displayAnnotation;
    }

    public String getDocumentation() {
        return documentation;
    }

    public void setDocumentation(String documentation) {
        this.documentation = documentation;
    }

}
