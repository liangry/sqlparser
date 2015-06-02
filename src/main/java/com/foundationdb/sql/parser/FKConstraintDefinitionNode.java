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

   Derby - Class org.apache.derby.impl.sql.compile.FKConstraintDefinitionNode

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
 * A FKConstraintDefintionNode represents table constraint definitions.
 *
 */

public class FKConstraintDefinitionNode extends ConstraintDefinitionNode
{
    public static enum MatchType {
        SIMPLE, FULL, PARTIAL
    }

    TableName refTableName;
    ResultColumnList refRcl;
    int refActionDeleteRule;    // referential action on delete
    int refActionUpdateRule;    // referential action on update
    MatchType matchType;
    boolean grouping, deferrable, initiallyDeferred;

    // For ADD
    public void init(Object constraintName, 
                     Object refTableName, 
                     Object fkRcl,
                     Object refRcl,
                     Object refActionDelete,
                     Object refActionUpdate,
                     Object matchType,
                     Object grouping,
                     Object deferrable,
                     Object initiallyDeferred) {
        super.init(constraintName,
                   ConstraintType.FOREIGN_KEY,
                   fkRcl, 
                   null,
                   null,
                   null);
        this.refRcl = (ResultColumnList)refRcl;
        this.refTableName = (TableName)refTableName;

        this.refActionDeleteRule = ((Integer)refActionDelete).intValue();
        this.refActionUpdateRule = ((Integer)refActionUpdate).intValue();

        this.matchType = (MatchType)matchType;
        this.grouping = ((Boolean)grouping).booleanValue();
        this.deferrable = ((Boolean)deferrable).booleanValue();
        this.initiallyDeferred = ((Boolean)initiallyDeferred).booleanValue();
    }

    // For DROP
    public void init(Object constraintName,
                     Object constraintType,
                     Object behavior,
                     Object grouping,
                     Object existenceCheck) {
        super.init(constraintName,
                   constraintType,
                   null,
                   null,
                   null,
                   null,
                   behavior,
                   ConstraintType.FOREIGN_KEY,
                   existenceCheck);
        this.grouping = ((Boolean)grouping).booleanValue();
    }

    /**
     * Fill this node with a deep copy of the given node.
     */
    public void copyFrom(QueryTreeNode node) throws StandardException {
        super.copyFrom(node);

        FKConstraintDefinitionNode other = (FKConstraintDefinitionNode)node;
        this.refTableName = (TableName)getNodeFactory().copyNode(other.refTableName,
                                                                 getParserContext());
        this.refRcl = (ResultColumnList)getNodeFactory().copyNode(other.refRcl,
                                                                  getParserContext());
        this.refActionDeleteRule = other.refActionDeleteRule;
        this.refActionUpdateRule = other.refActionUpdateRule;

        this.matchType = other.matchType;
        this.grouping = other.grouping;
        this.deferrable = other.deferrable;
        this.initiallyDeferred = other.initiallyDeferred;
    }

    public TableName getRefTableName() { 
        return refTableName; 
    }

    public ResultColumnList getRefResultColumnList() {
        return refRcl;
    }

    public int getRefActionDeleteRule() {
        return refActionDeleteRule;
    }
    public int getRefActionUpdateRule() {
        return refActionUpdateRule;
    }

    public MatchType getMatchType() {
        return matchType;
    }

    public boolean isGrouping() {
        return grouping;
    }
    
    public boolean isDeferrable() {
        return deferrable;
    }
    
    public boolean isInitiallyDeferred() {
        return initiallyDeferred;
    }
    
    public String toString() {
        return "refTable name : " + refTableName + "\n" +
            "matchType: " + matchType + "\n" +
            "grouping: " + grouping + "\n" + 
            "deferrable: " + deferrable + "\n" + 
            "initiallyDeferred: " + initiallyDeferred + "\n" + 
            super.toString();
    }
    

}
