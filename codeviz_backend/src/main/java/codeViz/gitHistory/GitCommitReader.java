package codeViz.gitHistory;

import codeViz.GraphGenerator;
import codeViz.entity.ClassEntity;
import codeViz.entity.Entity;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Class that reads git commit history
 *
 * @author Thanuja Sivaananthan
 */
public class GitCommitReader {

    private static final String gitCloneDirectory = "./testgithistory"; // local directory to clone into

    private final Git git;
    private final GraphGenerator graphGenerator;
    private ArrayList<CommitInfo> commitInfos; // may not need to store here if using from entities directly? mainly used for previous commit
    private LinkedHashMap<String, String> renamedClassEntityNames;

    /**
     * Create GitCommitReader that reads details locally
     * @param graphGenerator    the graph generator to use
     * @param localDirectory    the local directory to read from
     */
    public GitCommitReader(GraphGenerator graphGenerator, String localDirectory){
        this.graphGenerator = graphGenerator;
        this.commitInfos = new ArrayList<>();
        this.renamedClassEntityNames = new LinkedHashMap<>();
        try {
            this.git = Git.init().setDirectory(new File(localDirectory)).call();
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Create GitCommitReader that reads details via gitHub
     * @param graphGenerator    the graph generator to use
     * @param gitHubURI         the URI of the gitHub repository/fork
     * @param tokenPassword     the token password of the user
     */
    public GitCommitReader(GraphGenerator graphGenerator, String gitHubURI, String tokenPassword){
        this.graphGenerator = graphGenerator;
        this.commitInfos = new ArrayList<>();
        this.renamedClassEntityNames = new LinkedHashMap<>();
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
    }

    public Collection<CommitInfo> getCommitInfos() {
        return commitInfos;
    }

    /**
     * Store the commit history in order of most recent commit to the oldest commit
     * @param maxNumCommits the number of commits to get the history from, -1 if all commits
     */
    public void storeCommitHistory(int maxNumCommits) { // Note: public methods should probably not throw exceptions
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

            CommitInfo commitInfo = new CommitInfo(
                    currentCommit.getId().getName(),
                    currentCommit.getAuthorIdent().getName(),
                    currentCommit.getCommitTime(),
                    currentCommit.getShortMessage(),
                    entry.getOldPath(), entry.getNewPath(),
                    outputStream.toString()
            );
            commitInfos.add(commitInfo);

//            // FIXME connect commits like a linked list - might not be needed
//            if (futureCommit != null && commitIdsAndInfos.containsKey(futureCommit.getId().getName())){
//                CommitInfo futureCommitInfo = commitIdsAndInfos.get(futureCommit.getId().getName());
//                if (futureCommitInfo != null){
//                    futureCommitInfo.setPreviousCommit(commitInfo);
//                }
//            }

            if (commitInfo.getCommitType() != CommitType.DELETE && graphGenerator != null) { // Note: deletes will not have their code details stored
                ClassEntity classEntity = getClassEntity(entry.getNewPath());
                if (classEntity != null) {
                    classEntity.addCommitInfo(commitInfo);
                }
            }

        }

        //System.out.println("===============================");

    }


    /**
     * Get the classEntity that corresponds with the filename
     * @param fullFilename      the filename
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

        String keyName = "";
        for (String fileSection : fileSections){
            keyName += fileSection.replace(".java", "");
            if (packages.containsKey(keyName) || classes.containsKey(keyName)){
                System.out.println("FOUND ENTITY FOR " + fullFilename + " AS " + keyName);

                if (classes.containsKey(keyName)){
                    return (ClassEntity) classes.get(keyName);
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
}
