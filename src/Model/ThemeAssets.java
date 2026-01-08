package Model;

public class ThemeAssets {

    public final String bg;
    public final String tileNormal;
    public final String tileHover;
    public final String tileAlt;
    public final String flag;
    public final String mine;
    public final String question;
    public final String surprise;
    public final String heartFull;
    public final String heartEmpty;
    public final String refresh;
    public String music;

    public ThemeAssets(
            String bg,
            String tileNormal,
            String tileHover,
            String tileAlt,
            String flag,
            String mine,
            String question,
            String surprise,
            String heartFull,
            String heartEmpty,
            String refresh,
            String music
    ) {
        this.bg = bg;
        this.tileNormal = tileNormal;
        this.tileHover = tileHover;
        this.tileAlt = tileAlt;
        this.flag = flag;
        this.mine = mine;
        this.question = question;
        this.surprise = surprise;
        this.heartFull = heartFull;
        this.heartEmpty = heartEmpty;
        this.refresh = refresh;
        this.music=music;
    }
}
