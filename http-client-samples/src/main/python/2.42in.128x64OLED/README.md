## Server
```
$ cd server
$ python3 ssd1305_server.py --machine-name:$(hostname -I)
```    
Then from anywhere on the network
```
$ curl --location --request POST 'http://192.168.42.6:8080/ssd1305/display' \
--header 'Content-Type: application/json' \
--data-raw '[{
    "x": 2,
    "y": 10,
    "text": "This is line #1"
},
{
    "x": 2,
    "y": 20,
    "text": "This is line #2"
},
{
    "x": 2,
    "y": 30,
    "text": "This is line #3"
},
{
    "x": 2,
    "y": 40,
    "text": "And that would be line #4"
},
{
    "x": 2,
    "y": 50,
    "text": "finally #5"
}]' | jq

{
  "status": "OK"
}
```
The body payload is a json array, one element per line of text.
![ssd1305](../../../../images/ssd1305.jpg)
To clear the screen:
```
$ curl -X POST http://192.168.42.6:8080/ssd1305/clean
```
