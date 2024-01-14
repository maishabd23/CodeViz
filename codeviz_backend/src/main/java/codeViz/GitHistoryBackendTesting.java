package codeViz;

import codeViz.gitHistory.CommitInfo;
import codeViz.gitHistory.GitCommitReader;

// For backend testing
public class GitHistoryBackendTesting {

    // must be the root of the repo
    private static final String currentSrc = "./"; // weird things happen in intelliJ's project/vcs if I try setting this as anything else like ./codeviz_backend, ./codeviz_backend/src, etc
    private static final String gitHubURI = "https://github.com/thanujasiva/CodeViz.git"; // using own fork for now
    private static final String currentTarget = "./codeviz_backend/target/classes"; // /codeViz/entity";


    public static void main(String[] args) {

        JavaBytecodeReader javaBytecodeReader = new JavaBytecodeReader();
        javaBytecodeReader.generateEntitiesAndConnections(currentTarget);

        boolean isLocal = true;
        GitCommitReader gitCommitReader;

        // could either read locally or through the gitHub link
        if (isLocal) {
            gitCommitReader = new GitCommitReader(javaBytecodeReader.getGraphGenerator(), currentSrc);
        } else {
            // https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens
            // in the GitHub Developer Settings, create a token with the "repo" settings selected
            // (don't commit the actual value here)
            String tokenPassword = "";
            gitCommitReader = new GitCommitReader(javaBytecodeReader.getGraphGenerator(), gitHubURI, tokenPassword);
        }

        gitCommitReader.storeCommitHistory(10);

        System.out.println("Commit Info:");
        for (CommitInfo commitInfo : gitCommitReader.getCommitInfos()){
            System.out.println(commitInfo.toString() + "\n");
        }

    }
}
