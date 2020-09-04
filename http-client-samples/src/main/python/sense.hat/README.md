- Good tutorial [here](https://projects.raspberrypi.org/en/projects/getting-started-with-the-sense-hat/).
- API doc [here](https://pythonhosted.org/sense-hat/api/).

Start with a 
```
$ suso apt-get install sense-hat
```
And then...
    more to come.
    
## Server
```
$ python3 sense_hat_server.py --machine-name:$(hostname -I)
```    
