# SQLite on the Raspberry Pi

See:
- <https://pimylifeup.com/raspberry-pi-sqlite/>
- <https://sqlite.org/docs.html>
- <https://www.sqlitetutorial.net/sqlite-java/>
---
- [SQLite for Python](https://www.pythoncentral.io/introduction-to-sqlite-in-python/)
- [SQLite for NodeJS](https://www.sqlitetutorial.net/sqlite-nodejs/connect/)
---
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
sqlite> .exit
```
To format the `select` output:
```
sqlite> .mode columns
sqlite> .headers on
sqlite> select * from track;
Track_ID    Name                Location     Seating     Year_Opened
----------  ------------------  -----------  ----------  -----------
1           Auto Club Speedway  Fontana, CA  92000.0     1997.0     
2           Chicagoland Speedw  Joliet, IL   75000.0     2001.0     
3           Darlington Raceway  Darlington,  63000.0     1950.0     
4           Daytona Internatio  Daytona Bea  168000.0    1959.0     
5           Homestead-Miami Sp  Homestead,   65000.0     1995.0     
6           Kansas Speedway     Kansas City  81687.0     2001.0     
7           Martinsville Speed  Ridgeway, V  65000.0     1947.0     
8           Michigan Internati  Brooklyn, M  137243.0    1968.0     
9           Phoenix Internatio  Avondale, A  76812.0     1964.0     
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
  
### Initialize a new SQLite DB for images
1. Initialize (create the DB and the required tables)
```
$ sqlite3 sql/the_new_db.db < sql/image_db.sql
```
2. Then your DB is ready for the GUI
```
$ ./gui.sh sql/the_new_db.db
```

... Or more simply, just run `./init.db.sh`

> For the image creation date, use
> ```
> $ sqlite3 images.db
> sqlite> .mode columns
> sqlite> .headers on
> select name, imagetype, datetime(created / 1000,  'unixepoch', 'localtime') as created from images limit 5;
> name          imagetype   created
> ------------  ----------  -------------------
> IMG_3804.jpg  jpg         2020-05-30 16:21:32
> IMG_3805.jpg  jpg         2020-05-30 16:22:56
> IMG_3807.jpg  jpg         2020-05-30 16:23:53
> IMG_3809.jpg  jpg         2020-05-30 16:21:05
> IMG_3810.jpg  jpg         2020-05-30 16:21:22
> . . .
> 
> sqlite> .width 40 5 20
> sqlite> select name, imagetype as type, datetime(created / 1000,  'unixepoch', 'localtime') as created from images limit 10;
> name                                      type   created
> ----------------------------------------  -----  --------------------
> IMG_3804.jpg                              jpg    2020-05-30 16:21:32
> IMG_3805.jpg                              jpg    2020-05-30 16:22:56
> IMG_3807.jpg                              jpg    2020-05-30 16:23:53
> IMG_3809.jpg                              jpg    2020-05-30 16:21:05
> IMG_3810.jpg                              jpg    2020-05-30 16:21:22
> IMG_3812.jpg                              jpg    2020-05-30 16:23:31
> 0ba867ce-034a-4fbd-802d-2e258e628378.jpg  jpg    2020-09-11 21:07:52
> 1c171646-208a-4423-b568-2419084f1d09.jpg  jpg    2020-09-11 21:07:46
> 1efc5ca6-e8ed-44ce-b323-ad88808801ac.jpg  jpg    2020-09-21 21:20:36
> 2cef682d-d864-4bf9-b160-989948f4e1db.jpg  jpg    2020-09-12 03:36:43
> sqlite>
> ```
  
---
