import logging
import threading
import time


def thread_function(name: str) -> None:
    logging.info("Thread %s: starting", name)
    time.sleep(2)
    logging.info("Thread %s: finishing", name)


def loop(name: str) -> None:
    print("Starting thread {}".format(name))
    for i in range(10):
        print("\tIn loop #{}".format(i))
        time.sleep(1)
    print("End of loop in thread {}".format(name))


if __name__ == "__main__":
    format = "%(asctime)s: %(message)s"
    logging.basicConfig(format=format, level=logging.INFO,
                        datefmt="%H:%M:%S")

    logging.info("Main    : before creating thread")
    x = threading.Thread(target=thread_function, args=("Bim",))
    y = threading.Thread(target=loop, args=("Paf",))
    logging.info("Main    : before running thread")
    x.start()
    y.start()
    logging.info("Main    : wait for the thread to finish")
    # x.join()
    logging.info("Main    : Main all done")
