/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.compiler.syntax.tree;

import io.ballerina.compiler.internal.parser.tree.STNode;

import java.util.Objects;
import java.util.Optional;

/**
 * This is a generated syntax tree node.
 *
 * @since 2.0.0
 */
public class ServiceDeclarationNode extends ModuleMemberDeclarationNode {

    public ServiceDeclarationNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Optional<MetadataNode> metadata() {
        return optionalChildInBucket(0);
    }

    public Token serviceKeyword() {
        return childInBucket(1);
    }

    public Optional<TypeDescriptorNode> typeDescriptor() {
        return optionalChildInBucket(2);
    }

    public NodeList<Token> absoluteResourcePath() {
        return new NodeList<>(childInBucket(3));
    }

    public Token onKeyword() {
        return childInBucket(4);
    }

    public SeparatedNodeList<ExpressionNode> expressions() {
        return new SeparatedNodeList<>(childInBucket(5));
    }

    public Token openBraceToken() {
        return childInBucket(6);
    }

    public NodeList<Node> members() {
        return new NodeList<>(childInBucket(7));
    }

    public Token closeBraceToken() {
        return childInBucket(8);
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T apply(NodeTransformer<T> visitor) {
        return visitor.transform(this);
    }

    @Override
    protected String[] childNames() {
        return new String[]{
                "metadata",
                "serviceKeyword",
                "typeDescriptor",
                "absoluteResourcePath",
                "onKeyword",
                "expressions",
                "openBraceToken",
                "members",
                "closeBraceToken"};
    }

    public ServiceDeclarationNode modify(
            MetadataNode metadata,
            Token serviceKeyword,
            TypeDescriptorNode typeDescriptor,
            NodeList<Token> absoluteResourcePath,
            Token onKeyword,
            SeparatedNodeList<ExpressionNode> expressions,
            Token openBraceToken,
            NodeList<Node> members,
            Token closeBraceToken) {
        if (checkForReferenceEquality(
                metadata,
                serviceKeyword,
                typeDescriptor,
                absoluteResourcePath.underlyingListNode(),
                onKeyword,
                expressions.underlyingListNode(),
                openBraceToken,
                members.underlyingListNode(),
                closeBraceToken)) {
            return this;
        }

        return NodeFactory.createServiceDeclarationNode(
                metadata,
                serviceKeyword,
                typeDescriptor,
                absoluteResourcePath,
                onKeyword,
                expressions,
                openBraceToken,
                members,
                closeBraceToken);
    }

    public ServiceDeclarationNodeModifier modify() {
        return new ServiceDeclarationNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class ServiceDeclarationNodeModifier {
        private final ServiceDeclarationNode oldNode;
        private MetadataNode metadata;
        private Token serviceKeyword;
        private TypeDescriptorNode typeDescriptor;
        private NodeList<Token> absoluteResourcePath;
        private Token onKeyword;
        private SeparatedNodeList<ExpressionNode> expressions;
        private Token openBraceToken;
        private NodeList<Node> members;
        private Token closeBraceToken;

        public ServiceDeclarationNodeModifier(ServiceDeclarationNode oldNode) {
            this.oldNode = oldNode;
            this.metadata = oldNode.metadata().orElse(null);
            this.serviceKeyword = oldNode.serviceKeyword();
            this.typeDescriptor = oldNode.typeDescriptor().orElse(null);
            this.absoluteResourcePath = oldNode.absoluteResourcePath();
            this.onKeyword = oldNode.onKeyword();
            this.expressions = oldNode.expressions();
            this.openBraceToken = oldNode.openBraceToken();
            this.members = oldNode.members();
            this.closeBraceToken = oldNode.closeBraceToken();
        }

        public ServiceDeclarationNodeModifier withMetadata(
                MetadataNode metadata) {
            this.metadata = metadata;
            return this;
        }

        public ServiceDeclarationNodeModifier withServiceKeyword(
                Token serviceKeyword) {
            Objects.requireNonNull(serviceKeyword, "serviceKeyword must not be null");
            this.serviceKeyword = serviceKeyword;
            return this;
        }

        public ServiceDeclarationNodeModifier withTypeDescriptor(
                TypeDescriptorNode typeDescriptor) {
            this.typeDescriptor = typeDescriptor;
            return this;
        }

        public ServiceDeclarationNodeModifier withAbsoluteResourcePath(
                NodeList<Token> absoluteResourcePath) {
            Objects.requireNonNull(absoluteResourcePath, "absoluteResourcePath must not be null");
            this.absoluteResourcePath = absoluteResourcePath;
            return this;
        }

        public ServiceDeclarationNodeModifier withOnKeyword(
                Token onKeyword) {
            Objects.requireNonNull(onKeyword, "onKeyword must not be null");
            this.onKeyword = onKeyword;
            return this;
        }

        public ServiceDeclarationNodeModifier withExpressions(
                SeparatedNodeList<ExpressionNode> expressions) {
            Objects.requireNonNull(expressions, "expressions must not be null");
            this.expressions = expressions;
            return this;
        }

        public ServiceDeclarationNodeModifier withOpenBraceToken(
                Token openBraceToken) {
            Objects.requireNonNull(openBraceToken, "openBraceToken must not be null");
            this.openBraceToken = openBraceToken;
            return this;
        }

        public ServiceDeclarationNodeModifier withMembers(
                NodeList<Node> members) {
            Objects.requireNonNull(members, "members must not be null");
            this.members = members;
            return this;
        }

        public ServiceDeclarationNodeModifier withCloseBraceToken(
                Token closeBraceToken) {
            Objects.requireNonNull(closeBraceToken, "closeBraceToken must not be null");
            this.closeBraceToken = closeBraceToken;
            return this;
        }

        public ServiceDeclarationNode apply() {
            return oldNode.modify(
                    metadata,
                    serviceKeyword,
                    typeDescriptor,
                    absoluteResourcePath,
                    onKeyword,
                    expressions,
                    openBraceToken,
                    members,
                    closeBraceToken);
        }
    }
}
