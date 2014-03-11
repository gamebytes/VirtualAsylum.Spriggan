package org.virtualAsylum.spriggan;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;

import static org.virtualAsylum.spriggan.util.SprigganHelper.jsonRead;
import static org.virtualAsylum.spriggan.util.SprigganHelper.jsonWrite;

/**
 * Created by Morgan on 11/03/14.
 */
public class MainInterface extends BorderPane {
    public static final MainInterface instance = new MainInterface();
    private Settings settings;

    //LEFT - Addon Tree
    AnchorPane leftRoot = new AnchorPane();
    TreeView addonTree = new TreeView();
    TreeItem addonTreeRoot = new TreeItem();

    //TOP - Menu
    MenuBar menuBar = new MenuBar();
    Menu menuAddons = new Menu("Addons");
    MenuItem
            menuAddonsAdd = new MenuItem("Add New"),
            menuAddonsUpdateAll = new MenuItem("Update All");
    CheckMenuItem
            menuAddonsUpdateAuto = new CheckMenuItem("Update Automatically");

    //CENTER - Addon Control
    AnchorPane centerRoot = new AnchorPane();
    Label currentAddonName = new Label();
    Button
            currentAddonInstall = new Button("Install"),
            currentAddonUninstall = new Button("Uninstall");

    public MainInterface(){
        initSettings();
        initAddonTree();
        initMenu();
        initAddonControl();
    }

    private void initAddonControl() {
        setCenter(centerRoot);
        currentAddonName.setFont(new Font(currentAddonName.getFont().getFamily(), 20));
        AnchorPane.setLeftAnchor(currentAddonName, 5.0);
        AnchorPane.setTopAnchor(currentAddonName, 5.0);

        currentAddonInstall.setPrefWidth(100);
        currentAddonInstall.setDisable(true);
        AnchorPane.setLeftAnchor(currentAddonInstall, 5.0);
        AnchorPane.setBottomAnchor(currentAddonInstall, 5.0);

        currentAddonUninstall.setPrefWidth(100);
        currentAddonUninstall.setDisable(true);
        AnchorPane.setLeftAnchor(currentAddonUninstall, 110.0);
        AnchorPane.setBottomAnchor(currentAddonUninstall, 5.0);

        centerRoot.getChildren().addAll(currentAddonName, currentAddonInstall, currentAddonUninstall);
    }

    private void initSettings() {
        File settingsFile = new File(SprigganDirectories.home, "SprigganSettings.txt");
        if(settingsFile.exists()){
            settings = jsonRead(settingsFile, Settings.class);
            if(settings == null){
                settings = new Settings();
            }
        }else{
            settings = new Settings();
            jsonWrite(settingsFile, settings);
        }
        settings.automaticUpdate.addListener(e->jsonWrite(settingsFile, settings));
    }

    private void initMenu() {
        menuBar.getMenus().addAll(menuAddons);
        initMenuAddons();
        setTop(menuBar);
    }

    private void initMenuAddons() {
        menuAddons.getItems().addAll(menuAddonsAdd, new SeparatorMenuItem(), menuAddonsUpdateAuto, menuAddonsUpdateAll);

        menuAddonsAdd.setOnAction(e->menuAddonsAdd());

        menuAddonsUpdateAll.disableProperty().bind(menuAddonsUpdateAuto.selectedProperty());
        menuAddonsUpdateAll.setOnAction(e->menuAddonsUpdateAll());

        menuAddonsUpdateAuto.selectedProperty().bindBidirectional(settings.automaticUpdate);
    }

    private void menuAddonsAdd() {
        ModalInterfaceBase<AddAddonInterface> content = new ModalInterfaceBase<>(new AddAddonInterface(), (i,b)->menuAddonsAdd(i,b));
        Stage stage = new Stage();
        stage.setScene(new Scene(content, 300, 75));
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(getScene().getWindow());
        stage.initStyle(StageStyle.UTILITY);
        stage.setResizable(false);
        stage.show();
    }

