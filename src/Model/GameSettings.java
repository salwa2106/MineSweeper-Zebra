package Model;

public class GameSettings {
    private boolean soundEnabled = true;
    private boolean animationsEnabled = true;
    private Difficulty defaultDifficulty = Difficulty.EASY;
    private int maxSharedLives = 10;
    private boolean autoSaveHistory = true;

    public boolean isSoundEnabled() { return soundEnabled; }
    public void setSoundEnabled(boolean soundEnabled) { this.soundEnabled = soundEnabled; }

    public boolean isAnimationsEnabled() { return animationsEnabled; }
    public void setAnimationsEnabled(boolean animationsEnabled) { this.animationsEnabled = animationsEnabled; }

    public Difficulty getDefaultDifficulty() { return defaultDifficulty; }
    public void setDefaultDifficulty(Difficulty defaultDifficulty) { this.defaultDifficulty = defaultDifficulty; }

    public int getMaxSharedLives() { return maxSharedLives; }
    public void setMaxSharedLives(int maxSharedLives) { this.maxSharedLives = maxSharedLives; }

    public boolean isAutoSaveHistory() { return autoSaveHistory; }
    public void setAutoSaveHistory(boolean autoSaveHistory) { this.autoSaveHistory = autoSaveHistory; }
}
