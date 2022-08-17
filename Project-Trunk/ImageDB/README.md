# A SQLite DB for your pictures
This is a small tool with a Swing UI to store and tag your pictures in a SQLite database.  
You can drag-and-drop your pictures on the GUI to store them. You can them tag them (`arches, utah, jim`), and then search by tag names (with `or` or `and` connectors).  
You can also delete pictures from the database, or extract the selected ones onto your file system.

See [here](./SQLITE.md) for some first details about SQLite.

### Get started
- First, create and initialize your DB
```
$ sqlite3 sql/utah_trip.db < sql/image_db.sql
```
The line above can produce errors as it drops tables that may not exist.

- Then your DB is ready for the GUI
<!-- $ ./gui.sh --db-location:utah_trip.db -->
```
$ ./gui.sh sql/utah_trip.db
```

Use the small help [?] icon to get some doc...
