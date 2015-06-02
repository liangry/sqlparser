/**
 * Copyright 2011-2013 FoundationDB, LLC
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.foundationdb.sql.parser;

import com.foundationdb.sql.StandardException;
import com.foundationdb.sql.parser.JoinNode.JoinType;

import java.util.Properties;

public class IndexDefinitionNode extends TableElementNode implements IndexDefinition
{
    private boolean unique;
    private IndexColumnList columnList;
    private JoinType joinType;
    private StorageFormatNode storageFormat;

    /**
     * Initializer for a IndexDefinitionNode
     *
     * @param unique True means it's a unique index
     * @param indexName The name of the index
     * @param columnList A list of columns, in the order they
     */
    public void init(Object unique,
                     Object indexName,
                     Object columnList,
                     Object joinType,
                     Object storageFormat)
        throws StandardException {
        super.init(indexName);
        this.unique = ((Boolean)unique).booleanValue();
        this.columnList = (IndexColumnList)columnList;
        this.joinType = (JoinType)joinType;
        this.storageFormat = (StorageFormatNode) storageFormat;
    }

    /**
     * Fill this node with a deep copy of the given node.
     */
    public void copyFrom(QueryTreeNode node) throws StandardException {
        super.copyFrom(node);
        CreateIndexNode other = (CreateIndexNode)node;
        this.unique = other.unique;
        this.columnList = (IndexColumnList)
            getNodeFactory().copyNode(other.columnList, getParserContext());
        this.joinType = other.joinType;
        this.storageFormat = (StorageFormatNode)getNodeFactory().copyNode(other.storageFormat,
                                                                          getParserContext());
    }

    /**
     * Convert this object to a String.  See comments in QueryTreeNode.java
     * for how this should be done for tree printing.
     *
     * @return This object as a String
     */

    public String toString() {
        return super.toString() +
            "unique: " + unique + "\n" +
            "joinType: " + joinType + "\n";
    }

    public void printSubNodes(int depth) {
        super.printSubNodes(depth);
        if (columnList != null) {
            printLabel(depth, "indexColumnList: ");
            columnList.treePrint(depth+1);
        }
        if (storageFormat != null) {
            printLabel(depth, "storageFormat: ");
            storageFormat.treePrint(depth + 1);
        }
    }

    public void setUnique(boolean unique) {
        this.unique = unique;
    }

    //
    // IndexDefinition
    //

    public boolean isUnique() {
        return unique;
    }

    public JoinType getJoinType() {
        return joinType;
    }

    public IndexColumnList getIndexColumnList() {
        return columnList;
    }

    public StorageFormatNode getStorageFormat() {
        return storageFormat;
    }
}
