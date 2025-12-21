package Controller;

import Model.Difficulty;
import Model.GameSettings;
import Model.QuestionSettings;

public class SettingsController {

    private final GameSettings gameSettings = new GameSettings();
    private final QuestionSettings questionSettings = new QuestionSettings();

    // ----- expose models if you want (read-only usage ideally)
    public GameSettings getGameSettings() { return gameSettings; }
    public QuestionSettings getQuestionSettings() { return questionSettings; }

    // ===================== GAME SETTINGS (MVC) =====================
    public boolean isSoundEnabled() { return gameSettings.isSoundEnabled(); }
    public void setSoundEnabled(boolean v) { gameSettings.setSoundEnabled(v); }

    public boolean isAnimationsEnabled() { return gameSettings.isAnimationsEnabled(); }
    public void setAnimationsEnabled(boolean v) { gameSettings.setAnimationsEnabled(v); }

    public boolean isAutoSaveHistory() { return gameSettings.isAutoSaveHistory(); }
    public void setAutoSaveHistory(boolean v) { gameSettings.setAutoSaveHistory(v); }

    public Difficulty getDefaultDifficulty() { return gameSettings.getDefaultDifficulty(); }
    public void setDefaultDifficulty(Difficulty d) {
        if (d == null) d = Difficulty.EASY;
        gameSettings.setDefaultDifficulty(d);
    }

    public int getMaxSharedLives() { return gameSettings.getMaxSharedLives(); }
    public void setMaxSharedLives(int lives) {
        // clamp to UI limits (your slider is 1..10)
        int v = Math.max(1, Math.min(10, lives));
        gameSettings.setMaxSharedLives(v);
    }

    // ===================== QUESTION SETTINGS (MVC) =====================
    public boolean isQuestionsEnabled() { return questionSettings.isQuestionsEnabled(); }
    public void setQuestionsEnabled(boolean v) { questionSettings.setQuestionsEnabled(v); }

    public int getActivationCost() { return questionSettings.getActivationCost(); }
    public void setActivationCost(int v) { questionSettings.setActivationCost(Math.max(0, v)); }

    public boolean isOverrideActivationCost() { return questionSettings.isOverrideActivationCost(); }
    public void setOverrideActivationCost(boolean v) { questionSettings.setOverrideActivationCost(v); }

    public boolean isLimitPerGame() { return questionSettings.isLimitPerGame(); }
    public void setLimitPerGame(boolean v) { questionSettings.setLimitPerGame(v); }

    public int getMaxActivations() { return questionSettings.getMaxActivations(); }
    public void setMaxActivations(int v) { questionSettings.setMaxActivations(Math.max(1, v)); }
}
