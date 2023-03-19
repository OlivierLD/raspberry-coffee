#include <arpa/inet.h> // inet_addr()
#include <netdb.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <strings.h> // bzero()
#include <sys/socket.h>
#include <sys/types.h>
#include <unistd.h> // read(), write(), close()

#include <pthread.h>

#include <gtk/gtk.h>

/*
 * Work in Progress...
 */

#define MAX 512
#define PORT 7001
#define SA struct sockaddr

#ifndef TRUE
#define TRUE 1
#define FALSE 0
#endif

#define DELETE_EVENT "delete_event"

static volatile int keepWorking = TRUE;
/*static volatile*/ GtkWidget * lbl = NULL;

static void display_ps(char * s, pid_t pid) {
  (void) fprintf(stdout, "Process %d %s\n", pid, s);
}

void end_program (GtkWidget *wid, gpointer ptr) {
    keepWorking = FALSE;
    gtk_main_quit();
}

void update_label (gpointer ptr, char * label) {
    fprintf(stdout, "Updating label to %s\n", label);
    // gtk_label_set_text(GTK_LABEL(ptr), label); // Update the label
}

// Keep reading from the server until Window is closed
void func(int sockfd) {
	char buff[MAX];
	int n;

	while (keepWorking) {
		bzero(buff, sizeof(buff)); // Cute ;) !
		read(sockfd, buff, sizeof(buff));
		// printf("Received from Server : \n%s", buff);
		// Display in UI!!
		update_label(lbl, buff);
	}
	fprintf(stdout, ">> Exiting TCP reading loop.\n");
}

void readTCP() {
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
    func(sockfd); // to be interrupted by user

    printf(">> Finishing on user's request...\n");

    // close the socket
    close(sockfd);
    printf("Bye! \n");
}

// A normal C function that is executed as a thread
// when its name is specified in pthread_create()
void * myThreadFunc(void *vargp) {
    
    readTCP();

	return NULL;
}


int main (int argc, char *argv[]) {
    gtk_init(&argc, &argv);
    GtkWidget *win = gtk_window_new(GTK_WINDOW_TOPLEVEL);
    g_signal_connect(win, DELETE_EVENT, G_CALLBACK(end_program), NULL); // The close button in the header
    lbl = gtk_label_new("NMEA Data...                                                                     ");
    //                   |                                                                               |
    //                   |                                                                               79
    //                   0

	pthread_t thread_id;
	printf("Before Thread\n");
	pthread_create(&thread_id, NULL, myThreadFunc, NULL);
    printf("Thread created\n");
    //

    GtkWidget *box = gtk_vbox_new(FALSE, 5);
    gtk_box_pack_start(GTK_BOX(box), lbl, TRUE, TRUE, 0);

    gtk_container_add(GTK_CONTAINER(win), box);
    gtk_widget_show_all(win);
    gtk_main();

    // keepWorking = FALSE;
    fprintf(stdout, "After gtk_main\n");

	pthread_join(thread_id, NULL);
	printf("After Thread\n");

    gdk_threads_leave();

    return 0;
}