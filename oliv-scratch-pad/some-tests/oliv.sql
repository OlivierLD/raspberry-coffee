CREATE TABLE OLIV_TABLE(id INTEGER PRIMARY KEY AUTOINCREMENT, data VARCHAR2, date DATETIME);
INSERT INTO oliv_table (data, date) VALUES ("Hi there", datetime("now"));
select * from oliv_table;
