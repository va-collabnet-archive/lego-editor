package gov.va.legoEdit.gui.legoTreeView;

import gov.va.legoEdit.gui.util.Images;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

/**
 * LegoTreeNodeGraphic The node used to render all content in the tree.  Also has a progress indicator, an error indicator, and error tool tip.
 * @author Dan Armbrust 
 * Copyright 2013
 * 
 */
public class LegoTreeNodeGraphic
{
	private Tooltip tt;
	private ProgressIndicator pi;
	private HBox content;
	private ImageView errorImage;
	private StackPane sp;
	
	public LegoTreeNodeGraphic()
	{
		sp = new StackPane();
		sp.setMaxWidth(Double.MAX_VALUE);
		
		content = new HBox();
		content.setMinHeight(22.0);
		content.setSpacing(0.0);
		content.setMaxWidth(Double.MAX_VALUE);
		content.setAlignment(Pos.CENTER_LEFT);
		content.setFillHeight(true);
		sp.getChildren().add(content);
		StackPane.setAlignment(content, Pos.CENTER_LEFT);
		StackPane.setMargin(content, new Insets(0.0, 0, 0, 8.0));
		
		
		errorImage = new ImageView(Images.EXCLAMATION.getImage());
		errorImage.setFitWidth(12.0);
		errorImage.setFitHeight(12.0);
		errorImage.setVisible(false);
		errorImage.setTranslateX(-1.0);
		sp.getChildren().add(errorImage);
		StackPane.setAlignment(errorImage, Pos.TOP_LEFT);
		StackPane.setMargin(errorImage, new Insets(5.0, 0, 0, 0));

		tt = new Tooltip("");
		Tooltip.install(errorImage, tt);
		
		pi = new ProgressIndicator(-1.0);
		//note, can't get it any smaller than ~22 for some reason, hence, the scaling - but there is a bug in javafx, and it still thinks its bounds is 22, so have to have the row 22.
		pi.setMaxWidth(22.0);
		pi.setMaxHeight(22.0);
		pi.setVisible(false);
		pi.setMouseTransparent(true);
		pi.setScaleX(.5); 
		pi.setScaleY(.5);
		pi.setTranslateX(-6.0);
		sp.getChildren().add(pi);
		StackPane.setAlignment(pi, Pos.TOP_LEFT);
		
		//Need to do some subtle adjustments when the cell is tall, vs default size.
		content.heightProperty().addListener(new InvalidationListener()
		{
			@Override
			public void invalidated(Observable observable)
			{
				if (content.getHeight() <= 22)
				{
					StackPane.setMargin(errorImage, new Insets(5.0, 0, 0, 0));
					pi.setTranslateY(0.0);
				}
				else
				{
					StackPane.setMargin(errorImage, new Insets(2.0, 0, 0, 0));
					pi.setTranslateY(-3.0);
				}
				
			}
		});
	}
	
	/**
	 * This isn't a standard getChildren() method - it returns the children of the child HBOX.
	 */
	public ObservableList<Node> getChildren()
	{
		return content.getChildren();
	}
	
	public void showProgress(boolean show)
	{
		pi.setVisible(show);
	}
	
	public void showInvalid(boolean show)
	{
		errorImage.setVisible(show);
	}
	
	public void setInvalidReason(String reason)
	{
		tt.setText(reason);
	}
	
	public Node getNode()
	{
		return sp;
	}
}
