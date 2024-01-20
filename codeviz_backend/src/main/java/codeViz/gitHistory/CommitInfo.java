package codeViz.gitHistory;

import codeViz.TextAnnotate;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Class that stores information of a git commit
 *
 * @author Thanuja Sivaananthan
 */
public class CommitInfo {
    private final String id;
    private final String author;
    private final LocalDateTime date;
    private final String message;
    private CommitInfo previousCommit;
    private final String diff;
    private final String previousFilename; // TODO - handle file renames // TODO handle deletes
    private final String newFilename;
    private final CommitType commitType;

    /**
     *
     * @param id                commit id
     * @param author            author of the commit
     * @param date              date of the commit, as epoch time
     * @param message           commit message
     * @param previousFilename  previous filename
     * @param newFilename       new filename
     * @param diff              diff
     */
    public CommitInfo(
            String id,
            String author,
            long date,
            String message,
            String previousFilename, String newFilename,
            String diff
    ){
        this.id = id;
        this.author = author;

        // Convert epoch time to Java LocalDateTime
        Instant instant = Instant.ofEpochMilli(date * 1000L);
        this.date = instant.atZone(ZoneId.systemDefault()).toLocalDateTime();

        this.message = message;

        this.commitType = determineCommitType(previousFilename, newFilename);
        this.previousFilename = previousFilename;
        this.newFilename = newFilename;

        this.diff = setDiffColours(diff);
    }

    /**
     * Determine commit type based on filenames
     * @param previousFilename      previous filename
     * @param newFilename           new filename
     * @return  commit type
     */
    private CommitType determineCommitType(String previousFilename, String newFilename) {
        CommitType commitType;
        if (previousFilename.equals(newFilename)) {
            //System.out.println("Filename: " + entry.getNewPath());
            commitType = CommitType.EDIT;
        } else if (previousFilename.equals("/dev/null")){
            //System.out.println("Newly created Filename: " + newFilename);
            commitType = CommitType.CREATE;
        } else if (newFilename.equals("/dev/null")){
            //System.out.println("Deleted Filename: " + previousFilename);
            commitType = CommitType.DELETE;
        } else {
            // renamed file
            //System.out.println("Old Filename: " + previousFilename);
            //System.out.println("New Filename: " + newFilename);
            commitType = CommitType.RENAME;
        }
        return commitType;
    }

    public void setPreviousCommit(CommitInfo previousCommit) {
        this.previousCommit = previousCommit;
    }

    public String getId() {
        return id;
    }

    public CommitType getCommitType() {
        return commitType;
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
                line = TextAnnotate.GREEN.getJavaText() + line + "\n";
            } else if (line.startsWith("-")){
                line = TextAnnotate.RED.getJavaText() + line + "\n";
            } else {
                line = TextAnnotate.RESET.getJavaText() + line + "\n";
            }

            newDiff.append(line);

        }

        newDiff.append(TextAnnotate.RESET.getJavaText());

        return newDiff.toString();
    }

    /**
     * Display filename(s) based on the commit type
     * @return string with the filename(s)
     */
    private String filenameToString(){
        return switch (commitType) {
            case CREATE -> "\n, newFilename='" + newFilename + '\'';
            case EDIT -> "\n, filename='" + newFilename + '\'';
            case DELETE -> "\n, deletedFilename='" + previousFilename + '\'';
            case RENAME ->
                    "\n, previousFilename='" + previousFilename + '\'' + "\n, newFilename='" + newFilename + '\'';
        };
    }

    /**
     * Display date in gitHub date format
     * @return  date in the format: Week Month DD HH:MM:SS YYYY
     */
    private String dateToString(){
        String weekday = date.getDayOfWeek().name().substring(0,1).toUpperCase() + date.getDayOfWeek().name().substring(1).toLowerCase();
        String month = date.getMonth().name().substring(0,1).toUpperCase() + date.getMonth().name().substring(1).toLowerCase();
        return weekday + " " + month + " " + date.getDayOfMonth() + " " +
                date.getHour() + ":" + date.getMinute() + ":" + date.getSecond() + " " +
                date.getYear();
    }

    @Override
    public String toString() {
        String previousCommitId = null;
        if (previousCommit != null) previousCommitId = previousCommit.getId();
        return "CommitInfo{" +
                "id='" + id + '\''  +
                //"\n, previousCommit=" + previousCommitId +
                "\n, author='" + author + '\'' +
                "\n, date='" + dateToString() + '\'' +
                "\n, message='" + message + '\'' +

                "\n, commitType='" + commitType + '\'' +
                filenameToString() +
                "\n, diff='\n" + diff + '\'' + "\n" +
                '}';
    }
}