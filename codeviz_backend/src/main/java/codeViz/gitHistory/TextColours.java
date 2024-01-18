package codeViz.gitHistory;

public enum TextColours {
    RESET("\u001B[0m", "</font>"),
    GREEN("\u001B[32m", "<font color=\"green\">"),
    RED("\u001B[31m", "<font color=\"red\">");

    public final String ansiColour;
    public final String fontColour;

    private TextColours(String ansiColour, String fontColour) {
        this.ansiColour = ansiColour;
        this.fontColour = fontColour;
    }

    public String getAnsiColour() {
        return ansiColour;
    }

    public String getFontColour() {
        return fontColour;
    }
}
