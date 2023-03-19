#include <stdio.h>
#include <stdlib.h>
#include <unistd.h> //Header file for sleep(). man 3 sleep for details.
#include <pthread.h>
#include <signal.h> // Ctrl-C

/*
 * From https://www.geeksforgeeks.org/multithreading-c-2/
 * Compile with "gcc multithread.c -o multithread"
 */

#ifndef TRUE
#define TRUE 1
#define FALSE 0
#endif

static volatile int keepWorking = TRUE;
static volatile int nb = 0;

void interruptHandler(int dummy) {
    keepWorking = FALSE;
}

// A normal C function that is executed as a thread
// when its name is specified in pthread_create()
void * myThreadFunc(void *vargp) {
    while (keepWorking) {
        nb++;
        sleep(1);
        printf("Printing GeeksQuiz from Thread (%d)\n", nb);
    }
    printf("Exiting myThreadFunc\n");

	return NULL;
}

int main(int argc, char ** argv) {

    signal(SIGINT, interruptHandler); // Trap Ctrl-C

	pthread_t thread_id;
	printf("Before Thread\n");
	pthread_create(&thread_id, NULL, myThreadFunc, NULL);
    printf("Thread created\n");
    //
    while (keepWorking) {
        printf("\tFrom main thread, keep looping ! (nb: %d)\n", nb);
        sleep(2);
    }
    printf("Resuming main thread\n");
    // 
	pthread_join(thread_id, NULL);
	printf("After Thread\n");

	return 0;
}
