import groovyx.net.http.HTTPBuilder

def client = new HTTPBuilder('https://io.adafruit.com')
def key = '54c2767878ca793f2e3cae1c45d62aa7ae9f8056'
def headers = ['X-AIO-Key': key,
               'Accept': 'application/json']
client.setHeaders(headers)
client.get( path : '/api/feeds/onoff' ) { resp, reader ->

    println "response status: ${resp.statusLine}"
//  println 'Headers: -----------'
//  resp.headers.each { h ->
//      println " ${h.name} : ${h.value}"
//  }
    println("Reader is a ${reader.getClass().getName()}")
    println '-- Response data: -----'
    println reader.toString()
    println "Last Value: ${reader.last_value}"
    println '-----------------------'
}
