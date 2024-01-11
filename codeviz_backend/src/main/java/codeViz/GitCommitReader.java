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

import java.io.File;
import java.io.IOException;
import java.util.List;

public class GitCommitReader {

    // must be the root of the repo
    private static final String currentTarget = "./"; // weird things happen in intelliJ's project/vcs if I try setting this as anything else like ./codeviz_backend, ./codeviz_backend/src, etc
    private static final String gitHubURI = "https://github.com/thanujasiva/CodeViz.git"; // using own fork for now
    private static final String gitCloneDirectory = "./testgithistory"; // local directory to clone into

    private final Git git;
    
    public GitCommitReader(String localDirectory){
        try {
            this.git = Git.init().setDirectory(new File(localDirectory)).call();
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
    }

    public GitCommitReader(String gitHubURI, String tokenPassword){
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

    public static void main(String[] args) {

        boolean isLocal = true;
        GitCommitReader gitCommitReader;

        // could either read locally or through the gitHub link
        if (isLocal) {
            gitCommitReader = new GitCommitReader(currentTarget);
        } else {
            // https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens
            // in the GitHub Developer Settings, create a token with the "repo" settings selected
            // (don't commit the actual value here)
            String tokenPassword = "";
            gitCommitReader = new GitCommitReader(gitHubURI, tokenPassword);
        }

        gitCommitReader.getCommitHistory(10);
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
            }

            this.getCommitInfo(commit);

            firstCommit = false;
            nextCommit = commit;

        }
    }

    private void getCommitInfo(RevCommit currentCommit){
        System.out.println(currentCommit.getId());
        System.out.println(currentCommit.getTree().getId());
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
            System.out.println("Entry: " + entry + ", from: " + entry.getOldId() + ", to: " + entry.getNewId());

            if (entry.getOldPath().equals(entry.getNewPath())) {
                System.out.println("Filename: " + entry.getNewPath());
            } else if (entry.getOldPath().equals("/dev/null")){
                System.out.println("Newly created Filename: " + entry.getNewPath());
            } else if (entry.getNewPath().equals("/dev/null")){
                System.out.println("Deleted Filename: " + entry.getOldPath());
            } else {
                // renamed file
                System.out.println("Old Filename: " + entry.getOldPath());
                System.out.println("New Filename: " + entry.getNewPath());
            }

            try (DiffFormatter formatter = new DiffFormatter(System.out)) {
                formatter.setRepository(repository);
                formatter.format(entry);
            }

        }

        System.out.println("===============================");

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
