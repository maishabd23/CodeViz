package codeViz;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

public class CommitInformation {
    private final String id;
    private final String author;
    private final String message;
    private CommitInformation previousCommit;
    private final String diff;
    private final String previousFilename; // TODO - handle file renames // TODO handle deletes
    private final String newFilename;
    private final CommitType commitType;

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_RED = "\u001B[31m";

    public CommitInformation(
            String id,
            String author,
            String previousFilename, String newFilename, CommitType commitType, String message,
            String diff
    ){
        this.id = id;
        this.author = author;
        this.message = message;
        this.diff = setDiffColours(diff);
        this.previousFilename = previousFilename;
        this.newFilename = newFilename;
        this.commitType = commitType;
    }

    public void setPreviousCommit(CommitInformation previousCommit) {
        this.previousCommit = previousCommit;
    }

    private String setDiffColours(String diff){

        StringBuilder newDiff = new StringBuilder();
        BufferedReader bufReader = new BufferedReader(new StringReader(diff));

        String line;
        while(true)
        {
            try {
                if ((line = bufReader.readLine()) == null) break;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            if (line.startsWith("+")){
                line = ANSI_GREEN + line + "\n";
            } else if (line.startsWith("-")){
                line = ANSI_RED + line + "\n";
            } else {
                line = ANSI_RESET + line + "\n";
            }

            newDiff.append(line);

        }

        newDiff.append(ANSI_RESET);

        return newDiff.toString();
    }

    @Override
    public String toString() {
        return "CommitInformation{" +
                "id='" + id + '\'' + "\n" +
                ", previousCommit=" + previousCommit + "\n" +
                ", author='" + author + '\'' + "\n" +
                ", previousFilename='" + previousFilename + '\'' + "\n" +
                ", newFilename='" + newFilename + '\'' + "\n" +
                ", commitType='" + commitType + '\'' + "\n" +
                ", message='" + message + '\'' + "\n" +
                ", diff='\n" + diff + '\'' + "\n" +
                '}';
    }
}
