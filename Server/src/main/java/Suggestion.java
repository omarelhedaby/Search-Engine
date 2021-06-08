public class Suggestion {

    private String text;
    private int score;

    Suggestion(String sug,int sco)
    {
        this.text=sug;
        this.score=sco;
    }

    public int getScore()
    {
        return this.score;
    }

    public String getText()
    {
        return this.text;
    }
}
