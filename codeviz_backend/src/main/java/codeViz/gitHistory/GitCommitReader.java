package codeViz.gitHistory;

import codeViz.GraphGenerator;
import codeViz.entity.ClassEntity;
import codeViz.entity.Entity;
import org.apache.commons.collections4.keyvalue.MultiKey;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RenameDetector;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.*;
import org.eclipse.jgit.treewalk.filter.PathSuffixFilter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

/**
 * Class that reads git commit history
 *
 * @author Thanuja Sivaananthan
 */
public class GitCommitReader {

    private static final String gitCloneDirectory = "./testgithistory"; // local directory to clone into

    private Git git;
    private final GraphGenerator graphGenerator;
    private LinkedHashMap<String, String> renamedClassEntityNames;
    private GitDiffAssociationRules gitDiffAssociationRules;
    private static final int WEIGHT_ADJUSTER = 2;

    /**
     * Create GitCommitReader
     * @param graphGenerator    the graph generator to use
     */
    public GitCommitReader(GraphGenerator graphGenerator){
        this.graphGenerator = graphGenerator;
        this.renamedClassEntityNames = new LinkedHashMap<>();
        this.gitDiffAssociationRules = new GitDiffAssociationRules();
    }

    /**
     * Read commit history locally
     * @param localDirectory    the local directory to read from
     * @param maxNumCommits the number of commits to get the history from, -1 if all commits
     */
//    public void extractCommitHistory(String localDirectory, int maxNumCommits){
//        try {
//            git = Git.init().setDirectory(new File(localDirectory)).call();
//        } catch (GitAPIException e) {
//            throw new RuntimeException(e);
//        }
//        storeCommitHistory(maxNumCommits);
//    }

