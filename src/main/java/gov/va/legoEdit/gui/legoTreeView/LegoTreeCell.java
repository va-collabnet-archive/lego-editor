package gov.va.legoEdit.gui.legoTreeView;

import gov.va.legoEdit.LegoGUI;
import gov.va.legoEdit.gui.util.DropTargetLabel;
import gov.va.legoEdit.gui.util.LegoTreeItemComparator;
import gov.va.legoEdit.model.LegoListByReference;
import gov.va.legoEdit.model.LegoReference;
import gov.va.legoEdit.model.schemaModel.Assertion;
import gov.va.legoEdit.model.schemaModel.AssertionComponent;
import gov.va.legoEdit.model.schemaModel.AssertionComponents;
import gov.va.legoEdit.model.schemaModel.Concept;
import gov.va.legoEdit.model.schemaModel.Discernible;
import gov.va.legoEdit.model.schemaModel.Expression;
import gov.va.legoEdit.model.schemaModel.Interval;
import gov.va.legoEdit.model.schemaModel.Lego;
import gov.va.legoEdit.model.schemaModel.LegoList;
import gov.va.legoEdit.model.schemaModel.Measurement;
import gov.va.legoEdit.model.schemaModel.MeasurementConstant;
import gov.va.legoEdit.model.schemaModel.Pncs;
import gov.va.legoEdit.model.schemaModel.Point;
import gov.va.legoEdit.model.schemaModel.Qualifier;
import gov.va.legoEdit.model.schemaModel.Relation;
import gov.va.legoEdit.model.schemaModel.RelationGroup;
import gov.va.legoEdit.model.schemaModel.Stamp;
import gov.va.legoEdit.model.schemaModel.Timing;
import gov.va.legoEdit.model.schemaModel.Type;
import gov.va.legoEdit.model.schemaModel.Units;
import gov.va.legoEdit.model.schemaModel.Value;
import gov.va.legoEdit.storage.BDBDataStoreImpl;
import gov.va.legoEdit.util.TimeConvert;
import java.util.Observable;
import java.util.Observer;
import java.util.UUID;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
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
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LegoTreeCell<T> extends TreeCell<T>
{
    private static Logger logger = LoggerFactory.getLogger(LegoTreeCell.class);
    private static ObservableList<String> statusChoices_ = FXCollections.observableArrayList(new String[] { "Active", "Inactive" });

    @Override
    public void updateItem(T item, boolean empty)
    {
        super.updateItem(item, empty);
        final LegoTreeItem treeItem = (LegoTreeItem) getTreeItem();
        ContextMenu cm = new ContextMenu();
        
        if (empty || treeItem.getNodeType() == LegoTreeNodeType.blankLegoEndNode
                || treeItem.getNodeType() == LegoTreeNodeType.blankLegoListEndNode)
        {
            setText(null);
            setGraphic(null);
            
            if (((LegoTreeView) getTreeView()).getLego() != null
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
                cm.getItems().add(mi);
            }
            else if (((LegoTreeView) getTreeView()).getLego() == null
                    || (!empty && treeItem.getNodeType() == LegoTreeNodeType.blankLegoListEndNode))
            {
                MenuItem mi = new MenuItem("Create Lego List");
                mi.setOnAction(new EventHandler<ActionEvent>()
                {
                    @Override
                    public void handle(ActionEvent arg0)
                    {
                        LegoGUI.getInstance().showLegoListPropertiesDialog("", UUID.randomUUID().toString(),
                                new SimpleStringProperty(""));
                    }
                });
                cm.getItems().add(mi);
            }
        }
        else
        {
            if (treeItem.getNodeType() == LegoTreeNodeType.legoListByReference)
            {
                final SimpleStringProperty legoListDescriptionProperty = new SimpleStringProperty(
                        ((LegoListByReference) treeItem.getExtraData()).getGroupDescription());
                Tooltip tp = new Tooltip();
                tp.textProperty().bind(legoListDescriptionProperty);
                setTooltip(tp);

                legoListDescriptionProperty.addListener(new ChangeListener<String>()
                {
                    @Override
                    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
                    {
                        ((LegoList) treeItem.getExtraData()).setGroupDescription(newValue);
                    }
                });

                MenuItem mi;
                mi = new MenuItem("Create New Lego Within Lego List");
                mi.setOnAction(new EventHandler<ActionEvent>()
                {
                    @Override
                    public void handle(ActionEvent arg0)
                    {
                        LegoListByReference llbr = (LegoListByReference) treeItem.getExtraData();
                        LegoGUI.getInstance().showCreateLegoDialog(llbr, treeItem);
                    }
                });
                cm.getItems().add(mi);

                mi = new MenuItem("Show XML View");
                mi.setOnAction(new EventHandler<ActionEvent>()
                {
                    @Override
                    public void handle(ActionEvent arg0)
                    {
                        LegoListByReference llbr = (LegoListByReference) treeItem.getExtraData();
                        LegoGUI.getInstance().showXMLViewWindow(BDBDataStoreImpl.getInstance().getLegoListByID(llbr.getLegoListUUID()));
                    }
                });
                cm.getItems().add(mi);
                
                mi = new MenuItem("Properties");
                mi.setOnAction(new EventHandler<ActionEvent>()
                {
                    @Override
                    public void handle(ActionEvent arg0)
                    {
                        LegoListByReference llbr = (LegoListByReference) treeItem.getExtraData();
                        LegoGUI.getInstance().showLegoListPropertiesDialog(llbr.getGroupName(), llbr.getLegoListUUID(),
                                legoListDescriptionProperty);
                    }
                });
                cm.getItems().add(mi);
            }
            else if (treeItem.getNodeType() == LegoTreeNodeType.legoReference)
            {
                LegoReference legoReference = (LegoReference)treeItem.getExtraData();
                
                Label l = new Label("Lego");
                l.getStyleClass().add("boldLabel");
                setGraphic(l);
                StringProperty style = LegoGUI.getInstance().getLegoGUIController().getStyleForLego(legoReference); 
                if (style != null)
                {
                    styleProperty().bind(style);
                }
                setEditable(true);
            }
            else if (treeItem.getNodeType() == LegoTreeNodeType.status)
            {
                // TODO tie status back to in-memory lego
                HBox hbox = new HBox();
                hbox.setSpacing(10.0);
                hbox.setAlignment(Pos.CENTER_LEFT);

                Label status = new Label("Status");
                status.getStyleClass().add("boldLabel");
                hbox.getChildren().add(status);

                ChoiceBox<String> cb = new ChoiceBox<>(statusChoices_);
                String currentStatus = treeItem.getValue();
                cb.getSelectionModel().select(currentStatus);
                hbox.getChildren().add(cb);
                setGraphic(hbox);
            }
            else if (treeItem.getNodeType() == LegoTreeNodeType.pncsValue)
            {
                MenuItem mi = new MenuItem("Create New Lego for PNCS");
                mi.setOnAction(new EventHandler<ActionEvent>()
                {
                    @Override
                    public void handle(ActionEvent arg0)
                    {
                        createNewLego(treeItem);
                    }
                });
                cm.getItems().add(mi);
            }
            else if (treeItem.getNodeType() == LegoTreeNodeType.labeledUneditableString)
            {
                setGraphic(makeLabeledString(treeItem.getExtraData().toString(), item.toString()));
            }
            else if (treeItem.getNodeType() == LegoTreeNodeType.assertion)
            {
                DropTargetLabel assertionLabel = new DropTargetLabel("Assertion", cm);
                Assertion a = (Assertion) treeItem.getExtraData();
                
                HBox hbox = new HBox();
                hbox.setSpacing(10.0);
                hbox.setAlignment(Pos.CENTER_LEFT);
                hbox.getChildren().add(assertionLabel);
                hbox.getChildren().add(makeLabeledString("UUID", a.getAssertionUUID()));

                addMenus(a, treeItem, cm, assertionLabel);
                setGraphic(hbox);
                LegoGUI.getInstance().getLegoGUIController().addSnomedDropTarget(((LegoTreeView) getTreeView()).getLego(), assertionLabel);
            }
            else if (treeItem.getNodeType() == LegoTreeNodeType.assertionComponents)
            {
                addMenus((AssertionComponents) treeItem.getExtraData(), treeItem, cm);
            }
            else if (treeItem.getNodeType() == LegoTreeNodeType.assertionComponent)
            {
                addMenus((AssertionComponent) treeItem.getExtraData(), treeItem, cm);
            }
            else if (treeItem.getNodeType() == LegoTreeNodeType.assertionUUID)
            {
                String value = treeItem.getValue();
                final AssertionComponent ac = (AssertionComponent) ((LegoTreeItem) treeItem.getParent()).getExtraData();
                final TextField tf = new TextField();
                tf.setText(value == null ? "" : value);
                tf.setPromptText("UUID of another Assertion");
                tf.textProperty().addListener(new ChangeListener<String>()
                {
                    @Override
                    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
                    {
                        ac.setAssertionUUID(newValue);
                        System.out.println("assertionUUID changed");
                        // TODO validate
                    }
                });
                setGraphic(prependLabel("Assertion UUID", tf));
            }
            else if (treeItem.getNodeType() == LegoTreeNodeType.measurement)
            {
                addMenus((Measurement) treeItem.getExtraData(), treeItem, cm);
            }
            else if (treeItem.getNodeType() == LegoTreeNodeType.timing)
            {
                 addMenus((Timing) treeItem.getExtraData(), treeItem, cm);
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
                
                setGraphic(label);
                LegoGUI.getInstance().getLegoGUIController().addSnomedDropTarget(((LegoTreeView) getTreeView()).getLego(), label);
            }
            else if (treeItem.getNodeType() == LegoTreeNodeType.concept
                    || treeItem.getNodeType() == LegoTreeNodeType.conceptOptional)
            {
                Concept c = (Concept) treeItem.getExtraData();
                addMenus(c, treeItem, cm);

                String label = "";
                if (((LegoTreeItem) treeItem.getParent()).getExtraData() instanceof Relation
                        || ((LegoTreeItem) treeItem.getParent()).getExtraData() instanceof AssertionComponent)
                {
                    label = "Type";
                }
                else if (((LegoTreeItem) treeItem.getParent()).getExtraData() instanceof Measurement)
                {
                    label = "Units";
                }

                ConceptNode cn = new ConceptNode(label, c, treeItem.getNodeType(), ((LegoTreeView) getTreeView()).getLego());
                cn.addObserver(new Observer()
                {
                    @Override
                    public void update(Observable o, Object arg)
                    {
                        System.out.println("Concept updated");
                        // concept is already tied to the lego, shouldn't have to do anything. But could if we wanted.
                    }
                });
                setGraphic(cn.getNode());
            }
            else if (treeItem.getNodeType() == LegoTreeNodeType.relation)
            {
                DropTargetLabel relLabel = new DropTargetLabel(treeItem.getValue(), cm);                
                addMenus((Relation) treeItem.getExtraData(), treeItem, cm, relLabel);
                setGraphic(relLabel);
                LegoGUI.getInstance().getLegoGUIController().addSnomedDropTarget(((LegoTreeView) getTreeView()).getLego(), relLabel);
            }
            else if (treeItem.getNodeType() == LegoTreeNodeType.relationshipGroup)
            {
                addMenus((RelationGroup) treeItem.getExtraData(), treeItem, cm);
            }
            else if (treeItem.getNodeType() == LegoTreeNodeType.point)
            {
                final Point p = (Point) treeItem.getExtraData();
                final ComboBox<String> cb = new ComboBox<>();
                cb.setEditable(true);
                for (MeasurementConstant s : MeasurementConstant.values())
                {
                    cb.getItems().add(s.value());
                }
                cb.setPromptText("Enter a numeric value, or select from the list");
                cb.setVisibleRowCount(MeasurementConstant.values().length + 1);
                cb.setMaxWidth(Double.MAX_VALUE);
                cb.setMinWidth(320.0);

                if (p.getStringConstant() != null)
                {
                    cb.getSelectionModel().select(p.getStringConstant().value());
                }
                else if (p.getNumericValue() != null)
                {
                    cb.setValue(p.getNumericValue().toString());
                }

                cb.valueProperty().addListener(new ChangeListener<String>()
                {
                    @Override
                    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
                    {
                        try
                        {
                            float f = Float.parseFloat(newValue);
                            p.setStringConstant(null);
                            p.setNumericValue(f);
                            p.setInclusive(true);  // This point impl only handles single points, so always inclusive
                        }
                        catch (NumberFormatException e)
                        {
                            try
                            {
                                MeasurementConstant ms = MeasurementConstant.fromValue(newValue);
                                p.setNumericValue(null);
                                p.setStringConstant(ms);
                                p.setInclusive(true);  // This point impl only handles single points, so always inclusive
                            }
                            catch (IllegalArgumentException ex)
                            {
                                // TODO highlight invalid
                                System.out.println("INVALID");
                            }
                        }
                    }
                });

                addMenus(p, treeItem, cm);
                setGraphic(cb);
            }
            else if (treeItem.getNodeType() == LegoTreeNodeType.interval)
            {
                // TODO figure out how to do intervals and bounds
                addMenus((Interval) treeItem.getExtraData(), treeItem, cm);
            }
            else if (treeItem.getNodeType() == LegoTreeNodeType.value)
            {
                DropTargetLabel valueLabel = new DropTargetLabel(treeItem.getValue(), cm);
                addMenus((Value) treeItem.getExtraData(), treeItem, cm, valueLabel);
                setGraphic(valueLabel);
                LegoGUI.getInstance().getLegoGUIController().addSnomedDropTarget(((LegoTreeView) getTreeView()).getLego(), valueLabel);
            }
        }
        // done with massive if/else
        if (cm.getItems().size() > 0)
        {
            setContextMenu(cm);
        }
        
        if (getGraphic() == null)
        {
            setText(item == null ? null : item.toString());
            getStyleClass().add("boldLabel");
        }
        else
        {
            setText(null);
        }
    }

    private Node makeLabeledString(String label, final String value)
    {
        HBox hbox = new HBox();
        hbox.setSpacing(10.0);
        hbox.setAlignment(Pos.CENTER_LEFT);
        Label l = new Label(label);
        l.getStyleClass().add("boldLabel");
        hbox.getChildren().add(l);

        Label valueLabel = new Label(value);

        MenuItem mi = new MenuItem("Copy");
        mi.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent arg0)
            {
                ClipboardContent content = new ClipboardContent();
                content.putString(value);
                Clipboard.getSystemClipboard().setContent(content);
            }
        });

        valueLabel.setContextMenu(new ContextMenu(mi));
        hbox.getChildren().add(valueLabel);

        return hbox;
    }

    private Node prependLabel(String label, Node node)
    {
        HBox hbox = new HBox();
        hbox.setSpacing(10.0);
        hbox.setAlignment(Pos.CENTER_LEFT);
        Label l = new Label(label);
        l.getStyleClass().add("boldLabel");
        hbox.getChildren().add(l);
        hbox.getChildren().add(node);
        HBox.setHgrow(node, Priority.ALWAYS);
        return hbox;
    }

    private void removeTiming(Timing t, TreeItem<String> ti)
    {
        Assertion a = (Assertion) ((LegoTreeItem) ti.getParent()).getExtraData();
        a.setTiming(null);
        Event.fireEvent(ti.getParent(),
                new TreeItem.TreeModificationEvent<String>(TreeItem.valueChangedEvent(), ti.getParent()));
        ti.getParent().getChildren().remove(ti);
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
        else if (parent instanceof Timing)
        {
            // Don't allow removal here - they should remove the entire timing.
            logger.error("Dan messed up - this measurement removal shouldn't be called");
            return;
        }
        else
        {
            logger.error("Dan messed up! - don't know how to remove measurement - type " + parent.toString());
            return;
        }
        Event.fireEvent(ti.getParent(),
                new TreeItem.TreeModificationEvent<String>(TreeItem.valueChangedEvent(), ti.getParent()));
        ti.getParent().getChildren().remove(ti);
    }

    private void addUnits(Measurement m, TreeItem<String> ti)
    {
        Units u = new Units();
        Concept c = new Concept();
        u.setConcept(c);
        m.setUnits(u);
        ti.getChildren().add(new LegoTreeItem(c, LegoTreeNodeType.conceptOptional));
        expandAll(ti);
        Event.fireEvent(ti, new TreeItem.TreeModificationEvent<String>(TreeItem.valueChangedEvent(), ti));
    }

    private void addInterval(Measurement m, TreeItem<String> ti)
    {
        Interval i = new Interval();
        m.setInterval(i);
        ti.getChildren().add(new LegoTreeItem(i));
        expandAll(ti);
        Event.fireEvent(ti, new TreeItem.TreeModificationEvent<String>(TreeItem.valueChangedEvent(), ti));
    }

    private void removeInterval(Interval p, TreeItem<String> ti)
    {
        Measurement m = (Measurement) ((LegoTreeItem) ti.getParent()).getExtraData();
        m.setInterval(null);
        Event.fireEvent(ti.getParent(),
                new TreeItem.TreeModificationEvent<String>(TreeItem.valueChangedEvent(), ti.getParent()));
        ti.getParent().getChildren().remove(ti);
    }

    private void removePoint(Point p, TreeItem<String> ti)
    {
        Object parent = ((LegoTreeItem) ti.getParent()).getExtraData();
        if (parent instanceof Measurement)
        {
            Measurement m = (Measurement) parent;
            m.setPoint(null);
            Event.fireEvent(ti.getParent(),
                    new TreeItem.TreeModificationEvent<String>(TreeItem.valueChangedEvent(), ti.getParent()));
            ti.getParent().getChildren().remove(ti);
        }
        else
        {
            logger.error("unhandled point remove call");
        }
    }

    private void addPoint(Measurement m, TreeItem<String> ti)
    {
        Point p = new Point();
        m.setPoint(p);
        ti.getChildren().add(new LegoTreeItem(p));
        expandAll(ti);
        Event.fireEvent(ti, new TreeItem.TreeModificationEvent<String>(TreeItem.valueChangedEvent(), ti));
    }

    private void addMeasurement(Object parent, boolean withPoint, boolean withInterval, String unitsSCTID,
            TreeItem<String> ti)
    {
        Measurement m = new Measurement();
        if (parent instanceof Relation)
        {
            ((Relation) parent).getDestination().setMeasurement(m);
        }
        else if (parent instanceof Value)
        {
            ((Value) parent).setMeasurement(m);
        }
        else
        {
            logger.error("Unhandled measurement add request: " + parent);
            return;
        }

        if (withPoint)
        {
            m.setPoint(new Point());
        }
        if (withInterval)
        {
            m.setInterval(new Interval());
        }
        if (unitsSCTID != null)
        {
            Units u = new Units();
            Concept c = new Concept();
            c.setUuid(unitsSCTID);
            u.setConcept(c);
            m.setUnits(u);
        }
        ti.getChildren().add(new LegoTreeItem(m));
        expandAll(ti);
        Event.fireEvent(ti, new TreeItem.TreeModificationEvent<String>(TreeItem.valueChangedEvent(), ti));
    }

    private void addConcept(Object object, String sctUUID, TreeItem<String> ti)
    {
        Concept c = new Concept();
        c.setUuid(sctUUID);

        Expression expression;
        boolean addExpression = false;
        ;
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
            expandAll(lti);
        }
        else
        {
            if (type == LegoTreeNodeType.expressionDiscernible || type == LegoTreeNodeType.expressionQualifier)
            {
                type = LegoTreeNodeType.expressionOptional;
            }

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
                    expandAll(lti);
                }
            }

            Expression newExpression = new Expression();
            newExpression.setConcept(c);
            expression.getExpression().add(newExpression);
            LegoTreeItem lti = new LegoTreeItem(newExpression, type);
            ti.getChildren().add(lti);
            expandAll(lti);
        }
        FXCollections.sort(ti.getChildren(), new LegoTreeItemComparator(true));
        ti.setExpanded(true);
        Event.fireEvent(ti, new TreeItem.TreeModificationEvent<String>(TreeItem.valueChangedEvent(), ti));
    }

    private void removeConcept(TreeItem<String> ti)
    {
        Object parent = ((LegoTreeItem) ti.getParent()).getExtraData();
        if (parent instanceof Units)
        {
            ((Units) parent).setConcept(null);
        }
        else if (parent instanceof Type)
        {
            ((Type) parent).setConcept(null);
        }
        else
        {
            logger.error("Unhandled concept remove request: " + parent);
            return;
        }
        Event.fireEvent(ti.getParent(),
                new TreeItem.TreeModificationEvent<String>(TreeItem.valueChangedEvent(), ti.getParent()));
        ti.getParent().getChildren().remove(ti);
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
    }

    private void addTimingMeasurement(Timing t, boolean withPoint, boolean withInterval, TreeItem<String> ti)
    {
        Measurement m = new Measurement();
        t.setMeasurement(m);
        if (withPoint)
        {
            m.setPoint(new Point());
        }
        if (withInterval)
        {
            m.setInterval(new Interval());
        }
        ti.getChildren().add(new LegoTreeItem(m));
        expandAll(ti);
        Event.fireEvent(ti, new TreeItem.TreeModificationEvent<String>(TreeItem.valueChangedEvent(), ti));
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
        expandAll(lti);
        ti.setExpanded(true);
        Event.fireEvent(ti, new TreeItem.TreeModificationEvent<String>(TreeItem.valueChangedEvent(), ti));
    }

    private void addRelation(RelationGroup rg, TreeItem<String> ti)
    {
        Relation r = new Relation();
        rg.getRelation().add(r);
        LegoTreeItem lti = new LegoTreeItem(r);
        ti.getChildren().add(lti);
        expandAll(lti);
        ti.setExpanded(true);
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
    }

    private void removeRelationGroup(TreeItem<String> ti)
    {
        Expression e = (Expression) ((LegoTreeItem) ti.getParent()).getExtraData();
        e.getRelationGroup().clear();

        // No need to fire a parent event here - the options on Expression don't change with add/remove of a rel.
        ti.getParent().getChildren().remove(ti);
    }

    private void addRelationshipGroup(Expression e, TreeItem<String> ti)
    {
        RelationGroup rg = new RelationGroup();
        e.getRelationGroup().add(rg);
        ti.getChildren().add(new LegoTreeItem(rg));
        expandAll(ti);
        Event.fireEvent(ti, new TreeItem.TreeModificationEvent<String>(TreeItem.valueChangedEvent(), ti));
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
        expandAll(lti);
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
        expandAll(lti);
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
        expandAll(lti);
        Event.fireEvent(ti, new TreeItem.TreeModificationEvent<String>(TreeItem.valueChangedEvent(), ti));
    }

    private void addTiming(Assertion a, boolean withPoint, boolean withInterval, String sctUUID, TreeItem<String> ti)
    {
        Timing t = new Timing();
        a.setTiming(t);

        Measurement m = new Measurement();
        if (sctUUID != null)
        {
            Units u = new Units();
            Concept c = new Concept();
            c.setUuid(sctUUID);
            u.setConcept(c);
            m.setUnits(u);
        }
        t.setMeasurement(m);

        if (withPoint)
        {
            m.setPoint(new Point());
        }
        else if (withInterval)
        {
            m.setInterval(new Interval());
        }

        LegoTreeItem lti = new LegoTreeItem(t);
        ti.getChildren().add(lti);
        FXCollections.sort(ti.getChildren(), new LegoTreeItemComparator(true));
        expandAll(lti);
        ti.setExpanded(true);
        Event.fireEvent(ti, new TreeItem.TreeModificationEvent<String>(TreeItem.valueChangedEvent(), ti));
    }

    private void addAssertionComponent(AssertionComponents acs, TreeItem<String> ti)
    {
        AssertionComponent ac = new AssertionComponent();
        acs.getAssertionComponent().add(ac);

        LegoTreeItem lti = new LegoTreeItem(ac);
        ti.getChildren().add(lti);
        expandAll(lti);
        Event.fireEvent(ti, new TreeItem.TreeModificationEvent<String>(TreeItem.valueChangedEvent(), ti));
    }

    private void removeAssertionComponent(AssertionComponent ac, TreeItem<String> ti)
    {
        AssertionComponents acs = (AssertionComponents) ((LegoTreeItem) ti.getParent()).getExtraData();
        acs.getAssertionComponent().remove(ac);
        // No need to fire a parent event here - the options on AssertionComponents don't change with add/remove of a
        // component.
        ti.getParent().getChildren().remove(ti);
    }

    private void addAssertionComponents(Assertion a, TreeItem<String> ti)
    {
        AssertionComponents acs = new AssertionComponents();
        a.setAssertionComponents(acs);

        // go ahead and add the nested assertionComponent as well
        AssertionComponent ac = new AssertionComponent();
        acs.getAssertionComponent().add(ac);

        ti.getChildren().add(0, new LegoTreeItem(acs));
        expandAll(ti.getChildren().get(0));
        Event.fireEvent(ti, new TreeItem.TreeModificationEvent<String>(TreeItem.valueChangedEvent(), ti));
    }

    private void removeAssertionComponents(TreeItem<String> ti)
    {
        Assertion a = (Assertion) ((LegoTreeItem) ti.getParent()).getExtraData();
        a.setAssertionComponents(null);
        Event.fireEvent(ti.getParent(),
                new TreeItem.TreeModificationEvent<String>(TreeItem.valueChangedEvent(), ti.getParent()));
        ti.getParent().getChildren().remove(ti);
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
        // No need to fire event here, there is no parent to notify
    }

    private void addAssertion()
    {
        LegoTreeView ltv = (LegoTreeView) getTreeView();
        Assertion a = new Assertion();
        a.setAssertionUUID(UUID.randomUUID().toString());
        ltv.getLego().getAssertion().add(a);

        LegoTreeItem lti = new LegoTreeItem(a);
        ltv.getRoot().getChildren().add(lti);
        FXCollections.sort(ltv.getRoot().getChildren(), new LegoTreeItemComparator(true));
        expandAll(lti);
    }

    private void createNewLego(TreeItem<String> ti)
    {
        String pncsValue = ti.getValue();
        String pncsName = ti.getParent().getValue();

        // ID can be grabbed from any child lego of this treeItem.
        // Should always be at least one child
        int pncsId = ((Lego) ((LegoTreeItem) ti.getChildren().get(0)).getExtraData()).getPncs().getId();

        LegoList ll = (LegoList) ((LegoTreeItem) ti.getParent().getParent()).getExtraData();

        Lego l = new Lego();
        Pncs pncs = new Pncs();
        pncs.setName(pncsName);
        pncs.setValue(pncsValue);
        pncs.setId(pncsId);
        l.setPncs(pncs);

        Stamp s = new Stamp();
        s.setAuthor("author"); // TODO get the stamp details
        s.setModule("module");
        s.setPath("path");
        s.setStatus("Active");
        s.setTime(TimeConvert.convert(System.currentTimeMillis()));
        s.setUuid(UUID.randomUUID().toString());
        l.setStamp(s);

        l.setLegoUUID(UUID.randomUUID().toString());

        Assertion a = new Assertion();
        a.setAssertionUUID(UUID.randomUUID().toString());
        l.getAssertion().add(a);

        //TODO fix create lego option
//        ll.getLego().add(l);
//        LegoTreeItem lti = new LegoTreeItem(l);
//        ti.getChildren().add(lti);
//        LegoTreeView ltv = (LegoTreeView) getTreeView();
//        ltv.getSelectionModel().select(lti);
    }

    // TODO value menus
    
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
            label.getDropContextMenu().getItems().add(mi);
        }

        if (a.getTiming() == null)
        {
            mi = new MenuItem("Add an Interval Timing");
            mi.setOnAction(new EventHandler<ActionEvent>()
            {
                @Override
                public void handle(ActionEvent arg0)
                {
                    addTiming(a, false, true, null, treeItem);
                }
            });
            cm.getItems().add(mi);

            mi = new MenuItem("Add a Point Timing");
            mi.setOnAction(new EventHandler<ActionEvent>()
            {
                @Override
                public void handle(ActionEvent arg0)
                {
                    addTiming(a, true, false, null, treeItem);
                }
            });
            cm.getItems().add(mi);

            mi = new MenuItem("Drop as a Timing Unit");
            mi.setOnAction(new EventHandler<ActionEvent>()
            {
                @Override
                public void handle(ActionEvent arg0)
                {
                    addTiming(a, false, false, label.getDroppedValue(), treeItem);
                    label.setDroppedValue(null);
                }
            });
            label.getDropContextMenu().getItems().add(mi);
        }

        if (a.getAssertionComponents() == null)
        {
            mi = new MenuItem("Add an Assertion Component");
            mi.setOnAction(new EventHandler<ActionEvent>()
            {
                @Override
                public void handle(ActionEvent arg0)
                {
                    addAssertionComponents(a, treeItem);
                }
            });
            cm.getItems().add(mi);
        }

        mi = new MenuItem("Delete Assertion");
        mi.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent arg0)
            {
                removeAssertion(a, treeItem);
            }
        });
        cm.getItems().add(mi);
    }
    
    private void addMenus(final AssertionComponents acs, final LegoTreeItem treeItem, ContextMenu cm)
    {
        MenuItem mi = new MenuItem("Add Assertion Component");
        mi.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent arg0)
            {
                addAssertionComponent(acs, treeItem);
            }
        });
        cm.getItems().add(mi);

        mi = new MenuItem("Remove Assertion Components");
        mi.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent arg0)
            {
                removeAssertionComponents(treeItem);
            }
        });
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
        cm.getItems().add(mi);
    }
    
    private void addMenus(final Measurement m, final LegoTreeItem treeItem, ContextMenu cm)
    {
        MenuItem mi;
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
            cm.getItems().add(mi);
        }
        if (m.getInterval() == null && m.getPoint() == null)
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
            cm.getItems().add(mi);
        }

        if (!(((LegoTreeItem) treeItem.getParent()).getExtraData() instanceof Timing))
        {
            mi = new MenuItem("Remove Measurement");
            mi.setOnAction(new EventHandler<ActionEvent>()
            {
                @Override
                public void handle(ActionEvent arg0)
                {
                    removeMeasurement(m, treeItem);
                }
            });
            cm.getItems().add(mi);
        }
    }
    
    private void addMenus(final Timing t, final LegoTreeItem treeItem, ContextMenu cm)
    {
        MenuItem mi;
        if (t.getMeasurement() == null)
        {
            mi = new MenuItem("Add a Point Measurement");
            mi.setOnAction(new EventHandler<ActionEvent>()
            {
                @Override
                public void handle(ActionEvent arg0)
                {
                    addTimingMeasurement(t, true, false, treeItem);
                }
            });
            cm.getItems().add(mi);

            mi = new MenuItem("Add an Interval Measurement");
            mi.setOnAction(new EventHandler<ActionEvent>()
            {
                @Override
                public void handle(ActionEvent arg0)
                {
                    addTimingMeasurement(t, false, true, treeItem);
                }
            });
            cm.getItems().add(mi);
        }

        mi = new MenuItem("Remove Timing");
        mi.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent arg0)
            {
                removeTiming(t, treeItem);
            }
        });
        cm.getItems().add(mi);
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
            cm.getItems().add(mi);
        }
    }
    
    private void addMenus(final Concept c, final LegoTreeItem treeItem, ContextMenu cm)
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
            cm.getItems().add(mi);
        }
    }
    
    private void addMenus(final Relation r, final LegoTreeItem treeItem, ContextMenu cm, final DropTargetLabel label)
    {
        MenuItem mi;

        if (r.getDestination().getExpression() == null && r.getDestination().getMeasurement() == null
                && r.getDestination().getText() == null && r.getDestination().isBoolean() == null)
        {
            // TODO text / boolean
            mi = new MenuItem("Add a Destination Concept");
            mi.setOnAction(new EventHandler<ActionEvent>()
            {
                @Override
                public void handle(ActionEvent arg0)
                {
                    addConcept(r, null, treeItem);
                }
            });
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
            label.getDropContextMenu().getItems().add(mi);

            mi = new MenuItem("Add Interval Measurement");
            mi.setOnAction(new EventHandler<ActionEvent>()
            {
                @Override
                public void handle(ActionEvent arg0)
                {
                    addMeasurement(r, false, true, null, treeItem);
                }
            });
            cm.getItems().add(mi);

            mi = new MenuItem("Add Point Measurement");
            mi.setOnAction(new EventHandler<ActionEvent>()
            {
                @Override
                public void handle(ActionEvent arg0)
                {
                    addMeasurement(r, true, false, null, treeItem);
                }
            });
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
        cm.getItems().add(mi);
    }
    
    private void addMenus(final Point p, final LegoTreeItem treeItem, ContextMenu cm)
    {
        MenuItem mi = new MenuItem("Remove Point");
        mi.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent arg0)
            {
                removePoint(p, treeItem);
            }
        });
        cm.getItems().add(mi);
    }
    
    private void addMenus(final Interval i, final LegoTreeItem treeItem, ContextMenu cm) 
    {
        MenuItem mi = new MenuItem("Remove Interval");
        mi.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent arg0)
            {
                removeInterval(i, treeItem);
            }
        });
        cm.getItems().add(mi);
    }
    
    private void addMenus(final Value v, final LegoTreeItem treeItem, ContextMenu cm, final DropTargetLabel label)
    {
        if (v.getExpression() == null && v.getMeasurement() == null && v.getText() == null
                && v.isBoolean() == null)
        {
            // TODO text and boolean
            MenuItem mi = new MenuItem("Add a Concept");
            mi.setOnAction(new EventHandler<ActionEvent>()
            {
                @Override
                public void handle(ActionEvent arg0)
                {
                    addConcept(v, null, treeItem);
                }
            });
            cm.getItems().add(mi);

            mi = new MenuItem("Add Interval Measurement");
            mi.setOnAction(new EventHandler<ActionEvent>()
            {
                @Override
                public void handle(ActionEvent arg0)
                {
                    addMeasurement(v, false, true, null, treeItem);
                }
            });
            cm.getItems().add(mi);

            mi = new MenuItem("Add Point Measurement");
            mi.setOnAction(new EventHandler<ActionEvent>()
            {
                @Override
                public void handle(ActionEvent arg0)
                {
                    addMeasurement(v, true, false, null, treeItem);
                }
            });
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
            label.getDropContextMenu().getItems().add(mi);

            mi = new MenuItem("Drop as Measurement Units");
            mi.setOnAction(new EventHandler<ActionEvent>()
            {
                @Override
                public void handle(ActionEvent arg0)
                {
                    addMeasurement(v, true, false, label.getDroppedValue(), treeItem);
                    label.setDroppedValue(null);
                }
            });
            label.getDropContextMenu().getItems().add(mi);
        }
    }

    private void expandAll(TreeItem<String> ti)
    {
        ti.setExpanded(true);
        for (TreeItem<String> tiChild : ti.getChildren())
        {
            expandAll(tiChild);
        }
    }
}
