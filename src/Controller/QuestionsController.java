package Controller;

import Model.Question;
import View.QuestionsWizardFrame;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class QuestionsController implements QuestionsWizardFrame.QuestionsController {

    private final List<Question> questions = new ArrayList<>();

    public QuestionsController() {
        // auto-load default CSV from: src/resources/questions/questionsCell.csv
        try {
            importFromResource("/resources/questions/questionsCell.csv");
        } catch (Exception e) {
            System.out.println("Could not auto-load questionsCell.csv: " + e.getMessage());
        }
    }

    // =========================
    // Interface implementation
    // =========================

    @Override
    public List<Question> getAllQuestions() {
<<<<<<< HEAD
<<<<<<< HEAD
        // Always reflect current SysData state
=======
>>>>>>> 445d8746fcce5987b4933933532e178612f127f5
        return new ArrayList<>(SysData.getQuestions());
    }

    @Override
<<<<<<< HEAD
    public void addQuestion(Question q) throws Exception {

        // ✅ AUTO-GENERATE ID
        if (q.getId() <= 0) {
            int maxId = SysData.getQuestions().stream()
                    .mapToInt(Question::getId)
                    .max()
                    .orElse(0);
            q.setId(maxId + 1);
        }

        SysData.addQuestion(q);
        SysData.saveToCsv();
        SysData.loadFromCsv();
    }


    @Override
    public void updateQuestionAtIndex(int index, Question q) throws Exception {
        // Preserve original ID
        int originalId = SysData.getQuestions().get(index).getId();
        q.setId(originalId);

        SysData.updateQuestionAtIndex(index, q);
        SysData.saveToCsv();
        SysData.loadFromCsv();
    }

    @Override
    public void deleteQuestionAtIndex(int index) throws Exception {
        SysData.deleteQuestionAtIndex(index);
        SysData.saveToCsv();
        SysData.loadFromCsv();
=======
    public void addQuestion(Question q) {
        SysData.addQuestion(q);
        SysData.saveToCsv(); // ✅ persist immediately
        System.out.println("🔥 ADD called");

    }

    @Override
    public void updateQuestionAtIndex(int index, Question q) {
        List<Question> list = new ArrayList<>(SysData.getQuestions());
        if (index < 0 || index >= list.size())
            throw new IndexOutOfBoundsException("Invalid row index: " + index);

        list.set(index, q);

        SysData.clear();
        for (Question x : list) SysData.addQuestion(x);
        SysData.saveToCsv();
      
=======
        return new ArrayList<>(questions);
    }

    @Override
    public void addQuestion(Question q) {
        questions.add(q);
        syncToSysData();
    }

    @Override
    public void updateQuestionAtIndex(int index, Question q) {
        if (index < 0 || index >= questions.size())
            throw new IndexOutOfBoundsException("Invalid row index: " + index);
        questions.set(index, q);
        syncToSysData();
>>>>>>> parent of 6770128 (adding Id to questions)
    }

    @Override
    public void deleteQuestionAtIndex(int index) {
<<<<<<< HEAD
        System.out.println("🔥 DELETE called with index = " + index);
        List<Question> list = new ArrayList<>(SysData.getQuestions());
        if (index < 0 || index >= list.size())
            throw new IndexOutOfBoundsException("Invalid row index: " + index);

        list.remove(index);

        SysData.clear();
        for (Question x : list) SysData.addQuestion(x);
        SysData.saveToCsv();
        System.out.println("🔥 DELETE called index=" + index);

>>>>>>> 445d8746fcce5987b4933933532e178612f127f5
=======
        if (index < 0 || index >= questions.size())
            throw new IndexOutOfBoundsException("Invalid row index: " + index);
        questions.remove(index);
        syncToSysData();
>>>>>>> parent of 6770128 (adding Id to questions)
    }

    @Override
    public void importFromCsv(File file) throws Exception {
<<<<<<< HEAD
<<<<<<< HEAD
        // Overwrite the REAL questionsCell.csv
        File target = new File(
                SysData.class
                        .getProtectionDomain()
                        .getCodeSource()
                        .getLocation()
                        .getPath()
                        .replace("/bin", "")
                        + "/src/resources/questions/questionsCell.csv"
        );

        Files.copy(file.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);

        SysData.loadFromCsv();
=======
        List<Question> loaded = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

            String line;
            boolean firstLine = true;

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                // skip header if exists
                if (firstLine && line.toLowerCase().contains("question") && line.toLowerCase().contains("correct")) {
                    firstLine = false;
                    continue;
                }
                firstLine = false;

                String[] parts = line.split(",", -1);
                if (parts.length < 10) continue;

                Question q = new Question();
                q.setText(parts[0]);
                q.setOptA(parts[1]);
                q.setOptB(parts[2]);
                q.setOptC(parts[3]);
                q.setOptD(parts[4]);
                q.setCorrect(parts[5].isBlank() ? 'A' : parts[5].trim().toUpperCase().charAt(0));

                q.setPointsRight(parseIntOrDefault(parts[6], 3));
                q.setPointsWrong(parseIntOrDefault(parts[7], -1));
                q.setLifeDelta(parseIntOrDefault(parts[8], 1));

                String diff = parts[9].trim();
                q.setDifficulty(diff.isEmpty() ? "easy" : diff.toLowerCase());

                loaded.add(q);
            }
        }

        SysData.clear();
        for (Question q : loaded) SysData.addQuestion(q);
        SysData.saveToCsv(); // ✅ import replaces and saves
>>>>>>> 445d8746fcce5987b4933933532e178612f127f5
=======
        try (InputStream in = new FileInputStream(file)) {
            importFromStream(in);
            syncToSysData();
        }
>>>>>>> parent of 6770128 (adding Id to questions)
    }

    @Override
    public void exportToCsv(File file) throws Exception {
<<<<<<< HEAD
<<<<<<< HEAD
        SysData.saveToCsv();
=======
        try (OutputStream out = new FileOutputStream(file)) {
            exportToStream(out);
        }
    }
