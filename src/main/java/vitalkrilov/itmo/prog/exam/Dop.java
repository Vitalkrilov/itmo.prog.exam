package vitalkrilov.itmo.prog.exam;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.ArrayDeque;

public class Dop {
    public static String getUsage(String programName) {
        return String.format("Usage: %s <path> <substring>", programName);
    }

    private static boolean checkStringIsBinary(String s) {
        for (char c : s.toCharArray()) {
            if (c < 32 && c != 9 && c != 10 && c != 13) return true;
        }
        return false;
    }

    public static void main(String[] args) {
        String programName;
        {
            File f = new File(Dop.class.getProtectionDomain().getCodeSource().getLocation().getPath());
            if (f.isDirectory()) {
                programName = "Dop";
            } else {
                programName = f.getName();
            }
        }
        {
            var lArgs = Arrays.asList(args);
            if (lArgs.contains("--usage") || lArgs.contains("--help")) {
                System.out.println(getUsage(programName));
                return;
            }
        }
        if (args.length < 2) {
            System.err.printf("%s: invalid arguments count%n", programName);
            System.err.println(getUsage(programName));
            return;
        }
        String filePath = args[0];
        File rootDirectory = new File(filePath);
        if (!rootDirectory.exists()) {
            System.err.printf("%s: %s: path not found%n", programName, filePath);
            return;
        }
        if (!rootDirectory.isDirectory()) {
            System.err.printf("%s: %s: not a directory%n", programName, filePath);
            return;
        }
        Queue<File> q = new ArrayDeque<>();
        q.add(rootDirectory);
        String substring = args[1];
        while (true) {
            File f = q.poll();
            if (f == null) break;

            if (!f.exists()) continue; // Might become outdated due to deletion
            if (f.isDirectory()) {
                q.addAll(List.of(Objects.requireNonNull(f.listFiles(), "Possible race condition on system: got file which was directory.")));
            } else {
                if (!f.canRead()) {
                    System.err.printf("%s: %s: no read permission", programName, f.getPath());
                    continue;
                }
                LineNumberReader lnr;
                try {
                    lnr = new LineNumberReader(new BufferedReader(new FileReader(f)));
                } catch (FileNotFoundException e) {
                    continue; // Might become outdated due to deletion
                }
                boolean isCurrentFileBinary = false;
                try {
                    String line;
                    while (true) {
                        line = lnr.readLine();
                        if (line == null) break;

                        if (!isCurrentFileBinary) isCurrentFileBinary = checkStringIsBinary(line);

                        if (line.contains(substring)) {
                            //NOTE: We could cache results for sure binary detection before we print, but it's bad for resource usage, so it might print some lines before it understands that file is binary.

                            if (!isCurrentFileBinary) {
                                System.out.printf("%s:%d:%s%n", f.getPath(), lnr.getLineNumber(), line);
                            } else {
                                System.out.printf("%s: %s: binary file matches%n", programName, f.getPath());
                                break;
                            }
                        }
                    }
                } catch (IOException e) {
                    System.err.printf("%s: %s: file read error", programName, f.getPath());
                }
            }
        }
    }
}
