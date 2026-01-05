package Model;

import java.util.Locale;
import java.util.ResourceBundle;

public class l18n {
    private ResourceBundle bundle;
    private Language lang;

    public l18n(Language lang) {
        setLanguage(lang);
    }

    public void setLanguage(Language lang) {
        this.lang = lang;
        Locale locale = (lang == Language.HE) ? new Locale("he") : Locale.ENGLISH;
        bundle = ResourceBundle.getBundle("resources.i18n.messages", locale);
    }

    public String t(String key) {
        return bundle.getString(key);
    }

    public boolean isHebrew() {
        return lang == Language.HE;
    }
}
