package com.millerk97.ais.util;

import javafx.fxml.FXMLLoader;
import javafx.fxml.LoadException;
import javafx.scene.Node;

import java.io.IOException;

public class ScreenHelper {

    private static final String LOCATION = "/com/millerk97/fxml/";

    public static boolean loadFXML(Node controller, Node root) {
        return loadFXML(controller, root, null);
    }

    /**
     * @param controller   : call with <b>this</b>
     * @param root         : call with <b>this</b>
     * @param separateFXML : if not specified or <b>null</b> loads a .fxml of the
     *                     SimpleName of the Controller class, can be specified to
     *                     load other file
     * @return <b>true</b> if loaded successfully
     */
    public static boolean loadFXML(Node controller, Node root, String separateFXML) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(controller.getClass().getResource(
                    separateFXML != null ? separateFXML : "/com/millerk97/fxml/" + controller.getClass().getSimpleName() + ".fxml"));

            if (root != null) {
                fxmlLoader.setRoot(root);
            }
            fxmlLoader.setController(controller);
            fxmlLoader.load();
            return true;

        } catch (LoadException e) {
            //logger.log(Level.SEVERE, "LoadException in ScreenHelper " + e.getMessage(), e);
            e.printStackTrace();
        } catch (NullPointerException e) {
            // logger.log(Level.SEVERE, "NullPointerException in ScreenHelper " + e.getMessage(), e);
        } catch (IOException e) {
            // logger.log(Level.SEVERE, "IOException in ScreenHelper " + e.getMessage(), e);
        }
        return false;

    }

}
