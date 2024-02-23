package codeViz.gitHistory;

import codeViz.entity.ClassEntity;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.Map;

public class CommitInfo {

    private final String id;
    private final Map<ClassEntity, CommitDiffInfo> classesAndCommits; //stores the weight of connections

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
        this.classesAndCommits = new LinkedHashMap<>();

        this.author = author;

        // Convert epoch time to Java LocalDateTime
        Instant instant = Instant.ofEpochMilli(date * 1000L);
        this.date = instant.atZone(ZoneId.systemDefault()).toLocalDateTime();

        this.message = message;
    }

    public void addClassCommitPair(ClassEntity classEntity, CommitDiffInfo commitDiffInfo){
        classesAndCommits.put(classEntity, commitDiffInfo);
    }

    public String getId() {
        return id;
    }

    public Map<ClassEntity, CommitDiffInfo> getClassesAndCommits() {
        return classesAndCommits;
    }

    public boolean containsClass(ClassEntity classEntity){
        return classesAndCommits.containsKey(classEntity);
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
}