    /**
     * Read commit history via gitHub
     * @param gitHubURI         the URI of the gitHub repository/fork
     * @param tokenPassword     the token password of the user
     * @param maxNumCommits the number of commits to get the history from, -1 if all commits
     */
    public void extractCommitHistory(String gitHubURI, String tokenPassword, int maxNumCommits){
        // TODO - make this properly secure
        try {
            FileUtils.deleteDirectory(new File(gitCloneDirectory));
            this.git = Git.cloneRepository()
                    .setURI(gitHubURI)
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(tokenPassword, ""))
                    .setDirectory(new File(gitCloneDirectory))
                    .call();
        } catch (IOException | GitAPIException e) {
            throw new RuntimeException(e);
        }
        storeCommitHistory(maxNumCommits);
    }

    /**
     * Store the commit history in order of most recent commit to the oldest commit
     * @param maxNumCommits the number of commits to get the history from, -1 if all commits
     */
    private void storeCommitHistory(int maxNumCommits) { // Note: public methods should probably not throw exceptions
        // is an annotation - do not clear graphGenerator
        //TODO - make sure graphGenerator has commits cleared before adding a new set? or only add commits that are new
        renamedClassEntityNames.clear(); // FIXME - what if appending commits?
        gitDiffAssociationRules = new GitDiffAssociationRules();
        Iterable<RevCommit> log;
        try {
            log = git.log().call();
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
        boolean firstCommit = true;
        RevCommit nextCommit = null;
        RevCommit nextNextCommit = null;

        int numCommits = 0;

        for (RevCommit commit : log) {

            numCommits += 1;
            if (maxNumCommits > 0 && numCommits >= maxNumCommits) {
                break;
            }

            if (!firstCommit) { // means that nextCommit != null
                try {
                    this.getDiffs(commit, nextCommit, nextNextCommit);
                } catch (IOException | GitAPIException e) {
                    throw new RuntimeException(e);
                }

                //this.getCommitInfo(nextCommit);
            }

            firstCommit = false;
            nextNextCommit = nextCommit;
            nextCommit = commit;

        }

        // after storing all the commits and classes, now store the connections between classes and git commits
        // this will be used to annotate the code graph (how a change of entities is correlated)
        addGitHistoryConnections();
    }

    /**
     * Add connections between entities, based on git history
     */
    private void addGitHistoryConnections() {

        // for each pair of entities, want to store the diffs of each in a list
        // that can be used for correlation analysis after?
        MultiKeyMap classesAndDiffPairs = new MultiKeyMap(); // NOTE: has some limitations, try to only use locally
//        LinkedHashMap<ClassEntity, ClassEntity> classesPairs = new LinkedHashMap<>(); // TODO - test and switch from MultiKeyMap to this
//        ClassEntity dummyClassEntity = new ClassEntity("dummy");

        for (CommitInfo commitInfo : gitDiffAssociationRules.getCommitInfos()){
            Set<ClassEntity> classEntitySet = commitInfo.getClassesAndCommits().keySet();
            // TODO - fix size complexity - currently O(n^2), seems excessive
            for (ClassEntity outerClassEntity : classEntitySet){
                for (ClassEntity innerClassEntity : classEntitySet){
                    if (outerClassEntity.equals(innerClassEntity)){ // don't check against itself
                        continue;
                    }

                    if (!(classesAndDiffPairs.containsKey(outerClassEntity, innerClassEntity)
                        || classesAndDiffPairs.containsKey(innerClassEntity, outerClassEntity))){
                        classesAndDiffPairs.put(outerClassEntity, innerClassEntity, 0);
                    }

//                    if (!classesPairs.getOrDefault(outerClassEntity, dummyClassEntity).equals(innerClassEntity) &&
//                            !classesPairs.getOrDefault(innerClassEntity, dummyClassEntity).equals(outerClassEntity)){
//                        classesPairs.put(outerClassEntity, innerClassEntity);
//                    }
                }
            }
        }

        System.out.println("Done getting git diff pairs!");

//        for (ClassEntity classEntity1 : classesPairs.keySet()){
//            ClassEntity classEntity2 = classesPairs.get(classEntity1);
        for (Object object : classesAndDiffPairs.keySet()){
            MultiKey multiKey = (MultiKey) object;
            ClassEntity classEntity1 = (ClassEntity) multiKey.getKey(0);
            ClassEntity classEntity2 = (ClassEntity) multiKey.getKey(1);

            float confidenceA = gitDiffAssociationRules.calculateConfidence(classEntity1, classEntity2);
            float confidenceB = gitDiffAssociationRules.calculateConfidence(classEntity2, classEntity1);

            float weight = (confidenceA + confidenceB) / 2; // average?

            float adjustedWeight = weight * WEIGHT_ADJUSTER;
            System.out.println("Between " + classEntity1.getName() + " and " + classEntity2.getName() + ", weight = " + weight);
            classEntity1.addGitConnectedEntity(classEntity2, adjustedWeight);
            classEntity2.addGitConnectedEntity(classEntity1, adjustedWeight);
        }
    }

    public static int getWeightAdjuster() {
        return WEIGHT_ADJUSTER;
    }

    /**
     * Similar to the command: git diff <previous-commit> <new-commit>
     * @param previousCommit    previous commit
     * @param currentCommit     current commit
     * @param futureCommit      future commit
     */
    private void getDiffs(RevCommit previousCommit, RevCommit currentCommit, RevCommit futureCommit) throws IOException, GitAPIException {

        Repository repository = git.getRepository();

        // https://stackoverflow.com/questions/23334862/jgit-how-to-get-diff-from-multiple-revcommits
        AbstractTreeIterator oldTreeParser = prepareTreeParser(repository, previousCommit);
        AbstractTreeIterator newTreeParser = prepareTreeParser(repository, currentCommit);

        List<DiffEntry> diff = git.diff().
                setOldTree(oldTreeParser).
                setNewTree(newTreeParser).
                // to filter on Suffix
                        setPathFilter(PathSuffixFilter.create(".java")).
                call();

        RenameDetector renameDetector = new RenameDetector(repository); // TODO - test renamed files
        renameDetector.addAll(diff);
        diff = renameDetector.compute();

        CommitInfo commitInfo = new CommitInfo(
                currentCommit.getId().getName(),
                currentCommit.getAuthorIdent().getName(),
                currentCommit.getCommitTime(),
                currentCommit.getShortMessage()
        );
        gitDiffAssociationRules.addCommitInfo(commitInfo);

        for (DiffEntry entry : diff) {
            //System.out.println("Entry: " + entry + ", from: " + entry.getOldId() + ", to: " + entry.getNewId());

            if (entry.getScore() >= renameDetector.getRenameScore()) {
                System.out.println("file: " + entry.getOldPath() + " copied/moved to: " + entry.getNewPath());
                renamedClassEntityNames.put(entry.getOldPath(), entry.getNewPath()); // keep track of oldPath, a previous commit would have it as a newPath // FIXME could have duplicates
            }

            OutputStream outputStream = new ByteArrayOutputStream();
            DiffFormatter formatter = new DiffFormatter(outputStream);
            formatter.setRepository(repository);
            formatter.format(entry);
//            System.out.println(outputStream.toString());

            CommitDiffInfo commitDiffInfo = new CommitDiffInfo(
                    currentCommit.getId().getName(),
                    currentCommit.getAuthorIdent().getName(),
                    currentCommit.getCommitTime(),
                    currentCommit.getShortMessage(),
                    entry.getOldPath(), entry.getNewPath(),
                    outputStream.toString()
            );

            if (commitDiffInfo.getCommitType() != CommitType.DELETE && graphGenerator != null) { // Note: deletes will not have their code details stored
                ClassEntity classEntity = getClassEntity(entry.getNewPath());
                if (classEntity != null) {
                    classEntity.addCommitInfo(commitInfo);
                    commitInfo.addClassCommitPair(classEntity, commitDiffInfo);
                    gitDiffAssociationRules.addClassEntity(classEntity);
                }
            }

        }

        //System.out.println("===============================");

    }


    /**
     * Get the classEntity that corresponds with the filename
     * @param fullFilename      the filename of the Java Class
     * @return  the classEntity, or null if it doesn't exist in the graph generator
     */
    private ClassEntity getClassEntity(String fullFilename) {

        while (renamedClassEntityNames.containsKey(fullFilename)){ // TODO test multiple renames on a file // TODO - test with package creates/changes as well
            fullFilename = renamedClassEntityNames.get(fullFilename);
            System.out.println("renamed file, try with :" + fullFilename);
        }

        String[] fileSections = fullFilename.split("/");

        LinkedHashMap<String, Entity> packages = graphGenerator.getPackageEntities();
        LinkedHashMap<String, Entity> classes = graphGenerator.getClassEntities();

        // Note: need to iterate over each folder (fileSection)
        // because the valid package name might start in an inner folder.
        // This makes sure we know that we are starting at a valid package/class name
        String keyName = "";
        for (String fileSection : fileSections){
            fileSection = fileSection.replace(".java", "");
            keyName += fileSection;
            if (packages.containsKey(keyName) || classes.containsKey(fileSection)){ // TODO - change back to key name here and in GitHubRepoController

                if (classes.containsKey(fileSection)){
                    System.out.println("FOUND CLASS FOR " + fullFilename + " AS " + keyName);
                    return (ClassEntity) classes.get(fileSection);
                } else {
                    System.out.println("FOUND PACKAGE FOR " + fullFilename + " AS " + keyName);
                }
                keyName += ".";
            } else {
                keyName = "";
            }

        }

        // TODO - test renamed files
        System.out.println("COULD NOT FIND " + fullFilename);

        return null;
    }


    private static AbstractTreeIterator prepareTreeParser(Repository repository, RevCommit commit) throws IOException {
        // from the commit we can build the tree which allows us to construct the TreeParser

        RevWalk walk = new RevWalk(repository);
        ObjectId treeId = commit.getTree().getId();

        CanonicalTreeParser treeParser = new CanonicalTreeParser();
        ObjectReader reader = repository.newObjectReader();
        treeParser.reset(reader, treeId);

        walk.dispose();

        return treeParser;
    }

    public LinkedHashMap<String, String> getRenamedClassEntityNames() {
        return renamedClassEntityNames;
    }
}
