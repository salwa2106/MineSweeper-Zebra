package Model;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Static data holder for questions.
 * Loads them from a CSV file and lets the game pick random questions.
 */
public class SysData {

    // Change this if your CSV is somewhere else / has another name
    private static final String CSV_FILE = "questions.csv";
    private static final String SEP = ";";   // adjust to ',' if your file uses commas

    private static final List<Question> questions = new ArrayList<>();
    private static final Random rnd = new Random();

    /** Called once at program startup. */
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

    /** Returns a random question or null if none. */
    public static Question nextRandom() {
        if (questions.isEmpty()) return null;
        return questions.get(rnd.nextInt(questions.size()));
    }

    // -------------------- CSV I/O --------------------

    public static void loadFromCsv() {
        questions.clear();

        File f = new File(CSV_FILE);
        if (!f.exists()) {
            System.err.println("SysData: CSV file not found: " + CSV_FILE);
            return;
        }

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8))) {

            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                if (first && line.toLowerCase().contains("question")) {
                    // skip header line
                    first = false;
                    continue;
                }
                first = false;

                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(SEP, -1); // -1 => keep empty trailing fields
                if (parts.length < 9) {
                    // not enough columns – ignore
                    continue;
                }

                String text = parts[0];
                String a = parts[1];
                String b = parts[2];
                String c = parts[3];
                String d = parts[4];

                char correct = parts[5].trim().isEmpty()
                        ? 'A'
                        : parts[5].trim().toUpperCase().charAt(0);

                Integer right = parseIntOrNull(parts[6]);
                Integer wrong = parseIntOrNull(parts[7]);
                Integer life  = parseIntOrNull(parts[8]);

                Question q = new Question(text, a, b, c, d, correct, right, wrong, life);
                questions.add(q);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveToCsv() {
        File f = new File(CSV_FILE);
        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8))) {

            // header line – adapt to your course’s exact format if needed
            bw.write("question;optA;optB;optC;optD;correct;pointsRight;pointsWrong;lifeDelta");
            bw.newLine();

            for (Question q : questions) {
                bw.write(escape(q.getText()));        bw.write(SEP);
                bw.write(escape(q.getOptA()));        bw.write(SEP);
                bw.write(escape(q.getOptB()));        bw.write(SEP);
                bw.write(escape(q.getOptC()));        bw.write(SEP);
                bw.write(escape(q.getOptD()));        bw.write(SEP);
                bw.write(String.valueOf(q.getCorrect())); bw.write(SEP);
                bw.write(q.getPointsRight() == null ? "" : q.getPointsRight().toString()); bw.write(SEP);
                bw.write(q.getPointsWrong() == null ? "" : q.getPointsWrong().toString()); bw.write(SEP);
                bw.write(q.getLifeDelta() == null ? "" : q.getLifeDelta().toString());
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Integer parseIntOrNull(String s) {
        s = s.trim();
        if (s.isEmpty()) return null;
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String escape(String s) {
        if (s == null) return "";
        // super-simple escaping: just avoid breaking the separator
        return s.replace(SEP, ",");
    }
}
