package codeViz;

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
import java.util.ArrayList;
import java.util.List;

public class GitCommitReader {

    private static final String gitCloneDirectory = "./testgithistory"; // local directory to clone into

    private final Git git;
    private final GraphGenerator graphGenerator;
    private ArrayList<CommitInformation> commitInformations;
    
    public GitCommitReader(GraphGenerator graphGenerator, String localDirectory){
        this.graphGenerator = graphGenerator;
        this.commitInformations = new ArrayList<>();
        try {
            this.git = Git.init().setDirectory(new File(localDirectory)).call();
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
    }

    public GitCommitReader(GraphGenerator graphGenerator, String gitHubURI, String tokenPassword){
        this.graphGenerator = graphGenerator;
        this.commitInformations = new ArrayList<>();
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

    public ArrayList<CommitInformation> getCommitInformations() {
        return commitInformations;
    }

    /**
     * Get the commit history
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

        int numCommits = 0;

        for (RevCommit commit : log) {

            numCommits += 1;
            if (numCommits >= maxNumCommits) {
                break;
            }

            if (!firstCommit) { // means that nextCommit != null
                try {
                    this.getDiffs(commit, nextCommit);
                } catch (IOException | GitAPIException e) {
                    throw new RuntimeException(e);
                }

                //this.getCommitInfo(nextCommit);
            }

            firstCommit = false;
            nextCommit = commit;

        }
    }

    private void getCommitInfo(RevCommit currentCommit){
        System.out.println(currentCommit.getId().getName());
        System.out.println(currentCommit.getTree().getId().getName());
        System.out.println(currentCommit.getAuthorIdent().getName());
        System.out.println(currentCommit.getShortMessage());
    }

    /**
     * Similar to the command: git diff <previous-commit> <new-commit>
     * @param previousCommit    previous commit
     * @param newCommit         new commit
     */
    private void getDiffs(RevCommit previousCommit, RevCommit newCommit) throws IOException, GitAPIException {

        Repository repository = git.getRepository();

        // https://stackoverflow.com/questions/23334862/jgit-how-to-get-diff-from-multiple-revcommits
        AbstractTreeIterator oldTreeParser = prepareTreeParser(repository, previousCommit);
        AbstractTreeIterator newTreeParser = prepareTreeParser(repository, newCommit);

        List<DiffEntry> diff = git.diff().
                setOldTree(oldTreeParser).
                setNewTree(newTreeParser).
                // to filter on Suffix
                        setPathFilter(PathSuffixFilter.create(".java")).
                call();

        for (DiffEntry entry : diff) {
            //System.out.println("Entry: " + entry + ", from: " + entry.getOldId() + ", to: " + entry.getNewId());

            CommitType commitType;
            if (entry.getOldPath().equals(entry.getNewPath())) {
                //System.out.println("Filename: " + entry.getNewPath());
                commitType = CommitType.EDIT;
            } else if (entry.getOldPath().equals("/dev/null")){
                //System.out.println("Newly created Filename: " + entry.getNewPath());
                commitType = CommitType.CREATE;
            } else if (entry.getNewPath().equals("/dev/null")){
                //System.out.println("Deleted Filename: " + entry.getOldPath());
                commitType = CommitType.DELETE;
            } else {
                // renamed file
                //System.out.println("Old Filename: " + entry.getOldPath());
                //System.out.println("New Filename: " + entry.getNewPath());
                commitType = CommitType.RENAME;
            }

            OutputStream outputStream = new ByteArrayOutputStream();
            DiffFormatter formatter = new DiffFormatter(outputStream);
            formatter.setRepository(repository);
            formatter.format(entry);
//            System.out.println(outputStream.toString());

            CommitInformation commitInformation = new CommitInformation(
                    newCommit.getId().getName(),
                    newCommit.getAuthorIdent().getName(),
                    entry.getOldPath(), entry.getNewPath(), commitType, newCommit.getShortMessage(),
                    outputStream.toString()
            );
            commitInformations.add(commitInformation);


        }

        //System.out.println("===============================");

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
