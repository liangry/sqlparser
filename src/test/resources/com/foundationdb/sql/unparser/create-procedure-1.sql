CREATE PROCEDURE test.foo_bar(IN x INT, OUT y DOUBLE) LANGUAGE JAVA PARAMETER STYLE JAVA EXTERNAL NAME 'com.foundationdb.ProcTest.fooBar(int,double[])'
