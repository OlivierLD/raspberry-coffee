# SQLite in the Raspberry Pi

See:
- <https://pimylifeup.com/raspberry-pi-sqlite/>
- <https://sqlite.org/docs.html>
- <https://www.sqlitetutorial.net/sqlite-java/>

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
Ok, it works.  
Now:
```
sqlite> CREATE TABLE OLIV_TABLE(id INTEGER PRIMARY KEY AUTOINCREMENT, data VARCHAR2, date DATETIME);
sqlite> INSERT INTO oliv_table (data, date) VALUES ("Hi there", datetime("now"));
sqlite> select * from oliv_table;
1|Hi there|2021-03-29 01:51:51
sqlite> .quit
```
Notice that the file `oliv.db` was created if not there.  
You can reconnect to the same user later on, data are still there:
```
$ sqlite3 oliv.db
SQLite version 3.27.2 2019-02-25 16:06:06
Enter ".help" for usage hints.
sqlite> .tables
OLIV_TABLE
sqlite> select * from oliv_table;
1|Hi there|2021-03-29 01:51:51
sqlite> .dump OLIV_TABLE
PRAGMA foreign_keys=OFF;
BEGIN TRANSACTION;
CREATE TABLE OLIV_TABLE(id INTEGER PRIMARY KEY AUTOINCREMENT, data VARCHAR2, date DATETIME);
INSERT INTO OLIV_TABLE VALUES(1,'Hi there','2021-03-29 14:37:16');
COMMIT;
sqlite> 
```

JDBC Driver, Maven/Gradle:
```
implementation group: 'org.xerial', name: 'sqlite-jdbc', version: '3.34.0'
```

Execute a script from the command line:
```
$ sqlite3 oliv.db < oliv.sql
```
Execute a script from the sqlite3:
```
$ sqlite3 oliv.db
sqlite> .read oliv.sql
```

## A GUI for SQLite

- <https://sqlitebrowser.org/>
    - Works on Linux, Mac, and Windows.

