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

/* The original from which this derives bore the following: */

/*

   Derby - Class org.apache.derby.impl.sql.compile.AlterTableNode

   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to you under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */

package com.foundationdb.sql.parser;

import com.foundationdb.sql.StandardException;

/**
 * A AlterTableNode represents a DDL statement that alters a table.
 * It contains the name of the object to be created.
 *
 */
public class AlterTableNode extends DDLStatementNode
{
    // The alter table action
    public TableElementList tableElementList = null;

    /**
     * updateStatistics will indicate that we are here for updating the
     * statistics. It could be statistics of just one index or all the
     * indexes on a given table. 
     */
    private boolean updateStatistics = false;
    /**
     * The flag updateStatisticsAll will tell if we are going to update the 
     * statistics of all indexes or just one index on a table. 
     */
    private boolean updateStatisticsAll = false;
    /**
     * If statistic is getting updated for just one index, then 
     * indexNameForUpdateStatistics will tell the name of the specific index 
     * whose statistics need to be updated.
     */
    private String indexNameForUpdateStatistics;

    public boolean compressTable = false;
    public boolean sequential = false;
    // The following three (purge, defragment and truncateEndOfTable) apply for 
    // inplace compress.
    public boolean purge = false;
    public boolean defragment = false;
    public boolean truncateEndOfTable = false;
                
    public int behavior;             // StatementType.DROP_XXX for TRUNCATE TABLE, DROP COLUMN

    private int changeType = UNKNOWN_TYPE; // DDLStatementNode.XXX_TYPE.

    private boolean truncateTable = false;

    private ExistenceCheck existenceCheck;


    /**
     * Initializer for a AlterTableNode for updating the statistics. The user
     * can ask for update statistic of all the indexes or only a specific index
     *
     * @param objectName The name of the table whose index(es) will have
     *                                                  their statistics updated.
     * @param updateStatisticsAll If true then update the statistics of all 
     *                                                  the indexes on the table. If false, then update
     *                                                  the statistics of only the index provided as
     *                                                  3rd parameter here
     * @param indexName Only used if updateStatisticsAll is set to false.
     * @param existenceCheck ExistenceCheck for IF EXISTS
     *
     * @exception StandardException Thrown on error
     */
    public void init(Object objectName,
                     Object updateStatisticsAll,
                     Object indexName,
                     Object existenceCheck)
            throws StandardException {
        initAndCheck(objectName);
        this.updateStatisticsAll = ((Boolean)updateStatisticsAll).booleanValue();
        this.indexNameForUpdateStatistics = (String)indexName;
        this.updateStatistics = true;
        this.existenceCheck = (ExistenceCheck)existenceCheck;
    }

    /**
     * Initializer for a TRUNCATE TABLE or COMPRESS using temporary tables
     * rather than inplace compress
     *
     * @param objectName The name of the table being altered
     * @param arg2 <code>int[]</code>: Behavior CASCADE or RESTRICTED
     *             <code>Boolean</code>: Whether or not the COMPRESS is SEQUENTIAL
     * @param existenceCheck ExistenceCheck for IF EXISTS
     *
     * @exception StandardException Thrown on error
     */
    public void init(Object objectName,
                     Object arg2,
                     Object existenceCheck) throws StandardException {
        initAndCheck(objectName);

        if (arg2 instanceof int[]) {
            int[] bh = (int[])arg2;
            this.behavior = bh[0];
            this.truncateTable = true;
        }
        else {
            this.sequential = ((Boolean)arg2).booleanValue();
            this.compressTable = true;
        }
        this.existenceCheck = (ExistenceCheck)existenceCheck;
    }

