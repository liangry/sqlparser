CREATE TABLE T1
    (column1, column2, column3)
    AS
    SELECT column1, column2, column3 FROM T2
    ORDER BY column1 DESC
    OFFSET 1
    LIMIT 20
    WITH DATA