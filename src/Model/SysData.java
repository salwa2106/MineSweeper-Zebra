package Model;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class SysData {

    // EXACT PATH to your CSV file
	private static final String CSV_FILE = getCSVPath();


	private static String getCSVPath() {
	    try {
	        String path = SysData.class.getProtectionDomain()
	                .getCodeSource().getLocation().getPath();
	        String decoded = java.net.URLDecoder.decode(path, "UTF-8");

	        // remove /bin if running in Eclipse
	        if (decoded.contains("/bin")) {
	            decoded = decoded.substring(0, decoded.indexOf("/bin"));
	            System.out.println("CSV path (Dev): " + decoded + "/src/resources/questions/questionsCell.csv");
	            return decoded + "/src/resources/questions/questionsCell.csv";
	        }

	        // running from JAR
	        decoded = decoded.substring(0, decoded.lastIndexOf("/"));
	        System.out.println("CSV path (JAR): " + decoded + "/resources/questions/questionsCell.csv");
	        return decoded + "/resources/questions/questionsCell.csv";

	    } catch (Exception e) {
	        e.printStackTrace();
	        return null;
	    }
	}


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

    /** Returns a random question for the given difficulty ("easy","medium","hard","pro").
     *  If none exist, falls back to any question (nextRandom()).
     */
    public static Question nextRandomByDifficulty(String difficulty) {
        if (questions.isEmpty()) {
            System.err.println("⚠ No questions loaded. Check CSV file!");
            return null;
        }
        if (difficulty == null) {
            return nextRandom();
        }

        String d = difficulty.trim().toLowerCase();
        List<Question> filtered = new ArrayList<>();
        for (Question q : questions) {
            String qDiff = q.getDifficulty();
            if (qDiff == null) qDiff = "easy";
            if (qDiff.trim().toLowerCase().equals(d)) {
                filtered.add(q);
            }
        }

        if (filtered.isEmpty()) {
            System.err.println("⚠ No questions for difficulty: " + difficulty + ". Falling back to any question.");
            return nextRandom();
        }

        return filtered.get(rnd.nextInt(filtered.size()));
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
                char correct = parts[5].trim().isEmpty()
                        ? 'A'
                        : parts[5].trim().toUpperCase().charAt(0);

                Integer pr   = parseIntOrNull(parts[6]);
                Integer pw   = parseIntOrNull(parts[7]);
                Integer life = parseIntOrNull(parts[8]);

                // 🔹 NEW: difficulty column at index 9 (with default "easy" if missing)
                String difficulty = "easy";
                if (parts.length > 9 && parts[9] != null && !parts[9].trim().isEmpty()) {
                    difficulty = parts[9].trim().toLowerCase();
                }

                // 🔹 Updated constructor to include difficulty
                questions.add(new Question(text, optA, optB, optC, optD,
                                           correct, pr, pw, life, difficulty));

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

        	bw.write("Question,OptA,OptB,OptC,OptD,Correct,PointsRight,PointsWrong,LifeDelta,Difficulty");
        	bw.newLine();

        	for (Question q : questions) {
        	    String diff = q.getDifficulty();
        	    if (diff == null || diff.isBlank()) {
        	        diff = "easy";
        	    }

        	    bw.write(q.getText() + SEP);
        	    bw.write(q.getOptA() + SEP);
        	    bw.write(q.getOptB() + SEP);
        	    bw.write(q.getOptC() + SEP);
        	    bw.write(q.getOptD() + SEP);
        	    bw.write(q.getCorrect() + SEP);
        	    bw.write(nvl(q.getPointsRight()) + SEP);
        	    bw.write(nvl(q.getPointsWrong()) + SEP);
        	    bw.write(nvl(q.getLifeDelta()) + SEP);
        	    bw.write(diff);
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
