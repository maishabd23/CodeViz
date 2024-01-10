package codeViz;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.*;

import java.io.File;
import java.io.IOException;

public class JGitTest {

    // must be the root of the repo
    private static final String currentTarget = "./"; // weird things happen in intelliJ's project/vcs if I try setting this as anything else like ./codeviz_backend, ./codeviz_backend/src, etc
    private static final String gitHubURI = "https://github.com/thanujasiva/CodeViz.git"; // using own fork for now

    public static void main(String[] args) {
        try {

            Git git;
            boolean isLocal = true;

            // could either read locally or through the github link
            if (isLocal) {
                git = Git.init().setDirectory(new File(currentTarget)).call();
            } else {

                // TODO - make this properly secure


                final String gitCloneDirectory = "./testgithistory"; // local directory to clone into
                FileUtils.deleteDirectory(new File(gitCloneDirectory));

                //https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens
                // in the GitHub Developer Settings, create a token with the "repo" settings selected
                // (don't commit the actual value here)
                String tokenPassword = "";

                git = Git.cloneRepository()
                        .setURI(gitHubURI)
                        .setCredentialsProvider(new UsernamePasswordCredentialsProvider(tokenPassword, ""))
                        .setDirectory(new File(gitCloneDirectory))
                        .call();

            }

            Repository repository = git.getRepository();

            Iterable<RevCommit> log = git.log().call();
            for (RevCommit commit : log){
                System.out.println(commit.getId());
                System.out.println(commit.getTree().getId());
                System.out.println(commit.getAuthorIdent().getName());
                System.out.println(commit.getShortMessage());

                ObjectId treeId = commit.getTree().getId();

                TreeWalk treeWalk = new TreeWalk(repository);
                treeWalk.setRecursive(true); // to read inner files?
                treeWalk.reset(treeId);
                while (treeWalk.next()) { // FIXME - prints every file, even if it wasn't modified in the commit?
                    String path = treeWalk.getPathString();
                    if (path.endsWith(".java")) { // only look at java files
                        System.out.println(path);
                    }
                }

                // https://stackoverflow.com/questions/40590039/how-to-get-the-file-list-for-a-commit-with-jgit
                // If you are only interested in the changes that were recorded with a certain commit, see here: Creating Diffs with JGit or here: File diff against the last commit with JGit


                System.out.println("===============================");

            }


        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        } catch (CorruptObjectException e) {
            throw new RuntimeException(e);
        } catch (IncorrectObjectTypeException e) {
            throw new RuntimeException(e);
        } catch (MissingObjectException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }
}
