package codeViz;

import codeViz.entity.ClassEntity;
import codeViz.entity.Entity;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
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
    private LinkedHashMap<String, CommitInformation> commitInformations;
    private LinkedHashMap<String, String> renamedClassEntityNames;

    public GitCommitReader(GraphGenerator graphGenerator, String localDirectory){
        this.graphGenerator = graphGenerator;
        this.commitInformations = new LinkedHashMap<>();
        this.renamedClassEntityNames = new LinkedHashMap<>();
        try {
            this.git = Git.init().setDirectory(new File(localDirectory)).call();
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
    }

    public GitCommitReader(GraphGenerator graphGenerator, String gitHubURI, String tokenPassword){
        this.graphGenerator = graphGenerator;
        this.commitInformations = new LinkedHashMap<>();
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

    public Git getGit() {
        return git;
    }

    public Collection<CommitInformation> getCommitInformations() {
        return commitInformations.values();
    }

    /**
     * Get the commit history in order of most recent commit to the oldest commit
     * @param maxNumCommits the number of commits to get the history from
     */
    public void getCommitHistory(int maxNumCommits) { // Note: public methods should probably not throw exceptions
        Git git = this.getGit();
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
            if (numCommits >= maxNumCommits) {
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

        for (DiffEntry entry : diff) {
            //System.out.println("Entry: " + entry + ", from: " + entry.getOldId() + ", to: " + entry.getNewId());

            OutputStream outputStream = new ByteArrayOutputStream();
            DiffFormatter formatter = new DiffFormatter(outputStream);
            formatter.setRepository(repository);
            formatter.format(entry);
//            System.out.println(outputStream.toString());

            CommitInformation commitInformation = new CommitInformation(
                    currentCommit.getId().getName(),
                    currentCommit.getAuthorIdent().getName(),
                    currentCommit.getCommitTime(),
                    currentCommit.getShortMessage(),
                    entry.getOldPath(), entry.getNewPath(),
                    outputStream.toString()
            );
            commitInformations.put(commitInformation.getId(), commitInformation);

            // connect commits like a linked list - might not be needed
            if (futureCommit != null && commitInformations.containsKey(futureCommit.getId().getName())){
                CommitInformation futureCommitInformation = commitInformations.get(futureCommit.getId().getName());
                if (futureCommitInformation != null){
                    futureCommitInformation.setPreviousCommit(commitInformation);
                }
            }

            if (commitInformation.getCommitType() != CommitType.DELETE) { // Note: deletes will not have their code details stored
                ClassEntity classEntity = getClassEntity(entry.getNewPath());
                if (classEntity != null) {
                    classEntity.addCommitInformation(commitInformation);
                } else {
                    System.out.println("was null, try with " + entry.getOldPath());
                    classEntity = getClassEntity(entry.getOldPath());
                    if (classEntity != null) { // store previously named file list somewhere? FIXME could have duplicates
                        classEntity.addCommitInformation(commitInformation);
                        renamedClassEntityNames.put(entry.getOldPath(), entry.getNewPath());
                        System.out.println("PUT renamed :" + entry.getOldPath() + ", " + entry.getNewPath());
                    } else {
                        System.out.println("still null");
                    }
                }
            }

        }

        //System.out.println("===============================");

    }


    private ClassEntity getClassEntity(String fullFilename) {
        String[] fileSections = fullFilename.split("/");

        if (renamedClassEntityNames.containsKey(fullFilename)){
            fullFilename = renamedClassEntityNames.get(fullFilename);
            System.out.println("renamed file, try with :" + fullFilename);
        }

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
