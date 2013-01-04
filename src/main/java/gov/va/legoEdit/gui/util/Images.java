package gov.va.legoEdit.gui.util;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * 
 * @author kec
 * @author Dan Armbrust
 */
public enum Images
{
    //Tree Icons
    PRIMITIVE_SINGLE_PARENT(setupImage("/icons/fugue/16x16/icons-shadowless/navigation-nowhere-button-white.png")), 
    PRIMITIVE_MULTI_PARENT_CLOSED(setupImage("/icons/fugue/16x16/icons-shadowless/navigation-090-button-white.png")), 
    PRIMITIVE_MULTI_PARENT_OPEN(setupImage("/icons/fugue/16x16/icons-shadowless/navigation-045-button-white.png")), 
    DEFINED_SINGLE_PARENT(setupImage("/icons/fugue/16x16/icons-shadowless/navigation-nowhere-2.png")), 
    DEFINED_MULTI_PARENT_CLOSED(setupImage("/icons/fugue/16x16/icons-shadowless/navigation-090.png")), 
    DEFINED_MULTI_PARENT_OPEN(setupImage("/icons/fugue/16x16/icons-shadowless/navigation-045.png")), 
    ROOT(setupImage("/icons/fugue/16x16/icons-shadowless/node.png")),

    TAXONOMY_OPEN(setupImage("/icons/fugue/16x16/icons-shadowless/plus-small.png")), 
    TAXONOMY_CLOSE(setupImage("/icons/fugue/16x16/icons-shadowless/minus-small.png")),
    
    //Other GUI icons
    LEGO_ADD(setupImage("/icons/silk/16x16/brick_add.png")),
    LEGO_DELETE(setupImage("/icons/silk/16x16/brick_delete.png")),
    LEGO_EDIT(setupImage("/icons/silk/16x16/brick_edit.png")),
    LEGO(setupImage("/icons/silk/16x16/brick.png")),
    LEGO_IMPORT(setupImage("/icons/fugue/16x16/icons-shadowless/application-import.png")),
    LEGO_EXPORT_ALL(setupImage("/icons/fugue/16x16/icons-shadowless/application-export.png")),
    LEGO_EXPORT(setupImage("/icons/silk/16x16/brick_go.png")),
    LEGO_LIST_VIEW(setupImage("/icons/silk/16x16/bricks.png")),
    LEGO_SEARCH(setupImage("/icons/fugue/16x16/icons-shadowless/application-search-result.png")),
    XML_VIEW_16(setupImage("/icons/text-xml-icon-16x16.png")),
    XML_VIEW_32(setupImage("/icons/text-xml-icon-32x32.png")),
    PROPERTIES(setupImage("/icons/document-properties-icon.png")),
    PREFERENCES(setupImage("/icons/fugue/16x16/icons-shadowless/application-task.png")),
    APPLICATION(setupImage("/icons/fugue/16x16/icons-shadowless/application-block.png")),
    CONCEPT_VIEW(setupImage("/icons/fugue/16x16/icons-shadowless/gear.png")),
    SAVE(setupImage("/icons/fugue/16x16/icons-shadowless/disk-black.png")),
    EXIT(setupImage("/icons/fugue/16x16/icons-shadowless/cross.png"));
    
    private Image iconImage_;

    private Images(Image icon)
    {
        this.iconImage_ = icon;
    }

    public ImageView createImageView()
    {
        return Images.createImageView(iconImage_);
    }
    
    public Image getImage()
    {
        return this.iconImage_;
    }

    public static ImageView createImageView(Image image)
    {
        return new ImageView(image);
    }

    private static Image setupImage(String imageUrl)
    {
        return new Image(imageUrl, false);
    }

}