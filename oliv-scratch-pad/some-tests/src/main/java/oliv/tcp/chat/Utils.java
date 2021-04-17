package oliv.tcp.chat;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

public class Utils {

    public enum SERVER_COMMANDS {
        I_AM,
        WHO_S_THERE,
        I_M_OUT
    }

    public static String rpad(String s, int len) {
        return rpad(s, len, " ");
    }

    public static String rpad(String s, int len, String pad) {
        String str = s;
        while (str.length() < len) {
            str += pad;
        }
        return str;
    }

    public static String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return null;
        }
    }

    private final static String COMPILE_DATE_KEY = "Compile-date";

    public static String getCompileDate(boolean verbose) throws IOException {
        String compileDate = null;

        String strClassPath = System.getProperty("java.class.path");
        String[] splitCP = strClassPath.split(File.pathSeparator);
        if (verbose) {
            System.out.println("Classpath : ");
        }
        for (String one : splitCP) {
            if (verbose) {
                System.out.println(">>> [" + one + "]");
            }
            if (one.endsWith(".jar")) {
                File jar = new File(one);
                if (jar.exists()) {
                    JarInputStream jarInputStream = new JarInputStream(new FileInputStream(jar));
                    Manifest mf = jarInputStream.getManifest();
                    if (mf != null) {
                        if (verbose) {
                            System.out.printf("Manifest, %d entries. %s\n", mf.getMainAttributes().size(), mf);
                            mf.getMainAttributes().forEach((k, v) -> {
                                System.out.printf("[%s]:[%s]\n", k, v);
                            });
                        }
                        compileDate = mf.getMainAttributes().getValue(COMPILE_DATE_KEY);
                        if (verbose) {
                            if (compileDate != null) {
                                System.out.printf("Client compiled on %s%n", compileDate);
                            }
                        }
                    } else {
                        if (verbose) {
                            System.out.println("No manifest in " + one);
                        }
                    }
                } else {
                    if (verbose) {
                        System.out.printf("... %s does not exist.\n", one);
                    }
                }
            }
        }
        return compileDate;
    }
}
