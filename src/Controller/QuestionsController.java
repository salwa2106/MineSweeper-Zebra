package Controller;

import Model.Question;
import Model.SysData;
import View.QuestionsWizardFrame;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class QuestionsController implements QuestionsWizardFrame.QuestionsController {

    @Override
    public List<Question> getAllQuestions() {
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
      
    }

    @Override
    public void deleteQuestionAtIndex(int index) {
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
    }

    @Override
    public void importFromCsv(File file) throws Exception {
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
    }

    @Override
    public void exportToCsv(File file) throws Exception {
<<<<<<< HEAD
        SysData.saveToCsv();

        File source = new File(
                SysData.class
                        .getProtectionDomain()
                        .getCodeSource()
                        .getLocation()
                        .getPath()
                        .replace("/bin", "")
                        + "/src/resources/questions/questionsCell.csv"
        );

        Files.copy(source.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
=======
        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {

            bw.write("Question,OptA,OptB,OptC,OptD,Correct,PointsRight,PointsWrong,LifeDelta,Difficulty");
            bw.newLine();

            for (Question q : SysData.getQuestions()) {
                String diff = (q.getDifficulty() == null || q.getDifficulty().isBlank()) ? "easy" : q.getDifficulty();

                bw.write(csvEscape(q.getText()) + "," +
                        csvEscape(q.getOptA()) + "," +
                        csvEscape(q.getOptB()) + "," +
                        csvEscape(q.getOptC()) + "," +
                        csvEscape(q.getOptD()) + "," +
                        csvEscape(String.valueOf(q.getCorrect())) + "," +
                        csvEscape(String.valueOf(nvl(q.getPointsRight(), 3))) + "," +
                        csvEscape(String.valueOf(nvl(q.getPointsWrong(), -1))) + "," +
                        csvEscape(String.valueOf(nvl(q.getLifeDelta(), 1))) + "," +
                        csvEscape(diff));
                bw.newLine();
            }
        }
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
