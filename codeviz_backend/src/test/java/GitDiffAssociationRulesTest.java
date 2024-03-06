import codeViz.entity.ClassEntity;
import codeViz.gitHistory.CommitDiffInfo;
import codeViz.gitHistory.CommitInfo;
import codeViz.gitHistory.GitDiffAssociationRules;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GitDiffAssociationRulesTest {

    @Test
    public void testAssociationRules() {
        GitDiffAssociationRules gitDiffAssociationRules = new GitDiffAssociationRules();

        CommitInfo commitInfo1, commitInfo2, commitInfo3, commitInfo4, commitInfo5;

        commitInfo1 = new CommitInfo("0001", "testUser", 1707210179, "commit 1");
        commitInfo2 = new CommitInfo("0002", "testUser", 1678783231, "commit 2");
        commitInfo3 = new CommitInfo("0003", "testUser", 1581858671, "commit 3");
        commitInfo4 = new CommitInfo("0004", "testUser", 1581855300, "commit 4");
        commitInfo5 = new CommitInfo("0005", "testUser", 1515525693, "commit 5");


        gitDiffAssociationRules.addCommitInfo(commitInfo1);
        gitDiffAssociationRules.addCommitInfo(commitInfo2);
        gitDiffAssociationRules.addCommitInfo(commitInfo3);
        gitDiffAssociationRules.addCommitInfo(commitInfo4);
        gitDiffAssociationRules.addCommitInfo(commitInfo5);

        ClassEntity classEntityA, classEntityB, classEntityC, classEntityD;

        classEntityA = new ClassEntity("ClassA");
        classEntityB = new ClassEntity("ClassB");
        classEntityC = new ClassEntity("ClassC");
        classEntityD = new ClassEntity("ClassD");

        CommitDiffInfo dummyCommitDiffInfo = new CommitDiffInfo("001", "testUser", 1515525693, "commit", "className", "className", "");

        commitInfo1.addClassCommitPair(classEntityA, dummyCommitDiffInfo);
        gitDiffAssociationRules.addClassEntity(classEntityA);
        commitInfo1.addClassCommitPair(classEntityD, dummyCommitDiffInfo);
        gitDiffAssociationRules.addClassEntity(classEntityD);

        commitInfo2.addClassCommitPair(classEntityB, dummyCommitDiffInfo);
        gitDiffAssociationRules.addClassEntity(classEntityB);

        commitInfo3.addClassCommitPair(classEntityC, dummyCommitDiffInfo);
        gitDiffAssociationRules.addClassEntity(classEntityC);

        commitInfo4.addClassCommitPair(classEntityA, dummyCommitDiffInfo);
        gitDiffAssociationRules.addClassEntity(classEntityA);
        commitInfo4.addClassCommitPair(classEntityB, dummyCommitDiffInfo);
        gitDiffAssociationRules.addClassEntity(classEntityB);

        commitInfo5.addClassCommitPair(classEntityA, dummyCommitDiffInfo);
        gitDiffAssociationRules.addClassEntity(classEntityA);
        commitInfo5.addClassCommitPair(classEntityC, dummyCommitDiffInfo);
        gitDiffAssociationRules.addClassEntity(classEntityC);

        // float filesChanged = 3 + 2*2; // not actually needed
        // association rules formula = pAorB / pA = countAorB / countA

        assertEquals( (float) 4 / 3, gitDiffAssociationRules.calculateConfidence(classEntityA, classEntityB));
        assertEquals( (float) 4 / 3, gitDiffAssociationRules.calculateConfidence(classEntityA, classEntityC));

        assertEquals( (float) 4 / 2, gitDiffAssociationRules.calculateConfidence(classEntityB, classEntityA));
        assertEquals( (float) 4 / 2, gitDiffAssociationRules.calculateConfidence(classEntityC, classEntityA));
        assertEquals( (float) 4 / 2, gitDiffAssociationRules.calculateConfidence(classEntityB, classEntityC));
        assertEquals( (float) 4 / 2, gitDiffAssociationRules.calculateConfidence(classEntityC, classEntityB));

        assertEquals( (float) 3 / 3, gitDiffAssociationRules.calculateConfidence(classEntityA, classEntityD));
        assertEquals( (float) 3, gitDiffAssociationRules.calculateConfidence(classEntityD, classEntityA)); // classEntityD only appears once
    }
}
