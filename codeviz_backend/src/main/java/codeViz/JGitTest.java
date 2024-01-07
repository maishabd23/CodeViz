package codeViz;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.TreeWalk;

import java.io.File;
import java.io.IOException;

public class JGitTest {

    // must be the root of the repo
    private static String currentTarget = "./"; // src/main/java/codeViz/entity

    public static void main(String[] args) {
        try {
            Git git = Git.init().setDirectory(new File(currentTarget)).call();

//            // could instead do remote repo like this: (need to look into more)
//            Git git = Git.cloneRepository()
//                    .setURI("https://github.com/eclipse/jgit.git")
//                    .setDirectory("/path/to/repo")
//                    .call();

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
        }


    }
}