    /**
     * Initializer for AlterTableNode for
     * a) INPLACE COMPRESS
     * b) Generic alter table actions
     *
     * @param objectName The name of the table being altered
     * @param arg2 a) PURGE during INPLACE COMPRESS
     *             b) The alter table actions
     * @param arg3 a) DEFRAGMENT during INPLACE COMPRESS
     *             b) ADD_TYPE or DROP_TYPE
     * @param arg4 a) TRUNCATE END during INPLACE COMPRESS
     *             b) If drop is CASCADE or RESTRICTED
     * @param existenceCheck ExistenceCheck for IF EXISTS
     *
     * @exception StandardException Thrown on error
     */
    public void init(Object objectName,
                     Object arg2,
                     Object arg3,
                     Object arg4,
                     Object existenceCheck)
            throws StandardException {
        initAndCheck(objectName);

        if (arg2 instanceof Boolean) {
            this.purge = ((Boolean)arg2).booleanValue();
            this.defragment = ((Boolean)arg3).booleanValue();
            this.truncateEndOfTable = ((Boolean)arg4).booleanValue();
            this.compressTable = true;
        } else {
            this.tableElementList = (TableElementList)arg2;
            this.changeType = ((int[])arg3)[0];
            this.behavior = ((int[])arg4)[0];
            switch (this.changeType) {
                case ADD_TYPE:
                case DROP_TYPE:
                case MODIFY_TYPE:
                case LOCKING_TYPE:
                    // OK
                break;
                default:
                    throw new StandardException("ChangeType "+this.changeType+" Not implemented");
            }
        }
        this.existenceCheck = (ExistenceCheck)existenceCheck;
    }

    /**
     * Fill this node with a deep copy of the given node.
     */
    public void copyFrom(QueryTreeNode node) throws StandardException {
        super.copyFrom(node);

        AlterTableNode other = (AlterTableNode)node;
        this.tableElementList = (TableElementList)
            getNodeFactory().copyNode(other.tableElementList, getParserContext());
       // this.lockGranularity = other.lockGranularity;
        this.updateStatistics = other.updateStatistics;
        this.updateStatisticsAll = other.updateStatisticsAll;
        this.indexNameForUpdateStatistics = other.indexNameForUpdateStatistics;
        this.compressTable = other.compressTable;
        this.sequential = other.sequential;
        this.purge = other.purge;
        this.defragment = other.defragment;
        this.truncateEndOfTable = other.truncateEndOfTable;
        this.behavior = other.behavior;
        this.changeType = other.changeType;
        this.truncateTable = other.truncateTable;
        this.existenceCheck = other.existenceCheck;
    }

    /**
     * Convert this object to a String.  See comments in QueryTreeNode.java
     * for how this should be done for tree printing.
     *
     * @return This object as a String
     */

    public String toString() {
        return super.toString() +
            "objectName: " + getObjectName() + "\n" +
            "compressTable: " + compressTable + "\n" +
            "sequential: " + sequential + "\n" +
            "truncateTable: " + truncateTable + "\n" +
            "purge: " + purge + "\n" +
            "defragment: " + defragment + "\n" +
            "truncateEndOfTable: " + truncateEndOfTable + "\n" +
            "updateStatistics: " + updateStatistics + "\n" +
            "updateStatisticsAll: " + updateStatisticsAll + "\n" +
            "indexNameForUpdateStatistics: " + indexNameForUpdateStatistics + "\n" +
            "existenceCheck: " + existenceCheck + "\n";
    }

    /**
     * Prints the sub-nodes of this object.  See QueryTreeNode.java for
     * how tree printing is supposed to work.
     * @param depth The depth to indent the sub-nodes
     */
    public void printSubNodes(int depth) {
        if (tableElementList != null) {
            printLabel(depth, "tableElementList: ");
            tableElementList.treePrint(depth + 1);
        }
    }

    public String statementToString() {
        if (truncateTable)
            return "TRUNCATE TABLE";
        else
            return "ALTER TABLE";
    }

    public boolean isUpdateStatistics() {
        return updateStatistics;
    }

    public boolean isUpdateStatisticsAll() {
        return updateStatisticsAll;
    }

    public String getIndexNameForUpdateStatistics() {
        return indexNameForUpdateStatistics;
    }

    public boolean isCompressTable() {
        return compressTable;
    }

    public boolean isTruncateTable() {
        return truncateTable;
    }

    public int getChangeType() { 
        return changeType; 
    }

    public int getBehavior() {
        return behavior;
    }

    public boolean isCascade() {
        return (behavior == StatementType.DROP_CASCADE);
    }

    public ExistenceCheck getExistenceCheck() {
        return existenceCheck;
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

        if (tableElementList != null) {
            tableElementList.accept(v);
        }
    }
}
