CREATE TABLE items(
    iid INT NOT NULL PRIMARY KEY,
    sku INT,
    oid INT,
    GROUPING FOREIGN KEY(oid) REFERENCES orders,
    INDEX(items.sku, orders.odate, customers.name) USING LEFT JOIN
)