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
        return new ArrayList<>(questions);
    }

    @Override
    public void addQuestion(Question q) {
        questions.add(q);
    }

    @Override
    public void updateQuestionAtIndex(int index, Question q) {
        if (index < 0 || index >= questions.size())
            throw new IndexOutOfBoundsException("Invalid row index: " + index);
        questions.set(index, q);
    }

    @Override
    public void deleteQuestionAtIndex(int index) {
        if (index < 0 || index >= questions.size())
            throw new IndexOutOfBoundsException("Invalid row index: " + index);
        questions.remove(index);
    }

    @Override
    public void importFromCsv(File file) throws Exception {
        try (InputStream in = new FileInputStream(file)) {
            importFromStream(in);
        }
    }

    @Override
    public void exportToCsv(File file) throws Exception {
        try (OutputStream out = new FileOutputStream(file)) {
            exportToStream(out);
        }
    }

    // =========================
    // Resource helper
    // =========================

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
                        csvEscape(q.getOptA()) + "," +
                        csvEscape(q.getOptB()) + "," +
                        csvEscape(q.getOptC()) + "," +
                        csvEscape(q.getOptD()) + "," +
                        csvEscape(String.valueOf(q.getCorrect())) + "," +
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

    private String csvEscape(String s) {
        if (s == null) return "";
        boolean mustQuote = s.contains(",") || s.contains("\"") || s.contains("\n");
        String x = s.replace("\"", "\"\"");
        return mustQuote ? "\"" + x + "\"" : x;
    }
}
