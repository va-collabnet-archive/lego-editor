package gov.va.legoEdit.gui.legoTreeView;

import gov.va.legoEdit.LegoGUI;
import gov.va.legoEdit.LegoGUIModel;
import gov.va.legoEdit.formats.LegoXMLUtils;
import gov.va.legoEdit.gui.dialogs.YesNoDialogController.Answer;
import gov.va.legoEdit.gui.legoTreeView.PointNode.PointNodeType;
import gov.va.legoEdit.gui.util.CopyableLabel;
import gov.va.legoEdit.gui.util.CustomClipboard;
import gov.va.legoEdit.gui.util.DropTargetLabel;
import gov.va.legoEdit.gui.util.Images;
import gov.va.legoEdit.gui.util.LegoTab;
import gov.va.legoEdit.gui.util.LegoTreeItemComparator;
import gov.va.legoEdit.gui.util.Utility;
import gov.va.legoEdit.model.LegoListByReference;
import gov.va.legoEdit.model.LegoReference;
import gov.va.legoEdit.model.SchemaSummary;
import gov.va.legoEdit.model.schemaModel.Assertion;
import gov.va.legoEdit.model.schemaModel.AssertionComponent;
import gov.va.legoEdit.model.schemaModel.Bound;
import gov.va.legoEdit.model.schemaModel.Concept;
import gov.va.legoEdit.model.schemaModel.Destination;
import gov.va.legoEdit.model.schemaModel.Discernible;
import gov.va.legoEdit.model.schemaModel.Expression;
import gov.va.legoEdit.model.schemaModel.Interval;
import gov.va.legoEdit.model.schemaModel.Lego;
import gov.va.legoEdit.model.schemaModel.Measurement;
import gov.va.legoEdit.model.schemaModel.Pncs;
import gov.va.legoEdit.model.schemaModel.PointMeasurementConstant;
import gov.va.legoEdit.model.schemaModel.Qualifier;
import gov.va.legoEdit.model.schemaModel.Relation;
import gov.va.legoEdit.model.schemaModel.RelationGroup;
import gov.va.legoEdit.model.schemaModel.Stamp;
import gov.va.legoEdit.model.schemaModel.Type;
import gov.va.legoEdit.model.schemaModel.Units;
import gov.va.legoEdit.model.schemaModel.Value;
import gov.va.legoEdit.model.userPrefs.UserPreferences;
import gov.va.legoEdit.storage.BDBDataStoreImpl;
import gov.va.legoEdit.storage.WriteException;
import gov.va.legoEdit.util.TimeConvert;
import java.util.List;
import java.util.UUID;
import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javax.xml.bind.JAXBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LegoTreeCell<T> extends TreeCell<T>
{
    private static Logger logger = LoggerFactory.getLogger(LegoTreeCell.class);
    private static String sep = " \u25BB "; 
    public static ObservableList<String> statusChoices_ = FXCollections.observableArrayList(new String[] { "Active", "Inactive" });
    public static ObservableList<String> booleanChoices_ = FXCollections.observableArrayList(new String[] { "True", "False" });
    public static ObservableList<String> inclusiveChoices_ = FXCollections.observableArrayList(new String[] {"\u2264", "<"});
    
    private LegoTreeView treeView;
    
    public LegoTreeCell(LegoTreeView ltv)
    {
        //For reasons I don't understand, the getTreeView() method is unreliable, sometimes returning null.  Either a bug in javafx, or really poorly documented...
        this.treeView = ltv;
        
        addEventFilter(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>()
        {
            @Override
            public void handle(MouseEvent event)
            {
                if (event.getClickCount() > 1)
                {
                  final LegoTreeItem treeItem = (LegoTreeItem) getTreeItem();
                  if (treeItem.getNodeType() != null)
                  {
                      if (LegoTreeNodeType.legoReference == treeItem.getNodeType())
                      {
                          LegoReference lr = (LegoReference) treeItem.getExtraData();
                          LegoGUI.getInstance().getLegoGUIController().beginLegoEdit(lr, treeItem);
                      }
                  }
                }
            }
        });
    }

    @Override
    public void updateItem(T item, boolean empty)
    {
        try
        {
            super.updateItem(item, empty);
            final LegoTreeItem treeItem = (LegoTreeItem) getTreeItem();
            ContextMenu cm = new ContextMenu();
            //This is the first time I really don't understand the JavaFX API.  It appears to reuse the item values, when scrolling up and down... 
            //So if the item type changes from one position to another, and you don't unset (or reset) all of the same properties that were set previously, 
            //Things go wonky when you scroll.  I think its a  bug... have to find time to dig into it more later...
            //see http://javafx-jira.kenai.com/browse/RT-19629
            setEditable(false);
            setText(null);
            setGraphic(null);
            getStyleClass().remove("boldLabel");
            styleProperty().unbind();
            //Clear the drop shadow and bold - workaround for non-clearing styles
            setStyle("-fx-effect: innershadow(two-pass-box , white , 0, 0.0 , 0 , 0);"); 
            setTooltip(null);
            
            if (empty || treeItem.getNodeType() == LegoTreeNodeType.blankLegoEndNode
                    || treeItem.getNodeType() == LegoTreeNodeType.blankLegoListEndNode)
            {
                if (treeView.getLego() != null
                        || (!empty && treeItem.getNodeType() == LegoTreeNodeType.blankLegoEndNode))
                {
                    MenuItem mi = new MenuItem("Add Assertion");
                    mi.setOnAction(new EventHandler<ActionEvent>()
                    {
                        @Override
                        public void handle(ActionEvent arg0)
                        {
                            addAssertion();
                        }
                    });
                    mi.setGraphic(Images.ADD.createImageView());
                    cm.getItems().add(mi);
                    
                    mi = new MenuItem("Paste Assertion");
                    mi.setOnAction(new EventHandler<ActionEvent>()
                    {
                        @Override
                        public void handle(ActionEvent arg0)
                        {
                            pasteAssertion();
                        }
                    });
                    mi.visibleProperty().bind(CustomClipboard.containsAssertion);
                    mi.setGraphic(Images.PASTE.createImageView());
                    cm.getItems().add(mi);

                    
                }
                else if (treeView.getLego() == null
                        || (!empty && treeItem.getNodeType() == LegoTreeNodeType.blankLegoListEndNode))
                {
                    MenuItem mi = new MenuItem("Create Lego List");
                    mi.setOnAction(new EventHandler<ActionEvent>()
                    {
                        @Override
                        public void handle(ActionEvent arg0)
                        {
                            LegoGUI.getInstance().showLegoListPropertiesDialog(null, null);
                        }
                    });
                    mi.setGraphic(Images.LEGO_ADD.createImageView());
                    cm.getItems().add(mi);
                }
            }
            else
            {
                if (treeItem.getNodeType() == LegoTreeNodeType.legoListByReference)
                {
                    Tooltip tp = new Tooltip(((LegoListByReference) treeItem.getExtraData()).getGroupDescription());
                    setTooltip(tp);
                    treeItem.setValue(((LegoListByReference) treeItem.getExtraData()).getGroupName());
                    addMenus((LegoListByReference) treeItem.getExtraData(), treeItem, cm);
                }
                else if (treeItem.getNodeType() == LegoTreeNodeType.legoReference)
                {
                    final LegoReference legoReference = (LegoReference)treeItem.getExtraData();
                    final Label l = new Label(TimeConvert.format(legoReference.getStampTime()));
                    
                    setGraphic(l);
                    LegoTab lt = LegoGUI.getInstance().getLegoGUIController().getLegoEditTabIfOpen(legoReference.getUniqueId());
                    if (lt == null)
                    {
                        l.setGraphic(legoReference.isNew() ? Images.LEGO_EDIT.createImageView() : Images.LEGO.createImageView());
                    }
                    else
                    {
                        l.setGraphic(lt.hasUnsavedChangesProperty().get() ? Images.LEGO_EDIT.createImageView() : Images.LEGO.createImageView());
                        lt.hasUnsavedChangesProperty().addListener(new ChangeListener<Boolean>()
                        {
                            @Override
                            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
                            {
                                l.setGraphic(newValue ? Images.LEGO_EDIT.createImageView() : Images.LEGO.createImageView());
                            }
                        });
                    }
                    StringProperty style = LegoGUI.getInstance().getLegoGUIController().getStyleForLego(legoReference); 
                    if (style != null)
                    {
                        styleProperty().bind(style);
                        //Hack to set the graphic back when a lego is closed.  The style is updated when the lego is closed, so we know we 
                        //can set the graphic back to the not-edited graphic.
                        style.addListener(new ChangeListener<String>()
                        {
                            @Override
                            public void changed(ObservableValue<? extends String> observable, String oldValue,
                                    String newValue)
                            {
                                LegoTab lt = LegoGUI.getInstance().getLegoGUIController().getLegoEditTabIfOpen(legoReference.getUniqueId());
                                if (lt == null)
                                {
                                    l.setGraphic(legoReference.isNew() ? Images.LEGO_EDIT.createImageView() : Images.LEGO.createImageView());
                                }
                            }
                        });
                    }
                    addMenus(legoReference, treeItem, cm);
                }
                else if (treeItem.getNodeType() == LegoTreeNodeType.status)
                {
                    HBox hbox = new HBox();
                    hbox.setSpacing(10.0);
                    hbox.setAlignment(Pos.CENTER_LEFT);
    
                    Label status = new Label("Status");
                    hbox.getChildren().add(status);
    
                    ChoiceBox<String> cb = new ChoiceBox<>(statusChoices_);
                    final Stamp stamp = (Stamp)treeItem.getExtraData();
                    String currentStatus = stamp.getStatus();
                    cb.getSelectionModel().select(currentStatus);
                    hbox.getChildren().add(cb);
                    setGraphic(hbox);
                    
                    cb.valueProperty().addListener(new ChangeListener<String>()
                    {
                        @Override
                        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
                        {
                            stamp.setStatus(newValue);
                            treeView.contentChanged();
                        }
                    });
                }
                else if (treeItem.getNodeType() == LegoTreeNodeType.pncsValue)
                {
                    MenuItem mi = new MenuItem("Create New Lego for PNCS");
                    mi.setOnAction(new EventHandler<ActionEvent>()
                    {
                        @Override
                        public void handle(ActionEvent arg0)
                        {
                            createNewLego(treeItem, false);
                        }
                    });
                    mi.setGraphic(Images.LEGO_ADD.createImageView());
                    cm.getItems().add(mi);
                    
                    mi = new MenuItem("Paste Lego (using this PNCS name / value)");
                    mi.setOnAction(new EventHandler<ActionEvent>()
                    {
                        @Override
                        public void handle(ActionEvent arg0)
                        {
                            createNewLego(treeItem, true);
                        }
                    });
                    mi.visibleProperty().bind(CustomClipboard.containsLego);
                    mi.setGraphic(Images.PASTE.createImageView());
                    cm.getItems().add(mi);
                }
                else if (treeItem.getNodeType() == LegoTreeNodeType.assertion)
                {
                    DropTargetLabel assertionLabel = new DropTargetLabel("Assertion", cm);
                    Assertion a = (Assertion) treeItem.getExtraData();
                    
                    HBox hbox = new HBox();
                    hbox.setSpacing(10.0);
                    hbox.setAlignment(Pos.CENTER_LEFT);
                    hbox.getChildren().add(assertionLabel);
                    hbox.getChildren().add(new CopyableLabel(a.getAssertionUUID()));
    
                    addMenus(a, treeItem, cm, assertionLabel);
                    setGraphic(hbox);
                    LegoGUI.getInstance().getLegoGUIController().addSnomedDropTarget(treeView.getLego(), assertionLabel);
                }
                else if (treeItem.getNodeType() == LegoTreeNodeType.assertionComponent)
                {
                    addMenus((AssertionComponent) treeItem.getExtraData(), treeItem, cm);
                }
                else if (treeItem.getNodeType() == LegoTreeNodeType.comment)
                {
                    final Lego lego = (Lego)treeItem.getExtraData();
                    final TextField tf = new TextField();
                    tf.setText(lego.getComment() == null ? "" : lego.getComment());
                    tf.textProperty().addListener(new ChangeListener<String>()
                    {
                        @Override
                        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
                        {
                            lego.setComment(newValue.length() == 0 ? null : newValue);
                            treeView.contentChanged();
                        }
                    });
                    setGraphic(Utility.prependLabel("Comment", tf));
                }
                else if (treeItem.getNodeType() == LegoTreeNodeType.assertionUUID)
                {
                    String value = treeItem.getValue();
                    final AssertionComponent ac = (AssertionComponent) ((LegoTreeItem) treeItem.getParent()).getExtraData();
                    final TextField tf = new TextField();
                    tf.setText(value == null ? "" : value);
                    if (tf.getText().length() == 0)
                    {
                        tf.setEffect(Utility.redDropShadow);
                    }
                    tf.setPromptText("UUID of another Assertion");
                    tf.textProperty().addListener(new ChangeListener<String>()
                    {
                        @Override
                        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
                        {
                            ac.setAssertionUUID(newValue);
                            try
                            {
                                UUID.fromString(newValue);
                                tf.setEffect(null);
                            }
                            catch (Exception e)
                            {
                                tf.setEffect(Utility.redDropShadow);
                            }
                            treeView.contentChanged();
                        }
                    });
                    setGraphic(Utility.prependLabel("Assertion UUID", tf));
                    addMenus(LegoTreeNodeType.assertionUUID, ac, cm, treeItem);
                }
                else if (treeItem.getNodeType() == LegoTreeNodeType.measurement || treeItem.getNodeType() == LegoTreeNodeType.timingMeasurement)
                {
                    if (!treeItem.isExpanded())
                    {
                        setGraphic(new Label(treeItem.getValue() + sep + SchemaSummary.summary((Measurement)treeItem.getExtraData())));
                    }
                    else
                    {
                        setGraphic(new Label(treeItem.getValue()));
                    }
                    addMenus((Measurement) treeItem.getExtraData(), treeItem, cm);
                }
                else if (treeItem.getNodeType() == LegoTreeNodeType.expressionValue
                        || treeItem.getNodeType() == LegoTreeNodeType.expressionDestination
                        || treeItem.getNodeType() == LegoTreeNodeType.expressionDiscernible
                        || treeItem.getNodeType() == LegoTreeNodeType.expressionQualifier
                        || treeItem.getNodeType() == LegoTreeNodeType.expressionOptional)
                {
                    Expression e = (Expression) treeItem.getExtraData();
                    DropTargetLabel label = new DropTargetLabel("", cm);
    
                    String descriptionAddition = "";
                    if (treeItem.getNodeType() == LegoTreeNodeType.expressionDestination)
                    {
                        descriptionAddition = "Destination ";
                    }
                    else if (treeItem.getNodeType() == LegoTreeNodeType.expressionDiscernible)
                    {
                        descriptionAddition = "Discernible ";
                    }
                    else if (treeItem.getNodeType() == LegoTreeNodeType.expressionQualifier)
                    {
                        descriptionAddition = "Qualifier ";
                    }
                    label.setText(descriptionAddition + treeItem.getValue());
    
                    addMenus(e, treeItem, cm, label, descriptionAddition);
                    
                    HBox hbox = new HBox();
                    hbox.getChildren().add(label);
                    
                    if (!treeItem.isExpanded())
                    {
                        hbox.getChildren().add(new Label(sep + SchemaSummary.summary((Expression)treeItem.getExtraData())));
                    }
                    
                    setGraphic(hbox);
                    LegoGUI.getInstance().getLegoGUIController().addSnomedDropTarget(treeView.getLego(), label);
                }
                else if (treeItem.getNodeType() == LegoTreeNodeType.concept
                        || treeItem.getNodeType() == LegoTreeNodeType.conceptOptional)
                {
                    Concept c = (Concept) treeItem.getExtraData();
                   
                    String label = "";
                    ConceptUsageType cut = findType(treeItem);
                    if (ConceptUsageType.TYPE == cut)
                    {
                        label = "Type";
                    }
                    else if (ConceptUsageType.UNITS == cut)
                    {
                        label = "Units";
                    }
    
                    ConceptNode cn = new ConceptNode(label, c, cut, treeItem.getNodeType(), treeView);
                    addMenus(c, cn, treeItem, cm);
                    setGraphic(cn.getNode());
                }
                else if (treeItem.getNodeType() == LegoTreeNodeType.relation)
                {
                    DropTargetLabel relLabel = new DropTargetLabel(treeItem.getValue(), cm);                
                    addMenus((Relation) treeItem.getExtraData(), treeItem, cm, relLabel);
                    
                    HBox hbox = new HBox();
                    hbox.getChildren().add(relLabel);
                    if (!treeItem.isExpanded())
                    {
                        hbox.getChildren().add(new Label(sep + SchemaSummary.summary((Relation)treeItem.getExtraData())));
                    }
                    setGraphic(hbox);
                    LegoGUI.getInstance().getLegoGUIController().addSnomedDropTarget(treeView.getLego(), relLabel);
                }
                else if (treeItem.getNodeType() == LegoTreeNodeType.relationshipGroup)
                {
                    HBox hbox = new HBox();
                    hbox.getChildren().add(new Label(treeItem.getValue()));
                    if (!treeItem.isExpanded())
                    {
                        hbox.getChildren().add(new Label(sep + SchemaSummary.summary((RelationGroup)treeItem.getExtraData())));
                    }
                    setGraphic(hbox);
                    addMenus((RelationGroup) treeItem.getExtraData(), treeItem, cm);
                }
                else if (treeItem.getNodeType() == LegoTreeNodeType.point)
                {
                    //odd, yes - on the point, we pass in the measurement.
                    final Measurement m = (Measurement) treeItem.getExtraData();
                    PointNode pn = new PointNode(m, PointNode.PointNodeType.point, treeView);
                    PointNodeValidator pnv = new PointNodeValidator();
                    pnv.addPointNode(pn);
                    pnv.check();
                    addMenus(m, treeItem, cm);
                    setGraphic(pn.getNode());
                }
                else if (treeItem.getNodeType() == LegoTreeNodeType.interval)
                {
                    //odd, yes - on the interval, we pass in the measurement.
                    final Measurement m = (Measurement) treeItem.getExtraData();
                    if (m.getInterval() == null)
                    {
                        m.setInterval(new Interval());
                    }
                    if (m.getInterval().getLowerBound() == null)
                    {
                        m.getInterval().setLowerBound(new Bound());
                    }
                    if (m.getInterval().getUpperBound() == null)
                    {
                        m.getInterval().setUpperBound(new Bound());
                    }
                    
                    PointNodeValidator pnv = new PointNodeValidator();
                    Node boundLow = makeBoundNode("Lower", m, m.getInterval().getLowerBound(), PointNodeType.intervalLowBoundLow, PointNodeType.intervalLowBoundHigh, pnv);
                    Node boundHigh = makeBoundNode("Upper", m, m.getInterval().getUpperBound(), PointNodeType.intervalHighBoundLow, PointNodeType.intervalHighBoundHigh, pnv);
                    
                    pnv.check();
                    VBox vbox = new VBox();
                    vbox.setSpacing(5.0);
                    vbox.getChildren().add(boundLow);
                    vbox.getChildren().add(boundHigh);
                    
                    addMenus(m, treeItem, cm);
                    setGraphic(vbox);
                }
                else if (treeItem.getNodeType() == LegoTreeNodeType.bound)
                {
                    //odd, yes - on the bound, we pass in the measurement.
                    final Measurement m = (Measurement) treeItem.getExtraData();
                    addMenus(m, treeItem, cm);
                    if (m.getBound() == null)
                    {
                        m.setBound(new Bound());
                    }
                    PointNodeValidator pnv = new PointNodeValidator();
                    setGraphic(makeBoundNode(null, m, m.getBound(), PointNodeType.boundLow, PointNodeType.boundHigh, pnv));
                    pnv.check();
                }
                else if (treeItem.getNodeType() == LegoTreeNodeType.value)
                {
                    DropTargetLabel valueLabel = new DropTargetLabel(treeItem.getValue(), cm);
                    addMenus((Value) treeItem.getExtraData(), treeItem, cm, valueLabel);
                    
                    HBox hbox = new HBox();
                    hbox.getChildren().add(valueLabel);
                    
                    if (!treeItem.isExpanded())
                    {
                        hbox.getChildren().add(new Label(sep + SchemaSummary.summary((Value)treeItem.getExtraData())));
                    }
                    setGraphic(hbox);
                    LegoGUI.getInstance().getLegoGUIController().addSnomedDropTarget(treeView.getLego(), valueLabel);
                }
                else if (treeItem.getNodeType() == LegoTreeNodeType.text)
                {
                    //The object passed in might be a Destination or a value for the text field.
                    String text = "";
                    String prefixLabel = "";
                    final Object parent = treeItem.getExtraData();
                    if (parent instanceof Value)
                    {
                        text = ((Value)parent).getText();
                    }
                    else if (parent instanceof Destination)
                    {
                        text = ((Destination)parent).getText();
                        prefixLabel = "Dest";
                    }
                    else
                    {
                        logger.error("Unexpected tree constrution");
                    }
                    final TextField tf = new TextField();
                    tf.setText(text);
                    if (tf.getText().length() == 0)
                    {
                        tf.setEffect(Utility.redDropShadow);
                    }
                    tf.setPromptText("Any Text");
                    tf.textProperty().addListener(new ChangeListener<String>()
                    {
                        @Override
                        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
                        {
                            if (parent instanceof Value)
                            {
                                ((Value)parent).setText(newValue);
                            }
                            else if (parent instanceof Destination)
                            {
                                ((Destination)parent).setText(newValue);
                            }
                            
                            if (newValue.length() > 0)
                            {
                                tf.setEffect(null);
                            }
                            else
                            {
                                tf.setEffect(Utility.redDropShadow);
                            }
                            treeView.contentChanged();
                        }
                    });
                    setGraphic((prefixLabel.length() > 0 ? Utility.prependLabel(prefixLabel, tf) : tf));
                    addMenus(treeItem.getNodeType(), parent, cm, treeItem);
                }
                else if (treeItem.getNodeType() == LegoTreeNodeType.bool)
                {
                    //The object passed in might be a Destination or a value for the text field.
                    Boolean bool = null;
                    String prefixLabel = "";
                    final Object parent = treeItem.getExtraData();
                    if (parent instanceof Value)
                    {
                        bool = ((Value)parent).isBoolean();
                    }
                    else if (parent instanceof Destination)
                    {
                        bool = ((Destination)parent).isBoolean();
                        prefixLabel = "Dest";
                    }
                    else
                    {
                        logger.error("Unexpected tree constrution");
                    }
                    ChoiceBox<String> cb = new ChoiceBox<>(booleanChoices_);
                    if (bool)
                    {
                        cb.getSelectionModel().select(0);
                    }
                    else
                    {
                        cb.getSelectionModel().select(1);
                    }
                    cb.valueProperty().addListener(new ChangeListener<String>()
                    {
                        @Override
                        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
                        {
                            if (parent instanceof Value)
                            {
                                ((Value)parent).setBoolean(new Boolean(newValue));
                            }
                            else if (parent instanceof Destination)
                            {
                                ((Destination)parent).setBoolean(new Boolean(newValue));
                            }
                            treeView.contentChanged();
                        }
                    });
                    setGraphic((prefixLabel.length() > 0 ? Utility.prependLabel(prefixLabel, cb) : cb));
                    addMenus(treeItem.getNodeType(), parent, cm, treeItem);
                }
            }
            // done with massive if/else
            //Deal with javafx memory leak (at least help)
            if (getContextMenu() != null)
            {
                for (MenuItem mi : getContextMenu().getItems())
                {
                    mi.visibleProperty().unbind();
                }
                setContextMenu(null);
            }
            if (cm.getItems().size() > 0)
            {
                setContextMenu(cm);
            }
            
            if (getGraphic() == null)
            {
                setText(item == null ? null : item.toString());
            }
            else
            {
                setText(null);
            }
        }
        catch (Exception e)
        {
            logger.error("Unexpected", e);
            LegoGUI.getInstance().showErrorDialog("Unexpected Error", "There was an unexpected problem building the tree", 
                    "Please report this as a bug.  " + e.toString());
        }
    }
    
    private Node makeBoundNode(String labelText, Measurement m, final Bound b, PointNode.PointNodeType lowType, PointNode.PointNodeType highType, PointNodeValidator pnv)
    {
        PointNode low = new PointNode(m, lowType, treeView);
        PointNode high = new PointNode(m, highType, treeView);
        
        pnv.addPointNode(low);
        pnv.addPointNode(high);
        
        HBox hbox = new HBox();
        hbox.setMaxWidth(Double.MAX_VALUE);
        hbox.setSpacing(1.0);
        if (labelText != null && labelText.length() > 0)
        {
            Label label = new Label(labelText);
            hbox.getChildren().add(label);
            HBox.setMargin(label, new Insets(4.0, 4.0, 0.0, 0.0));
        }
        hbox.getChildren().add(low.getNode());
        HBox.setHgrow(low.getNode(), Priority.SOMETIMES);
        final ComboBox<String> cbLow = new ComboBox<>(inclusiveChoices_);
        cbLow.setMaxWidth(10.0);
        cbLow.getSelectionModel().select((b.isLowerPointInclusive() != null && !b.isLowerPointInclusive().booleanValue() ? 1 : 0));
        cbLow.valueProperty().addListener(new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
            {
                b.setLowerPointInclusive(cbLow.getSelectionModel().getSelectedIndex() == 0);
                treeView.contentChanged();
            }
        });
        hbox.getChildren().add(cbLow);
        Label middle = new Label(" X ");
        middle.getStyleClass().add("boldLabel");
        HBox.setMargin(middle, new Insets(4.0, 0.0, 0.0, 0.0));
        hbox.getChildren().add(middle);
        final ComboBox<String> cbHigh = new ComboBox<>(inclusiveChoices_);
        cbHigh.setMaxWidth(10.0);
        cbHigh.getSelectionModel().select((b.isUpperPointInclusive() != null && !b.isUpperPointInclusive().booleanValue() ? 1 : 0));
        cbHigh.valueProperty().addListener(new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
            {
                b.setUpperPointInclusive(cbHigh.getSelectionModel().getSelectedIndex() == 0);
                treeView.contentChanged();
            }
        });
        hbox.getChildren().add(cbHigh);
        hbox.getChildren().add(high.getNode());
        HBox.setHgrow(high.getNode(), Priority.SOMETIMES);
        
        return hbox;
    }

    private void removeMeasurement(Measurement m, TreeItem<String> ti)
    {
        Object parent = ((LegoTreeItem) ti.getParent()).getExtraData();
        if (parent instanceof Value)
        {
            ((Value) parent).setMeasurement(null);
        }
        else if (parent instanceof Relation)
        {
            ((Relation) parent).getDestination().setMeasurement(null);
        }
        else if (parent instanceof Assertion)
        {
            ((Assertion) parent).setTiming(null);
        }
        else
        {
            logger.error("Dan messed up! - don't know how to remove measurement - type " + parent.toString());
            return;
        }
        Event.fireEvent(ti.getParent(),
                new TreeItem.TreeModificationEvent<String>(TreeItem.valueChangedEvent(), ti.getParent()));
        ti.getParent().getChildren().remove(ti);
        treeView.contentChanged();
    }

    private void addUnits(Measurement m, TreeItem<String> ti)
    {
        Units u = new Units();
        Concept c = new Concept();
        u.setConcept(c);
        m.setUnits(u);
        ti.getChildren().add(new LegoTreeItem(c, LegoTreeNodeType.conceptOptional));
        Utility.expandAll(ti);
        treeView.contentChanged();
        Event.fireEvent(ti, new TreeItem.TreeModificationEvent<String>(TreeItem.valueChangedEvent(), ti));
    }

    private void addInterval(Measurement m, TreeItem<String> ti)
    {
        Interval i = new Interval();
        Bound low = new Bound();
        Bound high = new Bound();
        i.setLowerBound(low);
        i.setUpperBound(high);
        m.setInterval(i);
        ti.getChildren().add(new LegoTreeItem(m, LegoTreeNodeType.interval));
        FXCollections.sort(ti.getChildren(), new LegoTreeItemComparator(true));
        Utility.expandAll(ti);
        treeView.contentChanged();
        Event.fireEvent(ti, new TreeItem.TreeModificationEvent<String>(TreeItem.valueChangedEvent(), ti));
    }

    private void removeInterval(Measurement m, TreeItem<String> ti)
    {
        m.setInterval(null);
        Event.fireEvent(ti.getParent(),
                new TreeItem.TreeModificationEvent<String>(TreeItem.valueChangedEvent(), ti.getParent()));
        ti.getParent().getChildren().remove(ti);
        treeView.contentChanged();
    }

    private void addPoint(Measurement m, TreeItem<String> ti)
    {
        ti.getChildren().add(new LegoTreeItem(m, LegoTreeNodeType.point));
        FXCollections.sort(ti.getChildren(), new LegoTreeItemComparator(true));
        Utility.expandAll(ti);
        treeView.contentChanged();
        Event.fireEvent(ti, new TreeItem.TreeModificationEvent<String>(TreeItem.valueChangedEvent(), ti));
    }
    
    private void removePoint(Measurement m, TreeItem<String> ti)
    {
        m.setPoint(null);
        Event.fireEvent(ti.getParent(),
                new TreeItem.TreeModificationEvent<String>(TreeItem.valueChangedEvent(), ti.getParent()));
        ti.getParent().getChildren().remove(ti);
        treeView.contentChanged();
    }
    
    private void addBound(Measurement m, TreeItem<String> ti)
    {
        Bound b = new Bound();
        m.setBound(b);
        ti.getChildren().add(new LegoTreeItem(m, LegoTreeNodeType.bound));
        FXCollections.sort(ti.getChildren(), new LegoTreeItemComparator(true));
        Utility.expandAll(ti);
        treeView.contentChanged();
        Event.fireEvent(ti, new TreeItem.TreeModificationEvent<String>(TreeItem.valueChangedEvent(), ti));
    }
    
    private void removeBound(Measurement m, TreeItem<String> ti)
    {
        m.setBound(null);
        Event.fireEvent(ti.getParent(),
                new TreeItem.TreeModificationEvent<String>(TreeItem.valueChangedEvent(), ti.getParent()));
        ti.getParent().getChildren().remove(ti);
        treeView.contentChanged();
    }

    private void addMeasurement(Object parent, LegoTreeNodeType pointBoundOrInterval, String unitsSCTID,
            TreeItem<String> ti)
    {
        Measurement m = new Measurement();
        LegoTreeNodeType type = LegoTreeNodeType.measurement;
        if (parent instanceof Relation)
        {
            ((Relation) parent).getDestination().setMeasurement(m);
        }
        else if (parent instanceof Value)
        {
            ((Value) parent).setMeasurement(m);
        }
        else if (parent instanceof Assertion)
        {
            ((Assertion) parent).setTiming(m);
            type = LegoTreeNodeType.timingMeasurement;
        }
        else
        {
            logger.error("Unhandled measurement add request: " + parent);
            return;
        }
        
        if (unitsSCTID != null)
        {
            Units u = new Units();
            Concept c = new Concept();
            c.setUuid(unitsSCTID);
            u.setConcept(c);
            m.setUnits(u);
        }
        
        if (pointBoundOrInterval != null)
        {
            if (LegoTreeNodeType.point == pointBoundOrInterval)
            {
                m.setPoint(new PointMeasurementConstant());  //purposefully don't set a value inside this
            }
            else if (LegoTreeNodeType.bound == pointBoundOrInterval)
            {
                m.setBound(new Bound());
            }
            else if (LegoTreeNodeType.interval == pointBoundOrInterval)
            {
                m.setInterval(new Interval());
            }
        }
        
        LegoTreeItem lti = new LegoTreeItem(m, type);
        ti.getChildren().add(lti);
        
        FXCollections.sort(ti.getChildren(), new LegoTreeItemComparator(true));
        Utility.expandAll(ti);
        treeView.contentChanged();
        Event.fireEvent(ti, new TreeItem.TreeModificationEvent<String>(TreeItem.valueChangedEvent(), ti));
    }

    private void addConcept(Object object, String sctUUID, TreeItem<String> ti)
    {
        Concept c = new Concept();
        c.setUuid(sctUUID);

        Expression expression;
        boolean addExpression = false;

        LegoTreeNodeType type = ((LegoTreeItem) ti).getNodeType();

        if (object instanceof Value)
        {
            Value v = (Value) object;
            if (v.getExpression() == null)
            {
                expression = new Expression();
                v.setExpression(expression);
                addExpression = true;
                type = LegoTreeNodeType.expressionValue;
            }
            else
            {
                expression = v.getExpression();
            }
        }
        else if (object instanceof Relation)
        {
            Relation rel = (Relation) object;
            if (rel.getDestination().getExpression() == null)
            {
                expression = new Expression();
                rel.getDestination().setExpression(expression);
                type = LegoTreeNodeType.expressionDestination;
                addExpression = true;
            }
            else
            {
                expression = rel.getDestination().getExpression();
            }
        }
        else if (object instanceof Expression)
        {
            expression = (Expression) object;
        }
        else
        {
            logger.error("Unhandled concept add: " + object);
            return;
        }

        if (expression.getConcept() == null && expression.getExpression().size() == 0)
        {
            expression.setConcept(c);
            LegoTreeItem lti;
            if (addExpression)
            {
                lti = new LegoTreeItem(expression, type);
                ti.getChildren().add(lti);
            }
            else
            {
                lti = new LegoTreeItem(c, LegoTreeNodeType.concept);
                ti.getChildren().add(lti);
            }
            Utility.expandAll(lti);
        }
        else  //We know it has a concept
        {
            //Becomes optional when on a conjunction.  Value is already optional, so don't need to switch that one.
            if (type == LegoTreeNodeType.expressionDiscernible || type == LegoTreeNodeType.expressionQualifier)
            {
                type = LegoTreeNodeType.expressionOptional;
            }

            //Not a conjunction yet - convert to one
            if (expression.getConcept() != null)
            {
                // need to convert to a conjunction
                Concept currentConcept = expression.getConcept();

                LegoTreeItem currentConceptTreeItem = null;
                for (TreeItem<String> childItem : ti.getChildren())
                {
                    LegoTreeItem child = (LegoTreeItem) childItem;
                    if (child.getExtraData() instanceof Concept)
                    {
                        currentConceptTreeItem = child;
                        break;
                    }
                }
                if (currentConceptTreeItem == null)
                {
                    logger.error("Couldn't find the tree item!");
                }
                else
                {
                    Expression e = new Expression();
                    e.setConcept(currentConcept);
                    expression.setConcept(null);
                    expression.getExpression().add(e);

                    LegoTreeItem lti = new LegoTreeItem(e, type);
                    ti.getChildren().add(lti);
                    ti.getChildren().remove(currentConceptTreeItem);
                    Utility.expandAll(lti);
                }
            }

            //Add to the conjunction
            Expression newExpression = new Expression();
            newExpression.setConcept(c);
            expression.getExpression().add(newExpression);
            LegoTreeItem lti = new LegoTreeItem(newExpression, type);
            ti.getChildren().add(lti);
            Utility.expandAll(lti);
        }
        FXCollections.sort(ti.getChildren(), new LegoTreeItemComparator(true));
        ti.setExpanded(true);
        treeView.contentChanged();
        Event.fireEvent(ti, new TreeItem.TreeModificationEvent<String>(TreeItem.valueChangedEvent(), ti));
    }

    private void removeConcept(TreeItem<String> ti)
    {
        Object parent = ((LegoTreeItem) ti.getParent()).getExtraData();
        if (parent instanceof Measurement)
        {
            ((Measurement) parent).setUnits(null);
        }
        else
        {
            logger.error("Unhandled concept remove request: " + parent);
            return;
        }
        Event.fireEvent(ti.getParent(),
                new TreeItem.TreeModificationEvent<String>(TreeItem.valueChangedEvent(), ti.getParent()));
        ti.getParent().getChildren().remove(ti);
        treeView.contentChanged();
    }
    
    private void pasteExpression(Expression oldExpression, LegoTreeItem oldTreeItem)
    {
        try
        {
            Expression e;
            if (CustomClipboard.containsType(Expression.class))
            {
                e = CustomClipboard.getExpression();
            }
            else if (CustomClipboard.containsType(Discernible.class))
            {
                e = CustomClipboard.getDiscernible().getExpression();
            }
            else if (CustomClipboard.containsType(Qualifier.class))
            {
                e = CustomClipboard.getQualifier().getExpression();
            }
            else
            {
                CustomClipboard.updateBindings();
                LegoGUI.getInstance().showErrorDialog("No Pasteable Expression on Clipboard",
                        "The Clipboard does not contain an Expression", null);
                return;
            }

            // Ugly but easy clone impl...
            Expression clonedExpression = LegoXMLUtils.readExpression(LegoXMLUtils.toXML(e));

            boolean expand = oldTreeItem.isExpanded();
            LegoTreeItem parentLegoTreeItem = (LegoTreeItem) oldTreeItem.getParent();

            LegoTreeItem newLTI = null;
            if (oldTreeItem.getNodeType() == LegoTreeNodeType.expressionDiscernible)
            {
                Assertion a = (Assertion) parentLegoTreeItem.getExtraData();
                a.getDiscernible().setExpression(clonedExpression);
                newLTI = new LegoTreeItem(clonedExpression, LegoTreeNodeType.expressionDiscernible);
            }
            else if (oldTreeItem.getNodeType() == LegoTreeNodeType.expressionQualifier)
            {
                Assertion a = (Assertion) parentLegoTreeItem.getExtraData();
                a.getQualifier().setExpression(clonedExpression);
                newLTI = new LegoTreeItem(clonedExpression, LegoTreeNodeType.expressionQualifier);
            }
            else if (oldTreeItem.getNodeType() == LegoTreeNodeType.expressionValue)
            {
                Value v = (Value) parentLegoTreeItem.getExtraData();
                v.setExpression(clonedExpression);
                newLTI = new LegoTreeItem(clonedExpression, LegoTreeNodeType.expressionValue);
            }
            else if (oldTreeItem.getNodeType() == LegoTreeNodeType.expressionDestination)
            {
                Relation r = (Relation) parentLegoTreeItem.getExtraData();
                r.getDestination().setExpression(clonedExpression);
                newLTI = new LegoTreeItem(clonedExpression, LegoTreeNodeType.expressionDestination);
            }
            else if (oldTreeItem.getNodeType() == LegoTreeNodeType.expressionOptional)
            {
                Expression parentExpression = (Expression) parentLegoTreeItem.getExtraData();
                parentExpression.getExpression().remove(oldExpression);
                parentExpression.getExpression().add(clonedExpression);
                newLTI = new LegoTreeItem(clonedExpression, LegoTreeNodeType.expressionOptional);
            }
            else
            {
                // I think I have all the cases handled....
                logger.error("Unhandled paste type in expression "
                        + parentLegoTreeItem.getExtraData().getClass().getName());
                LegoGUI.getInstance().showErrorDialog("Unhandled paste operation", "Unhandled paste operation",
                        "Please file a bug with your log file attached");
                return;
            }

            // Need to delete the old value tree node
            parentLegoTreeItem.getChildren().remove(oldTreeItem);

            newLTI.setExpanded(expand);
            parentLegoTreeItem.getChildren().add(newLTI);
            FXCollections.sort(parentLegoTreeItem.getChildren(), new LegoTreeItemComparator(true));
            treeView.contentChanged();
            Event.fireEvent(parentLegoTreeItem, new TreeItem.TreeModificationEvent<String>(
                    TreeItem.valueChangedEvent(), parentLegoTreeItem));

        }
        catch (JAXBException e)
        {
            logger.error("Unexpected error handling paste", e);
            LegoGUI.getInstance().showErrorDialog("Unexpected Error during paste", "Unexpected error during paste",
                    e.toString());
        }
    }

    private void removeExpression(Expression e, TreeItem<String> ti)
    {
        Object parent = ((LegoTreeItem) ti.getParent()).getExtraData();
        if (parent instanceof Value)
        {
            ((Value) parent).setExpression(null);
        }
        else if (parent instanceof Relation)
        {
            ((Relation) parent).getDestination().setExpression(null);
        }
        else if (parent instanceof Expression)
        {
            Expression parentExpression = (Expression) parent;
            if (!parentExpression.getExpression().remove(e))
            {
                logger.error("Failed to delete expression?");
            }
            if (parentExpression.getExpression().size() == 1)
            {
                // convert back to a non-conjunction
                Expression conjunctionExpression = parentExpression.getExpression().get(0);
                parentExpression.getExpression().clear();
                parentExpression.setConcept(conjunctionExpression.getConcept());
                parentExpression.getRelation().addAll(conjunctionExpression.getRelation());
                parentExpression.getRelationGroup().addAll(conjunctionExpression.getRelationGroup());
                ti.getParent().getChildren()
                        .add(new LegoTreeItem(conjunctionExpression.getConcept(), LegoTreeNodeType.concept));

                for (TreeItem<String> sibling : ti.getParent().getChildren())
                {
                    if (((LegoTreeItem) sibling).getExtraData() == conjunctionExpression)
                    {
                        ti.getParent().getChildren().remove(sibling);
                        break;
                    }
                }
                FXCollections.sort(ti.getParent().getChildren(), new LegoTreeItemComparator(true));
            }
        }
        Event.fireEvent(ti.getParent(),
                new TreeItem.TreeModificationEvent<String>(TreeItem.valueChangedEvent(), ti.getParent()));
        ti.getParent().getChildren().remove(ti);
        treeView.contentChanged();
    }

    private void addRelation(Expression e, String sctUUID, TreeItem<String> ti)
    {
        Relation r = new Relation();
        if (sctUUID != null)
        {
            Concept c = new Concept();
            c.setUuid(sctUUID);
            Type t = new Type();
            r.setType(t);
            t.setConcept(c);
        }

        e.getRelation().add(r);
        LegoTreeItem lti = new LegoTreeItem(r);
        ti.getChildren().add(lti);
        Utility.expandAll(lti);
        ti.setExpanded(true);
        treeView.contentChanged();
        Event.fireEvent(ti, new TreeItem.TreeModificationEvent<String>(TreeItem.valueChangedEvent(), ti));
    }

    private void addRelation(RelationGroup rg, TreeItem<String> ti)
    {
        Relation r = new Relation();
        rg.getRelation().add(r);
        LegoTreeItem lti = new LegoTreeItem(r);
        ti.getChildren().add(lti);
        Utility.expandAll(lti);
        ti.setExpanded(true);
        treeView.contentChanged();
        Event.fireEvent(ti, new TreeItem.TreeModificationEvent<String>(TreeItem.valueChangedEvent(), ti));
    }

    private void removeRelation(Relation rel, TreeItem<String> ti)
    {
        Object parent = ((LegoTreeItem) ti.getParent()).getExtraData();
        if (parent instanceof Expression)
        {
            ((Expression) parent).getRelation().remove(rel);
        }
        else if (parent instanceof RelationGroup)
        {
            ((RelationGroup) parent).getRelation().remove(rel);
        }
        else
        {
            logger.error("Unhandled rel remove request");
        }

        // No need to fire a parent event here - the options on Expression don't change with add/remove of a rel.
        ti.getParent().getChildren().remove(ti);
        treeView.contentChanged();
    }

    private void removeRelationGroup(TreeItem<String> ti)
    {
        Expression e = (Expression) ((LegoTreeItem) ti.getParent()).getExtraData();
        e.getRelationGroup().clear();

        // No need to fire a parent event here - the options on Expression don't change with add/remove of a rel.
        ti.getParent().getChildren().remove(ti);
        treeView.contentChanged();
    }

    private void addRelationshipGroup(Expression e, TreeItem<String> ti)
    {
        RelationGroup rg = new RelationGroup();
        e.getRelationGroup().add(rg);
        ti.getChildren().add(new LegoTreeItem(rg));
        Utility.expandAll(ti);
        treeView.contentChanged();
        Event.fireEvent(ti, new TreeItem.TreeModificationEvent<String>(TreeItem.valueChangedEvent(), ti));
    }
    
    private void pasteValue(Assertion parentAssertion, TreeItem<String> parentTreeItem)
    {
        try
        {
            if (CustomClipboard.containsType(Value.class))
            {
                Value v = CustomClipboard.getValue();
                //need to clone this assertion - then change the UUID.
                //Ugly but easy clone impl...
                Value clonedValue = LegoXMLUtils.readValue(LegoXMLUtils.toXML(v));

                boolean expand = true;
                if (parentAssertion.getValue() != null)
                {
                    //Need to delete the old value tree node
                    for (TreeItem<String> lti : parentTreeItem.getChildren())
                    {
                        if (((LegoTreeItem)lti).getExtraData() instanceof Value)
                        {
                            expand = lti.isExpanded();
                            parentTreeItem.getChildren().remove(lti);
                            break;
                        }
                    }
                }
                parentAssertion.setValue(clonedValue);
                LegoTreeItem lti = new LegoTreeItem(clonedValue);
                parentTreeItem.getChildren().add(lti);
                FXCollections.sort(parentTreeItem.getChildren(), new LegoTreeItemComparator(true));
                lti.setExpanded(expand);
                treeView.contentChanged();
                Event.fireEvent(parentTreeItem, new TreeItem.TreeModificationEvent<String>(TreeItem.valueChangedEvent(), parentTreeItem));
            }
            else
            {
                CustomClipboard.updateBindings();
                LegoGUI.getInstance().showErrorDialog("No Value on Clipboard", "The Clipboard does not contain a Value", null);
            }
        }
        catch (JAXBException e)
        {
            logger.error("Unexpected error handling paste", e);
            LegoGUI.getInstance().showErrorDialog("Unexpected Error during paste", "Unexpected error during paste", e.toString());
        }
    }

    private void addValue(Assertion a, String sctUUID, TreeItem<String> ti)
    {
        Value v = new Value();
        a.setValue(v);
        Expression e = new Expression();
        v.setExpression(e);
        if (sctUUID != null)
        {
            Concept c = new Concept();
            c.setUuid(sctUUID);
            e.setConcept(c);
        }
        LegoTreeItem lti = new LegoTreeItem(v);
        ti.getChildren().add(lti);
        FXCollections.sort(ti.getChildren(), new LegoTreeItemComparator(true));
        Utility.expandAll(lti);
        treeView.contentChanged();
        Event.fireEvent(ti, new TreeItem.TreeModificationEvent<String>(TreeItem.valueChangedEvent(), ti));
    }

    private void addQualifier(Assertion a, String sctUUID, TreeItem<String> ti)
    {
        Qualifier q = new Qualifier();
        a.setQualifier(q);
        Expression e = new Expression();
        q.setExpression(e);
        Concept c = new Concept();
        c.setUuid(sctUUID);
        e.setConcept(c);
        LegoTreeItem lti = new LegoTreeItem(e, LegoTreeNodeType.expressionQualifier);
        ti.getChildren().add(lti);
        FXCollections.sort(ti.getChildren(), new LegoTreeItemComparator(true));
        Utility.expandAll(lti);
        treeView.contentChanged();
        Event.fireEvent(ti, new TreeItem.TreeModificationEvent<String>(TreeItem.valueChangedEvent(), ti));
    }

    private void addDiscernibile(Assertion a, String sctUUID, TreeItem<String> ti)
    {
        Discernible d = new Discernible();
        a.setDiscernible(d);
        Expression e = new Expression();
        d.setExpression(e);
        Concept c = new Concept();
        c.setUuid(sctUUID);
        e.setConcept(c);
        LegoTreeItem lti = new LegoTreeItem(e, LegoTreeNodeType.expressionDiscernible);
        ti.getChildren().add(lti);
        FXCollections.sort(ti.getChildren(), new LegoTreeItemComparator(true));
        Utility.expandAll(lti);
        treeView.contentChanged();
        Event.fireEvent(ti, new TreeItem.TreeModificationEvent<String>(TreeItem.valueChangedEvent(), ti));
    }

    private void addTiming(Assertion a, LegoTreeNodeType pointBoundOrInterval, String sctUUID, TreeItem<String> ti)
    {
        Measurement m = new Measurement();
        if (sctUUID != null)
        {
            Units u = new Units();
            Concept c = new Concept();
            c.setUuid(sctUUID);
            u.setConcept(c);
            m.setUnits(u);
        }
        a.setTiming(m);

        if (pointBoundOrInterval != null)
        {
            if (LegoTreeNodeType.point == pointBoundOrInterval)
            {
                m.setPoint(new PointMeasurementConstant());  //purposefully don't set a value inside this
            }
            else if (LegoTreeNodeType.bound == pointBoundOrInterval)
            {
                m.setBound(new Bound());
            }
            else if (LegoTreeNodeType.interval == pointBoundOrInterval)
            {
                m.setInterval(new Interval());
            }
        }
        
        LegoTreeItem lti = new LegoTreeItem(m, LegoTreeNodeType.timingMeasurement);
        ti.getChildren().add(lti);
        FXCollections.sort(ti.getChildren(), new LegoTreeItemComparator(true));
        Utility.expandAll(lti);
        ti.setExpanded(true);
        treeView.contentChanged();
        Event.fireEvent(ti, new TreeItem.TreeModificationEvent<String>(TreeItem.valueChangedEvent(), ti));
    }

    private void addAssertionComponent(Assertion a, TreeItem<String> ti)
    {
        AssertionComponent ac = new AssertionComponent();
        a.getAssertionComponent().add(ac);
        treeView.contentChanged();
        LegoTreeItem lti = new LegoTreeItem(ac);
        ti.getChildren().add(lti);
        FXCollections.sort(ti.getChildren(), new LegoTreeItemComparator(true));
        Utility.expandAll(lti);
        Event.fireEvent(ti, new TreeItem.TreeModificationEvent<String>(TreeItem.valueChangedEvent(), ti));
    }

    private void removeAssertionComponent(AssertionComponent ac, TreeItem<String> ti)
    {
        Assertion a = (Assertion) ((LegoTreeItem) ti.getParent()).getExtraData();
        a.getAssertionComponent().remove(ac);
        // No need to fire a parent event here - the options on AssertionComponents don't change with add/remove of a
        // component.
        ti.getParent().getChildren().remove(ti);
        treeView.contentChanged();
    }

    private void removeAssertion(Assertion a, TreeItem<String> ti)
    {
        LegoTreeView ltv = (LegoTreeView) getTreeView();
        for (Assertion assertion : ltv.getLego().getAssertion())
        {
            if (assertion.getAssertionUUID().equals(a.getAssertionUUID()))
            {
                ltv.getLego().getAssertion().remove(assertion);
                break;
            }
        }
        ltv.getRoot().getChildren().remove(ti);
        treeView.contentChanged();
        // No need to fire event here, there is no parent to notify
    }
    
    private void addText(Object parent, TreeItem<String> ti)
    {
        LegoTreeItem lti = null;
        if (parent instanceof Value)
        {
            ((Value)parent).setText("");
            lti = new LegoTreeItem((Value)parent, LegoTreeNodeType.text);
        }
        else if (parent instanceof Destination)
        {
            ((Destination)parent).setText("");
            lti = new LegoTreeItem((Destination)parent, LegoTreeNodeType.text);
        }
        else
        {
            logger.error("Unexpected addText call");
        }
        treeView.contentChanged();
        ti.getChildren().add(lti);
        Utility.expandAll(lti);
        Event.fireEvent(ti, new TreeItem.TreeModificationEvent<String>(TreeItem.valueChangedEvent(), ti));
    }
    
    private void removeText(Object parent, TreeItem<String> ti)
    {
        if (parent instanceof Value)
        {
            ((Value)parent).setText(null);
        }
        else if (parent instanceof Destination)
        {
            ((Destination)parent).setText(null);
        }
        else
        {
            logger.error("Unexpected removeText call");
        }
        Event.fireEvent(ti.getParent(),
                new TreeItem.TreeModificationEvent<String>(TreeItem.valueChangedEvent(), ti.getParent()));
        ti.getParent().getChildren().remove(ti);
        treeView.contentChanged();
    }
    
    private void addBoolean(Object parent, TreeItem<String> ti)
    {
        LegoTreeItem lti = null;
        if (parent instanceof Value)
        {
            ((Value)parent).setBoolean(new Boolean(true));
            lti = new LegoTreeItem((Value)parent, LegoTreeNodeType.bool);
        }
        else if (parent instanceof Destination)
        {
            ((Destination)parent).setBoolean(new Boolean(true));
            lti = new LegoTreeItem((Destination)parent, LegoTreeNodeType.bool);
        }
        else
        {
            logger.error("Unexpected addBoolean call");
        }
        treeView.contentChanged();
        ti.getChildren().add(lti);
        FXCollections.sort(ti.getChildren(), new LegoTreeItemComparator(true));
        Utility.expandAll(lti);
        Event.fireEvent(ti, new TreeItem.TreeModificationEvent<String>(TreeItem.valueChangedEvent(), ti));
    }
    
    private void removeBoolean(Object parent, TreeItem<String> ti)
    {
        if (parent instanceof Value)
        {
            ((Value)parent).setBoolean(null);
        }
        else if (parent instanceof Destination)
        {
            ((Destination)parent).setBoolean(null);
        }
        else
        {
            logger.error("Unexpected removeBoolean call");
        }
        Event.fireEvent(ti.getParent(),
                new TreeItem.TreeModificationEvent<String>(TreeItem.valueChangedEvent(), ti.getParent()));
        ti.getParent().getChildren().remove(ti);
        treeView.contentChanged();
    }
    
    private void pasteAssertion()
    {
        try
        {
            if (CustomClipboard.containsType(Assertion.class))
            {
                Assertion a = CustomClipboard.getAssertion();
                //need to clone this assertion - then change the UUID.
                //Ugly but easy clone impl...
                Assertion clonedAssertion = LegoXMLUtils.readAssertion(LegoXMLUtils.toXML(a));
                
                LegoTreeView ltv = (LegoTreeView) getTreeView();
                //Change the Assertion UUID 
                clonedAssertion.setAssertionUUID(UUID.randomUUID().toString());
                ltv.getLego().getAssertion().add(clonedAssertion);
                treeView.contentChanged();
            
                LegoTreeItem lti = new LegoTreeItem(clonedAssertion);
                ltv.getRoot().getChildren().add(lti);
                FXCollections.sort(ltv.getRoot().getChildren(), new LegoTreeItemComparator(true));
                lti.setExpanded(true);
            }
            else
            {
                CustomClipboard.updateBindings();
                LegoGUI.getInstance().showErrorDialog("No Assertion on Clipboard", "The Clipboard does not contain an Assertion", null);
            }
        }
        catch (JAXBException e)
        {
            logger.error("Unexpected error handling paste", e);
            LegoGUI.getInstance().showErrorDialog("Unexpected Error during paste", "Unexpected error during paste", e.toString());
        }
    }

    private void addAssertion()
    {
        LegoTreeView ltv = (LegoTreeView) getTreeView();
        Assertion a = new Assertion();
        a.setAssertionUUID(UUID.randomUUID().toString());
        ltv.getLego().getAssertion().add(a);
        treeView.contentChanged();

        LegoTreeItem lti = new LegoTreeItem(a);
        ltv.getRoot().getChildren().add(lti);
        FXCollections.sort(ltv.getRoot().getChildren(), new LegoTreeItemComparator(true));
        Utility.expandAll(lti);
    }

    private void createNewLego(LegoTreeItem ti, boolean fromPaste)
    {
        if (ti.getNodeType() == LegoTreeNodeType.pncsValue)
        {
            String pncsValue = ti.getValue();
            String pncsName = ti.getParent().getValue();
    
            // ID can be grabbed from any child lego of this treeItem.
            // Should always be at least one child
            int pncsId = ((LegoReference) ((LegoTreeItem) ti.getChildren().get(0)).getExtraData()).getPncs().getId();
            LegoListByReference llbr = (LegoListByReference) ((LegoTreeItem) ti.getParent().getParent()).getExtraData();
            
            Pncs pncs = new Pncs();
            pncs.setName(pncsName);
            pncs.setValue(pncsValue);
            pncs.setId(pncsId);
            
            UserPreferences up = LegoGUIModel.getInstance().getUserPreferences(); 
            
            Lego l;
            if (fromPaste)
            {
                l = CustomClipboard.getLego();
                if (l == null)
                {
                    LegoGUI.getInstance().showErrorDialog("Not a Lego", "The Clipboard does not contain a Lego", null);
                    CustomClipboard.updateBindings();
                    return;
                }
                Stamp s = l.getStamp();
                if (s == null)
                {
                    //set everything
                    s = new Stamp();
                    s.setAuthor(up.getAuthor());
                    s.setModule(up.getModule());
                    s.setPath(up.getPath());
                    s.setStatus(statusChoices_.get(0));
                    s.setTime(TimeConvert.convert(System.currentTimeMillis()));
                    s.setUuid(UUID.randomUUID().toString());
                    l.setStamp(s);
                }
                else
                {
                    //just set the author, time and uuid
                    s.setAuthor(up.getAuthor());
                    s.setTime(TimeConvert.convert(System.currentTimeMillis()));
                    s.setUuid(UUID.randomUUID().toString());
                }
                l.setStamp(s);
                
                //Change all the assertion UUIDs
                for (Assertion a : l.getAssertion())
                {
                    a.setAssertionUUID(UUID.randomUUID().toString());
                }
            }
            else
            {
                l = new Lego();
        
                Stamp s = new Stamp();
                s.setAuthor(up.getAuthor());
                s.setModule(up.getModule());
                s.setPath(up.getPath());
                s.setStatus(statusChoices_.get(0));
                s.setTime(TimeConvert.convert(System.currentTimeMillis()));
                s.setUuid(UUID.randomUUID().toString());
                l.setStamp(s);
        
                Assertion a = new Assertion();
                a.setAssertionUUID(UUID.randomUUID().toString());
                l.getAssertion().add(a);
            }
    
            l.setPncs(pncs);
            l.setLegoUUID(UUID.randomUUID().toString());
            
            LegoReference lr = new LegoReference(l);
            lr.setIsNew(true);
            llbr.getLegoReference().add(lr);
            LegoTreeItem lti = new LegoTreeItem(lr);
            ti.getChildren().add(lti);
            LegoTreeView ltv = (LegoTreeView) getTreeView();
            ltv.getSelectionModel().select(lti);
            LegoGUI.getInstance().getLegoGUIController().addNewLego(llbr.getLegoListUUID(), l);
        }
        else
        {
            logger.error("Unhandled create lego request!");
        }
    }
    
    private void addMenus(final LegoListByReference llbr, final LegoTreeItem treeItem, ContextMenu cm)
    {
        MenuItem mi;
        mi = new MenuItem("Create New Lego Within Lego List");
        mi.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent arg0)
            {
                LegoGUI.getInstance().showCreateLegoDialog(llbr, treeItem, false);
            }
        });
        mi.setGraphic(Images.LEGO_ADD.createImageView());
        cm.getItems().add(mi);
        
        mi = new MenuItem("Paste Lego into Lego List");
        mi.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent arg0)
            {
                LegoGUI.getInstance().showCreateLegoDialog(llbr, treeItem, true);
            }
        });
        mi.visibleProperty().bind(CustomClipboard.containsLego);
        mi.setGraphic(Images.PASTE.createImageView());
        cm.getItems().add(mi);

        mi = new MenuItem("Show XML View");
        mi.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent arg0)
            {
                LegoGUI.getInstance().showXMLViewWindow(BDBDataStoreImpl.getInstance().getLegoListByID(llbr.getLegoListUUID()));
            }
        });
        mi.setGraphic(Images.XML_VIEW_16.createImageView());
        cm.getItems().add(mi);
        
        mi = new MenuItem("Properties");
        mi.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent arg0)
            {
                LegoGUI.getInstance().showLegoListPropertiesDialog(llbr, treeItem);
            }
        });
        mi.setGraphic(Images.PROPERTIES.createImageView());
        cm.getItems().add(mi);
        
        mi = new MenuItem("Export as XML");
        mi.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent arg0)
            {
                Platform.runLater(new Runnable()
                {
                    
                    @Override
                    public void run()
                    {
                        LegoGUIModel.getInstance().exportLegoList(llbr);
                    }
                });
            }
        });
        mi.setGraphic(Images.LEGO_EXPORT.createImageView());
        cm.getItems().add(mi);
        
        mi = new MenuItem("Delete Lego List");
        mi.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent arg0)
            {
                Answer a = LegoGUI.getInstance().showYesNoDialog("Really delete Lego List?", "Are you sure that you want to delete the Lego List?  "
                        + "This will delete all contained Legos.");
                if (a == Answer.YES)
                {
                    try
                    {
                        LegoGUIModel.getInstance().removeLegoList(llbr);
                    }
                    catch (WriteException e)
                    {
                        logger.error("Error deleting lego list", e);
                        LegoGUI.getInstance().showErrorDialog("Error Removing Lego List", "Unexpected error removing lego list", e.toString());
                    }
                }
            }
        });
        mi.setGraphic(Images.LEGO_DELETE.createImageView());
        cm.getItems().add(mi);
    }
    
    private void addMenus(final LegoReference legoReference, final LegoTreeItem lti, ContextMenu cm)
    {
        MenuItem mi;
        mi = new MenuItem("Delete Lego");
        mi.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent arg0)
            {
                Answer a = LegoGUI.getInstance().showYesNoDialog("Really delete Lego?", "Are you sure that you want to delete the Lego?");
                if (a == Answer.YES)
                {
                    //From legoReference treeItem, go up past pncsName and pncs value to get the LegoListReference
                    try
                    {
                        LegoGUIModel.getInstance().removeLego(
                                (LegoListByReference)((LegoTreeItem)lti.getParent().getParent().getParent()).getExtraData(), legoReference);
                    }
                    catch (WriteException e)
                    {
                        logger.error("Error deleting lego", e);
                        LegoGUI.getInstance().showErrorDialog("Error Removing Lego", "Unexpected error removing lego", e.toString());
                    }
                }
                
            }
        });
        mi.setGraphic(Images.LEGO_DELETE.createImageView());
        cm.getItems().add(mi);
        
        if (!legoReference.isNew())
        {
            mi = new MenuItem("Copy Lego");
            mi.setOnAction(new EventHandler<ActionEvent>()
            {
                @Override
                public void handle(ActionEvent arg0)
                {
                    Lego lego = BDBDataStoreImpl.getInstance().getLego(legoReference.getLegoUUID(), legoReference.getStampUUID());
                    if (lego != null)
                    {
                        CustomClipboard.set(lego);
                    }
                    else
                    {
                        LegoGUI.getInstance().showErrorDialog("Lego Not Found", "Couldn't find the desired Lego in the Database", 
                                "Legos can only be copied after they have been stored to the database.");
                    }
                }
            });
            mi.setGraphic(Images.COPY.createImageView());
            cm.getItems().add(mi);
            
            mi = new MenuItem("Create Template...");
            mi.setOnAction(new EventHandler<ActionEvent>()
            {
                @Override
                public void handle(ActionEvent arg0)
                {
                    Lego lego = BDBDataStoreImpl.getInstance().getLego(legoReference.getLegoUUID(), legoReference.getStampUUID());
                    if (lego != null)
                    {
                        LegoGUI.getInstance().showCreateTemplateDialog(lego);
                    }
                    else
                    {
                        LegoGUI.getInstance().showErrorDialog("Lego Not Found", "Couldn't find the desired Lego in the Database", 
                                "Legos can only be used as a template after they have been stored to the database.");
                    }
                }
            });
            mi.setGraphic(Images.TEMPLATE.createImageView());
            cm.getItems().add(mi);
        }
    }
    
    private void addMenus(LegoTreeNodeType type, final Object parent, ContextMenu cm, final TreeItem<String> treeItem)
    {
        MenuItem mi;
        if (type == LegoTreeNodeType.assertionUUID)
        {
            final AssertionComponent ac = (AssertionComponent) parent;
            mi = new MenuItem("View the defining Lego");
            mi.setOnAction(new EventHandler<ActionEvent>()
            {
                @Override
                public void handle(ActionEvent arg0)
                {
                    String uuid = ac.getAssertionUUID();
                    try
                    {
                        UUID.fromString(uuid);
                        List<Lego> result = BDBDataStoreImpl.getInstance().getLegosContainingAssertion(uuid);
                        if (result.size() == 0)
                        {
                            LegoGUI.getInstance().showErrorDialog("UUID Not Found", "No Lego could be found which contains the specified ID", "");
                        }
                        else
                        {
                            Lego newest = result.get(0);
                            for (int i = 1; i < result.size(); i++)
                            {
                                if (result.get(i).getStamp().getTime().toGregorianCalendar().getTimeInMillis() > 
                                    newest.getStamp().getTime().toGregorianCalendar().getTimeInMillis())
                                {
                                    newest = result.get(i);
                                }
                            }
                            LegoGUI.getInstance().getLegoGUIController().beginLegoEdit(new LegoReference(newest), null);
                        }
                    }
                    catch (Exception e)
                    {
                        LegoGUI.getInstance().showErrorDialog("Invalid UUID", "The Lego Assertion UUID must be a valid UUID", "");
                    }
                }
            });
            mi.setGraphic(Images.LEGO.createImageView());
            cm.getItems().add(mi);
        }
        else if (type == LegoTreeNodeType.text)
        {
            mi = new MenuItem("Remove Text");
            mi.setOnAction(new EventHandler<ActionEvent>()
            {
                @Override
                public void handle(ActionEvent arg0)
                {
                    removeText(parent, treeItem);
                }
            });
            mi.setGraphic(Images.DELETE.createImageView());
            cm.getItems().add(mi);
        }
        else if (type == LegoTreeNodeType.bool)
        {
            mi = new MenuItem("Remove Boolean");
            mi.setOnAction(new EventHandler<ActionEvent>()
            {
                @Override
                public void handle(ActionEvent arg0)
                {
                    removeBoolean(parent, treeItem);
                }
            });
            mi.setGraphic(Images.DELETE.createImageView());
            cm.getItems().add(mi);
        }
        else
        {
            throw new RuntimeException("oops");
        }
    }
    
    private void addMenus(final Assertion a, final LegoTreeItem treeItem, ContextMenu cm, final DropTargetLabel label)
    {
        MenuItem mi;

        if (a.getDiscernible() == null || a.getDiscernible().getExpression() == null)
        {
            mi = new MenuItem("Add a Discernibile");
            mi.setOnAction(new EventHandler<ActionEvent>()
            {
                @Override
                public void handle(ActionEvent arg0)
                {
                    addDiscernibile(a, null, treeItem);
                }
            });
            mi.setGraphic(Images.ADD.createImageView());
            cm.getItems().add(mi);

            mi = new MenuItem("Drop as a Discernibile");
            mi.setOnAction(new EventHandler<ActionEvent>()
            {
                @Override
                public void handle(ActionEvent arg0)
                {
                    addDiscernibile(a, label.getDroppedValue(), treeItem);
                    label.setDroppedValue(null);
                }
            });
            mi.setGraphic(Images.ADD.createImageView());
            label.getDropContextMenu().getItems().add(mi);
        }

        if (a.getQualifier() == null || a.getQualifier().getExpression() == null)
        {
            mi = new MenuItem("Add a Qualifier");
            mi.setOnAction(new EventHandler<ActionEvent>()
            {
                @Override
                public void handle(ActionEvent arg0)
                {
                    addQualifier(a, null, treeItem);
                }
            });
            mi.setGraphic(Images.ADD.createImageView());
            cm.getItems().add(mi);

            mi = new MenuItem("Drop as a Qualifier");
            mi.setOnAction(new EventHandler<ActionEvent>()
            {
                @Override
                public void handle(ActionEvent arg0)
                {
                    addQualifier(a, label.getDroppedValue(), treeItem);
                    label.setDroppedValue(null);
                }
            });
            mi.setGraphic(Images.ADD.createImageView());
            label.getDropContextMenu().getItems().add(mi);
        }

        if (a.getValue() == null)
        {
            mi = new MenuItem("Add a Value");
            mi.setOnAction(new EventHandler<ActionEvent>()
            {
                @Override
                public void handle(ActionEvent arg0)
                {
                    addValue(a, null, treeItem);
                }
            });
            mi.setGraphic(Images.ADD.createImageView());
            cm.getItems().add(mi);
            
            mi = new MenuItem("Paste Value");
            mi.setOnAction(new EventHandler<ActionEvent>()
            {
                @Override
                public void handle(ActionEvent arg0)
                {
                    pasteValue(a, treeItem);
                }
            });
            mi.visibleProperty().bind(CustomClipboard.containsValue);
            mi.setGraphic(Images.PASTE.createImageView());
            cm.getItems().add(mi);
        }

        if (a.getValue() != null && a.getValue().getExpression() == null
                && a.getValue().getMeasurement() == null && a.getValue().getText() == null
                && a.getValue().isBoolean() == null)
        {
            mi = new MenuItem("Drop as a Value");
            mi.setOnAction(new EventHandler<ActionEvent>()
            {
                @Override
                public void handle(ActionEvent arg0)
                {
                    addValue(a, label.getDroppedValue(), treeItem);
                    label.setDroppedValue(null);
                }
            });
            mi.setGraphic(Images.ADD.createImageView());
            label.getDropContextMenu().getItems().add(mi);
        }

        if (a.getTiming() == null)
        {
            mi = new MenuItem("Add a Point Timing");
            mi.setOnAction(new EventHandler<ActionEvent>()
            {
                @Override
                public void handle(ActionEvent arg0)
                {
                    addTiming(a, LegoTreeNodeType.point, null, treeItem);
                }
            });
            mi.setGraphic(Images.ADD.createImageView());
            cm.getItems().add(mi);
            
            mi = new MenuItem("Add a Bound Timing");
            mi.setOnAction(new EventHandler<ActionEvent>()
            {
                @Override
                public void handle(ActionEvent arg0)
                {
                    addTiming(a, LegoTreeNodeType.bound, null, treeItem);
                }
            });
            mi.setGraphic(Images.ADD.createImageView());
            cm.getItems().add(mi);
            
            mi = new MenuItem("Add an Interval Timing");
            mi.setOnAction(new EventHandler<ActionEvent>()
            {
                @Override
                public void handle(ActionEvent arg0)
                {
                    addTiming(a, LegoTreeNodeType.interval, null, treeItem);
                }
            });
            mi.setGraphic(Images.ADD.createImageView());
            cm.getItems().add(mi);

            mi = new MenuItem("Drop as a Timing Unit");
            mi.setOnAction(new EventHandler<ActionEvent>()
            {
                @Override
                public void handle(ActionEvent arg0)
                {
                    addTiming(a, null, label.getDroppedValue(), treeItem);
                    label.setDroppedValue(null);
                }
            });
            mi.setGraphic(Images.ADD.createImageView());
            label.getDropContextMenu().getItems().add(mi);
        }

        mi = new MenuItem("Add an Assertion Component");
        mi.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent arg0)
            {
                addAssertionComponent(a, treeItem);
            }
        });
        mi.setGraphic(Images.ADD.createImageView());
        cm.getItems().add(mi);

        mi = new MenuItem("Copy Assertion");
        mi.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent arg0)
            {
                CustomClipboard.set(a);
            }
        });
        mi.setGraphic(Images.COPY.createImageView());
        cm.getItems().add(mi);
        
        mi = new MenuItem("Create Template...");
        mi.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent arg0)
            {
                LegoGUI.getInstance().showCreateTemplateDialog(a);
            }
        });
        mi.setGraphic(Images.TEMPLATE.createImageView());
        cm.getItems().add(mi);
        
        mi = new MenuItem("Remove Assertion");
        mi.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent arg0)
            {
                removeAssertion(a, treeItem);
            }
        });
        mi.setGraphic(Images.DELETE.createImageView());
        cm.getItems().add(mi);
    }
    
    private void addMenus(final AssertionComponent ac, final LegoTreeItem treeItem, ContextMenu cm)
    {
        MenuItem mi = new MenuItem("Remove Assertion Component");
        mi.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent arg0)
            {
                removeAssertionComponent(ac, treeItem);
            }
        });
        mi.setGraphic(Images.DELETE.createImageView());
        cm.getItems().add(mi);
    }
    
    private void addMenus(final Measurement m, final LegoTreeItem treeItem, ContextMenu cm)
    {
        MenuItem mi;
        if (treeItem.getNodeType() == LegoTreeNodeType.point)
        {
            mi = new MenuItem("Remove Point");
            mi.setOnAction(new EventHandler<ActionEvent>()
            {
                @Override
                public void handle(ActionEvent arg0)
                {
                    removePoint(m, treeItem);
                }
            });
            mi.setGraphic(Images.DELETE.createImageView());
            cm.getItems().add(mi);
        }
        else if (treeItem.getNodeType() == LegoTreeNodeType.bound)
        {
            mi = new MenuItem("Remove Bound");
            mi.setOnAction(new EventHandler<ActionEvent>()
            {
                @Override
                public void handle(ActionEvent arg0)
                {
                    removeBound(m, treeItem);
                }
            });
            mi.setGraphic(Images.DELETE.createImageView());
            cm.getItems().add(mi);
        }
        else if (treeItem.getNodeType() == LegoTreeNodeType.interval)
        {
            mi = new MenuItem("Remove Interval");
            mi.setOnAction(new EventHandler<ActionEvent>()
            {
                @Override
                public void handle(ActionEvent arg0)
                {
                    removeInterval(m, treeItem);
                }
            });
            mi.setGraphic(Images.DELETE.createImageView());
            cm.getItems().add(mi);
        }
        else
        {
            if (m.getUnits() == null || m.getUnits().getConcept() == null)
            {
                mi = new MenuItem("Add Units");
                mi.setOnAction(new EventHandler<ActionEvent>()
                {
                    @Override
                    public void handle(ActionEvent arg0)
                    {
                        addUnits(m, treeItem);
                    }
                });
                mi.setGraphic(Images.ADD.createImageView());
                cm.getItems().add(mi);
            }
            if (m.getInterval() == null && m.getPoint() == null && m.getBound() == null)
            {
                mi = new MenuItem("Add Point");
                mi.setOnAction(new EventHandler<ActionEvent>()
                {
                    @Override
                    public void handle(ActionEvent arg0)
                    {
                        addPoint(m, treeItem);
                    }
                });
                mi.setGraphic(Images.ADD.createImageView());
                cm.getItems().add(mi);
                
                mi = new MenuItem("Add Bound");
                mi.setOnAction(new EventHandler<ActionEvent>()
                {
                    @Override
                    public void handle(ActionEvent arg0)
                    {
                        addBound(m, treeItem);
                    }
                });
                mi.setGraphic(Images.ADD.createImageView());
                cm.getItems().add(mi);
    
                mi = new MenuItem("Add Interval");
                mi.setOnAction(new EventHandler<ActionEvent>()
                {
                    @Override
                    public void handle(ActionEvent arg0)
                    {
                        addInterval(m, treeItem);
                    }
                });
                mi.setGraphic(Images.ADD.createImageView());
                cm.getItems().add(mi);
            }
    
            mi = new MenuItem("Remove " + (treeItem.getNodeType() == LegoTreeNodeType.timingMeasurement ? "Timing" : "Measurement"));
            mi.setOnAction(new EventHandler<ActionEvent>()
            {
                @Override
                public void handle(ActionEvent arg0)
                {
                    removeMeasurement(m, treeItem);
                }
            });
            mi.setGraphic(Images.DELETE.createImageView());
            cm.getItems().add(mi);
        }
    }
    
    private void addMenus(final Expression e, final LegoTreeItem treeItem, ContextMenu cm, final DropTargetLabel label, String descriptionAddition)
    {
        MenuItem mi = new MenuItem("Add a " + descriptionAddition + "Concept");
        mi.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent arg0)
            {
                addConcept(e, null, treeItem);
            }
        });
        mi.setGraphic(Images.ADD.createImageView());
        cm.getItems().add(mi);

        mi = new MenuItem("Add a Relationship");
        mi.setOnAction(new EventHandler<ActionEvent>()
        {

            @Override
            public void handle(ActionEvent arg0)
            {
                addRelation(e, null, treeItem);
            }
        });
        mi.setGraphic(Images.ADD.createImageView());
        cm.getItems().add(mi);

        mi = new MenuItem("Add a Relationship Group");
        mi.setOnAction(new EventHandler<ActionEvent>()
        {

            @Override
            public void handle(ActionEvent arg0)
            {
                addRelationshipGroup(e, treeItem);
            }
        });
        mi.setGraphic(Images.ADD.createImageView());
        cm.getItems().add(mi);

        mi = new MenuItem("Drop as a Concept");
        mi.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent arg0)
            {
                addConcept(e, label.getDroppedValue(), treeItem);
                label.setDroppedValue(null);
            }
        });
        mi.setGraphic(Images.ADD.createImageView());
        label.getDropContextMenu().getItems().add(mi);

        mi = new MenuItem("Drop as a Relation Type");
        mi.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent arg0)
            {
                addRelation(e, label.getDroppedValue(), treeItem);
                label.setDroppedValue(null);
            }
        });
        mi.setGraphic(Images.ADD.createImageView());
        label.getDropContextMenu().getItems().add(mi);

        if (treeItem.getNodeType() == LegoTreeNodeType.expressionValue
                || treeItem.getNodeType() == LegoTreeNodeType.expressionDestination
                || treeItem.getNodeType() == LegoTreeNodeType.expressionOptional)
        {
            mi = new MenuItem("Remove Expression");
            mi.setOnAction(new EventHandler<ActionEvent>()
            {

                @Override
                public void handle(ActionEvent arg0)
                {
                    removeExpression(e, treeItem);
                }
            });
            mi.setGraphic(Images.DELETE.createImageView());
            cm.getItems().add(mi);
        }
        
        mi = new MenuItem("Copy " + descriptionAddition + "Expression");
        mi.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent arg0)
            {
                if (treeItem.getNodeType() == LegoTreeNodeType.expressionDiscernible)
                {
                    CustomClipboard.set(((Assertion)((LegoTreeItem)treeItem.getParent()).getExtraData()).getDiscernible());
                }
                else if (treeItem.getNodeType() == LegoTreeNodeType.expressionQualifier)
                {
                    CustomClipboard.set(((Assertion)((LegoTreeItem)treeItem.getParent()).getExtraData()).getQualifier());
                }
                else
                {
                    CustomClipboard.set(e);
                }
            }
        });
        mi.setGraphic(Images.COPY.createImageView());
        cm.getItems().add(mi);
        
        mi = new MenuItem("Create Template...");
        mi.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent arg0)
            {
                if (treeItem.getNodeType() == LegoTreeNodeType.expressionDiscernible)
                {
                    LegoGUI.getInstance().showCreateTemplateDialog(((Assertion)((LegoTreeItem)treeItem.getParent()).getExtraData()).getDiscernible());
                }
                else if (treeItem.getNodeType() == LegoTreeNodeType.expressionQualifier)
                {
                    LegoGUI.getInstance().showCreateTemplateDialog(((Assertion)((LegoTreeItem)treeItem.getParent()).getExtraData()).getQualifier());
                }
                else
                {
                    LegoGUI.getInstance().showCreateTemplateDialog(e);
                }
            }
        });
        mi.setGraphic(Images.TEMPLATE.createImageView());
        cm.getItems().add(mi);
        
        mi = new MenuItem("Paste " + descriptionAddition + "Expression (Replace Existing)");
        mi.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent arg0)
            {
                pasteExpression(e, treeItem);
            }
        });
        
        if (treeItem.getNodeType() == LegoTreeNodeType.expressionDiscernible)
        {
            mi.visibleProperty().bind(CustomClipboard.containsDiscernible);
        }
        else if (treeItem.getNodeType() == LegoTreeNodeType.expressionQualifier)
        {
            mi.visibleProperty().bind(CustomClipboard.containsQualifier);
        }
        else
        {
            mi.visibleProperty().bind(CustomClipboard.containsExpression);
        }
        
        mi.setGraphic(Images.PASTE.createImageView());
        cm.getItems().add(mi);
        
    }
    
    private void addMenus(final Concept c, final ConceptNode cn, final LegoTreeItem treeItem, ContextMenu cm)
    {
        if (treeItem.getNodeType() == LegoTreeNodeType.conceptOptional)
        {
            MenuItem mi = new MenuItem("Remove Concept");
            mi.setOnAction(new EventHandler<ActionEvent>()
            {

                @Override
                public void handle(ActionEvent arg0)
                {
                    removeConcept(treeItem);
                }
            });
            mi.setGraphic(Images.DELETE.createImageView());
            cm.getItems().add(mi);
        }
        
        MenuItem mi = new MenuItem("View Concept");
        mi.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent arg0)
            {
                if (cn.isValid())
                {
                    LegoGUI.getInstance().showSnomedConceptDialog(UUID.fromString(c.getUuid()));
                }
                else
                {
                    LegoGUI.getInstance().showErrorDialog("Unknown Concept", "Can't lookup an invalid concept", "");
                }
            }
        });
        mi.setGraphic(Images.CONCEPT_VIEW.createImageView());
        cm.getItems().add(mi);
        
    }
    
    private void addMenus(final Relation r, final LegoTreeItem treeItem, ContextMenu cm, final DropTargetLabel label)
    {
        MenuItem mi;

        if (r.getDestination().getExpression() == null && r.getDestination().getMeasurement() == null
                && r.getDestination().getText() == null && r.getDestination().isBoolean() == null)
        {
            mi = new MenuItem("Add Dest Concept");
            mi.setOnAction(new EventHandler<ActionEvent>()
            {
                @Override
                public void handle(ActionEvent arg0)
                {
                    addConcept(r, null, treeItem);
                }
            });
            mi.setGraphic(Images.ADD.createImageView());
            cm.getItems().add(mi);

            mi = new MenuItem("Drop as the Destination Concept");
            mi.setOnAction(new EventHandler<ActionEvent>()
            {
                @Override
                public void handle(ActionEvent arg0)
                {
                    addConcept(r, label.getDroppedValue(), treeItem);
                    label.setDroppedValue(null);
                }
            });
            mi.setGraphic(Images.ADD.createImageView());
            label.getDropContextMenu().getItems().add(mi);

            mi = new MenuItem("Add Dest Point");
            mi.setOnAction(new EventHandler<ActionEvent>()
            {
                @Override
                public void handle(ActionEvent arg0)
                {
                    addMeasurement(r, LegoTreeNodeType.point, null, treeItem);
                }
            });
            mi.setGraphic(Images.ADD.createImageView());
            cm.getItems().add(mi);
            
            mi = new MenuItem("Add Dest Bound ");
            mi.setOnAction(new EventHandler<ActionEvent>()
            {
                @Override
                public void handle(ActionEvent arg0)
                {
                    addMeasurement(r, LegoTreeNodeType.bound, null, treeItem);
                }
            });
            mi.setGraphic(Images.ADD.createImageView());
            cm.getItems().add(mi);
            
            mi = new MenuItem("Add Dest Interval ");
            mi.setOnAction(new EventHandler<ActionEvent>()
            {
                @Override
                public void handle(ActionEvent arg0)
                {
                    addMeasurement(r, LegoTreeNodeType.interval, null, treeItem);
                }
            });
            mi.setGraphic(Images.ADD.createImageView());
            cm.getItems().add(mi);
           
            mi = new MenuItem("Add Dest Text");
            mi.setOnAction(new EventHandler<ActionEvent>()
            {
                @Override
                public void handle(ActionEvent arg0)
                {
                    addText(r.getDestination(), treeItem);
                }
            });
            mi.setGraphic(Images.ADD.createImageView());
            cm.getItems().add(mi);
            
            mi = new MenuItem("Add Dest Boolean");
            mi.setOnAction(new EventHandler<ActionEvent>()
            {
                @Override
                public void handle(ActionEvent arg0)
                {
                    addBoolean(r.getDestination(), treeItem);
                }
            });
            mi.setGraphic(Images.ADD.createImageView());
            cm.getItems().add(mi);
        }

        mi = new MenuItem("Remove Relationship");
        mi.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent arg0)
            {
                removeRelation(r, treeItem);
            }
        });
        mi.setGraphic(Images.DELETE.createImageView());
        cm.getItems().add(mi);
    }
    
    private void addMenus(final RelationGroup rg, final LegoTreeItem treeItem, ContextMenu cm)
    {
        MenuItem mi = new MenuItem("Add Relationship");
        mi.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent arg0)
            {
                addRelation(rg, treeItem);
            }
        });
        mi.setGraphic(Images.ADD.createImageView());
        cm.getItems().add(mi);

        mi = new MenuItem("Remove Relationship Group");
        mi.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent arg0)
            {
                removeRelationGroup(treeItem);
            }
        });
        mi.setGraphic(Images.DELETE.createImageView());
        cm.getItems().add(mi);
    }
   
    private void addMenus(final Value v, final LegoTreeItem treeItem, ContextMenu cm, final DropTargetLabel label)
    {
        if (v.getExpression() == null && v.getMeasurement() == null && v.getText() == null
                && v.isBoolean() == null)
        {
            MenuItem mi = new MenuItem("Add a Concept");
            mi.setOnAction(new EventHandler<ActionEvent>()
            {
                @Override
                public void handle(ActionEvent arg0)
                {
                    addConcept(v, null, treeItem);
                }
            });
            mi.setGraphic(Images.ADD.createImageView());
            cm.getItems().add(mi);

            mi = new MenuItem("Add Point Measurement");
            mi.setOnAction(new EventHandler<ActionEvent>()
            {
                @Override
                public void handle(ActionEvent arg0)
                {
                    addMeasurement(v, LegoTreeNodeType.point, null, treeItem);
                }
            });
            mi.setGraphic(Images.ADD.createImageView());
            cm.getItems().add(mi);
            
            mi = new MenuItem("Add Bound Measurement");
            mi.setOnAction(new EventHandler<ActionEvent>()
            {
                @Override
                public void handle(ActionEvent arg0)
                {
                    addMeasurement(v, LegoTreeNodeType.bound, null, treeItem);
                }
            });
            mi.setGraphic(Images.ADD.createImageView());
            cm.getItems().add(mi);
            
            mi = new MenuItem("Add Interval Measurement");
            mi.setOnAction(new EventHandler<ActionEvent>()
            {
                @Override
                public void handle(ActionEvent arg0)
                {
                    addMeasurement(v, LegoTreeNodeType.interval, null, treeItem);
                }
            });
            mi.setGraphic(Images.ADD.createImageView());
            cm.getItems().add(mi);

            mi = new MenuItem("Drop as a Concept");
            mi.setOnAction(new EventHandler<ActionEvent>()
            {
                @Override
                public void handle(ActionEvent arg0)
                {
                    addConcept(v, label.getDroppedValue(), treeItem);
                    label.setDroppedValue(null);
                }
            });
            mi.setGraphic(Images.ADD.createImageView());
            label.getDropContextMenu().getItems().add(mi);

            mi = new MenuItem("Drop as Measurement Units");
            mi.setOnAction(new EventHandler<ActionEvent>()
            {
                @Override
                public void handle(ActionEvent arg0)
                {
                    addMeasurement(v, null, label.getDroppedValue(), treeItem);
                    label.setDroppedValue(null);
                }
            });
            mi.setGraphic(Images.ADD.createImageView());
            label.getDropContextMenu().getItems().add(mi);
            
            mi = new MenuItem("Add Text");
            mi.setOnAction(new EventHandler<ActionEvent>()
            {
                @Override
                public void handle(ActionEvent arg0)
                {
                    addText(v, treeItem);
                }
            });
            mi.setGraphic(Images.ADD.createImageView());
            cm.getItems().add(mi);
            
            mi = new MenuItem("Add Boolean");
            mi.setOnAction(new EventHandler<ActionEvent>()
            {
                @Override
                public void handle(ActionEvent arg0)
                {
                    addBoolean(v, treeItem);
                }
            });
            mi.setGraphic(Images.ADD.createImageView());
            cm.getItems().add(mi);
        }
        MenuItem mi = new MenuItem("Copy Value");
        mi.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent arg0)
            {
                CustomClipboard.set(v);
            }
        });
        mi.setGraphic(Images.COPY.createImageView());
        cm.getItems().add(mi);
        
        mi = new MenuItem("Create Template...");
        mi.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent arg0)
            {
                LegoGUI.getInstance().showCreateTemplateDialog(v);
            }
        });
        mi.setGraphic(Images.TEMPLATE.createImageView());
        cm.getItems().add(mi);
        
        mi = new MenuItem("Paste Value (Replace Existing)");
        mi.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent arg0)
            {
                pasteValue((Assertion)((LegoTreeItem)treeItem.getParent()).getExtraData(), treeItem.getParent());
            }
        });
        mi.visibleProperty().bind(CustomClipboard.containsValue);
        mi.setGraphic(Images.PASTE.createImageView());
        cm.getItems().add(mi);
        
    }
    
    private ConceptUsageType findType(LegoTreeItem lti)
    {
        if (LegoTreeNodeType.expressionDestination == lti.getNodeType())
        {
            return ConceptUsageType.REL_DESTINATION;
        }
        Object data = lti.getExtraData();
        if (data != null)
        {
            if (data instanceof Relation || data instanceof AssertionComponent)
            {
                return ConceptUsageType.TYPE;
            }
            else if (data instanceof Measurement)
            {
                return ConceptUsageType.UNITS;
            }
            else if (data instanceof Expression)
            {
                if (LegoTreeNodeType.expressionDiscernible == lti.getNodeType())
                {
                    return ConceptUsageType.DISCERNIBLE;
                }
                else if (LegoTreeNodeType.expressionQualifier == lti.getNodeType())
                {
                    return ConceptUsageType.QUALIFIER;
                }
            }
            else if (data instanceof Value)
            {
                return ConceptUsageType.VALUE;
            }
        }
        
        //Didn't find it... recurse...
        Object parent = lti.getParent();
        if (parent != null && parent instanceof LegoTreeItem)
        {
            return findType((LegoTreeItem)parent);
        }
        return null;
    }
    
    @Override
    protected void finalize() throws Throwable
    {
        //Help deal with javafx memory leaks
        styleProperty().unbind();
        ContextMenu cm = getContextMenu();
        if (cm != null)
        {
            for (MenuItem mi : cm.getItems())
            {
                mi.visibleProperty().unbind();
            }
        }
        super.finalize();
    }
}
