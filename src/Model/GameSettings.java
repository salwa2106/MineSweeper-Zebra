package Model;

public class GameSettings {
    private boolean soundEnabled = true;
    private Difficulty defaultDifficulty = Difficulty.EASY;
    private int maxSharedLives = 10;
    private boolean autoSaveHistory = true;
    private Language language = Language.EN;
    private AppTheme theme = AppTheme.DARK;
    


    public boolean isSoundEnabled() { return soundEnabled; }
    public void setSoundEnabled(boolean soundEnabled) { this.soundEnabled = soundEnabled; }

    public Difficulty getDefaultDifficulty() { return defaultDifficulty; }
    public void setDefaultDifficulty(Difficulty defaultDifficulty) { this.defaultDifficulty = defaultDifficulty; }

    public int getMaxSharedLives() { return maxSharedLives; }
    public void setMaxSharedLives(int maxSharedLives) { this.maxSharedLives = maxSharedLives; }

    public boolean isAutoSaveHistory() { return autoSaveHistory; }
    public void setAutoSaveHistory(boolean autoSaveHistory) { this.autoSaveHistory = autoSaveHistory; }
    
    // ---------- Language ----------
    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        if (language == null) language = Language.EN;
        this.language = language;
    }

    // ---------- Theme ----------
    public AppTheme getTheme() {
        return theme;
    }

    public void setTheme(AppTheme theme) {
        if (theme == null) theme = AppTheme.DARK;
        this.theme = theme;
    }
	
}
