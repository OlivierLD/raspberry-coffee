## Server
```
$ cd server
$ python3 ssd1305_server.py --machine-name:$(hostname -I)
```    
Then from anywhere on the network
```
$ curl -X POST http://192.168.42.6:8080/ssd1305/display -d "Display This" | jq
```
