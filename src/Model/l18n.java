package Model;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

public class l18n {
    private ResourceBundle bundle;
    private Language lang;

    public l18n(Language lang) {
        setLanguage(lang);
    }

    public void setLanguage(Language lang) {
        this.lang = (lang == null) ? Language.EN : lang;

        Locale locale = (this.lang == Language.HE) ? new Locale("he") : Locale.ENGLISH;

        // ✅ Load UTF-8 properties (NOT the default ISO-8859-1)
        bundle = ResourceBundle.getBundle("resources.i18n.messages", locale, new ResourceBundle.Control() {
            @Override
            public ResourceBundle newBundle(String baseName, Locale locale, String format,
                                            ClassLoader loader, boolean reload)
                    throws java.io.IOException {

                String bundleName = toBundleName(baseName, locale);
                String resourceName = toResourceName(bundleName, "properties");

                try (InputStream is = loader.getResourceAsStream(resourceName)) {
                    if (is == null) return null;
                    return new PropertyResourceBundle(new InputStreamReader(is, StandardCharsets.UTF_8));
                }
            }
        });
    }

    public String t(String key) {
        try {
            return bundle.getString(key);
        } catch (MissingResourceException e) {
            return "!" + key + "!";
        }
    }

    public boolean isHebrew() {
        return lang == Language.HE;
    }
}
