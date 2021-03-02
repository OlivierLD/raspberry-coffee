# Micronaut Playground 
- [Micronaut latest doc](https://docs.micronaut.io/latest/guide/index.html)
- [Quick start](https://docs.micronaut.io/latest/guide/index.html#quickStart)

## Feature http-client documentation

- [Micronaut Micronaut HTTP Client documentation](https://docs.micronaut.io/latest/guide/index.html#httpClient)

### A Tesseract Headless Service
In the package `image.service`

Scaffolding generated with
```
$ mn create-app mn.image.service
```

### Tesseract
A few links:
- [Tesseract and Docker](https://github.com/tesseract-shadow/tesseract-ocr-re)
- [Tesseract-ocr](https://tesseract-ocr.github.io/tessdoc/4.0-Docker-Containers.html)
- [Google Open Source](https://opensource.google/projects/tesseract)
- [Tesseract OCR git repo](https://github.com/tesseract-ocr/tesseract)
- [Simplest invocation on an image](https://tesseract-ocr.github.io/tessdoc/Command-Line-Usage.html#simplest-invocation-to-ocr-an-image)
- [CLI Usage](https://tesseract-ocr.github.io/tessdoc/Command-Line-Usage.html)
- [Installing Tesseract for OCR](https://www.pyimagesearch.com/2017/07/03/installing-tesseract-for-ocr/)

### Service Resources
`POST /ocr/jpg`, with `MULTIPART_FORM_DATA` (`@Part("file")`).
- OK from `curl`, could not get it to work from Apache HttpClient.

`POST /ocr/jpg2`, with `APPLICATION_OCTET_STREAM` (`@Body byte[] data`).
- All good, from `curl`, and Apache HttpClient.

In both cases, the service writes the receives data stream as a temporary file, and runs this 
system command on it:
```
tesseract tempfile stdout -l eng --psm 1 --oem 3 hocr
```
The output is read from the `Runtime.getRuntime().exec(dmc)` (ths is an `XHTML` document),
and turned into a corresponding `JSON` document, returned to the client.
