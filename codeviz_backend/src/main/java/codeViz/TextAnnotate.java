package codeViz;

public enum TextAnnotate {
    NEW_LINE("\n", "<br>"),
    RESET("\u001B[0m", "</font>"),
    GREEN("\u001B[32m", "<font color=\"green\">"),
    RED("\u001B[31m", "<font color=\"red\">"),
    BOLD("\033[0;1m", "<b>"),
    BOLD_OFF("\0", "</b>");

    public final String javaText;
    public final String htmlText;

    private TextAnnotate(String javaText, String htmlText) {
        this.javaText = javaText;
        this.htmlText = htmlText;
    }

    public String getJavaText() {
        return javaText;
    }

    public String getHtmlText() {
        return htmlText;
    }

    public static String javaToHtml(String string){
        string = string.replace(TextAnnotate.NEW_LINE.getJavaText(), TextAnnotate.NEW_LINE.getHtmlText());
        string = string.replace(TextAnnotate.RED.getJavaText(), TextAnnotate.RED.getHtmlText());
        string = string.replace(TextAnnotate.GREEN.getJavaText(), TextAnnotate.GREEN.getHtmlText());
        string = string.replace(TextAnnotate.RESET.getJavaText(), TextAnnotate.RESET.getHtmlText());
        string = string.replace(TextAnnotate.BOLD.getJavaText(), TextAnnotate.BOLD.getHtmlText());
        string = string.replace(TextAnnotate.BOLD_OFF.getJavaText(), TextAnnotate.BOLD_OFF.getHtmlText());
        return string;
    }
    
   
}
