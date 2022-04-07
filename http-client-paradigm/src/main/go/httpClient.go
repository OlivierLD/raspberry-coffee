package main

import (
    "fmt"
    "io/ioutil"
    "net/http"
    "time"
)

func main() {
    c := http.Client{Timeout: time.Duration(1) * time.Second}
    resp, err := c.Get("https://www.google.com")
    if err != nil {
        fmt.Printf("Error %s", err)
        return
    }
    defer resp.Body.Close()
    body, err := ioutil.ReadAll(resp.Body)
    fmt.Printf("Body : %s", body)
}
