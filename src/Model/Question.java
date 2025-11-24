package Model;

/**
 * One trivia question loaded from the CSV.
 */
public class Question {

    private String text;
    private String optA;
    private String optB;
    private String optC;
    private String optD;
    private char   correct;      // 'A' / 'B' / 'C' / 'D'
    private Integer pointsRight; // can be null => default
    private Integer pointsWrong; // can be null => default
    private Integer lifeDelta;   // can be null => default

    public Question() {
    }

    public Question(String text, String optA, String optB, String optC, String optD,
                    char correct, Integer pointsRight, Integer pointsWrong,
                    Integer lifeDelta) {
        this.text = text;
        this.optA = optA;
        this.optB = optB;
        this.optC = optC;
        this.optD = optD;
        this.correct = Character.toUpperCase(correct);
        this.pointsRight = pointsRight;
        this.pointsWrong = pointsWrong;
        this.lifeDelta = lifeDelta;
    }

    // Getters
    public String getText()        { return text; }
    public String getOptA()        { return optA; }
    public String getOptB()        { return optB; }
    public String getOptC()        { return optC; }
    public String getOptD()        { return optD; }
    public char   getCorrect()     { return correct; }
    public Integer getPointsRight(){ return pointsRight; }
    public Integer getPointsWrong(){ return pointsWrong; }
    public Integer getLifeDelta()  { return lifeDelta; }

    // Setters (used by wizard or if you want)
    public void setText(String text)              { this.text = text; }
    public void setOptA(String optA)              { this.optA = optA; }
    public void setOptB(String optB)              { this.optB = optB; }
    public void setOptC(String optC)              { this.optC = optC; }
    public void setOptD(String optD)              { this.optD = optD; }
    public void setCorrect(char correct)          { this.correct = Character.toUpperCase(correct); }
    public void setPointsRight(Integer pointsRight){ this.pointsRight = pointsRight; }
    public void setPointsWrong(Integer pointsWrong){ this.pointsWrong = pointsWrong; }
    public void setLifeDelta(Integer lifeDelta)   { this.lifeDelta = lifeDelta; }
}
