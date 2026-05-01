package storage;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CsvRepository {
    private static final String DATA_DIR = "data";

    static {
        File dir = new File(DATA_DIR);
        if (!dir.exists()) {
            dir.mkdir();
        }
    }

    public static void saveLines(String filename, List<String> lines) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_DIR + File.separator + filename))) {
            for (String line : lines) {
                writer.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<String> readLines(String filename) {
        List<String> lines = new ArrayList<>();
        File file = new File(DATA_DIR + File.separator + filename);
        if (!file.exists()) {
            return lines;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }

    public static void updateLine(String filename, String id, String newLine) {
        List<String> lines = readLines(filename);
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).startsWith(id + ",")) {
                lines.set(i, newLine);
                break;
            }
        }
        saveLines(filename, lines);
    }

    public static void deleteLine(String filename, String id) {
        List<String> lines = readLines(filename);
        lines.removeIf(line -> line.startsWith(id + ","));
        saveLines(filename, lines);
    }
}
