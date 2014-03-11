package org.virtualAsylum.spriggan;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

import java.util.concurrent.Callable;

/**
 * Created by Morgan on 11/03/14.
 */
public class ModalInterfaceBase<NodeType extends Region> extends AnchorPane {

    Button
            submitButton,
            cancelButton = new Button("Cancel");

    Callable<NodeType> okayHandler;
    public ModalInterfaceBase(NodeType content, OkayHandler<NodeType> okayHandler){
        this(content, okayHandler, "Submit");
    }
    public ModalInterfaceBase(NodeType content, OkayHandler<NodeType> okayHandler, String submitText){
        submitButton = new Button(submitText);
        getChildren().addAll(submitButton, cancelButton, content);

        submitButton.setOnAction(e -> okayHandler.handle(content, this));
        submitButton.setPrefWidth(100);
        cancelButton.setOnAction(e->close());
        cancelButton.setPrefWidth(100);

        setBottomAnchor(submitButton, 5.0);
        setLeftAnchor(submitButton, 5.0);
        setBottomAnchor(cancelButton, 5.0);
        setRightAnchor(cancelButton, 5.0);

        setTopAnchor(content, 5.0);
        setBottomAnchor(content, 35.0);
        setLeftAnchor(content, 5.0);
        setRightAnchor(content, 5.0);
    }

    public void close(){
        ((Stage)getScene().getWindow()).close();
    }

    public static interface OkayHandler<NodeType extends Region>{
        public void handle(NodeType content, ModalInterfaceBase<NodeType> base);
    }
}
