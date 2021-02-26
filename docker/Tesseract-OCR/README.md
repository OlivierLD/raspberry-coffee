# Tesseract OCR
- See [this](https://www.pyimagesearch.com/2017/07/03/installing-tesseract-for-ocr/)

### Deploy with Docker
The easiest way to get started with this image is by simply pulling it from Docker Hub.

- Pull the image from Docker Hub:
```
 docker pull clearlinux/tesseract-ocr
```
- Start a container using the examples below
```
 docker run --rm -it --name myapp -v "$PWD":/app -w /app clearlinux/tesseract-ocr tesseract xxx.tiff stdout --oem 1
```
or if trained data for language is in $PWD,
```
 docker run --rm -it --name myapp -e "TESSDATA_PREFIX=/app" -v "$PWD":/app -w /app clearlinux/tesseract-ocr tesseract xxx.tiff stdout --oem 1
```

### On a Mac
```
$ brew install tesseract
```

### Usage
Command line doc: <https://tesseract-ocr.github.io/tessdoc/Command-Line-Usage.html#simplest-invocation-to-ocr-an-image>

```
$ tesseract -v
```
Test it:
```
$ tesseract ./oliv-ai/OpenCV-doc-processing/FormProcessingSampleData/gas.receipt.jpg stdout
 ECONO GAS
 
 SERRXHHRKR POO
 
 2907 SAN BRUNO AVE W
 SAN BRUNO ee
 94066
 
 12/87/2020 586788149
 18:20:24 AM
 
 HAKK XRXK KKK 1543
 Visa
 
 INVOICE 6816333
 AUTH 855400
 
 PUMP# 3
 REGULAR CR 8.7646
 PRICE/GAL $3.099
 
 aE Th eal)
 
 CREDIT Ce tenTIG)
```

Generate `hocr` output:
```
$ tesseract ./oliv-ai/OpenCV-doc-processing/FormProcessingSampleData/gas.receipt.jpg stdout --oem 1 -l eng hocr > gas.hocr
$ tesseract ./integration-tests/src/test/resources/gas.receipt.jpg receipt -l eng --psm 1 --oem 3 txt pdf hocr 
```
then
```
$ head gas.hocr 
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
 <head>
  <title></title>
  <meta http-equiv="Content-Type" content="text/html;charset=utf-8"/>
  <meta name='ocr-system' content='tesseract 4.1.1' />
  <meta name='ocr-capabilities' content='ocr_page ocr_carea ocr_par ocr_line ocrx_word ocrp_wconf'/>
 </head>
. . .
```

...etc.

---
