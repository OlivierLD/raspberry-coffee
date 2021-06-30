package oliv.oda.flow;

public class UpdateV1 {

    private static String enterStatus() {
        String status = System.console().readLine();
        return status;
    }

    public static void main(String... args) {

        // Step one, enter parameters
        System.out.print("Enter something > ");
        String prm1 = System.console().readLine();

        boolean allGood = false;
        boolean keepMoving = false;
        boolean goFixing = false;
        boolean goTesting = false;

        // Perform task, simulate output
        String status = "";
        while (!"A".equals(status) && !"B".equals(status) && !"C".equals(status)) {
            System.out.print("Enter status =>  A: All good, B: Warnings, C: Errors > ");
            status = enterStatus().toUpperCase();
        }

        if ("A".equals(status)) {
            System.out.println("All good.");
            allGood = true;
            keepMoving = true;
        } else {
          if ("B".equals(status)) {
              System.out.println("There are warnings.");
              System.out.print("Do you want to fix the warnings y|n ? > ");
              String resp = enterStatus().toUpperCase();
              if ("N".equals(resp)) {
                keepMoving = true;
              }
          } else {
              System.out.println("There are errors");
              goFixing = true;
          }
        }
        if (allGood) {
            goTesting = true;
        } else {
            if (keepMoving) {
                goTesting = true;
            } else {
                goFixing = true;
            }
        }
        if (goTesting) {
            System.out.println("Now Testing (TBD).");
        } else {
            if (goFixing) {
                System.out.println("You need to fix the stuff before doing anything else.");
            } else {
                System.out.println("You should not see that.");
            }
        }
    }

}
