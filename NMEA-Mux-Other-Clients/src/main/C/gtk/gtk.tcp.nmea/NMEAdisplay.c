#include <arpa/inet.h> // inet_addr()
#include <netdb.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <strings.h> // bzero()
#include <sys/socket.h>
#include <sys/types.h>
#include <unistd.h> // read(), write(), close()

#include <gtk/gtk.h>

/*
 * Work in Progress
 */

#define MAX 512
#define PORT 7001
#define SA struct sockaddr

#ifndef TRUE
#define TRUE 1
#define FALSE 0
#endif

#define DELETE_EVENT "delete_event"

static volatile int keepReceiving = TRUE;
GtkWidget * lbl = NULL;

static void display_ps(char * s, pid_t pid) {
  (void) fprintf(stdout, "Process %d %s\n", pid, s);
}

void end_program (GtkWidget *wid, gpointer ptr) {
    keepReceiving = FALSE;
    gtk_main_quit();
}

void update_label (gpointer ptr, char * label) {
    fprintf(stdout, "Updating label to %s\n", label);
    gtk_label_set_text(GTK_LABEL(ptr), label); // Update the label
}

// Keep reading from the server until Ctrl-C is intercepted.
void func(int sockfd) {
	char buff[MAX];
	int n;

	while (keepReceiving) {
		bzero(buff, sizeof(buff)); // Cute ;) !
		read(sockfd, buff, sizeof(buff));
		// printf("Received from Server : \n%s", buff);
		// Display in UI!!
		update_label(lbl, buff);
	}
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

    printf("Finishing on user's request...\n");

    // close the socket
    close(sockfd);
    printf("Bye! \n");
}

int main (int argc, char *argv[]) {
    gtk_init(&argc, &argv);
    GtkWidget *win = gtk_window_new(GTK_WINDOW_TOPLEVEL);
    g_signal_connect(win, DELETE_EVENT, G_CALLBACK(end_program), NULL); // The close button in the header
    lbl = gtk_label_new("NMEA Data...                                                                     ");
    //                   |                                                                               |
    //                   |                                                                               79
    //                   0
    GtkWidget *box = gtk_vbox_new(FALSE, 5);
    gtk_box_pack_start(GTK_BOX(box), lbl, TRUE, TRUE, 0);

    gtk_container_add(GTK_CONTAINER(win), box);
    gtk_widget_show_all(win);

    pid_t procid;
    pid_t id;
    char * var = "Common data ";

    switch (id = fork()) {
        case -1 :
          perror ("Problem creating child");
          exit(2);
        case 0:
          procid = getpid();
          display_ps(var, procid);
          var = "Child";
          // gtk_main();
          readTCP();
          break;
        default :
         fprintf(stdout, "Value of Child process is %d\n", id);
         procid = getpid();
         display_ps(var, procid);
         var = "Parent";
         // read TCP
         // readTCP();
         break;
    }
    display_ps(var, procid);
    fprintf(stdout, "Back in main thread\n");

    gtk_main();
    gdk_threads_leave();

    return 0;
}