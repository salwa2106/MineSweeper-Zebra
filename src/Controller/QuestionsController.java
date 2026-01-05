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
        // Always reflect current SysData state
        return new ArrayList<>(SysData.getQuestions());
    }

    @Override
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
    }

    @Override
    public void importFromCsv(File file) throws Exception {
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
    }

    @Override
    public void exportToCsv(File file) throws Exception {
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
    }
}
