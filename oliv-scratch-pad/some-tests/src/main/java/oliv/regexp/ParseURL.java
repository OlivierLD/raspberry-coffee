package oliv.regexp;

import java.net.URL;

public class ParseURL {

    public static void main(String... args) throws Exception {

//        URL aURL = new URL("http://example.com:80/docs/books/tutorial/index.html?name=networking#DOWNLOADING");
        URL aURL = new URL("http://100.102.90.104:80/docs/books/tutorial/index.html?name=networking#DOWNLOADING");

        System.out.println("protocol = " + aURL.getProtocol()); //http
        System.out.println("authority = " + aURL.getAuthority()); //example.com:80
        System.out.println("host = " + aURL.getHost()); //example.com
        System.out.println("port = " + aURL.getPort()); //80
        System.out.println("path = " + aURL.getPath()); //  /docs/books/tutorial/index.html
        System.out.println("query = " + aURL.getQuery()); //name=networking
        System.out.println("filename = " + aURL.getFile()); ///docs/books/tutorial/index.html?name=networking
        System.out.println("ref = " + aURL.getRef()); //DOWNLOADING}
    }
}