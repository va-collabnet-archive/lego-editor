/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.legoEdit.gui.util;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 *
 * @author kec
 */
public class IconHelper {
            
    public static ImageView getImageView(Image icon) {
        ImageView iv = new ImageView();
        iv.setImage(icon);
        return iv;
    }
        
    public static Image setupImage(String image) {
        Image icon = new Image(image, false);
        return icon;
    }

}
