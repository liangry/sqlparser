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
import com.foundationdb.sql.parser.SQLParserFeature;
import com.foundationdb.sql.parser.StatementNode;

import org.junit.Before;
import org.junit.Test;
import static junit.framework.Assert.*;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.Collection;
import java.util.EnumSet;

@RunWith(Parameterized.class)
public class NodeToStringTest extends TestBase implements TestBase.GenerateAndCheckResult
{
    public static final File RESOURCE_DIR = 
        new File("src/test/resources/"
                 + NodeToStringTest.class.getPackage().getName().replace('.', '/'));

    protected SQLParser parser;
    protected NodeToString unparser;
    protected String[] featureLines;

    @Before
    public void before() throws Exception {
        parser = new SQLParser();
        unparser = new NodeToString();
        if (featureLines != null)
            parseFeatures(featureLines, parser.getFeatures());
    }

    @Parameters(name="{0}")
    public static Collection<Object[]> statements() throws Exception {
        return sqlAndExpectedAndExtra(RESOURCE_DIR, ".features");
    }

    public NodeToStringTest(String caseName, String sql, 
                            String expected, String error, String[] featureLines) {
        super(caseName, sql, expected, error);
        this.featureLines = featureLines;
    }

    @Test
    public void testUnparser() throws Exception {
        generateAndCheckResult();
    }

    @Override
    public String generateResult() throws Exception {
        StatementNode stmt = parser.parseStatement(sql);
        return unparser.toString(stmt);
    }

    @Override
    public void checkResult(String result) {
        assertEquals(caseName, expected, result);
    }

}
