package gov.va.legoEdit.gui.legoTreeView;

import gov.va.legoEdit.LegoGUI;
import gov.va.legoEdit.gui.util.DropTargetLabel;
import gov.va.legoEdit.gui.util.LegoTreeItemComparator;
import gov.va.legoEdit.model.schemaModel.Assertion;
import gov.va.legoEdit.model.schemaModel.AssertionComponent;
import gov.va.legoEdit.model.schemaModel.AssertionComponents;
import gov.va.legoEdit.model.schemaModel.Concept;
import gov.va.legoEdit.model.schemaModel.Discernible;
import gov.va.legoEdit.model.schemaModel.Interval;
import gov.va.legoEdit.model.schemaModel.Lego;
import gov.va.legoEdit.model.schemaModel.LegoList;
import gov.va.legoEdit.model.schemaModel.Measurement;
import gov.va.legoEdit.model.schemaModel.MeasurementString;
import gov.va.legoEdit.model.schemaModel.Pncs;
import gov.va.legoEdit.model.schemaModel.Point;
import gov.va.legoEdit.model.schemaModel.Qualifier;
import gov.va.legoEdit.model.schemaModel.Rel;
import gov.va.legoEdit.model.schemaModel.Stamp;
import gov.va.legoEdit.model.schemaModel.Timing;
import gov.va.legoEdit.model.schemaModel.Units;
import gov.va.legoEdit.model.schemaModel.Value;
import gov.va.legoEdit.util.TimeConvert;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.UUID;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
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
    private static ObservableList<String> statusChoices_ = FXCollections.observableArrayList(new String[] {"Active", "Inactive"});
    
    @Override
    public void updateItem(T item, boolean empty)
    {
        super.updateItem(item, empty);

        final LegoTreeItem treeItem = (LegoTreeItem) getTreeItem();
        
        if (empty)
        {
            setText(null);
            setGraphic(null);
        }
        else
        {

            if (treeItem.getNodeType() == null)
            {
                // Treat as a basic string
                setText(item == null ? "null" : item.toString());
                setGraphic(null);
            }
            else
            {
                if (treeItem.getNodeType() == LegoTreeNodeType.legoList)
                {
                    setText(item.toString());
                    setGraphic(null);
                    final SimpleStringProperty legoListDescriptionProperty = new SimpleStringProperty(((LegoList)treeItem.getExtraData()).getGroupDescription());
                    Tooltip tp = new Tooltip();
                    tp.textProperty().bind(legoListDescriptionProperty);
                    setTooltip(tp);
                    
                    legoListDescriptionProperty.addListener(new ChangeListener<String>()
                    {
                        @Override
                        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
                        {
                            ((LegoList)treeItem.getExtraData()).setGroupDescription(newValue);
                        }
                    });
                    
                    ArrayList<MenuItem> menuItems = new ArrayList<>();
                    
                    menuItems.add(new MenuItem("Add New Lego"));
                    menuItems.get(0).setOnAction(new EventHandler<ActionEvent>()
                    {
                        @Override
                        public void handle(ActionEvent arg0)
                        {
                            LegoList ll = (LegoList)treeItem.getExtraData(); 
                            LegoGUI.getInstance().showCreateLegoDialog(ll, treeItem);
                        }
                    });

                    menuItems.add(new MenuItem("Show XML View"));
                    menuItems.get(1).setOnAction(new EventHandler<ActionEvent>()
                    {
                        @Override
                        public void handle(ActionEvent arg0)
                        {
                            LegoList ll = (LegoList)treeItem.getExtraData(); 
                            LegoGUI.getInstance().showXMLViewWindow(ll);
                        }
                    });
                    menuItems.add(new MenuItem("Properties"));
                    menuItems.get(2).setOnAction(new EventHandler<ActionEvent>()
                    {
                        @Override
                        public void handle(ActionEvent arg0)
                        {
                            LegoList ll = (LegoList)treeItem.getExtraData(); 
                            LegoGUI.getInstance().showLegoListPropertiesDialog(ll.getGroupName(),
                                    ll.getLegoListUUID(), legoListDescriptionProperty);
                        }
                    });
                    
                    setContextMenu(new ContextMenu(menuItems.toArray(new MenuItem[0])));
                }
                else if (treeItem.getNodeType() == LegoTreeNodeType.legoListLego)
                {
                    //TODO not used?
                    setText(null);
                    Label l = new Label("Lego");
                    setGraphic(l);
                    if (LegoGUI.getInstance().getLegoGUIController().getLegoBeingEdited() == treeItem.getExtraData())
                    {
                        l.getStyleClass().add("boldLabel");
                        getStyleClass().add("selectedLego");
                    }
                    setEditable(true);
                }
                else if (treeItem.getNodeType() == LegoTreeNodeType.addLegoListPlaceholder)
                {
                    setText(null);
                    Button btn = new Button("Add Lego List");
                    btn.setOnAction(new EventHandler<ActionEvent>()
                    {
                        @Override
                        public void handle(ActionEvent arg0)
                        {
                            LegoGUI.getInstance().showLegoListPropertiesDialog("", UUID.randomUUID().toString(), new SimpleStringProperty(""));
                        }
                    });
                    setGraphic(btn);
                }
                else if (treeItem.getNodeType() == LegoTreeNodeType.status)
                {
                    //TODO tie status back to in-memory lego
                    setText(null);
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
                else if (treeItem.getNodeType() == LegoTreeNodeType.addAssertionPlaceholder)
                {
                    setText(null);
                    Button btn = new Button(treeItem.getValue());
                    btn.setOnAction(new EventHandler<ActionEvent>()
                    {
                        @Override
                        public void handle(ActionEvent arg0)
                        {
                            addAssertion(treeItem);
                        }
                    });
                    setGraphic(btn);
                }
                else if (treeItem.getNodeType() == LegoTreeNodeType.pncsValue)
                {
                    setText(item.toString());
                    setGraphic(null);
                   
                    ArrayList<MenuItem> menuItems = new ArrayList<>();

                    menuItems.add(new MenuItem("Create New Lego for PNCS"));
                    menuItems.get(0).setOnAction(new EventHandler<ActionEvent>()
                    {
                        @Override
                        public void handle(ActionEvent arg0)
                        {
                            createNewLego(treeItem);
                        }
                    });
                    
                    setContextMenu(new ContextMenu(menuItems.toArray(new MenuItem[0])));
                }     
                else if (treeItem.getNodeType() == LegoTreeNodeType.labeledUneditableString)
                {
                    setText(null);
                    setGraphic(makeLabeledString(treeItem.getExtraData().toString(), item.toString()));
                }
                else if (treeItem.getNodeType() == LegoTreeNodeType.assertion)
                {
                    final Assertion a = (Assertion)treeItem.getExtraData();
                    setText(null);
                    HBox hbox = new HBox();
                    hbox.setSpacing(10.0);
                    hbox.setAlignment(Pos.CENTER_LEFT);

                    final DropTargetLabel assertionLabel = new DropTargetLabel("Assertion");
                    MenuItem mi;
                    ContextMenu standardContextMenu = new ContextMenu();
                    ContextMenu dropContextMenu = new ContextMenu();
                    
                    if (a.getDiscernible() == null || a.getDiscernible().getConcept() == null)
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
                        standardContextMenu.getItems().add(mi);
                        
                        mi = new MenuItem("Drop as a Discernibile");
                        mi.setOnAction(new EventHandler<ActionEvent>()
                        {
                            @Override
                            public void handle(ActionEvent arg0)
                            {
                                addDiscernibile(a, assertionLabel.getDroppedValue(), treeItem);
                                assertionLabel.setDroppedValue(null);
                            }
                        });
                        dropContextMenu.getItems().add(mi);
                    }
                    
                    if (a.getQualifier() == null || a.getQualifier().getConcept() == null)
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
                        standardContextMenu.getItems().add(mi);
                        
                        mi = new MenuItem("Drop as a Qualifier");
                        mi.setOnAction(new EventHandler<ActionEvent>()
                        {
                            @Override
                            public void handle(ActionEvent arg0)
                            {
                                addQualifier(a, assertionLabel.getDroppedValue(), treeItem);
                                assertionLabel.setDroppedValue(null);
                            }
                        });
                        dropContextMenu.getItems().add(mi);
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
                        standardContextMenu.getItems().add(mi);
                    }
                    if (a.getValue() != null && a.getValue().getConcept() == null && a.getValue().getMeasurement() == null)
                    {
                        mi = new MenuItem("Drop as a Value");
                        mi.setOnAction(new EventHandler<ActionEvent>()
                        {
                            @Override
                            public void handle(ActionEvent arg0)
                            {
                                addValue(a, assertionLabel.getDroppedValue(), treeItem);
                                assertionLabel.setDroppedValue(null);
                            }
                        });
                        dropContextMenu.getItems().add(mi);
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
                        standardContextMenu.getItems().add(mi);
                        
                        mi = new MenuItem("Add a Point Timing");
                        mi.setOnAction(new EventHandler<ActionEvent>()
                        {
                            @Override
                            public void handle(ActionEvent arg0)
                            {
                                addTiming(a, true, false, null, treeItem);
                            }
                        });
                        standardContextMenu.getItems().add(mi);
                        
                        mi = new MenuItem("Drop as a Timing Unit");
                        mi.setOnAction(new EventHandler<ActionEvent>()
                        {
                            @Override
                            public void handle(ActionEvent arg0)
                            {
                                addTiming(a, false, false, assertionLabel.getDroppedValue(), treeItem);
                                assertionLabel.setDroppedValue(null);
                            }
                        });
                        dropContextMenu.getItems().add(mi);
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
                        standardContextMenu.getItems().add(mi);
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
                    standardContextMenu.getItems().add(mi);
                    
                    assertionLabel.setContextMenu(standardContextMenu);
                    assertionLabel.setDropContextMenu(dropContextMenu);
                    
                    LegoGUI.getInstance().getLegoGUIController().addSnomedDropTarget(((LegoTreeView)getTreeView()).getLego(), assertionLabel);
                    hbox.getChildren().add(assertionLabel);
                    
                    hbox.getChildren().add(makeLabeledString("UUID", a.getAssertionUUID()));
                    setGraphic(hbox);
                }
                else if (treeItem.getNodeType() == LegoTreeNodeType.assertionComponents)
                {
                    final AssertionComponents acs = (AssertionComponents)treeItem.getExtraData();
                    setText(item.toString());
                    ContextMenu cm = new ContextMenu();
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
                    setContextMenu(cm);
                }
                else if (treeItem.getNodeType() == LegoTreeNodeType.assertionComponent)
                {
                    final AssertionComponent ac = (AssertionComponent)treeItem.getExtraData();
                    setText(item.toString());
                    ContextMenu cm = new ContextMenu();
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
                    
                    setContextMenu(cm);
                }
                else if (treeItem.getNodeType() == LegoTreeNodeType.assertionUUID)
                {
                    String value = treeItem.getValue();
                    
                    final AssertionComponent ac = (AssertionComponent)((LegoTreeItem)treeItem.getParent()).getExtraData();
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
                else if (treeItem.getNodeType() == LegoTreeNodeType.assertionTypeConcept)
                {
                    Concept c = (Concept)treeItem.getExtraData();
                    final ConceptNode cn = new ConceptNode("Type", c, treeItem.getNodeType(), ((LegoTreeView)getTreeView()).getLego());
                    cn.addObserver(new Observer()
                    {
                        @Override
                        public void update(Observable o, Object arg)
                        {
                            System.out.println("Concept updated");
                            //concept is already tied to the lego, shouldn't have to do anything.  But could if we wanted.
                        }
                    });
                    
                    setGraphic(cn.getNode());
                }
                else if (treeItem.getNodeType() == LegoTreeNodeType.measurement)
                {
                    setText(treeItem.getValue());
                    setGraphic(null);
                    final Measurement m = (Measurement) treeItem.getExtraData();

                    ContextMenu cm = new ContextMenu();
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

                    if (!(((LegoTreeItem)treeItem.getParent()).getExtraData() instanceof Timing))
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
                    setContextMenu(cm);
                }
                else if (treeItem.getNodeType() == LegoTreeNodeType.timing)
                {
                    final Timing t = (Timing)treeItem.getExtraData(); 
                    setText(item == null ? "null" : item.toString());
                    setGraphic(null);
                    
                    ContextMenu cm = new ContextMenu();
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
                    setContextMenu(cm);
                }
                else if (treeItem.getNodeType() == LegoTreeNodeType.destinationConcept
                        || treeItem.getNodeType() == LegoTreeNodeType.typeConcept
                        || treeItem.getNodeType() == LegoTreeNodeType.relConcept
                        || treeItem.getNodeType() == LegoTreeNodeType.unitsConcept
                        || treeItem.getNodeType() == LegoTreeNodeType.valueConcept)

                {
                    setText(null);
                    final Concept c = (Concept) treeItem.getExtraData();
                    
                    ContextMenu cm = new ContextMenu();
                    MenuItem mi = new MenuItem("Add a relationship");
                    mi.setOnAction(new EventHandler<ActionEvent>()
                    {

                        @Override
                        public void handle(ActionEvent arg0)
                        {
                            addRelationship(c, treeItem);
                        }
                    });
                    cm.getItems().add(mi);
                    
                    if (treeItem.getNodeType() == LegoTreeNodeType.relConcept
                            || treeItem.getNodeType() == LegoTreeNodeType.valueConcept)
                    {
                        mi = new MenuItem("Remove Concept");
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
                    
                    if (treeItem.getNodeType() == LegoTreeNodeType.unitsConcept)
                    {
                        mi = new MenuItem("Remove Units");
                        mi.setOnAction(new EventHandler<ActionEvent>()
                        {

                            @Override
                            public void handle(ActionEvent arg0)
                            {
                                removeUnitsConcept(treeItem);
                            }
                        });
                        cm.getItems().add(mi);
                    }
                    
                    String label = "";
                    if (treeItem.getNodeType() == LegoTreeNodeType.typeConcept)
                    {
                        label = "Type";
                    }
                    else if (treeItem.getNodeType() == LegoTreeNodeType.relConcept)
                    {
                        label = "Rel";
                    }
                    else if (treeItem.getNodeType() == LegoTreeNodeType.unitsConcept)
                    {
                        label = "Units";
                    }
                    
                    setContextMenu(cm);
                    ConceptNode cn = new ConceptNode(label, c, treeItem.getNodeType(), ((LegoTreeView)getTreeView()).getLego());
                    setGraphic(cn.getNode());
                }
                else if (treeItem.getNodeType() == LegoTreeNodeType.relation)
                {
                    final Rel r = (Rel) treeItem.getExtraData();
                    
                    final DropTargetLabel relLabel = new DropTargetLabel(treeItem.getValue());

                    ContextMenu standardContextMenu = new ContextMenu();
                    ContextMenu dropContextMenu = new ContextMenu();
                    MenuItem mi;
                    
                    if (r.getDestination().getConcept() == null && r.getDestination().getMeasurement() == null)
                    {
                        mi = new MenuItem("Add Concept");
                        mi.setOnAction(new EventHandler<ActionEvent>()
                        {
                            @Override
                            public void handle(ActionEvent arg0)
                            {
                                addConcept(r, null, treeItem);
                            }
                        });
                        standardContextMenu.getItems().add(mi);
                        
                        mi = new MenuItem("Drop as the Rel Concept");
                        mi.setOnAction(new EventHandler<ActionEvent>()
                        {
                            @Override
                            public void handle(ActionEvent arg0)
                            {
                                addConcept(r, relLabel.getDroppedValue(), treeItem);
                                relLabel.setDroppedValue(null);
                            }
                        });
                        dropContextMenu.getItems().add(mi);
                        
                        mi = new MenuItem("Add Interval Measurement");
                        mi.setOnAction(new EventHandler<ActionEvent>()
                        {
                            @Override
                            public void handle(ActionEvent arg0)
                            {
                                addMeasurement(r, false, true, null, treeItem);
                            }
                        });
                        standardContextMenu.getItems().add(mi);
                        
                        mi = new MenuItem("Add Point Measurement");
                        mi.setOnAction(new EventHandler<ActionEvent>()
                        {
                            @Override
                            public void handle(ActionEvent arg0)
                            {
                                addMeasurement(r, true, false, null, treeItem);
                            }
                        });
                        standardContextMenu.getItems().add(mi);
                    }
                    
                    mi = new MenuItem("Remove Relationship");
                    mi.setOnAction(new EventHandler<ActionEvent>()
                    {
                        @Override
                        public void handle(ActionEvent arg0)
                        {
                            removeRelationship(r, treeItem);
                        }
                    });
                    standardContextMenu.getItems().add(mi);
 
                    setContextMenu(standardContextMenu);
                    relLabel.setContextMenu(standardContextMenu);
                    relLabel.setDropContextMenu(dropContextMenu);
                    setGraphic(relLabel);
                    LegoGUI.getInstance().getLegoGUIController().addSnomedDropTarget(((LegoTreeView)getTreeView()).getLego(), relLabel);
                    
                }
                else if (treeItem.getNodeType() == LegoTreeNodeType.point)
                {
                    final Point p = (Point) treeItem.getExtraData();
                    final ComboBox<String> cb = new ComboBox<>();
                    cb.setEditable(true);
                    for (MeasurementString s : MeasurementString.values())
                    {
                        cb.getItems().add(s.value());
                    }
                    cb.setPromptText("Enter a numeric value, or select from the list");
                    cb.setVisibleRowCount(MeasurementString.values().length + 1);
                    cb.setPrefWidth(350.0);
                    
                    if (p.getStringValue() != null)
                    {
                        cb.getSelectionModel().select(p.getStringValue().value());
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
                                p.setStringValue(null);
                                p.setNumericValue(f);
                                p.setInclusive(true);  //This point impl only handles single points, so always inclusive
                            }
                            catch (NumberFormatException e)
                            {
                                try
                                {
                                    MeasurementString ms = MeasurementString.fromValue(newValue);
                                    p.setNumericValue(null);
                                    p.setStringValue(ms);
                                    p.setInclusive(true);  //This point impl only handles single points, so always inclusive
                                }
                                catch (IllegalArgumentException ex)
                                {
                                  //TODO highlight invalid
                                    System.out.println("INVALID");
                                }
                            }
                        }
                    });
                    
                    ContextMenu cm = new ContextMenu();
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
                    setContextMenu(cm);
                    setGraphic(cb);
                }
                else if (treeItem.getNodeType() == LegoTreeNodeType.interval)
                {
                    final Interval i = (Interval) treeItem.getExtraData();
                    setText("Interval");
                    //TODO figure out how to do intervals and bounds
                    
                    ContextMenu cm = new ContextMenu();
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
                    setContextMenu(cm);
                }
                else if (treeItem.getNodeType() == LegoTreeNodeType.value)
                {
                    final Value v = (Value)treeItem.getExtraData();
                    final DropTargetLabel valueLabel = new DropTargetLabel(treeItem.getValue());
                    
                    ContextMenu standardContextMenu = new ContextMenu();
                    ContextMenu dropContextMenu = new ContextMenu();
                    
                    if (v.getConcept() == null && v.getMeasurement() == null)
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
                        standardContextMenu.getItems().add(mi);
                        
                        mi = new MenuItem("Add Interval Measurement");
                        mi.setOnAction(new EventHandler<ActionEvent>()
                        {
                            @Override
                            public void handle(ActionEvent arg0)
                            {
                                addMeasurement(v, false, true, null, treeItem);
                            }
                        });
                        standardContextMenu.getItems().add(mi);
                        
                        mi = new MenuItem("Add Point Measurement");
                        mi.setOnAction(new EventHandler<ActionEvent>()
                        {
                            @Override
                            public void handle(ActionEvent arg0)
                            {
                                addMeasurement(v, true, false, null, treeItem);
                            }
                        });
                        standardContextMenu.getItems().add(mi);
                        
                        mi = new MenuItem("Drop as a Concept");
                        mi.setOnAction(new EventHandler<ActionEvent>()
                        {
                            @Override
                            public void handle(ActionEvent arg0)
                            {
                                addConcept(v, valueLabel.getDroppedValue(), treeItem);
                                valueLabel.setDroppedValue(null);
                            }
                        });
                        dropContextMenu.getItems().add(mi);
                        
                        mi = new MenuItem("Drop as Measurement Units");
                        mi.setOnAction(new EventHandler<ActionEvent>()
                        {
                            @Override
                            public void handle(ActionEvent arg0)
                            {
                                addMeasurement(v, true, false, valueLabel.getDroppedValue(), treeItem);
                                valueLabel.setDroppedValue(null);
                            }
                        });
                        dropContextMenu.getItems().add(mi);
                        
                        setContextMenu(standardContextMenu);
                    }
                    
                    setGraphic(valueLabel);
                    valueLabel.setContextMenu(standardContextMenu);
                    valueLabel.setDropContextMenu(dropContextMenu);
                    LegoGUI.getInstance().getLegoGUIController().addSnomedDropTarget(((LegoTreeView)getTreeView()).getLego(), valueLabel);
                }
                 
                else
                {
                    // Treat as a basic string
                    setText(item == null ? "null" : item.toString());
                    setGraphic(null);

                }
            }
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
        Assertion a = (Assertion)((LegoTreeItem)ti.getParent()).getExtraData();
        a.setTiming(null);
        Event.fireEvent(ti.getParent(), new TreeItem.TreeModificationEvent<String>(TreeItem.valueChangedEvent(), ti.getParent()));
        ti.getParent().getChildren().remove(ti);
    }
    
    private void removeMeasurement(Measurement m, TreeItem<String> ti)
    {
        Object parent = ((LegoTreeItem)ti.getParent()).getExtraData();
        if (parent instanceof Value)
        {
            ((Value)parent).setMeasurement(null);
        }
        else if (parent instanceof Rel)
        {
            ((Rel)parent).getDestination().setMeasurement(null);
        }
        else if (parent instanceof Timing)
        {
            //Don't allow removal here - they should remove the entire timing.
            logger.error("Dan messed up - this measurement removal shouldn't be called");
            return;
        }
        else 
        {
            logger.error("Dan messed up! - don't know how to remove measurement - type " + parent.toString());
            return;
        }
        Event.fireEvent(ti.getParent(), new TreeItem.TreeModificationEvent<String>(TreeItem.valueChangedEvent(), ti.getParent()));
        ti.getParent().getChildren().remove(ti);
    }
    
    private void addUnits(Measurement m, TreeItem<String> ti)
    {
        Units u = new Units();
        Concept c = new Concept();
        u.setConcept(c);
        m.setUnits(u);
        ti.getChildren().add(new LegoTreeItem(c, LegoTreeNodeType.unitsConcept));
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
        Measurement m = (Measurement)((LegoTreeItem)ti.getParent()).getExtraData();
        m.setInterval(null);
        Event.fireEvent(ti.getParent(), new TreeItem.TreeModificationEvent<String>(TreeItem.valueChangedEvent(), ti.getParent()));
        ti.getParent().getChildren().remove(ti);
    }
    
    private void removePoint(Point p, TreeItem<String> ti)
    {
        Object parent = ((LegoTreeItem)ti.getParent()).getExtraData();
        if (parent instanceof Measurement)
        {
            Measurement m = (Measurement)parent;
            m.setPoint(null);
            Event.fireEvent(ti.getParent(), new TreeItem.TreeModificationEvent<String>(TreeItem.valueChangedEvent(), ti.getParent()));
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
    
    private void removeUnitsConcept(TreeItem<String> ti)
    {
        Measurement m = (Measurement)((LegoTreeItem)ti.getParent()).getExtraData();  //We don't put the units filler in the tree - straight up to Measurement
        m.setUnits(null);
        Event.fireEvent(ti.getParent(), new TreeItem.TreeModificationEvent<String>(TreeItem.valueChangedEvent(), ti.getParent()));
        ti.getParent().getChildren().remove(ti);
    }
        
    private void addMeasurement(Object parent, boolean withPoint, boolean withInterval, String unitsSCTID, TreeItem<String> ti)
    {
        Measurement m = new Measurement();
        if (parent instanceof Rel)
        {
            ((Rel)parent).getDestination().setMeasurement(m);
        }
        else if (parent instanceof Value)
        {
            ((Value)parent).setMeasurement(m);
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
    
    private void addConcept(Object p, String sctUUID, TreeItem<String> ti)
    {
        Concept c = new Concept();
        LegoTreeNodeType type;
        if (p instanceof Value)
        {
            ((Value)p).setConcept(c);
            type = LegoTreeNodeType.valueConcept;
        }
        else if (p instanceof Rel)
        {
           ((Rel)p).getDestination().setConcept(c);
           type = LegoTreeNodeType.relConcept;
        }
        else
        {
            logger.error("Unhandled concept add: " + p);
            return;
        }

        c.setUuid(sctUUID);
        ti.getChildren().add(new LegoTreeItem(c, type));
        expandAll(ti);
        Event.fireEvent(ti, new TreeItem.TreeModificationEvent<String>(TreeItem.valueChangedEvent(), ti));
    }
    
    private void removeConcept(TreeItem<String> ti)
    {
        Object parent = ((LegoTreeItem)ti.getParent()).getExtraData();
        if (parent instanceof Value)
        {
            ((Value)parent).setConcept(null);
        }
        else if (parent instanceof Rel)
        {
            ((Rel)parent).getDestination().setConcept(null);
        }
        else
        {
            logger.error("Unhandled concept remove request: " + parent);
            return;
        }
        Event.fireEvent(ti.getParent(), new TreeItem.TreeModificationEvent<String>(TreeItem.valueChangedEvent(), ti.getParent()));
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
    
    private void addRelationship(Concept c, TreeItem<String> ti)
    {
        Rel r = new Rel();
        c.getRel().add(r);
        ti.getChildren().add(new LegoTreeItem(r));
        expandAll(ti);
        Event.fireEvent(ti, new TreeItem.TreeModificationEvent<String>(TreeItem.valueChangedEvent(), ti));
    }

    
    private void removeRelationship(Rel rel, TreeItem<String> ti)
    {
        Concept c = (Concept)((LegoTreeItem)ti.getParent()).getExtraData();
        c.getRel().remove(rel);
        //No need to fire a parent event here - the options on Concept don't change with add/remove of a rel.
        ti.getParent().getChildren().remove(ti);
    }
    
    
    private void addValue(Assertion a, String sctUUID, TreeItem<String> ti)
    {
        Value v = new Value();
        a.setValue(v);
        if (sctUUID != null)
        {
            Concept c = new Concept();
            c.setUuid(sctUUID);
            v.setConcept(c);
        }
        int pos = 0;
        ti.getChildren().add(pos, new LegoTreeItem(v));
        FXCollections.sort(ti.getChildren(), new LegoTreeItemComparator(true));
        expandAll(ti.getChildren().get(pos));
        Event.fireEvent(ti, new TreeItem.TreeModificationEvent<String>(TreeItem.valueChangedEvent(), ti));
    }
    
    private void addQualifier(Assertion a, String sctUUID, TreeItem<String> ti)
    {
        Qualifier q = new Qualifier();
        a.setQualifier(q);
        Concept c = new Concept();
        c.setUuid(sctUUID);
        q.setConcept(c);
        int pos = 0;
        ti.getChildren().add(pos, new LegoTreeItem(q));
        FXCollections.sort(ti.getChildren(), new LegoTreeItemComparator(true));
        expandAll(ti.getChildren().get(pos));
        Event.fireEvent(ti, new TreeItem.TreeModificationEvent<String>(TreeItem.valueChangedEvent(), ti));
    }
    
    private void addDiscernibile(Assertion a, String sctUUID, TreeItem<String> ti)
    {
        Discernible d = new Discernible();
        a.setDiscernible(d);
        Concept c = new Concept();
        c.setUuid(sctUUID);
        d.setConcept(c);
        int pos = 0;
        ti.getChildren().add(pos, new LegoTreeItem(d));
        FXCollections.sort(ti.getChildren(), new LegoTreeItemComparator(true));
        expandAll(ti.getChildren().get(pos));
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
        
        int pos = 0;
        ti.getChildren().add(pos, new LegoTreeItem(t));
        FXCollections.sort(ti.getChildren(), new LegoTreeItemComparator(true));
        expandAll(ti.getChildren().get(pos));
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
        AssertionComponents acs = (AssertionComponents)((LegoTreeItem)ti.getParent()).getExtraData();
        acs.getAssertionComponent().remove(ac);
        //No need to fire a parent event here - the options on AssertionComponents don't change with add/remove of a component.
        ti.getParent().getChildren().remove(ti);
    }
    
    private void addAssertionComponents(Assertion a, TreeItem<String> ti)
    {
        AssertionComponents acs = new AssertionComponents();
        a.setAssertionComponents(acs);

        //  go ahead and add the nested assertionComponent as well
        AssertionComponent ac = new AssertionComponent();
        acs.getAssertionComponent().add(ac);
        
        ti.getChildren().add(0, new LegoTreeItem(acs));
        expandAll(ti.getChildren().get(0));
        Event.fireEvent(ti, new TreeItem.TreeModificationEvent<String>(TreeItem.valueChangedEvent(), ti));
    }
    
    private void removeAssertionComponents(TreeItem<String> ti)
    {
         Assertion a = (Assertion)((LegoTreeItem)ti.getParent()).getExtraData();
         a.setAssertionComponents(null);
         Event.fireEvent(ti.getParent(), new TreeItem.TreeModificationEvent<String>(TreeItem.valueChangedEvent(), ti.getParent()));
         ti.getParent().getChildren().remove(ti);
    }
    
    private void removeAssertion(Assertion a, TreeItem<String> ti)
    {
        LegoTreeView ltv = (LegoTreeView)getTreeView();
        for (Assertion assertion : ltv.getLego().getAssertion())
        {
            if (assertion.getAssertionUUID().equals(a.getAssertionUUID()))
            {
                ltv.getLego().getAssertion().remove(assertion);
                break;
            }
        }
        ltv.getRoot().getChildren().remove(ti);
        //No need to fire event here, there is no parent to notify
    }
    
    private void addAssertion(TreeItem<String> ti)
    {
        LegoTreeView ltv = (LegoTreeView)getTreeView();
        Assertion a = new Assertion();
        a.setAssertionUUID(UUID.randomUUID().toString());
        ltv.getLego().getAssertion().add(a);
        
        LegoTreeItem lti = new LegoTreeItem(a);
        ltv.getRoot().getChildren().add(lti);
        FXCollections.sort(ltv.getRoot().getChildren(), new LegoTreeItemComparator(true));
        expandAll(lti);
        Event.fireEvent(ti, new TreeItem.TreeModificationEvent<String>(TreeItem.valueChangedEvent(), ti));
    }
    
    private void createNewLego(TreeItem<String> ti)
    {
        String pncsValue = ti.getValue();
        String pncsName = ti.getParent().getValue();
        
        //ID can be grabbed from any child lego of this treeItem.
        //Should always be at least one child
        int pncsId = ((Lego)((LegoTreeItem)ti.getChildren().get(0)).getExtraData()).getPncs().getId();
        
        LegoList ll = (LegoList)((LegoTreeItem)ti.getParent().getParent()).getExtraData();

        Lego l = new Lego();
        Pncs pncs = new Pncs();
        pncs.setName(pncsName);
        pncs.setValue(pncsValue);
        pncs.setId(pncsId); 
        l.setPncs(pncs);
        
        Stamp s = new Stamp();
        s.setAuthor("author"); //TODO get the stamp details
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
        
        ll.getLego().add(l);
        LegoTreeItem lti = new LegoTreeItem(l);
        ti.getChildren().add(lti);
        LegoTreeView ltv = (LegoTreeView)getTreeView();
        ltv.getSelectionModel().select(lti);
    }
    //TODO value menus
    
    private void expandAll(TreeItem<String> ti)
    {
        ti.setExpanded(true);
        for (TreeItem<String> tiChild : ti.getChildren())
        {
            expandAll(tiChild);
        }
    }
    
// Didn't like this... simpler in some ways, but lose all the expand collapse stuff.. to much work to get right.
//    private void replaceTreeItem(LegoTreeItem current, LegoTreeItem replacement)
//    {
//        LegoTreeView ltv = (LegoTreeView)getTreeView();
//        ObservableList<TreeItem<String>> levelOne = ltv.getRoot().getChildren();
//        for (int i = 0; i < levelOne.size(); i++)
//        {
//            if (levelOne.get(i) == current)
//            {
//                levelOne.remove(i);
//                levelOne.add(i, replacement);
//                return;
//            }
//        }
//        for (TreeItem<String> ti : ltv.getRoot().getChildren())
//        {
//            if (replaceTreeItem(ti.getChildren(), current, replacement))
//            {
//                return;
//            }
//        }
//    }
//    
//    private boolean replaceTreeItem(ObservableList<TreeItem<String>> items, LegoTreeItem current, LegoTreeItem replacement)
//    {
//        for (int i = 0; i < items.size(); i++)
//        {
//            if (items.get(i) == current)
//            {
//                items.remove(i);
//                items.add(i, replacement);
//                return true;
//            }
//        }
//        for (TreeItem<String> ti : items)
//        {
//            if (replaceTreeItem(ti.getChildren(), current, replacement))
//            {
//                return true;
//            }
//        }
//        return false;
//    }
}
