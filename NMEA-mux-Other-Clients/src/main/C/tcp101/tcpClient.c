#include <arpa/inet.h> // inet_addr()
#include <netdb.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <strings.h> // bzero()
#include <sys/socket.h>
#include <unistd.h> // read(), write(), close()
#include <signal.h> // Ctrl-C

#define MAX 512
#define PORT 7001
#define SA struct sockaddr

#ifndef TRUE
#define TRUE 1
#define FALSE 0
#endif

/*
 * Good resource at https://www.geeksforgeeks.org/tcp-server-client-implementation-in-c/
 */

static volatile int keepReceiving = TRUE;

void intHandler(int dummy) {
    keepReceiving = FALSE;
}

// Keep reading from the server until Ctrl-C is intercepted.
void func(int sockfd) {
	char buff[MAX];
	int n;

	signal(SIGINT, intHandler); // Trap Ctrl-C

	while (keepReceiving) {
		bzero(buff, sizeof(buff)); // Cute ;) !
		read(sockfd, buff, sizeof(buff));
		printf("Received from Server : \n%s", buff);
	}
}

int main(int argc, char **argv) {
	int sockfd, connfd;
	struct sockaddr_in servaddr, cli;

	// socket create and verification
	sockfd = socket(AF_INET, SOCK_STREAM, 0);
	if (sockfd == -1) {
		printf("Socket creation failed...\n");
		exit(0);
	} else {
		printf("Socket successfully created..\n");
	}
	bzero(&servaddr, sizeof(servaddr));

	// assign IP, PORT
	servaddr.sin_family = AF_INET;
	servaddr.sin_addr.s_addr = inet_addr("127.0.0.1"); // aka localhost
	servaddr.sin_port = htons(PORT);

	// connect the client socket to server socket
	if (connect(sockfd, (SA*)&servaddr, sizeof(servaddr)) != 0) {
		printf("Connection with the server failed...\n");
		exit(0);
	} else {
		printf("Connected to the server..\n");
    }
	// function to get NMEA data from the server
	func(sockfd); // interrupted by a Ctrl-C
	printf("Finishing on user's request...\n");

	// close the socket
	close(sockfd);
	printf("Bye! \n");
	return 0;
}
