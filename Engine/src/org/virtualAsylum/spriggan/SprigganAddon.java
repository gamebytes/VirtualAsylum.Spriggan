package org.virtualAsylum.spriggan;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import javafx.beans.property.*;
import javafx.scene.control.TreeItem;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.transport.FetchResult;
import org.virtualAsylum.spriggan.util.JavaFXDeserializer;
import org.virtualAsylum.spriggan.util.JavaFXSerializer;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Morgan on 11/03/14.
 */
public class SprigganAddon {

    private void recursiveDelete(File file) {
        if(file.isDirectory()){
            for(File sub : file.listFiles()){
                recursiveDelete(sub);
            }
        }
        file.delete();
    }
    private void daemon(Runnable runnable){
        Thread downloadThread = new Thread(runnable);
        downloadThread.setDaemon(true);
        downloadThread.start();
    }

    @JsonSerialize(using = JavaFXSerializer.String.class, as=SimpleStringProperty.class)
    @JsonDeserialize(using = JavaFXDeserializer.String.class, as=SimpleStringProperty.class)
    public final SimpleStringProperty
            author = new SimpleStringProperty(this, "author"),
            name = new SimpleStringProperty(this, "name"),
            branch = new SimpleStringProperty(this, "isInstalled");
    @JsonSerialize(using = JavaFXSerializer.Boolean.class, as=SimpleBooleanProperty.class)
    @JsonDeserialize(using = JavaFXDeserializer.Boolean.class, as=SimpleBooleanProperty.class)
    public final SimpleBooleanProperty
            isInstalled = new SimpleBooleanProperty(this, "isInstalled", false);
    @JsonIgnore
    public final SimpleObjectProperty<SprigganAddonState>
            state = new SimpleObjectProperty(this, "state", SprigganAddonState.IDLE);
    @JsonIgnore
    public TreeItem treeItem;
    @JsonIgnore
    public Exception lastError;

    public SprigganAddon(){}
    public SprigganAddon(String author, String name){
        this(author, name, "master");
    }
    public SprigganAddon(String author, String name, String branch) {
        this.author.set(author);
        this.name.set(name);
        this.branch.set(branch);
    }

    @JsonIgnore
    public Collection<String> getInstallIgnore(){
        File iiFile = new File(getRepositoryDirectory(), ".installIgnore");
        ArrayList<String> result = new ArrayList(){{
            add(".git");
            add(".displayName");
            add(".gitignore");
            add(".installIgnore");
        }};
        if(iiFile.exists()){
            try {
                BufferedReader reader = new BufferedReader(new FileReader(iiFile));
                while (reader.ready()){
                    result.add(reader.readLine().trim());
                }
                reader.close();
                return result;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
    @JsonIgnore
    public String getSignature(){
        return String.format("%s.%s.%s", author.get(), name.get(), branch.get());
    }
    @JsonIgnore
    public File getRepositoryDirectory(){
        return new File(SprigganDirectories.management, getSignature());
    }
    @JsonIgnore
    public String getDisplayName(){
        File dnFile = new File(getRepositoryDirectory(), ".displayName");
        if(dnFile.exists()){
            try {
                BufferedReader reader = new BufferedReader(new FileReader(dnFile));
                String result = reader.readLine();
                reader.close();
                return result;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return getSignature();
    }
    @JsonIgnore
    public String getRepositoryURI() {
        return String.format("https://github.com/%s/%s.git", author.get(), name.get());
    }

    public void download() {
        state.set(SprigganAddonState.DOWNLOADING);
        daemon(()->doDownload());
    }
    void doDownload(){
        File directory = getRepositoryDirectory();
        if(directory.exists()){
            recursiveDelete(directory);
        }
        CloneCommand cloneCommand = Git.cloneRepository()
                .setURI(getRepositoryURI())
                .setBranch(branch.get())
                .setDirectory(directory);
        try {
            cloneCommand.call();
            state.set(SprigganAddonState.IDLE);
        } catch (GitAPIException e) {
            lastError = e;
            state.set(SprigganAddonState.ERROR);
        }
    }

    public void update() {
        state.set(SprigganAddonState.UPDATE_CHECK);
        daemon(()-> doUpdateCheck());
    }
    private void doUpdateCheck() {
        File localGitDir = new File(getRepositoryDirectory(), ".git");
        FileRepository localRepo = null;
        try {
            localRepo = new FileRepository(localGitDir);
        } catch (IOException e) {
            lastError = e;
            state.set(SprigganAddonState.ERROR);
        }

        if(localRepo != null){
            Git localGit = new Git(localRepo);
            try {
                FetchResult fetchResult = localGit.fetch()
                        .setDryRun(true)
                        .call();
                if(!fetchResult.getTrackingRefUpdates().isEmpty()){
                    state.set(SprigganAddonState.UPDATING);
                    doUpdate(localGit);
                }else{
                    state.set(SprigganAddonState.IDLE);
                }
            } catch (GitAPIException e) {
                lastError = e;
                state.set(SprigganAddonState.ERROR);
            }
        }
    }

    void doUpdate(Git git){
        try {
            PullResult call = git.pull().call();
            if(call.isSuccessful()){
                if(isInstalled.get()){
                    doInstall();
                }else{
                    state.set(SprigganAddonState.IDLE);
                }
            }else{
                lastError = new Exception("Unknown Pull Exception");
                state.set(SprigganAddonState.ERROR);
            }
        } catch (GitAPIException e) {
            lastError = e;
            state.set(SprigganAddonState.ERROR);
        }
    }

    public void install(){
        state.set(SprigganAddonState.INSTALLING);
        daemon(()->doInstall());
    }

    public void doInstall() {
        File installDir = new File(SprigganDirectories.installation, getSignature()),
             repoDir = getRepositoryDirectory();
        if(installDir.exists()){
            recursiveDelete(installDir);
        }
        installDir.mkdirs();
        Collection<String> installIgnoreNames = getInstallIgnore();
        ArrayList<String> installIgnoreFiles = new ArrayList();
        for(String iiname : installIgnoreNames){
            installIgnoreFiles.add(new File(repoDir, iiname).getAbsolutePath());
        }
        for(File repoFile : repoDir.listFiles()){
            tryInstall(installDir, repoFile, installIgnoreFiles);
        }

        isInstalled.set(true);
        state.set(SprigganAddonState.IDLE);
        SprigganRepositoryManager.saveRepository();
    }
    private void tryInstall(File installDir, File file, Collection<String> installIgnore) {
        if(installIgnore.contains(file.getAbsolutePath())){
            return;
        }
        File install = new File(installDir, file.getName());
        try {
            copyFileUsingFileStreams(file, install);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //http://examples.javacodegeeks.com/core-java/io/file/4-ways-to-copy-file-in-java/
    private static void copyFileUsingFileStreams(File source, File dest)
            throws IOException {
        InputStream input = null;
        OutputStream output = null;
        try {
            input = new FileInputStream(source);
            output = new FileOutputStream(dest);
            byte[] buf = new byte[1024];
            int bytesRead;
            while ((bytesRead = input.read(buf)) > 0) {
                output.write(buf, 0, bytesRead);
            }
        } finally {
            input.close();
            output.close();
        }
    }

    public void uninstall() {
        daemon(()->{
            recursiveDelete(new File(SprigganDirectories.installation, getSignature()));
            isInstalled.set(false);
            SprigganRepositoryManager.saveRepository();
        });
    }
}
