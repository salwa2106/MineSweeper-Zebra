package Controller;

import Model.GameSettings;
import Model.QuestionSettings;

public class SettingsController {

    private final GameSettings gameSettings = new GameSettings();
    private final QuestionSettings questionSettings = new QuestionSettings();

    public GameSettings getGameSettings() { return gameSettings; }
    public QuestionSettings getQuestionSettings() { return questionSettings; }
}
