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

package com.foundationdb.sql.unparser;

import com.foundationdb.sql.TestBase;

import com.foundationdb.sql.parser.SQLParser;
import com.foundationdb.sql.parser.StatementNode;

import org.junit.Before;
import org.junit.Test;
import static junit.framework.Assert.*;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.Collection;
import java.util.List;

@RunWith(Parameterized.class)
public class ParseMultipleTest extends TestBase implements TestBase.GenerateAndCheckResult
{
    public static final File RESOURCE_DIR = 
        new File(NodeToStringTest.RESOURCE_DIR, "multiple");

    protected SQLParser parser;
    protected NodeToString unparser;

    @Before
    public void before() throws Exception {
        parser = new SQLParser();
        unparser = new NodeToString();
    }

    @Parameters(name="{0}")
    public static Collection<Object[]> statements() throws Exception {
        return sqlAndExpected(RESOURCE_DIR);
    }

    public ParseMultipleTest(String caseName, String sql, 
                             String expected, String error) {
        super(caseName, sql, expected, error);
    }

    @Test
    public void testParseMultiple() throws Exception {
        generateAndCheckResult();
    }

    @Override
    public String generateResult() throws Exception {
        List<StatementNode> stmts = parser.parseStatements(sql);
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < stmts.size(); i++) {
            if (i > 0) str.append("\n");
            str.append("[" + i + "]: ");
            str.append(unparser.toString(stmts.get(i)));
            str.append(";");
        }
        return str.toString();
    }

    @Override
    public void checkResult(String result) {
        assertEquals(caseName, expected, result);
    }

}
