package Model;

/**
 * One trivia question loaded from the CSV.
 */
public class Question {
    private int    id;
    private String text;
    private String optA;
    private String optB;
    private String optC;
    private String optD;
    private char   correct;      // 'A' / 'B' / 'C' / 'D'
    private Integer pointsRight; 
    private Integer pointsWrong; 
    private Integer lifeDelta;  
    private String difficulty;   // "easy", "medium", "hard", "pro"

    public Question() {}

    public Question(int id, String text, String optA, String optB, String optC, String optD,
            char correct, Integer pointsRight, Integer pointsWrong,
            Integer lifeDelta, String difficulty) {
this.id = id;
this.text = text;
this.optA = optA;
this.optB = optB;
this.optC = optC;
this.optD = optD;
this.correct = Character.toUpperCase(correct);
this.pointsRight = pointsRight;
this.pointsWrong = pointsWrong;
this.lifeDelta = lifeDelta;
this.difficulty = difficulty;
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
    public String getDifficulty() {
        return difficulty;
    }


    // Setters
    public void setText(String text) { this.text = text; }
    public void setOptA(String optA) { this.optA = optA; }
    public void setOptB(String optB) { this.optB = optB; }
    public void setOptC(String optC) { this.optC = optC; }
    public void setOptD(String optD) { this.optD = optD; }
    public void setCorrect(char correct) {
        this.correct = Character.toUpperCase(correct);
    }
    public void setPointsRight(Integer pointsRight){ this.pointsRight = pointsRight; }
    public void setPointsWrong(Integer pointsWrong){ this.pointsWrong = pointsWrong; }
    public void setLifeDelta(Integer lifeDelta) { this.lifeDelta = lifeDelta; }
    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}
