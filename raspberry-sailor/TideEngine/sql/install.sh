#!/bin/bash
unzip sql.zip
echo -e "Creating tables (dropping first if they exist)"
sqlite3 tides.db < create_tables.sql
echo -e "Populating tables (takes time)"
sqlite3 tides.db < populate_tables.sql
#
echo -e "Tide DB is ready."
