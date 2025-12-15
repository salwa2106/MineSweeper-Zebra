package Model;

import java.util.EnumSet;
import java.util.Set;

public class QuestionSettings {

    public enum QDiff { EASY, MEDIUM, HARD, PRO }

    private boolean questionsEnabled = true;

    // Which question difficulties are allowed
    private Set<QDiff> allowed = EnumSet.of(QDiff.EASY, QDiff.MEDIUM, QDiff.HARD, QDiff.PRO);

    // Override activation cost (optional). If false → use your current cost by game difficulty.
    private boolean overrideActivationCost = false;
    private int activationCost = 5;

    // Limit question activations per game (optional)
    private boolean limitPerGame = false;
    private int maxActivations = 10;

    public boolean isQuestionsEnabled() { return questionsEnabled; }
    public void setQuestionsEnabled(boolean questionsEnabled) { this.questionsEnabled = questionsEnabled; }

    public Set<QDiff> getAllowed() { return allowed; }
    public void setAllowed(Set<QDiff> allowed) { this.allowed = allowed; }

    public boolean isOverrideActivationCost() { return overrideActivationCost; }
    public void setOverrideActivationCost(boolean overrideActivationCost) { this.overrideActivationCost = overrideActivationCost; }

    public int getActivationCost() { return activationCost; }
    public void setActivationCost(int activationCost) { this.activationCost = activationCost; }

    public boolean isLimitPerGame() { return limitPerGame; }
    public void setLimitPerGame(boolean limitPerGame) { this.limitPerGame = limitPerGame; }

    public int getMaxActivations() { return maxActivations; }
    public void setMaxActivations(int maxActivations) { this.maxActivations = maxActivations; }
}
