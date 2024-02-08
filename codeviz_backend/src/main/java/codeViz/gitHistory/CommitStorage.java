package codeViz.gitHistory;

import codeViz.entity.ClassEntity;

import java.util.LinkedHashMap;
import java.util.Map;

public class CommitStorage {

    private final String id;
    private final Map<ClassEntity, CommitInfo> classesAndCommits; //stores the weight of connections


    public CommitStorage(String id){
        this.id = id;
        this.classesAndCommits = new LinkedHashMap<>();
    }

    public void addClassCommitPair(ClassEntity classEntity, CommitInfo commitInfo){
        classesAndCommits.put(classEntity, commitInfo);
    }

    public String getId() {
        return id;
    }

    public Map<ClassEntity, CommitInfo> getClassesAndCommits() {
        return classesAndCommits;
    }
}
