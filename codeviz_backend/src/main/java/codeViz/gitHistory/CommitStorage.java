package codeViz.gitHistory;

import codeViz.entity.ClassEntity;

import java.util.LinkedHashSet;
import java.util.Set;

public class CommitStorage {

    private final String id;
    private final Set<CommitInfo> commitInfos;
    private final Set<ClassEntity> classEntities;

    public CommitStorage(String id){
        this.id = id;
        this.commitInfos = new LinkedHashSet<>();
        this.classEntities = new LinkedHashSet<>();
    }

    public void addCommitInfo(CommitInfo commitInfo){
        commitInfos.add(commitInfo);
    }

    public void addClassEntity(ClassEntity classEntity){
        this.classEntities.add(classEntity);
    }

    public String getId() {
        return id;
    }

    public Set<ClassEntity> getClassEntities() {
        return classEntities;
    }

    public Set<CommitInfo> getCommitInfos() {
        return commitInfos;
    }
}
