#include <stdio.h>      /* printf, sprintf */
#include <stdlib.h>     /* exit */
#include <unistd.h>     /* read, write, close */
#include <string.h>     /* memcpy, memset, etc */
#include <sys/socket.h> /* socket, connect */
#include <netinet/in.h> /* struct sockaddr_in, struct sockaddr */
#include <netdb.h>      /* struct hostent, gethostbyname */

/**
 * This is an HTTP Client, using REST request to read data from a multiplexer.
 * The server needs to be identified by its name or IP, and HTTP port.
 * 
 * This is just a scaffolding for similar utilities.
 */

#ifndef TRUE
#define TRUE 1
#define FALSE 0
#endif

#define CR 13
#define NL 10

#define MESS_SIZE 1024
#define RESPONSE_SIZE 16384  // Warning: this can be a limitation


const char * VERBOSE_PRM = "--verbose:";
const char * MACHINE_PRM = "--machine-name:";
const char * PORT_PRM = "--port:";

char errorMess[MESS_SIZE];

void error(const char *msg) {
    perror(msg);
    exit(0);
}

/**
 * @brief Find the string that follows a CR-NL-CR-NL
 * 
 * @param fullResponse As it came from the server
 * @return int The required offset, -1 if not found.
 */
int findPayloadStart(char * fullResponse) {
    int payloadStartsAt = -1;
    for (int i=0; i<strlen(fullResponse); i++) {
        // fprintf(stdout, "Char at %d: %c (%d)\n", i, (char)response[i], (int)response[i]);
        if (i > 4 && fullResponse[i] == NL && fullResponse[i-1] == CR && fullResponse[i-2] == NL && fullResponse[i-3] == CR) {
            // fprintf(stdout, ">>> Found CRNL-CRNL at %d\n", i);
            payloadStartsAt = i + 1;
            break;
        }
    }
    return payloadStartsAt;
}

const int DEBUG=FALSE;  // User set, in the code. TODO Replace with a define, ifdef, -D, etc.

int main(int argc, char **argv) {

    // default values
    int portno = 9999;
    char * host = "localhost"; // "192.168.42.6";
    char * rest_request = "GET /mux/cache HTTP/1.0\r\n\r\n";
    // char * rest_request = "GET /oplist HTTP/1.0\r\n\r\n";
    int verbose = FALSE;


    char prmValue[128];

    // TODO override ALL values above from the args.
    if (argc > 1) {
        for (int i=1; i<argc; i++) {
            memset(prmValue, 0, sizeof(prmValue)); // init, all 0
            if (DEBUG) {
              fprintf(stdout, "Prm #%d: [%s]\n", i, argv[i]);
            }
            if (strncmp(argv[i], VERBOSE_PRM, strlen(VERBOSE_PRM)) == 0) {
                memcpy(&prmValue[0], &argv[i][strlen(VERBOSE_PRM)], strlen(argv[i]) - strlen(VERBOSE_PRM));
                if (DEBUG) {
                  fprintf(stdout, "Found VERBOSE prm:%s (value:%d)\n", prmValue, strcasecmp(prmValue, "TRUE")); // substr(argv[i], strlen(VERBOSE_PRM))); // prmValue);
                }
                if (strcasecmp(prmValue, "TRUE") == 0) {
                    verbose = TRUE;
                }
            } else if (strncmp(argv[i], MACHINE_PRM, strlen(MACHINE_PRM)) == 0) {
                memcpy(&prmValue[0], &argv[i][strlen(MACHINE_PRM)], strlen(argv[i]) - strlen(MACHINE_PRM));
                host = (char *)calloc(128, sizeof(char));
                strcpy(host, prmValue);
            } else if (strncmp(argv[i], PORT_PRM, strlen(PORT_PRM)) == 0) {
                memcpy(&prmValue[0], &argv[i][strlen(PORT_PRM)], strlen(argv[i]) - strlen(PORT_PRM));
                portno = atoi(prmValue);
            } else {
                fprintf(stdout, "Unsupported parameter %s\n", argv[i]);
            }
        }
        if (DEBUG) {
            fprintf(stdout, "verbose: %s, host: %s, port: %d\n", verbose ? "true" : "false", host, portno);
        }
    }

    if (verbose) {
        fprintf(stdout, "Will run request\n\t%s on %s:%d\n", rest_request, host, portno);
    }

    struct hostent * server;
    struct sockaddr_in serv_addr;
    int sockfd, bytes, sent, received, total;
    char message[MESS_SIZE], response[RESPONSE_SIZE];

    strcpy(message, rest_request);
    if (verbose) {
        fprintf(stdout, "Request:\n%s\n", message);
    }

    /* create the socket */
    sockfd = socket(AF_INET, SOCK_STREAM, 0);
    if (sockfd < 0) {
        sprintf(errorMess, "ERROR opening socket (%d)", sockfd);
        error(errorMess);
    }
    /* lookup the ip address */
    server = gethostbyname(host);
    if (server == NULL) {
        sprintf(errorMess, "ERROR, no such host [%s]", host);
        error(errorMess);
    }
    /* fill in the structure */
    memset(&serv_addr, 0, sizeof(serv_addr));
    serv_addr.sin_family = AF_INET;
    serv_addr.sin_port = htons(portno);
    memcpy(&serv_addr.sin_addr.s_addr, server->h_addr, server->h_length);

    /* connect the socket */
    if (connect(sockfd, (struct sockaddr *)&serv_addr, sizeof(serv_addr)) < 0) {
        error("ERROR connecting to server");
    }

    /* send the request */
    total = strlen(message);
    sent = 0;
    do {
        bytes = write(sockfd, message + sent, total - sent);
        if (bytes < 0) {
            error("ERROR writing request to socket");
        }
        if (bytes == 0) {
            break;
        }
        sent += bytes;
    } while (sent < total);

    /* receive the response */
    memset(response, 0, sizeof(response)); // init, all 0
    total = sizeof(response) - 1;
    received = 0;
    do {
        bytes = read(sockfd, response + received, total - received);
        if (bytes < 0) {
            sprintf(errorMess, "ERROR reading response from socket (total %d, received %d)", total, received);
            error(errorMess);
        }
        if (bytes == 0) {
            break;
        }
        received += bytes;
    } while (received < total);

    if (received == total) {
        sprintf(errorMess, "ERROR storing complete response from socket (%d)", total);
        error(errorMess);
    }
    /* close the socket */
    close(sockfd);

    /* process response */
    if (verbose) {
      fprintf(stdout, "Full response:\n%s\n", response);
    }

    int payloadStartsAt = findPayloadStart(response);
    if (payloadStartsAt > -1) {
        char payload[RESPONSE_SIZE];
        memset(payload, 0, sizeof(payload)); // init, all 0
        memcpy(&payload[0], &response[payloadStartsAt], strlen(response) - payloadStartsAt);
        if (verbose) {
            fprintf(stdout, "Payload:\n");
        }
        fprintf(stdout, "%s\n", payload);
    } else {
        fprintf(stdout, "Payload was not found in the response.");
    }

    return 0;
}