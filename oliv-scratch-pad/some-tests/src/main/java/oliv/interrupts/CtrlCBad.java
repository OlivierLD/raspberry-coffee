package oliv.interrupts;

public class CtrlCBad {
    /**
     * This is not correct
     */
    public static void main(String...args) {

        final Thread itsMe = Thread.currentThread();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nOops! Trapped exit signal...");
            synchronized (itsMe) {
                itsMe.notify();
            }
        }));
        System.out.println("Starting... Ctrl-C to stop.");
        try {
            synchronized (itsMe) {
                itsMe.wait();
            }
            System.out.println("Ok, ok! I'm leaving!");
            // This stuff takes time
            Thread.sleep(5_000L);
            System.out.println("Done cleaning my stuff!");
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
        System.out.println("Bye!");
    }
}
