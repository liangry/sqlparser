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

package com.foundationdb.sql;

import com.foundationdb.sql.parser.SQLParserFeature;
import org.junit.ComparisonFailure;
import org.junit.Ignore;
import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Ignore
public class TestBase
{
    protected TestBase() {
    }

    protected String caseName, sql, expected, error;

    protected TestBase(String caseName, String sql, String expected, String error) {
        this.caseName = caseName;
        this.sql = sql;
        this.expected = expected;
        this.error = error;
    }

    public static File[] listSQLFiles(File dir) {
        File[] result = dir.listFiles(new RegexFilenameFilter(".*\\.sql"));
        Arrays.sort(result, new Comparator<File>() {
                        public int compare(File f1, File f2) {
                            return f1.getName().compareTo(f2.getName());
                        }
                    });
        return result;
    }

    public static File changeSuffix(File sqlFile, String suffix) {
        return new File(sqlFile.getParentFile(),
                        sqlFile.getName().replace(".sql", suffix));
    }

    public static String fileContents(File file) throws IOException {
        Reader reader = null;
        try {
            reader = new InputStreamReader(new FileInputStream(file), "UTF-8");
            StringBuilder str = new StringBuilder();
            char[] buf = new char[128];
            while (true) {
                int nc = reader.read(buf);
                if (nc < 0) break;
                str.append(buf, 0, nc);
            }
            int cridx = 0;
            while (true) {
                cridx = str.indexOf("\r", cridx);
                if (cridx < 0) break;
                str.deleteCharAt(cridx);
            }
            return str.toString();
        }
        finally {
            if (reader != null) {
                try {
                    reader.close();
                }
                catch (IOException ex) {
                }
            }
        }
    }

    public static String[] fileContentsArray(File file) throws IOException {
        Reader reader = null;
        List<String> result = new ArrayList<String>();
        try {
            reader = new InputStreamReader(new FileInputStream(file), "UTF-8");
            BufferedReader buffered = new BufferedReader(reader);
            while (true) {
                String line = buffered.readLine();
                if (line == null) break;
                result.add(line);
            }
        }
        finally {
            if (reader != null) {
                try {
                    reader.close();
                }
                catch (IOException ex) {
                }
            }
        }
        return result.toArray(new String[result.size()]);
    }

    public static Collection<Object[]> sqlAndExpected(File dir) 
            throws IOException {
        return sqlAndExpectedAndExtra(dir, null);
    }

    public static Collection<Object[]> sqlAndExpectedAndParams(File dir) 
            throws IOException {
        return sqlAndExpectedAndExtra(dir, ".params");
    }
    
    static final boolean RUN_FAILING_TESTS = Boolean.getBoolean("foundationdb.sql.test.runFailing");

    /** @deprecated Equivalent to sqlAndExpected() or sqlAndExpectedAndParams(). */
    @Deprecated
    public static Collection<Object[]> sqlAndExpected(File dir, 
                                                      boolean andParams) throws IOException {
        return andParams ? sqlAndExpectedAndParams(dir) : sqlAndExpected(dir);
    }

    public static Collection<Object[]> sqlAndExpectedAndExtra(File dir,
                                                              String extraExtension)
            throws IOException {
        Collection<Object[]> result = new ArrayList<Object[]>();
        for (File sqlFile : listSQLFiles(dir)) {
            String caseName = sqlFile.getName().replace(".sql", "");
            if (changeSuffix(sqlFile, ".fail").exists() && !RUN_FAILING_TESTS)
                continue;
            String sql = fileContents(sqlFile);
            String expected, error;
            File expectedFile = changeSuffix(sqlFile, ".expected");
            if (expectedFile.exists())
                expected = fileContents(expectedFile);
            else
                expected = null;
            File errorFile = changeSuffix(sqlFile, ".error");
            if (errorFile.exists())
                error = fileContents(errorFile);
            else
                error = null;
            if (extraExtension != null) {
                String[] extra = null;
                File extraFile = changeSuffix(sqlFile, extraExtension);
                if (extraFile.exists()) {
                    extra = fileContentsArray(extraFile);
                }
                result.add(new Object[] {
                               caseName, sql, expected, error, extra
                           });
            }
            else {
                result.add(new Object[] {
                               caseName, sql, expected, error
                           });
            }
        }
        return result;
    }

    /** A class implementing this can call {@link #generateAndCheckResult(). */
    public interface GenerateAndCheckResult {
        public String generateResult() throws Exception;
        public void checkResult(String result) throws IOException;
    }

    public static void generateAndCheckResult(GenerateAndCheckResult handler,
                                              String caseName, 
                                              String expected, String error) 
            throws Exception {
        if ((expected != null) && (error != null)) {
            fail(caseName + ": both expected result and expected error specified.");
        }
        String result = null;
        Exception errorResult = null;
        try {
            result = handler.generateResult().replace("\r", "");
        }
        catch (Exception ex) {
            errorResult = ex;
        }
        if (error != null) {
            if (errorResult == null)
                fail(caseName + ": error expected but none thrown");
            else
                assertEquals(caseName, error, errorResult.toString().replace("\r", ""));
        }
        else if (errorResult != null) {
            throw errorResult;
        }
        else if (expected == null) {
            fail(caseName + " no expected result given. actual='" + result + "'");
        }
        else {
            handler.checkResult(result);
        }
    }

    /** @see GenerateAndCheckResult */
    protected void generateAndCheckResult() throws Exception {
        generateAndCheckResult((GenerateAndCheckResult)this, caseName, expected, error);
    }

    public static void assertEqualsWithoutHashes(String caseName,
                                                 String expected, String actual) 
            throws IOException {
        assertEqualsWithoutPattern(caseName, 
                                   expected, actual, 
                                   CompareWithoutHashes.HASH_REGEX);
    }

    public static void assertEqualsWithoutPattern(String caseName,
                                                  String expected, String actual, 
                                                  String regex) 
            throws IOException {
        CompareWithoutHashes comparer = new CompareWithoutHashes(regex);
        if (!comparer.match(new StringReader(expected), new StringReader(actual)))
            throw new ComparisonFailure(caseName, comparer.converter(expected,actual), actual);
    }

    protected static void parseFeatures(String[] featureLines, Set<SQLParserFeature> features)
        throws IOException {
        for(String line : featureLines) {
            boolean add;
            switch (line.charAt(0)) {
                case '+':
                    add = true;
                    break;
                case '-':
                    add = false;
                    break;
                default:
                    throw new IOException("Malformed features line: should start with + or - " + line);
            }
            SQLParserFeature feature = SQLParserFeature.valueOf(line.substring(1));
            if (add)
                features.add(feature);
            else
                features.remove(feature);
        }
    }

}
