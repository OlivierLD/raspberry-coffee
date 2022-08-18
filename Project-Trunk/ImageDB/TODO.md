## Ideas ?
- Display a list of the labels used on a given DB ✅ Done.
```
select label, count(label) as count
from tags
group by label
order by lower(label);
```


---
