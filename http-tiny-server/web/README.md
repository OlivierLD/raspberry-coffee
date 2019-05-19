## HTTPServer resources

Use this file to test the HTTPServer, reach
`http:///localhost:[port]/web/index.html`

You can also zip the `web` directory into a `web.zip` (sibling of the `web` folder),
and reach `http:///localhost:[port]/zip/index.html`. 

You can also zip the directory the way you like, your url will eventually depend on what you did.

**_All the static documents can be archived in the same zip_**

Example
```
 $ zip -r ../web.zip * -x 2019/**\* 
```
Done from _**<u>inside</u>**_ the `web` folder, excluding the `2019` folder (Weather Composites, in this example). 

See the size of the required jar file, by running
```
$ ls -lisah ./build/libs
```

Even in its minimal config, this is not too big, for a REST and static pages server... 

---
