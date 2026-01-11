package Model;

public enum Language {
    EN, HE;

    @Override
    public String toString() {
        return SysData.getI18n().t(
            this == EN ? "lang.en" : "lang.he"
        );
    }
}
