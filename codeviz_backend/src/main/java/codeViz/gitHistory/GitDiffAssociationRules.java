package codeViz.gitHistory;

import codeViz.entity.ClassEntity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class GitDiffAssociationRules {
    private final Map<ClassEntity, Integer> classCounts;
    private final ArrayList<CommitInfo> commitInfos;
    private int totalFileChangeCount;

    public GitDiffAssociationRules(){
        this.classCounts = new LinkedHashMap<>();
        this.commitInfos = new ArrayList<>();
        this.totalFileChangeCount = 0;
    }

    public void addClassEntity(ClassEntity classEntity){
        int initialWeight = classCounts.getOrDefault(classEntity, 0);
        //System.out.println(initialWeight);
        classCounts.put(classEntity, initialWeight + 1);
        totalFileChangeCount += 1;
    }

    public void addCommitInfo(CommitInfo commitInfo){
        commitInfos.add(commitInfo);
    }

    public float calculateConfidence(ClassEntity classEntityA, ClassEntity classEntityB){
        float pA = (float) classCounts.getOrDefault(classEntityA, 0) / totalFileChangeCount;
        float countAorB = 0;

        for (CommitInfo commitInfo : commitInfos){
            if (commitInfo.containsClass(classEntityA) || commitInfo.containsClass(classEntityB)){
                countAorB += 1;
            }
        }

        float pAorB = countAorB / totalFileChangeCount;

        if (pA == 0 || pAorB == 0){
            return 0;
        }

        float confidence = pAorB / pA;
        System.out.println("A: " + classEntityA.getName() + " and B: " + classEntityB.getName() + " confidence: " + confidence);

        return confidence;
    }

    public ArrayList<CommitInfo> getCommitInfos() {
        return commitInfos;
    }
}
