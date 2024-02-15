package codeViz.gitHistory;

import codeViz.entity.ClassEntity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class GitDiffAssociationRules {
    private final Map<ClassEntity, Integer> classCounts;
    private ArrayList<CommitStorage> commitStorages;
    private int totalFileChangeCount;

    public GitDiffAssociationRules(){
        this.classCounts = new LinkedHashMap<>();
        this.commitStorages = new ArrayList<>();
        this.totalFileChangeCount = 0;
    }

    public void addClassEntity(ClassEntity classEntity){
        int initialWeight = classCounts.getOrDefault(classEntity, 0);
        //System.out.println(initialWeight);
        classCounts.put(classEntity, initialWeight + 1);
        totalFileChangeCount += 1;
    }

    public void addCommitStorage(CommitStorage commitStorage){
        commitStorages.add(commitStorage);
    }

    public float calculateConfidence(ClassEntity classEntityA, ClassEntity classEntityB){
        float pA = (float) classCounts.getOrDefault(classEntityA, 0) / totalFileChangeCount;
        float countAorB = 0;

        for (CommitStorage commitStorage : commitStorages){
            if (commitStorage.containsClass(classEntityA) || commitStorage.containsClass(classEntityB)){
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

    public ArrayList<CommitStorage> getCommitStorages() {
        return commitStorages;
    }
}
