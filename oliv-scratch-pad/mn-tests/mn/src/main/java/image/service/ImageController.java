package image.service;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Part;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.multipart.CompletedFileUpload;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@Controller("/ocr")
public class ImageController {

    @Post(value = "/jpg", consumes = MediaType.MULTIPART_FORM_DATA, produces = MediaType.APPLICATION_JSON)
    public String processImage(HttpRequest request, @Part("file") CompletedFileUpload file) {
        System.out.println("Processing Image...");
        System.out.printf("Request: %s%n", request);
        String ct = request.getHeaders().get("Content-Type");
        System.out.printf("Content-Type: %s%n", ct);
        Optional<Object> body = request.getBody(Object.class);
        if (body.isPresent()) {
            System.out.printf("Body: %s%n", body.get());
        } else {
            System.out.println("NO Body");
        }
        System.out.printf("Content-Length: %d%n", request.getContentLength());


        String finalOutput = "{}";
        try {
            File img = File.createTempFile(file.getFilename(), ".jpg");
            System.out.printf("Writing in %s%n", img.getAbsolutePath());
            InputStream inputStream = file.getInputStream();
            if (inputStream == null) {
                System.out.println(String.format(">> Null input stream for image %s.", file.getFilename()));
                throw new RuntimeException(String.format("Null Input Stream for %s", file.getFilename()));
            }
            System.out.println(String.format(">> Reading bytes for image %s.", file.getFilename()));
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nbRead;
            byte[] data = new byte[16_384];

            // Java 9 has a inputStream.readAllBytes() ...
            while ((nbRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nbRead);
            }
            byte[] imgData = buffer.toByteArray();
            ByteArrayInputStream bis = new ByteArrayInputStream(imgData);
            BufferedImage bImage2 = ImageIO.read(bis);
            ImageIO.write(bImage2, "jpg", img);
            // Invoke Tesseract on the image
            String tesseractCommand = String.format("tesseract %s stdout -l eng --psm 1 --oem 3 hocr", img.getAbsolutePath());
            Process process = Runtime.getRuntime().exec(tesseractCommand);
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuffer sb = new StringBuffer();
            String line;
            while ((line = in.readLine()) != null) {
//                System.out.println(line);
                sb.append(line);
            }
            in.close();
            finalOutput = sb.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return finalOutput;
    }

    @Post(value = "/jpg2", consumes = MediaType.APPLICATION_OCTET_STREAM, produces = MediaType.APPLICATION_JSON)
    public HttpResponse<String> processImage2(HttpRequest request, @Body byte[] data) {
        System.out.println("Processing Image... from bytes");
        if (data != null) {
            System.out.printf("Received %d bytes of data.%n", data.length);
        } else {
            System.out.println("NO data.");
        }
        System.out.printf("Request: %s%n", request);
        String ct = request.getHeaders().get("Content-Type");
        System.out.printf("Content-Type: %s%n", ct);
        Optional<Object> body = request.getBody(Object.class);
        if (body.isPresent()) {
            System.out.printf("Body: %s%n", body.get());
        } else {
            System.out.println("NO Body");
        }
        System.out.printf("Content-Length: %d%n", request.getContentLength());

        String finalOutput = "{}";
        try {
            File img = File.createTempFile("img", ".jpg");
            System.out.printf("Writing in %s%n", img.getAbsolutePath());

            Path path = Paths.get(img.getAbsolutePath());
            Files.write(path, data);

            // Invoke Tesseract on the image
            String tesseractCommand = String.format("tesseract %s stdout -l eng --psm 1 --oem 3 hocr", img.getAbsolutePath());
            Process process = Runtime.getRuntime().exec(tesseractCommand);
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuffer sb = new StringBuffer();
            String line;
            while ((line = in.readLine()) != null) {
//                System.out.println(line);
                sb.append(line);
            }
            in.close();
            finalOutput = sb.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
            return HttpResponse.badRequest(ex.toString());
        }
        return HttpResponse.ok(finalOutput);
    }
}
