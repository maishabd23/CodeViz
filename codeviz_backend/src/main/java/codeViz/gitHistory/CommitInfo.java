package codeViz.gitHistory;

import codeViz.entity.ClassEntity;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Set;

public class CommitInfo {

    private final String id;
    private final Set<ClassEntity> classes; //stores the weight of connections

    private final String author;
    private final LocalDateTime date;
    private final String message;

    /**
     * Create new Commit Storage
     * @param id                commit id
     * @param author            author of the commit
     * @param date              date of the commit, as epoch time
     * @param message           commit message
     */
    public CommitInfo(
            String id,
            String author,
            long date,
            String message
    ){
        this.id = id;
        this.classes = new HashSet<>();

        this.author = author;

        // Convert epoch time to Java LocalDateTime
        Instant instant = Instant.ofEpochMilli(date * 1000L);
        this.date = instant.atZone(ZoneId.systemDefault()).toLocalDateTime();

        this.message = message;
    }

    public void addClass(ClassEntity classEntity){
        classes.add(classEntity);
    }

    public String getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public Set<ClassEntity> getClasses() {
        return classes;
    }

    public boolean containsClass(ClassEntity classEntity){
        return classes.contains(classEntity);
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
        // follow the format of the "git log" display
        return "Commit " + id  + "\n" +
                "Author: " + author + "\n" +
                "Date: " + dateToString() + "\n" +
                message
                ;
    }

    /**
     * Determine commit type based on filenames
     * @param previousFilename      previous filename
     * @param newFilename           new filename
     * @return  commit type
     */
    public static CommitType determineCommitType(String previousFilename, String newFilename) {
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
}
