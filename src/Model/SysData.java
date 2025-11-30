package Model;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class SysData {

    // EXACT PATH to your CSV file
    private static final String CSV_FILE = "src/resources/questions/questionsCell.csv";

    // Use comma for CSV (Excel exported with commas)
    private static final String SEP = ",";

    private static final List<Question> questions = new ArrayList<>();
    private static final Random rnd = new Random();

    /** Called once when program starts (From MineSweeperPrototype) */
    public static void init() {
        loadFromCsv();
    }

    public static List<Question> getQuestions() {
        return Collections.unmodifiableList(questions);
    }

    public static void clear() {
        questions.clear();
    }

    public static void addQuestion(Question q) {
        questions.add(q);
    }

    /** Returns a random question from list, or null if empty. */
    public static Question nextRandom() {
        if (questions.isEmpty()) {
            System.err.println("⚠ No questions loaded. Check CSV file!");
            return null;
        }
        return questions.get(rnd.nextInt(questions.size()));
    }

    // -------------------- CSV LOADING --------------------

    public static void loadFromCsv() {
        questions.clear();

        File file = new File(CSV_FILE);
        if (!file.exists()) {
            System.err.println("❌ ERROR: CSV file not found: " + file.getAbsolutePath());
            return;
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(file), StandardCharsets.UTF_8))) {

            String line;
            boolean headerSkipped = false;

            while ((line = br.readLine()) != null) {
                if (!headerSkipped) { // Skip first header line
                    headerSkipped = true;
                    continue;
                }

                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(SEP, -1);
                if (parts.length < 9) {
                    System.err.println("⚠ Invalid row skipped: " + line);
                    continue;
                }

                String text = parts[0];
                String optA = parts[1];
                String optB = parts[2];
                String optC = parts[3];
                String optD = parts[4];
                char correct = parts[5].trim().isEmpty() ? 'A' : parts[5].trim().toUpperCase().charAt(0);

                Integer pr = parseIntOrNull(parts[6]);
                Integer pw = parseIntOrNull(parts[7]);
                Integer life = parseIntOrNull(parts[8]);

                questions.add(new Question(text, optA, optB, optC, optD, correct, pr, pw, life));
            }

            System.out.println("✔ Loaded " + questions.size() + " questions from CSV.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // -------------------- CSV SAVING --------------------

    public static void saveToCsv() {
        File f = new File(CSV_FILE);

        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(f), StandardCharsets.UTF_8))) {

            bw.write("Question,OptA,OptB,OptC,OptD,Correct,PointsRight,PointsWrong,LifeDelta");
            bw.newLine();

            for (Question q : questions) {
                bw.write(q.getText() + SEP);
                bw.write(q.getOptA() + SEP);
                bw.write(q.getOptB() + SEP);
                bw.write(q.getOptC() + SEP);
                bw.write(q.getOptD() + SEP);
                bw.write(q.getCorrect() + SEP);
                bw.write(nvl(q.getPointsRight()) + SEP);
                bw.write(nvl(q.getPointsWrong()) + SEP);
                bw.write(nvl(q.getLifeDelta()));
                bw.newLine();
            }

            System.out.println("✔ CSV saved successfully at " + f.getAbsolutePath());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // -------------------- Helpers --------------------

    private static Integer parseIntOrNull(String s) {
        try {
            s = s.trim();
            return s.isEmpty() ? null : Integer.parseInt(s);
        } catch (Exception e) {
            return null;
        }
    }

    private static String nvl(Integer n) {
        return n == null ? "" : n.toString();
    }
}