>>>>>>> parent of 6770128 (adding Id to questions)

    // =========================
    // Resource helper
    // =========================

<<<<<<< HEAD
        Files.copy(source.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
=======
        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {

            bw.write("Question,OptA,OptB,OptC,OptD,Correct,PointsRight,PointsWrong,LifeDelta,Difficulty");
            bw.newLine();

            for (Question q : SysData.getQuestions()) {
                String diff = (q.getDifficulty() == null || q.getDifficulty().isBlank()) ? "easy" : q.getDifficulty();

                bw.write(csvEscape(q.getText()) + "," +
=======
    private void importFromResource(String resourcePath) throws Exception {
        InputStream in = QuestionsController.class.getResourceAsStream(resourcePath);
        if (in == null) throw new FileNotFoundException("Resource not found: " + resourcePath);
        try (in) {
            importFromStream(in);
        }
    }

    // =========================
    // CSV logic
    // =========================

    /**
     * Expected CSV order (very common in your project):
     * id,text,optA,optB,optC,optD,correct,pointsRight,pointsWrong,lifeDelta,difficulty
     *
     * If your file has header, it's skipped automatically.
     */
    private void importFromStream(InputStream in) throws Exception {
        List<Question> loaded = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            boolean firstLine = true;

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                if (firstLine && looksLikeHeader(line)) {
                    firstLine = false;
                    continue;
                }
                firstLine = false;

                List<String> cols = parseCsvLine(line);

                // Need at least 11 columns based on your Question model
                if (cols.size() < 10) {
                    throw new IllegalArgumentException("CSV row has < 10 columns: " + cols);
                }


                // 0 = id (we ignore in model, since Question doesn't have ID field)
                String text = cols.get(0);
                String a = cols.get(1);
                String b = cols.get(2);
                String c = cols.get(3);
                String d = cols.get(4);

                char correct = safeChar(cols.get(5), 'A');
                Integer pointsRight = safeInt(cols.get(6));
                Integer pointsWrong = safeInt(cols.get(7));
                Integer lifeDelta = safeInt(cols.get(8));
                String difficulty = cols.get(9);

                Question q = new Question();
                q.setText(text);
                q.setOptA(a);
                q.setOptB(b);
                q.setOptC(c);
                q.setOptD(d);
                q.setCorrect(correct);
                q.setPointsRight(pointsRight);
                q.setPointsWrong(pointsWrong);
                q.setLifeDelta(lifeDelta);
                q.setDifficulty(difficulty);


                loaded.add(q);
            }
        }

        questions.clear();
        questions.addAll(loaded);
    }

    private void exportToStream(OutputStream out) throws Exception {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8))) {
            // header
            bw.write("id,text,optA,optB,optC,optD,correct,pointsRight,pointsWrong,lifeDelta,difficulty");
            bw.newLine();

            int id = 1;
            for (Question q : questions) {
                bw.write(id++ + "," +
                        csvEscape(q.getText()) + "," +
>>>>>>> parent of 6770128 (adding Id to questions)
                        csvEscape(q.getOptA()) + "," +
                        csvEscape(q.getOptB()) + "," +
                        csvEscape(q.getOptC()) + "," +
                        csvEscape(q.getOptD()) + "," +
                        csvEscape(String.valueOf(q.getCorrect())) + "," +
<<<<<<< HEAD
                        csvEscape(String.valueOf(nvl(q.getPointsRight(), 3))) + "," +
                        csvEscape(String.valueOf(nvl(q.getPointsWrong(), -1))) + "," +
                        csvEscape(String.valueOf(nvl(q.getLifeDelta(), 1))) + "," +
                        csvEscape(diff));
                bw.newLine();
            }
        }
