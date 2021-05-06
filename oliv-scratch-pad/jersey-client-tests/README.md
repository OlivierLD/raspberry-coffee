To Update a decision table, try this:

```
$ cat tx.json
{ 
  "update": "'Approval Amount' = range(350)", 
  "where": "'Manager' = 'Alex'" 
}

$ ./patch.dt.sh \
   --decision-table:src/main/resources/approval.strategy.dt.json \
   --transformation-file:tx.json
```