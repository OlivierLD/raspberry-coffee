## HTTPServer resources

Use this file to test the HTTPServer, reach
`http:///localhost:[port]/web/index.html`

You can also zip the `web` directory into a `web.zip` (sibling of the `web` folder),
and reach `http:///localhost:[port]/zip/web/index.html`, or `http:///localhost:[port]/zip/index.html`, depending on how you archived the documents.

**_All the static documents can be archived in the same zip_**

Example
```
 $ zip -r ../web.zip * -x 2019/**\* 
```
Done from **inside** the `web` folder, excluding the `2019` folder (Weather Composites). 

---
