package org.virtualAsylum.spriggan;

import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;

import java.io.File;

import static org.virtualAsylum.spriggan.util.SprigganHelper.*;

/**
 * Created by Morgan on 11/03/14.
 */
public class SprigganRepositoryManager {
    private SprigganRepositoryManager(){}

    static SimpleListProperty<SprigganAddon> repository = new SimpleListProperty<>(FXCollections.observableArrayList());
    public static void loadRepository() {
        File indexFile = new File(SprigganDirectories.management, "index");
        SprigganAddon[] index = null;
        if(indexFile.exists()){
            index = jsonRead(indexFile, SprigganAddon[].class);
            repository.get().addAll(index);
        }
    }

    public static void saveRepository(){
        File indexFile = new File(SprigganDirectories.management, "index");
        jsonWrite(indexFile, repository.get());
    }

    public static void addChangeListener(ListChangeListener<SprigganAddon> listener){
        repository.addListener(listener);
    }

    public static interface RepositoryChangeHandler{
        public void handle(Iterable<? extends SprigganAddon> added, Iterable<? extends SprigganAddon> removed);
    }
}
