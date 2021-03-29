# SQLite in the Raspberry Pi

See:
- <https://pimylifeup.com/raspberry-pi-sqlite/>
- <https://sqlite.org/docs.html>

```
sudo apt update
sudo apt full-upgrade
sudo apt install sqlite3
```

```
$ sqlite3 oliv.db
SQLite version 3.27.2 2019-02-25 16:06:06
Enter ".help" for usage hints.
sqlite>
. . . 
```
Ok, ot works.  
Now:
```
sqlite> CREATE TABLE OLIV_TABLE(id INTEGER PRIMARY KEY AUTOINCREMENT, data VARCHAR2, date DATETIME);
sqlite> INSERT INTO oliv_table (data, date) VALUES ("Hi there", datetime("now"));
sqlite> select * from oliv_table;
1|Hi there|2021-03-29 01:51:51
sqlite> .quit
```
Notice that the file `oliv.db` was created. You can reconnect to the same user later on, data are still there:
```
$ sqlite3 oliv.db
SQLite version 3.27.2 2019-02-25 16:06:06
Enter ".help" for usage hints.
sqlite> .tables
OLIV_TABLE
sqlite> select * from oliv_table;
1|Hi there|2021-03-29 01:51:51
sqlite>
```

JDBC Driver, Maven/Gradle:
```
implementation group: 'org.xerial', name: 'sqlite-jdbc', version: '3.34.0'
```