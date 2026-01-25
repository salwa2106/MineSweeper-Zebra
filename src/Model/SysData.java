package Model;

import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class SysData {

    // EXACT PATH to your CSV file
    private static final String CSV_FILE = getCSVPath();
    private static final String USERS_CSV_FILE = getUsersCSVPath();

    // -------------------- I18N (LANGUAGE) --------------------
    private static Language language = Language.EN;
    private static l18n i18n = new l18n(language);
    private static float musicVolume = 0.7f; // 70% default (0.0–1.0)
 // -------------------- TURN TIMER (IN-MEMORY) --------------------
    private static boolean turnTimerEnabled = true;
    private static int turnSecondsPerTurn = 30;

    public static boolean isTurnTimerEnabled() {
        return turnTimerEnabled;
    }

    public static void setTurnTimerEnabled(boolean enabled) {
        turnTimerEnabled = enabled;
    }

    public static int getTurnSecondsPerTurn() {
        return turnSecondsPerTurn;
    }

    public static void setTurnSecondsPerTurn(int seconds) {
        turnSecondsPerTurn = Math.max(5, Math.min(120, seconds));
    }

    public static float getMusicVolume() {
        return musicVolume;

    }
    public static Language getLanguage() {
        return language;
    }

    public static void setLanguage(Language lang) {
        if (lang == null) lang = Language.EN;
        language = lang;

        // הכי בטוח: ליצור i18n חדש לפי השפה
        i18n = new l18n(language);
    }
    public static void setMusicVolume(float v) {
        musicVolume = Math.max(0f, Math.min(1f, v));
    }


    public static l18n getI18n() {
        return i18n;
    }

    public static void applyGlobalFont(Component root) {
        if (root == null) return;

        boolean isHebrew = getI18n() != null && getI18n().isHebrew();

        Font base = new Font("SansSerif", Font.PLAIN, 14);
        if (isHebrew && base.canDisplayUpTo("אבגדהוזחטיכלמנסעפצקרשת") != -1) {
            base = new Font("SansSerif", Font.PLAIN, 14);
        }

        applyFontRecursively(root, base);
    }

    private static void applyFontRecursively(Component c, Font f) {
        if (c == null) return;
        c.setFont(f);

        if (c instanceof Container cont) {
            for (Component child : cont.getComponents()) {
                applyFontRecursively(child, f);
            }
        }
    }


    // -------------------- THEME & MUSIC --------------------

    private static ThemeType currentTheme = ThemeType.FOREST;
    private static boolean musicEnabled = true;

    
    public static ThemeType getTheme() {
        return currentTheme;
    }

    public static void setTheme(ThemeType theme) {
        if (theme == null) {
            theme = ThemeType.FOREST;
        }
        currentTheme = theme;
    }

    public static boolean isMusicEnabled() {
        return musicEnabled;
    }

    public static void setMusicEnabled(boolean enabled) {
        musicEnabled = enabled;
    }

 // -------------------- SFX (SOUND EFFECTS) --------------------
    private static boolean soundEffectsEnabled = true;

    public static boolean isSoundEffectsEnabled() {
        return soundEffectsEnabled;
    }

    public static void setSoundEffectsEnabled(boolean enabled) {
        soundEffectsEnabled = enabled;
    }

    // -------------------- LOGIN SESSION --------------------

    private static String currentUsername = null;
    private static String currentRole = null;

    public static String getCurrentUsername() { return currentUsername; }
    public static String getCurrentRole() { return currentRole; }

    public static boolean isAdmin() {
        return currentRole != null && currentRole.equalsIgnoreCase("ADMIN");
    }

    public static void logout() {
        currentUsername = null;
        currentRole = null;
    }

    // -------------------- USERS CSV --------------------

    private static String getUsersCSVPath() {
        try {
            String path = SysData.class.getProtectionDomain()
                    .getCodeSource().getLocation().getPath();
            String decoded = java.net.URLDecoder.decode(path, "UTF-8");

            if (decoded.contains("/bin")) {
                decoded = decoded.substring(0, decoded.indexOf("/bin"));
                return decoded + "/src/resources/users/users.csv";
            }

            decoded = decoded.substring(0, decoded.lastIndexOf("/"));
            return decoded + "/resources/users/users.csv";

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public static boolean addUser(String username, String password, String role) {

        if (username == null || password == null || role == null) return false;

        try {
            File file = new File(USERS_CSV_FILE);

            // create file if not exists
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }

            // check if user already exists
            if (userExists(username)) {
                return false;
            }

            try (BufferedWriter bw = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(file, true), StandardCharsets.UTF_8))) {

                bw.write(username + "," + password + "," + role.toUpperCase());
                bw.newLine();
            }

            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String authenticateUser(String username, String password) {

        if (username == null || password == null) return null;

        username = username.trim();
        password = password.trim();

        File file = new File(USERS_CSV_FILE);
        if (!file.exists()) {
            System.out.println("❌ users.csv not found at: " + file.getAbsolutePath());
            return null;
        }

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

            String line;
            while ((line = br.readLine()) != null) {

                if (line.trim().isEmpty()) continue;

                // ✅ remove BOM if exists (CRITICAL FIX)
                line = line.replace("\uFEFF", "");

                // ✅ skip header
                String lower = line.toLowerCase();
                if (lower.startsWith("username") && lower.contains("password")) {
                    continue;
                }

                String[] parts = line.split(",", -1);
                if (parts.length < 3) continue;

                String u = parts[0].trim();
                String p = parts[1].trim();
                String role = parts[2].trim();

                System.out.println("CSV READ -> [" + u + "] [" + p + "]");

                if (u.equals(username) && p.equals(password)) {
                    currentUsername = u;     // ✅ store who logged in
                    currentRole = role;      // ✅ store role (ADMIN/USER)
                    return role;
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static boolean userExists(String username) {

        if (username == null) return false;
        username = username.trim();

        File file = new File(USERS_CSV_FILE);
        if (!file.exists()) return false;

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

            String line;
            while ((line = br.readLine()) != null) {

                if (line.trim().isEmpty()) continue;

                line = line.replace("\uFEFF", "");

                String lower = line.toLowerCase();
                if (lower.startsWith("username") && lower.contains("password")) {
                    continue;
                }

                String[] parts = line.split(",", -1);
                if (parts.length < 1) continue;

                if (parts[0].trim().equals(username)) {
                    return true;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }
    // -------------------- QUESTIONS CSV --------------------

    private static String getCSVPath() {
        try {
            String path = SysData.class.getProtectionDomain()
                    .getCodeSource().getLocation().getPath();
            String decoded = java.net.URLDecoder.decode(path, "UTF-8");

            if (decoded.contains("/bin")) {
                decoded = decoded.substring(0, decoded.indexOf("/bin"));
                return decoded + "/src/resources/questions/questionsCell.csv";
            }

            decoded = decoded.substring(0, decoded.lastIndexOf("/"));
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
    
 // ===================== QUESTIONS DEDUPLICATION =====================

 // ===================== QUESTIONS DEDUPLICATION =====================

    private static String normalize(String s) {
        if (s == null) return "";
        return s.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    // duplicate question = same question text
    private static String questionTextKey(Question q) {
        return normalize(q.getText());
    }

    private static String answersKey(Question q) {
        // normalize all answers, handle nulls, trim, lower, and collapse spaces
        List<String> opts = new ArrayList<>();
        opts.add(normalize(q.getOptA()));
        opts.add(normalize(q.getOptB()));
        opts.add(normalize(q.getOptC()));
        opts.add(normalize(q.getOptD()));

        // if you want to ignore empty answers, uncomment:
        // opts.removeIf(String::isEmpty);

        // sort so A/B/C/D order doesn't matter
        Collections.sort(opts);

        // join into one comparable key
        return String.join("|", opts);
    }

   
    public static void deduplicateQuestions() {
        Set<String> seenQuestions = new HashSet<>();
        Set<String> seenAnswers   = new HashSet<>();

        List<Question> cleaned = new ArrayList<>();

        for (Question q : questions) {
            if (q == null) continue;

            if (q.getDifficulty() == null || q.getDifficulty().isBlank())
                q.setDifficulty("easy");

            if (q.getText() == null || q.getText().trim().isEmpty())
                continue;

            String qKey = questionTextKey(q);
            String aKey = answersKey(q);

            // reject if question already exists OR answers already exist
            if (seenQuestions.contains(qKey)) continue;
            if (seenAnswers.contains(aKey)) continue;

            seenQuestions.add(qKey);
            seenAnswers.add(aKey);
            cleaned.add(q);
        }

        questions.clear();
        questions.addAll(cleaned);
    }


    /** Adds a question only if it doesn't already exist (same question + answers). */
    /** 
     * Adds question only if:
     * - Question text is not already used
     * - Answers A/B/C/D are not already used
     */
    public static boolean addQuestionNoDuplicate(Question q) {
        if (q == null) return false;

        if (q.getDifficulty() == null || q.getDifficulty().isBlank())
            q.setDifficulty("easy");

        if (q.getText() == null || q.getText().trim().isEmpty())
            return false;

        String newQKey = questionTextKey(q);
        String newAKey = answersKey(q);

        for (Question existing : questions) {
            if (existing == null) continue;

            if (questionTextKey(existing).equals(newQKey)) {
                return false; // duplicate question
            }
            if (answersKey(existing).equals(newAKey)) {
                return false; // duplicate answers
            }
        }

        questions.add(q);
        return true;
    }



    /** Returns a random question from list, or null if empty. */
    public static Question nextRandom() {
        if (questions.isEmpty()) {
            System.err.println("⚠ No questions loaded. Check CSV file!");
            return null;
        }
        return questions.get(rnd.nextInt(questions.size()));
    }

    /** Returns a random question for the given difficulty ("easy","medium","hard","pro"). */
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

                // difficulty column index 9 (default easy)
                String difficulty = "easy";
                if (parts.length > 9 && parts[9] != null && !parts[9].trim().isEmpty()) {
                    difficulty = parts[9].trim().toLowerCase();
                }

                Question q = new Question();
                q.setText(text);
                q.setOptA(optA);
                q.setOptB(optB);
                q.setOptC(optC);
                q.setOptD(optD);
                q.setCorrect(correct);
                q.setPointsRight(pr);
                q.setPointsWrong(pw);
                q.setLifeDelta(life);
                q.setDifficulty(difficulty);

                questions.add(q);
            }

        } catch (IOException e) {
            e.printStackTrace();
            
        }
        
        deduplicateQuestions();
        System.out.println("✔ Loaded " + questions.size() + " questions from CSV.");
    }

    // -------------------- CSV SAVING --------------------
    public static void saveToCsv() {
        System.out.println("🔥 SAVE called");
        File f = new File(CSV_FILE);

        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(f), StandardCharsets.UTF_8))) {

            bw.write("Question,OptA,OptB,OptC,OptD,Correct,PointsRight,PointsWrong,LifeDelta,Difficulty");
            bw.newLine();

            for (Question q : questions) {
                String diff = q.getDifficulty();
                if (diff == null || diff.isBlank()) diff = "easy";

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

        System.out.println("🔥 SysData.saveToCsv CALLED");
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
 // ===================== DEDUP HELPERS =====================
    private static String norm(String s) {
        return (s == null) ? "" : s.trim().replaceAll("\\s+", " ").toLowerCase();
    }

   

 
}