# REST File Upload sample
### Server side
Build and run
```
 $ ../../gradlew clean shadowJar
 $ java -jar build/libs/RESTFileUpload-1.0-all.jar  
```

### Client side

- From `curl`:
```
$ curl -F 'data=@./sampledata/data.txt' http://localhost:9999/server/upload
$ curl -F 'data=@./sampledata/RPiDesktop.png' http://localhost:9999/server/upload
$ curl -F 'data=@./sampledata/data.txt' -F 'data=@./sampledata/RPiDesktop.png' http://localhost:9999/server/upload
$ curl -vX POST http://localhost:9999/server/upload -H "Content-Type: image/png" --data-binary  @./sampledata/RPiDesktop.png

```

- From `Ajax`:
```javascript
function AjaxFileUpload() {
    var file = document.getElementById("files");
    //var file = fileInput;
    var fd = new FormData();
    fd.append("imageFileData", file);
    var xhr = new XMLHttpRequest();
    xhr.open("POST", '/server/upload');
    xhr.onreadystatechange = () => {
        if (xhr.readyState === 4) {
             alert('success');
        } else if (uploadResult === 'success') { // TODO Really?
             alert('error');
        }
    };
    xhr.send(fd);
}
```