    private void menuAddonsAdd(AddAddonInterface i, ModalInterfaceBase b) {
        boolean isValid = true;
        String input = i.input.getText().trim();
        if(input.length() == 0){
            isValid = false;
        }
        //Future evaluation of conditions

        if(isValid){
            b.close();

            try{
                addURL(new URL(input));
            }catch (Exception e){
                addPath(input);
            }
        }
    }

    private void addPath(String input) {
        ArrayList<String> split = new ArrayList();
        for(String part : input.split("\\/")){
            if(!part.isEmpty()){
                split.add(part.trim());
            }
        }

        switch (split.size()){
            case 2:
                add(new SprigganAddon(split.get(0), split.get(1)));
                return;
            case 3:
                add(new SprigganAddon(split.get(0), split.get(1), split.get(2)));
                return;
            case 4:
                if(split.get(2).equals("tree")){
                    add(new SprigganAddon(split.get(0), split.get(1), split.get(3)));
                }
                break;
        }

        //Bad path
    }

    private void add(SprigganAddon addon) {
        SprigganRepositoryManager.repository.add(addon);
        SprigganRepositoryManager.saveRepository();
        addTreeNode(addon);
        addon.download();
    }

    private void addURL(URL url) {
        if(url.getHost().equals("github.com")){
            addPath(url.getPath());
        }else{
            //Invalid host
        }
    }

    private void menuAddonsUpdateAll() {
        for(Object item : addonTreeRoot.getChildren()){
            SprigganAddon addon = ((AddonGraphic)((TreeItem)item).getGraphic()).addon;
            addon.update();
        }
    }

    private void initAddonTree() {
        addonTree.setRoot(addonTreeRoot);
        addonTree.setShowRoot(false);
        addonTree.setFocusTraversable(false);
        AnchorPane.setBottomAnchor(addonTree, -1.0);
        AnchorPane.setTopAnchor(addonTree, -1.0);
        AnchorPane.setLeftAnchor(addonTree, -1.0);
        AnchorPane.setRightAnchor(addonTree, 0.0);
        leftRoot.getChildren().add(addonTree);
        setLeft(leftRoot);

        for(SprigganAddon addon : SprigganRepositoryManager.repository.get()){
            addTreeNode(addon);
            if(settings.automaticUpdate.get()){
                addon.update();
            }
        }

        addonTree.getSelectionModel().selectedItemProperty().addListener((a,b,c)->addonTreeSelectionChanged(b, c));
    }

    void addTreeNode(SprigganAddon addon){
        TreeItem addonItem = new TreeItem();
        TreeItem addonStateItem = new TreeItem();
        addonItem.setValue(addon.getDisplayName());

        addonStateItem.setValue(addon.state.get().text);
        addon.state.addListener((a,b,c)->{
            addonStateItem.setValue(c.text);
            addonItem.setValue(addon.getDisplayName());
        });
        addonItem.getChildren().add(addonStateItem);

        addonItem.setGraphic(new AddonGraphic(addon));

        addonItem.setExpanded(true);
        addonItem.expandedProperty().addListener(e -> addonItem.setExpanded(true));
        addonTreeRoot.getChildren().add(addonItem);
        addon.treeItem = addonItem;
    }

    class AddonGraphic extends Pane {
        public final SprigganAddon addon;
        AddonGraphic(SprigganAddon addon){
            this.addon = addon;
        }
    }

    private void addonTreeSelectionChanged(Object b, Object c) {
        TreeItem treeItem = (TreeItem)c;
        if(treeItem.getChildren().isEmpty()){
            treeItem = treeItem.getParent();
            addonTree.getSelectionModel().select(treeItem);
        }
        SprigganAddon addon = ((AddonGraphic)treeItem.getGraphic()).addon;

        currentAddonName.setText(addon.getDisplayName());
        currentAddonInstall.setDisable(addon.isInstalled.get());
        currentAddonInstall.setOnAction(e -> {
            currentAddonInstall.setDisable(true);
            addon.install();
        });
        currentAddonUninstall.disableProperty().unbind();
        currentAddonUninstall.disableProperty().bind(currentAddonInstall.disableProperty().not());
        currentAddonUninstall.setOnAction(e->{
            currentAddonInstall.setDisable(false);
            addon.uninstall();
        });
    }
}
