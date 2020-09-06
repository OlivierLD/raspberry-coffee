package threadqueue;

import java.util.*;

public class SampleThreadQueue {

    private List<Thread> threadQueue = new ArrayList<>() {
        @Override
        public boolean add(Thread thread) {
            boolean added;
            synchronized (this) {
                added = super.add(thread);
            }
            System.out.println(String.format("List has now %d element(s).", this.size()));
            System.out.println("----------------------------------");
            this.stream().forEach(t -> System.out.println(String.format("1 - %s, alive:%s", t.getName(), t.isAlive())));
            System.out.println("----------------------------------");
            if (this.size() > 1) {
                synchronized (this) {
                    try {
                        this.wait();
                    } catch (InterruptedException ie) {
                        ie.printStackTrace();
                    }
                    synchronized (this) {
                        this.remove(0);
                    }
                    if (this.size() > 0) {
                        synchronized (this) {
                            System.out.println(String.format("...Staring %s (%d)", this.get(0).getName(), System.currentTimeMillis()));
                            System.out.println("----------------------------------");
                            this.stream().forEach(t -> System.out.println(String.format("2 - %s, alive:%s", t.getName(), t.isAlive())));
                            System.out.println("----------------------------------");
                            if (!this.get(0).isAlive()) {
                                this.get(0).start();
                            } else {
                                try {
                                    this.wait();
                                } catch (InterruptedException ie) {
                                    ie.printStackTrace();
                                }
                            }
                        }
                    }
                }
            } else {
                if (this.size() == 1) {
                    synchronized (this) {
                        System.out.println(String.format("...Staring %s (%d)", this.get(0).getName(), System.currentTimeMillis()));
                        this.get(0).start();
                    }
                }
            }
            return added;
        }
    };


    public SampleThreadQueue() {
        Thread first = new Thread(() -> {
            System.out.println(String.format(">> First thread starting at %d", System.currentTimeMillis()));
            try {
                Thread.sleep(5_000);
            } catch (InterruptedException ie) {
                System.out.println("Ooops");
            }
            synchronized (threadQueue) {
                threadQueue.notifyAll(); // Important, to unlock the queue at the end!
            }
            System.out.println(String.format("<< First thread finished at %d", System.currentTimeMillis()));
        }, "first-thread");
        new Thread(() -> {
            synchronized (threadQueue) {
                threadQueue.add(first);
            }
        }).start();
        System.out.println("First added");
        try {
            Thread.sleep(500);
        } catch (Exception ie) {
            ie.printStackTrace();
        }

        Thread second = new Thread(() -> {
            System.out.println(String.format(">> Second thread starting at %d", System.currentTimeMillis()));
            try {
                Thread.sleep(6_000);
            } catch (InterruptedException ie) {
                System.out.println("Ooops");
            }
            synchronized (threadQueue) {
                threadQueue.notifyAll(); // Important, to unlock the queue at the end!
            }
            System.out.println(String.format("<< Second thread finished at %d", System.currentTimeMillis()));
        }, "second-thread");
        new Thread(() -> {
            synchronized (threadQueue) {
                threadQueue.add(second);
            }
        }).start();
        System.out.println("Second added");
        try {
            Thread.sleep(500);
        } catch (Exception ie) {
            ie.printStackTrace();
        }

        Thread third = new Thread(() -> {
            System.out.println(String.format(">> Third thread starting at %d", System.currentTimeMillis()));
            try {
                Thread.sleep(10_000);
            } catch (InterruptedException ie) {
                System.out.println("Ooops");
            }
            synchronized (threadQueue) {
                threadQueue.notifyAll(); // Important, to unlock the queue at the end!
            }
            System.out.println(String.format("<< Third thread finished at %d", System.currentTimeMillis()));
        }, "third-thread");
        new Thread(() -> {
            synchronized (threadQueue) {
                threadQueue.add(third);
            }
        }).start();
        System.out.println("Third added");
//        try {
//            Thread.sleep(500);
//        } catch (Exception ie) {
//            ie.printStackTrace();
//        }
    }

    public static void main(String... args) {
        new SampleThreadQueue();
    }
}
