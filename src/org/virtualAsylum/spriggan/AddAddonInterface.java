package org.virtualAsylum.spriggan;

import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

/**
 * Created by Morgan on 11/03/14.
 */
public class AddAddonInterface extends AnchorPane {

    TextField input = new TextField();

    public AddAddonInterface(){
        setLeftAnchor(input, 0.0);
        setRightAnchor(input, 0.0);
        setTopAnchor(input, 0.0);
        setBottomAnchor(input, 0.0);

        getChildren().add(input);
    }
}
