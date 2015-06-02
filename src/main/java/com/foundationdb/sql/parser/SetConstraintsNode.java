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

/**
 * A SET CONSTRAINTS statement.
 */

public class SetConstraintsNode extends StatementNode
{
    private boolean all;
    private TableNameList constraints;
    private boolean deferred;

    /**
     * Initializer for SetTransactionIsolationNode
     *
     * @param all whether to affect all constraints
     * @param constraints list of constraints to affect
     * @param deferred <code>true</code> to defer
     */
    public void init(Object all,
                     Object constraints,
                     Object deferred) {
        this.all = (Boolean)all;
        this.constraints = (TableNameList)constraints;
        this.deferred = (Boolean)deferred;
    }

    public boolean isAll() {
        return all;
    }

    public TableNameList getConstraints() {
        return constraints;
    }

    public boolean isDeferred() {
        return deferred;
    }

    /**
     * Fill this node with a deep copy of the given node.
     */
    public void copyFrom(QueryTreeNode node) throws StandardException {
        super.copyFrom(node);

        SetConstraintsNode other = (SetConstraintsNode)node;
        this.all = other.all;
        this.constraints = (TableNameList)getNodeFactory().copyNode(other.constraints,
                                                                    getParserContext());
        this.deferred = other.deferred;
    }

    /**
     * Convert this object to a String.  See comments in QueryTreeNode.java
     * for how this should be done for tree printing.
     *
     * @return This object as a String
     */

    public String toString() {
        return "all: " + all + "\n" +
            super.toString();
    }

    /**
     * Prints the sub-nodes of this object.  See QueryTreeNode.java for
     * how tree printing is supposed to work.
     *
     * @param depth The depth of this node in the tree
     */

    public void printSubNodes(int depth) {
        super.printSubNodes(depth);

        if (constraints != null) {
            printLabel(depth, "constraints: ");
            constraints.treePrint(depth + 1);
        }
    }

    /**
     * Accept the visitor for all visitable children of this node.
     * 
     * @param v the visitor
     *
     * @exception StandardException on error
     */
    void acceptChildren(Visitor v) throws StandardException {
        super.acceptChildren(v);

        if (constraints != null) {
            constraints = (TableNameList)constraints.accept(v);
        }
    }

    public String statementToString() {
        return "SET CONSTRAINTS";
    }

}
