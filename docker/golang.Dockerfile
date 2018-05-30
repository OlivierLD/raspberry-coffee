FROM golang:1.8

#
# Uncomment the ENV lines if a proxy is needed.
#
# ENV http_proxy http://www-proxy.us.oracle.com:80
# ENV https_proxy http://www-proxy.us.oracle.com:80

RUN echo "alias ll='ls -lisah'" >> $HOME/.bashrc
RUN apt-get update
RUN apt-get install -y sysvbanner
RUN echo "banner Golang!" >> $HOME/.bashrc

WORKDIR /go/src
# From local file system to image
COPY ./go/app ./app
#    |        |
#    |        In the Docker image
#    On the host (this machine)
#
WORKDIR /go/src/app

RUN go get -d -v ./...
RUN go install -v ./...
# run ./app after that go build
RUN go build

# ENV http_proxy ""
# ENV https_proxy ""

CMD ["app"]
