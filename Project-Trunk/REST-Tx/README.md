# REST Tx (transformer)
A playground for an XML/XSD/XSL experiment...

Here is a first scenario:
1. From a Web UI, paste an XML Schema Definition in a text area
1. Submit the document to a REST service
1. The REST service parses the document, and return a subset of its content, in `json` for the user to select the fields he's interested in
1. The corresponding Web Page is displayed, with check-boxes where appropriate
1. The user checks the boxes he needs, and submit the result to a REST service
1. The corresponding XSL Stylesheet is generated, and returned to the user


> Note: apply an XSL StyleSheet from the command line:
>
> Use oracle.xml.parser.v2.oraxsl.class, see <https://docs.oracle.com/cd/B10501_01/appdev.920/a96616/arxml04.htm>
> like this:
```
$ java -cp build/libs/REST-Tx-1.0-all.jar oracle.xml.parser.v2.oraxsl
oraxsl: Number of arguments specified (0) is illegal
 usage: oraxsl options* source? stylesheet? result?
             -w                          Show warnings
             -e <error log>              A file to write errors to
             -l <xml file list>          List of files to transform
             -d <directory>              Directory with files to transform
             -x <source extension>       Extensions to exclude 
             -i <source extension>       Extensions to include 
             -s <stylesheet>             Stylesheet to use
             -r <result extension>       Extension to use for results
             -o <result directory>       Directory to place results
             -p <param list>             List of Params 
             -t <# of threads>           Number of threads to use
             -v                          Verbose mode
             -debug                      Debug mode
 Please refer to the readme file for more information on the above options 
```
Example:
```
$ java -cp build/libs/REST-Tx-1.0-all.jar oracle.xml.parser.v2.oraxsl /.../xml/sample.invoice.xml /.../xml/xml2json.xsl | jq
```

```json
{
  "ins:invoice": {
    "xmlns:ins": "urn://some.stuff",
    "ins:customer": {
      "id": "CUST-001",
      "ins:name": {
        "ins:first-name": "Bruce",
        "ins:last-name": "Wayne"
      },
      "ins:address": {
        "ins:number": "1",
        "ins:street": "Wayne Avenue",
        "ins:city": "Gotham City",
        "ins:zip-code": "12345"
      }
    },
    "ins:items": {
      "ins:item": [
        {
          "ins:sku": "SKU-XXX",
          "ins:description": "Bat Mobile"
        },
        {
          "ins:sku": "SKU-YYY",
          "ins:description": "Bat Suit"
        },
        {
          "ins:sku": "SKU-ZZZ",
          "ins:description": "Bat Hood"
        }
      ]
    },
    "ins:total": {
      "ins:currency": "USD",
      "text": "9876.54"
    }
  }
}
```

---
 