=======
                        csvEscape(String.valueOf(nvl(q.getPointsRight(), 0))) + "," +
                        csvEscape(String.valueOf(nvl(q.getPointsWrong(), 0))) + "," +
                        csvEscape(String.valueOf(nvl(q.getLifeDelta(), 0))) + "," +
                        csvEscape(q.getDifficulty()));
                bw.newLine();
            }
        }
    }

    // =========================
    // Helpers
    // =========================

    private boolean looksLikeHeader(String line) {
        String lower = line.toLowerCase(Locale.ROOT);
        return lower.contains("text") && lower.contains("correct");
    }

    private String normalizeDifficulty(String s) {
        if (s == null) return "easy";
        String x = s.trim().toLowerCase(Locale.ROOT);
        if (x.isEmpty()) return "easy";
        // accept "Easy"/"EASY"
        if (x.equals("e")) return "easy";
        if (x.equals("m")) return "medium";
        if (x.equals("h")) return "hard";
        if (x.equals("p")) return "pro";
        return x;
    }

    private Integer safeInt(String s) {
        try { return Integer.valueOf(s.trim()); }
        catch (Exception e) { return 0; }
    }

    private char safeChar(String s, char def) {
        if (s == null) return def;
        String t = s.trim();
        if (t.isEmpty()) return def;
        return Character.toUpperCase(t.charAt(0));
    }

    private int nvl(Integer x, int def) {
        return x == null ? def : x;
    }

    private List<String> parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);

            if (ch == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    cur.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (ch == ',' && !inQuotes) {
                result.add(cur.toString().trim());
                cur.setLength(0);
            } else {
                cur.append(ch);
            }
        }
        result.add(cur.toString().trim());
        return result;
    }
    
    private void syncToSysData() {
        Model.SysData.clear();
        for (Question q : questions) {
            Model.SysData.addQuestion(q);
        }
        Model.SysData.saveToCsv();
    }


    private String csvEscape(String s) {
        if (s == null) return "";
        boolean mustQuote = s.contains(",") || s.contains("\"") || s.contains("\n");
        String x = s.replace("\"", "\"\"");
        return mustQuote ? "\"" + x + "\"" : x;
>>>>>>> parent of 6770128 (adding Id to questions)
    }

    private int parseIntOrDefault(String s, int def) {
        try { return Integer.parseInt(s.trim()); }
        catch (Exception e) { return def; }
    }

    private int nvl(Integer x, int def) {
        return x == null ? def : x;
    }

    private String csvEscape(String s) {
        if (s == null) return "";
        boolean mustQuote = s.contains(",") || s.contains("\"") || s.contains("\n");
        String x = s.replace("\"", "\"\"");
        return mustQuote ? "\"" + x + "\"" : x;
>>>>>>> 445d8746fcce5987b4933933532e178612f127f5
    }
    
    
}
