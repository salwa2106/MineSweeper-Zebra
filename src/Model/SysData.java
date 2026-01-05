package Model;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class SysData {

<<<<<<< HEAD
    private static final String CSV_FILE = getCSVPath();
    private static final String USERS_CSV_FILE = getUsersCSVPath();

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

            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }

            if (userExists(username)) return false;

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
        if (!file.exists()) return null;

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                line = line.replace("\uFEFF", "");

                String lower = line.toLowerCase();
                if (lower.startsWith("username") && lower.contains("password")) continue;

                String[] parts = line.split(",", -1);
                if (parts.length < 3) continue;

                String u = parts[0].trim();
                String p = parts[1].trim();
                String role = parts[2].trim();

                if (u.equals(username) && p.equals(password)) {
                    currentUsername = u;
                    currentRole = role;
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
                if (lower.startsWith("username") && lower.contains("password")) continue;

                String[] parts = line.split(",", -1);
                if (parts.length < 1) continue;

                if (parts[0].trim().equals(username)) return true;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

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

=======
    // EXACT PATH to your CSV file
    private static final String CSV_FILE = getCSVPath();
    private static final String USERS_CSV_FILE = getUsersCSVPath();

    // -------------------- I18N (LANGUAGE) --------------------
    private static final l18n i18n = new l18n(Language.EN);

    public static l18n getI18n() {
        return i18n;
    }

    public static void setLanguage(Language lang) {
        if (lang == null) lang = Language.EN;
        i18n.setLanguage(lang);
    }

    // (Optional) if you want SysData to also remember theme globally:
    private static AppTheme currentTheme = AppTheme.DARK;

    public static AppTheme getTheme() {
        return currentTheme;
    }

    public static void setTheme(AppTheme theme) {
        if (theme == null) theme = AppTheme.DARK;
        currentTheme = theme;
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

    private static String getUsersCSVPath() {
        try {
            String path = SysData.class.getProtectionDomain()
                    .getCodeSource().getLocation().getPath();
            String decoded = java.net.URLDecoder.decode(path, "UTF-8");

            // remove /bin if running in Eclipse
            if (decoded.contains("/bin")) {
                decoded = decoded.substring(0, decoded.indexOf("/bin"));
                System.out.println("Users CSV path (Dev): " + decoded + "/src/resources/users/users.csv");
                return decoded + "/src/resources/users/users.csv";
            }

            // running from JAR
            decoded = decoded.substring(0, decoded.lastIndexOf("/"));
            System.out.println("Users CSV path (JAR): " + decoded + "/resources/users/users.csv");
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
>>>>>>> 445d8746fcce5987b4933933532e178612f127f5
    private static final String SEP = ",";
    private static final List<Question> questions = new ArrayList<>();
    private static final Random rnd = new Random();

    public static void init() {
        loadFromCsv();
    }

    public static List<Question> getQuestions() {
        return Collections.unmodifiableList(questions);
    }

    // ✅ IMPORTANT: internal mutable access (used by controller)
    static List<Question> getQuestionsMutable() {
        return questions;
    }

    public static void clear() {
        questions.clear();
    }

    public static void addQuestion(Question q) {
        questions.add(q);
    }

    public static void updateQuestionAtIndex(int index, Question q) {
        questions.set(index, q);
    }

    public static void deleteQuestionAtIndex(int index) {
        questions.remove(index);
    }

    public static Question nextRandom() {
        if (questions.isEmpty()) return null;
        return questions.get(rnd.nextInt(questions.size()));
    }

<<<<<<< HEAD
=======
    /** Returns a random question for the given difficulty ("easy","medium","hard","pro"). */
>>>>>>> 445d8746fcce5987b4933933532e178612f127f5
    public static Question nextRandomByDifficulty(String difficulty) {
        if (questions.isEmpty()) return null;
        if (difficulty == null) return nextRandom();

        String d = difficulty.trim().toLowerCase();
        List<Question> filtered = new ArrayList<>();
        for (Question q : questions) {
            String qDiff = q.getDifficulty();
            if (qDiff == null) qDiff = "easy";
            if (qDiff.trim().toLowerCase().equals(d)) filtered.add(q);
        }
        if (filtered.isEmpty()) return nextRandom();
        return filtered.get(rnd.nextInt(filtered.size()));
    }

<<<<<<< HEAD
=======
    // -------------------- CSV LOADING --------------------
>>>>>>> 445d8746fcce5987b4933933532e178612f127f5
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
                if (!headerSkipped) {
                    headerSkipped = true;
                    continue;
                }
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(SEP, -1);

                // ✅ NEW FORMAT: ID + 10 columns = 11 total
                if (parts.length < 10) {
                    System.err.println("⚠ Invalid row skipped: " + line);
                    continue;
                }

                int id = parseIntOrDefault(parts[0], 0);
                String text = parts[1];
                String optA = parts[2];
                String optB = parts[3];
                String optC = parts[4];
                String optD = parts[5];

                char correct = parts[6].trim().isEmpty()
                        ? 'A'
                        : parts[6].trim().toUpperCase().charAt(0);

                Integer pr   = parseIntOrNull(parts[7]);
                Integer pw   = parseIntOrNull(parts[8]);
                Integer life = parseIntOrNull(parts[9]);

<<<<<<< HEAD
=======
                // difficulty column index 9 (default easy)
>>>>>>> 445d8746fcce5987b4933933532e178612f127f5
                String difficulty = "easy";
                if (parts.length > 10 && parts[10] != null && !parts[10].trim().isEmpty()) {
                    difficulty = parts[10].trim().toLowerCase();
                }

                Question q = new Question();
                q.setId(id);
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

        System.out.println("✔ Loaded " + questions.size() + " questions from CSV.");
    }

<<<<<<< HEAD
=======
    // -------------------- CSV SAVING --------------------
>>>>>>> 445d8746fcce5987b4933933532e178612f127f5
    public static void saveToCsv() {
        System.out.println("🔥 SAVE called");
        File f = new File(CSV_FILE);

        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(f), StandardCharsets.UTF_8))) {

<<<<<<< HEAD
            // ✅ NEW HEADER WITH ID
            bw.write("ID,Question,OptA,OptB,OptC,OptD,Correct,PointsRight,PointsWrong,LifeDelta,Difficulty");
=======
            bw.write("Question,OptA,OptB,OptC,OptD,Correct,PointsRight,PointsWrong,LifeDelta,Difficulty");
>>>>>>> 445d8746fcce5987b4933933532e178612f127f5
            bw.newLine();

            for (Question q : questions) {
                String diff = q.getDifficulty();
                if (diff == null || diff.isBlank()) diff = "easy";

<<<<<<< HEAD
                bw.write(q.getId() + SEP);
=======
>>>>>>> 445d8746fcce5987b4933933532e178612f127f5
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
<<<<<<< HEAD
=======

            System.out.println("✔ CSV saved successfully at " + f.getAbsolutePath());
>>>>>>> 445d8746fcce5987b4933933532e178612f127f5

        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("🔥 SysData.saveToCsv CALLED");
    }

<<<<<<< HEAD
=======
    // -------------------- Helpers --------------------
>>>>>>> 445d8746fcce5987b4933933532e178612f127f5
    private static Integer parseIntOrNull(String s) {
        try {
            s = s.trim();
            return s.isEmpty() ? null : Integer.parseInt(s);
        } catch (Exception e) {
            return null;
        }
    }

    private static int parseIntOrDefault(String s, int def) {
        try { return Integer.parseInt(s.trim()); }
        catch (Exception e) { return def; }
    }

    private static String nvl(Integer n) {
        return n == null ? "" : n.toString();
    }
}
