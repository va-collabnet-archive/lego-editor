package gov.va.legoEdit.gui.legoTreeView;

import gov.va.legoEdit.LegoGUI;
import gov.va.legoEdit.LegoGUIModel;
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
import gov.va.legoEdit.model.schemaModel.LegoList;
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
import gov.va.legoEdit.storage.LegoStatus;
import gov.va.legoEdit.storage.WriteException;
import gov.va.legoEdit.util.TimeConvert;
import java.util.Arrays;
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
import javafx.scene.image.ImageView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LegoTreeCell - A monstrosity of a class that handles the display and user interaction of each node within the lego tree.
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 * Copyright 2013
 * 
 */
public class LegoTreeCell<T> extends TreeCell<T>
{
	private static Logger logger = LoggerFactory.getLogger(LegoTreeCell.class);
	private static String sep = " \u25BB ";
	private static ObservableList<String> statusChoices_ = FXCollections.observableArrayList(new String[] { LegoStatus.Active.name(), LegoStatus.Inactive.name()});
	private static ObservableList<String> booleanChoices_ = FXCollections.observableArrayList(new String[] { "True", "False" });
	private static ObservableList<String> inclusiveChoices_ = FXCollections.observableArrayList(new String[] { "\u2264", "<" });
	public static boolean isMac = (System.getProperty("os.name").toLowerCase().indexOf("mac") >= 0);

	private LegoTreeView treeView;

	public LegoTreeCell(LegoTreeView ltv)
	{
		// For reasons I don't understand, the getTreeView() method is unreliable, sometimes returning null. Either a bug in javafx, or really poorly
		// documented...
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
			LegoTreeNodeGraphic nodeBox = new LegoTreeNodeGraphic();
			
			// This is the first time I really don't understand the JavaFX API. It appears to reuse the item values, when scrolling up and down...
			// So if the item type changes from one position to another, and you don't unset (or reset) all of the same properties that were set
			// previously see http://javafx-jira.kenai.com/browse/RT-19629
			setEditable(false);
			setText(null);
			setGraphic(null);
			getStyleClass().remove("boldLabel");
			styleProperty().unbind();
			// Clear the drop shadow and bold - workaround for non-clearing styles
			setStyle("-fx-effect: innershadow(two-pass-box , white , 0, 0.0 , 0 , 0);");
			setTooltip(null);
			if (treeItem != null)
			{
				treeItem.setTreeNodeGraphic(nodeBox);
				treeItem.updateValidityImageThreaded();
				if (!treeItem.isLeaf())
				{
					MenuItem mi = new MenuItem("Expand All");
					mi.setOnAction(new EventHandler<ActionEvent>()
					{
						@Override
						public void handle(ActionEvent arg0)
						{
							Utility.expandAll(treeItem);
						}
					});
					mi.setGraphic(Images.EXPAND_ALL.createImageView());
					cm.getItems().add(mi);
				}
			}

			if (empty || treeItem.getNodeType() == LegoTreeNodeType.blankLegoEndNode || treeItem.getNodeType() == LegoTreeNodeType.blankLegoListEndNode)
			{
				if (treeView.getLego() != null || (!empty && treeItem.getNodeType() == LegoTreeNodeType.blankLegoEndNode))
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
				else if (treeView.getLego() == null || (!empty && treeItem.getNodeType() == LegoTreeNodeType.blankLegoListEndNode))
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
					final LegoReference legoReference = (LegoReference) treeItem.getExtraData();
					final Label l = new Label(TimeConvert.format(legoReference.getStampTime()));

					nodeBox.getChildren().add(l);
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
						// Hack to set the graphic back when a lego is closed. The style is updated when the lego is closed, so we know we
						// can set the graphic back to the not-edited graphic.
						style.addListener(new ChangeListener<String>()
						{
							@Override
							public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
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

					Label status = new Label("Status");
					hbox.getChildren().add(status);

					ChoiceBox<String> cb = new ChoiceBox<>(statusChoices_);
					final Stamp stamp = (Stamp) treeItem.getExtraData();
					String currentStatus = stamp.getStatus();
					cb.getSelectionModel().select(currentStatus);
					hbox.getChildren().add(cb);
					nodeBox.getChildren().add(hbox);

					cb.valueProperty().addListener(new ChangeListener<String>()
					{
						@Override
						public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
						{
							stamp.setStatus(newValue);
							treeView.contentChanged(null);
						}
					});
				}
				else if (treeItem.getNodeType() == LegoTreeNodeType.pncsName)
				{
					MenuItem mi = new MenuItem("Create New Lego for PNCS");
					mi.setOnAction(new EventHandler<ActionEvent>()
					{
						@Override
						public void handle(ActionEvent arg0)
						{
							LegoGUI.getInstance().showCreateLegoDialog((LegoListByReference) treeItem.getLegoParent().getExtraData(), 
									treeItem, false);
						}
					});
					mi.setGraphic(Images.LEGO_ADD.createImageView());
					cm.getItems().add(mi);
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
					nodeBox.getChildren().add(hbox);
					LegoGUI.getInstance().getLegoGUIController().addSnomedDropTarget(treeView.getLego(), assertionLabel);
				}
				else if (treeItem.getNodeType() == LegoTreeNodeType.assertionComponent)
				{
					AssertionComponent ac = (AssertionComponent)treeItem.getExtraData();
					addMenus((AssertionComponent) treeItem.getExtraData(), treeItem, cm);
					if (treeItem.isExpanded())
					{
						final ConceptNode cn = new ConceptNode("Type", ac.getType().getConcept(), ConceptUsageType.TYPE, treeItem, treeView);
						final ContextMenu typeMenu = new ContextMenu();
						addMenus(ac.getType().getConcept(), cn, treeItem, typeMenu);
						cn.setContextMenu(typeMenu);
						nodeBox.getChildren().add(Utility.prependLabel(treeItem.getValue(), cn.getNode(), 5.0));
						HBox.setHgrow(cn.getNode(), Priority.SOMETIMES);
					}
				}
				else if (treeItem.getNodeType() == LegoTreeNodeType.comment)
				{
					final Lego lego = (Lego) treeItem.getExtraData();
					final TextField tf = new TextField();
					tf.setText(lego.getComment() == null ? "" : lego.getComment());
					tf.setMaxWidth(Double.MAX_VALUE);
					tf.textProperty().addListener(new ChangeListener<String>()
					{
						@Override
						public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
						{
							lego.setComment(newValue.length() == 0 ? null : newValue);
							treeView.contentChanged(null);
						}
					});
					nodeBox.getChildren().add(Utility.prependLabel(treeItem.getValue(), tf, 5.0));
				}
				else if (treeItem.getNodeType() == LegoTreeNodeType.assertionUUID)
				{
					String value = treeItem.getValue();
					final AssertionComponent ac = (AssertionComponent) treeItem.getLegoParent().getExtraData();
					TextField tf = new TextField();
					final ImageView invalidImage = Images.EXCLAMATION.createImageView();
					invalidImage.setVisible(false);
					invalidImage.setFitHeight(16.0);
					invalidImage.setFitWidth(16.0);
					final Tooltip tt = new Tooltip("");
					
					treeItem.getLegoParent().isValidThreaded(new BooleanCallback()
					{
						@Override
						public void sendResult(final boolean result)
						{
							Platform.runLater(new Runnable()
							{
								@Override
								public void run()
								{
									invalidImage.setVisible(!result);
									tt.setText(treeItem.getLegoParent().getInvalidReason());
								}
							});
						}
					});
					
					
					tf.setText(value == null ? "" : value);
					tf.setPromptText("UUID of another Assertion");
					tf.textProperty().addListener(new ChangeListener<String>()
					{
						@Override
						public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
						{
							ac.setAssertionUUID(newValue);
							treeView.contentChanged(treeItem.getLegoParent());
							treeItem.getLegoParent().isValidThreaded(new BooleanCallback()
							{
								@Override
								public void sendResult(final boolean result)
								{
									Platform.runLater(new Runnable()
									{
										
										@Override
										public void run()
										{
											invalidImage.setVisible(!result);
											tt.setText(treeItem.getLegoParent().getInvalidReason());
										}
									});
								}
							});
						}
					});

					Tooltip.install(invalidImage, tt);
					
					StackPane sp = new StackPane();
					sp.getChildren().add(tf);
					sp.getChildren().add(invalidImage);
					StackPane.setAlignment(invalidImage, Pos.CENTER_RIGHT);
					StackPane.setMargin(invalidImage, new Insets(0.0, 5.0, 0.0, 0.0));
					
					nodeBox.getChildren().add(Utility.prependLabel("Assertion UUID", sp, 5.0));
					addMenus(LegoTreeNodeType.assertionUUID, ac, cm, treeItem);
				}
				else if (treeItem.getNodeType() == LegoTreeNodeType.measurementEmpty|| treeItem.getNodeType() == LegoTreeNodeType.measurementBound
						|| treeItem.getNodeType() == LegoTreeNodeType.measurementInterval || treeItem.getNodeType() == LegoTreeNodeType.measurementPoint)
				{
					final Measurement m = (Measurement) treeItem.getExtraData();
					Node editor = null;
					
					if (treeItem.getNodeType() == LegoTreeNodeType.measurementPoint)
					{
						PointNode pn = new PointNode(m, PointNode.PointNodeType.point, treeView, treeItem);
						editor = pn.getNode();
					}
					else if (treeItem.getNodeType() == LegoTreeNodeType.measurementBound)
					{
						if (m.getBound() == null)
						{
							m.setBound(new Bound());
						}
						
						PointNode low = new PointNode(m, PointNodeType.boundLow, treeView, treeItem);
						PointNode high = new PointNode(m, PointNodeType.boundHigh, treeView, treeItem);
						low.setPartnerNodes(high);
						high.setPartnerNodes(low);
						editor = makeBoundNode("Bound", m.getBound(), low, high);
					}
					else if (treeItem.getNodeType() == LegoTreeNodeType.measurementInterval)
					{
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
						
						PointNode lowLow = new PointNode(m, PointNodeType.intervalLowBoundLow, treeView, treeItem);
						PointNode lowHigh = new PointNode(m, PointNodeType.intervalLowBoundHigh, treeView, treeItem);
						PointNode highLow = new PointNode(m, PointNodeType.intervalHighBoundLow, treeView, treeItem);
						PointNode highHigh = new PointNode(m, PointNodeType.intervalHighBoundHigh, treeView, treeItem);

						lowLow.setPartnerNodes(lowHigh, highLow, highHigh);
						lowHigh.setPartnerNodes(lowLow, highLow, highHigh);
						highLow.setPartnerNodes(lowLow, lowHigh, highHigh);
						highHigh.setPartnerNodes(lowLow, lowHigh, highLow);
						
						Node boundLow = makeBoundNode("Lower Bound", m.getInterval().getLowerBound(), lowLow, lowHigh);
						Node boundHigh = makeBoundNode("Upper Bound", m.getInterval().getUpperBound(), highLow, highHigh);

						VBox vbox = new VBox();
						vbox.setSpacing(5.0);
						vbox.getChildren().add(boundLow);
						vbox.getChildren().add(boundHigh);
						editor = vbox;
					}
					else
					{
						//no editor
					}
					
					Node units = null;
					
					if (m.getUnits() != null && m.getUnits().getConcept() != null)
					{
						ConceptUsageType cut = ConceptUsageType.UNITS;
						ConceptNode cn = new ConceptNode("Units", m.getUnits().getConcept(), cut, treeItem, treeView);
						final Node localUnits = cn.getNode();
						units = localUnits;
						final ContextMenu unitsMenu = new ContextMenu();
						addMenus(m.getUnits().getConcept(), cn, treeItem, unitsMenu);
						units.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>()
						{
							@Override
							public void handle(ContextMenuEvent event)
							{
								unitsMenu.show(localUnits, event.getScreenX(), event.getScreenY());
								event.consume(); //prevent the node menu - also see hack code inside ConceptNode which suppresses lower down menus
							}
						});
					}
					
					if (editor != null)
					{
						final Node localEditor = editor;
						final ContextMenu editorMenu = new ContextMenu();
						addMenus(m, treeItem, editorMenu);
						editor.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>()
						{
							@Override
							public void handle(ContextMenuEvent event)
							{
								editorMenu.show(localEditor, event.getScreenX(), event.getScreenY());
								event.consume(); //prevent the node menu
							}
						});
					}
					
					DropTargetLabel dtl = new DropTargetLabel(treeItem.getValue(), cm);
					addMenus(m, treeItem, cm, dtl);
					LegoGUI.getInstance().getLegoGUIController().addSnomedDropTarget(treeView.getLego(), dtl);
					
					VBox vBox = new VBox();
					vBox.setSpacing(5.0);
					
					
					HBox firstRow = new HBox();
					firstRow.setFillHeight(true);
					firstRow.setSpacing(5.0);
					firstRow.getChildren().add(dtl);
					dtl.setMaxHeight(Double.MAX_VALUE);
					dtl.setAlignment(Pos.CENTER_LEFT);
					
					if (editor != null)
					{
						if (treeItem.getNodeType() == LegoTreeNodeType.measurementEmpty|| treeItem.getNodeType() == LegoTreeNodeType.measurementPoint)
						{
							firstRow.getChildren().add(editor);
							HBox.setHgrow(editor, Priority.SOMETIMES);
						}
					}
					else
					{
						Label l = new Label("<null measurement>");
						firstRow.getChildren().add(l);
						l.setMaxHeight(Double.MAX_VALUE);
						l.setAlignment(Pos.CENTER_LEFT);
					}
					
					if (units != null)
					{
						firstRow.getChildren().add(units);
						HBox.setHgrow(units, Priority.SOMETIMES);
					}
					
					vBox.getChildren().add(firstRow);
					
					if (editor != null 
							&& treeItem.getNodeType() == LegoTreeNodeType.measurementInterval || treeItem.getNodeType() == LegoTreeNodeType.measurementBound)
					{
						vBox.getChildren().add(editor);
						VBox.setMargin(editor, new Insets(0, 0, 0, 10.0));
					}
					
					nodeBox.getChildren().add(vBox);
				}
				else if (treeItem.getNodeType() == LegoTreeNodeType.expressionValue
						|| treeItem.getNodeType() == LegoTreeNodeType.expressionDestination
						|| treeItem.getNodeType() == LegoTreeNodeType.expressionDiscernible
						|| treeItem.getNodeType() == LegoTreeNodeType.expressionQualifier
						|| treeItem.getNodeType() == LegoTreeNodeType.expressionOptional)
				{
					Expression e = (Expression) treeItem.getExtraData();

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
					DropTargetLabel label = new DropTargetLabel(descriptionAddition + treeItem.getValue(), cm);
					addMenus(e, treeItem, cm, label, descriptionAddition);

					HBox hbox = new HBox();
					hbox.setSpacing(5.0);
					hbox.getChildren().add(label);
					hbox.setFillHeight(true);
					label.setMaxHeight(Double.MAX_VALUE);
					label.setAlignment(Pos.CENTER_LEFT);
					
					//putting the single expression on the same line.
					if (e.getConcept() != null)
					{
						ConceptUsageType cut = treeItem.getConceptUsageType();
						final ConceptNode cn = new ConceptNode("", e.getConcept(), cut, treeItem, treeView);
						final ContextMenu conceptContextMenu = new ContextMenu();
						addMenus(e.getConcept(), cn, treeItem, conceptContextMenu);
						cn.setContextMenu(conceptContextMenu);
						hbox.getChildren().add(cn.getNode());
						HBox.setHgrow(cn.getNode(), Priority.SOMETIMES);
					}

					nodeBox.getChildren().add(hbox);
					HBox.setHgrow(hbox, Priority.SOMETIMES);
					LegoGUI.getInstance().getLegoGUIController().addSnomedDropTarget(treeView.getLego(), label);
				}
				else if (treeItem.getNodeType() == LegoTreeNodeType.concept)
				{
					Concept c = (Concept) treeItem.getExtraData();

					String label = "";
					ConceptUsageType cut = treeItem.getConceptUsageType();
					if (ConceptUsageType.TYPE == cut)
					{
						label = "Type";
					}
					else if (ConceptUsageType.UNITS == cut)
					{
						label = "Units";
					}

					ConceptNode cn = new ConceptNode(label, c, cut, treeItem, treeView);
					addMenus(c, cn, treeItem, cm);
					nodeBox.getChildren().add(cn.getNode());
				}
				else if (treeItem.getNodeType() == LegoTreeNodeType.relation)
				{
					DropTargetLabel relLabel = new DropTargetLabel(treeItem.getValue(), cm);
					relLabel.setAlignment(Pos.CENTER_LEFT);
					relLabel.setMaxHeight(Double.MAX_VALUE);
					Relation r = (Relation) treeItem.getExtraData();
					addMenus(r, treeItem, cm, relLabel);

					HBox hbox = new HBox();
					hbox.getChildren().add(relLabel);
					if (treeItem.isExpanded())
					{
						final ConceptNode cn = new ConceptNode("Type", r.getType().getConcept(), ConceptUsageType.TYPE, treeItem, treeView);
						final ContextMenu typeMenu = new ContextMenu();
						addMenus(r.getType().getConcept(), cn, treeItem, typeMenu);
						cn.setContextMenu(typeMenu);
						hbox.getChildren().add(cn.getNode());
						HBox.setHgrow(cn.getNode(), Priority.SOMETIMES);
						HBox.setMargin(cn.getNode(), new Insets(0,0,0,5.0));
					}
					else
					{
						Label l = new Label(sep + SchemaSummary.summary((Relation) treeItem.getExtraData()));
						l.setMaxHeight(Double.MAX_VALUE);
						l.setAlignment(Pos.CENTER_LEFT);
						hbox.getChildren().add(l);
					}
					nodeBox.getChildren().add(hbox);
					LegoGUI.getInstance().getLegoGUIController().addSnomedDropTarget(treeView.getLego(), relLabel);
				}
				else if (treeItem.getNodeType() == LegoTreeNodeType.relationshipGroup)
				{
					HBox hbox = new HBox();
					hbox.getChildren().add(new Label(treeItem.getValue()));
					if (!treeItem.isExpanded())
					{
						hbox.getChildren().add(new Label(sep + SchemaSummary.summary((RelationGroup) treeItem.getExtraData())));
					}
					nodeBox.getChildren().add(hbox);
					addMenus((RelationGroup) treeItem.getExtraData(), treeItem, cm);
				}
				else if (treeItem.getNodeType() == LegoTreeNodeType.value)
				{
					DropTargetLabel valueLabel = new DropTargetLabel(treeItem.getValue(), cm);
					addMenus((Value) treeItem.getExtraData(), treeItem, cm, valueLabel);

					HBox hbox = new HBox();
					hbox.getChildren().add(valueLabel);
					valueLabel.setMaxHeight(Double.MAX_VALUE);
					valueLabel.setAlignment(Pos.CENTER_LEFT);

					if (!treeItem.isExpanded())
					{
						Label l = new Label(sep + SchemaSummary.summary((Value) treeItem.getExtraData()));
						l.setMaxHeight(Double.MAX_VALUE);
						l.setAlignment(Pos.CENTER_LEFT);
						hbox.getChildren().add(l);
					}
					nodeBox.getChildren().add(hbox);
					LegoGUI.getInstance().getLegoGUIController().addSnomedDropTarget(treeView.getLego(), valueLabel);
				}
				else if (treeItem.getNodeType() == LegoTreeNodeType.text)
				{
					// The object passed in might be a Destination or a value for the text field.
					String text = "";
					String prefixLabel = "";
					final Object parent = treeItem.getExtraData();
					if (parent instanceof Value)
					{
						text = ((Value) parent).getText();
						prefixLabel = "Text";
					}
					else if (parent instanceof Destination)
					{
						text = ((Destination) parent).getText();
						prefixLabel = "Dest";
					}
					else
					{
						logger.error("Unexpected tree constrution");
					}
					
					final ImageView invalidImage = Images.EXCLAMATION.createImageView();
					invalidImage.setVisible(false);
					invalidImage.setFitHeight(16.0);
					invalidImage.setFitWidth(16.0);
					final Tooltip tt = new Tooltip("");
					
					treeItem.isValidThreaded(new BooleanCallback()
					{
						@Override
						public void sendResult(final boolean result)
						{
							Platform.runLater(new Runnable()
							{
								
								@Override
								public void run()
								{
									invalidImage.setVisible(!result);
									tt.setText(treeItem.getInvalidReason());
								}
							});
						}
					});
					
					TextField tf = new TextField();
					tf.setText(text);
					tf.setPromptText("Any Text");
					tf.textProperty().addListener(new ChangeListener<String>()
					{
						@Override
						public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
						{
							if (parent instanceof Value)
							{
								((Value) parent).setText(newValue);
							}
							else if (parent instanceof Destination)
							{
								((Destination) parent).setText(newValue);
							}
							treeView.contentChanged(treeItem);
							treeItem.isValidThreaded(new BooleanCallback()
							{
								@Override
								public void sendResult(final boolean result)
								{
									Platform.runLater(new Runnable()
									{
										
										@Override
										public void run()
										{
											invalidImage.setVisible(!result);
											tt.setText(treeItem.getInvalidReason());
										}
									});
								}
							});
						}
					});
					
					Tooltip.install(invalidImage, tt);
					
					StackPane sp = new StackPane();
					sp.getChildren().add(tf);
					sp.getChildren().add(invalidImage);
					StackPane.setAlignment(invalidImage, Pos.CENTER_RIGHT);
					StackPane.setMargin(invalidImage, new Insets(0.0, 5.0, 0.0, 0.0));
					
					nodeBox.getChildren().add((prefixLabel.length() > 0 ? Utility.prependLabel(prefixLabel, sp, 10.0) : sp));
					addMenus(treeItem.getNodeType(), parent, cm, treeItem);
				}
				else if (treeItem.getNodeType() == LegoTreeNodeType.bool)
				{
					// The object passed in might be a Destination or a value for the text field.
					Boolean bool = null;
					String prefixLabel = "";
					final Object parent = treeItem.getExtraData();
					if (parent instanceof Value)
					{
						bool = ((Value) parent).isBoolean();
					}
					else if (parent instanceof Destination)
					{
						bool = ((Destination) parent).isBoolean();
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
								((Value) parent).setBoolean(new Boolean(newValue));
							}
							else if (parent instanceof Destination)
							{
								((Destination) parent).setBoolean(new Boolean(newValue));
							}
							treeView.contentChanged(null);
						}
					});
					nodeBox.getChildren().add((prefixLabel.length() > 0 ? Utility.prependLabel(prefixLabel, cb, 10.0) : cb));
					addMenus(treeItem.getNodeType(), parent, cm, treeItem);
				}
			}
			// done with massive if/else
			// Deal with javafx memory leak (at least help)
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
			
			if (nodeBox.getChildren().size() == 0 )
			{
				if (item != null)
				{
					nodeBox.getChildren().add(new Label(item.toString()));
				}
			}
			if (nodeBox.getChildren().size() > 0)
			{
				HBox.setHgrow(nodeBox.getChildren().get(nodeBox.getChildren().size() - 1), Priority.SOMETIMES);
			}
			setGraphic(nodeBox.getNode());
		}
		catch (Exception e)
		{
			logger.error("Unexpected", e);
			LegoGUI.getInstance().showErrorDialog("Unexpected Error", "There was an unexpected problem building the tree",
					"Please report this as a bug.  " + e.toString());
		}
	}

	private Node makeBoundNode(String labelText, final Bound b, PointNode low, PointNode high)
	{
		HBox hbox = new HBox();
		hbox.setMaxWidth(Double.MAX_VALUE);
		hbox.setSpacing(1.0);
		if (labelText != null && labelText.length() > 0)
		{
			Label label = new Label(labelText);
			label.setMaxHeight(Double.MAX_VALUE);
			label.setAlignment(Pos.CENTER_LEFT);
			label.setMinWidth(Label.USE_PREF_SIZE);
			hbox.getChildren().add(label);
			HBox.setMargin(label, new Insets(0.0, 4.0, 0.0, 0.0));
		}
		hbox.getChildren().add(low.getNode());
		HBox.setHgrow(low.getNode(), Priority.SOMETIMES);
		final ComboBox<String> cbLow = new ComboBox<>(inclusiveChoices_);
		cbLow.setMaxWidth(60.0);
		cbLow.getSelectionModel().select((b.isLowerPointInclusive() != null && !b.isLowerPointInclusive().booleanValue() ? 1 : 0));
		cbLow.valueProperty().addListener(new ChangeListener<String>()
		{
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
			{
				b.setLowerPointInclusive(cbLow.getSelectionModel().getSelectedIndex() == 0);
				treeView.contentChanged(null);
			}
		});
		hbox.getChildren().add(cbLow);
		Label middle = new Label(" X ");
		middle.getStyleClass().add("boldLabel");
		middle.setMinWidth(Label.USE_PREF_SIZE);
		middle.setMaxHeight(Double.MAX_VALUE);
		middle.setAlignment(Pos.CENTER_LEFT);
		hbox.getChildren().add(middle);
		final ComboBox<String> cbHigh = new ComboBox<>(inclusiveChoices_);
		cbHigh.setMaxWidth(60.0);
		cbHigh.getSelectionModel().select((b.isUpperPointInclusive() != null && !b.isUpperPointInclusive().booleanValue() ? 1 : 0));
		cbHigh.valueProperty().addListener(new ChangeListener<String>()
		{
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
			{
				b.setUpperPointInclusive(cbHigh.getSelectionModel().getSelectedIndex() == 0);
				treeView.contentChanged(null);
			}
		});
		hbox.getChildren().add(cbHigh);
		hbox.getChildren().add(high.getNode());
		HBox.setHgrow(high.getNode(), Priority.SOMETIMES);

		return hbox;
	}

	private void removeMeasurement(Measurement m, LegoTreeItem ti)
	{
		Object parent = ti.getLegoParent().getExtraData();
		LegoTreeItem treeParent = ti.getLegoParent();
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
		Event.fireEvent(treeParent, new TreeItem.TreeModificationEvent<String>(TreeItem.valueChangedEvent(), treeParent));
		treeParent.getChildren().remove(ti);
		treeView.contentChanged(treeParent);
	}

	private void addUnits(Measurement m, String sctUUID, LegoTreeItem ti)
	{
		Units u = new Units();
		Concept c = new Concept();
		if (sctUUID != null)
		{
			c.setUuid(sctUUID);
		}
		u.setConcept(c);
		m.setUnits(u);
		//Units are displayed on the measurement constant, so no node to add.
		treeView.contentChanged(ti);
		Event.fireEvent(ti, new TreeItem.TreeModificationEvent<String>(TreeItem.valueChangedEvent(), ti));
	}

	private void addInterval(Measurement m, LegoTreeItem ti)
	{
		Interval i = new Interval();
		Bound low = new Bound();
		Bound high = new Bound();
		i.setLowerBound(low);
		i.setUpperBound(high);
		m.setInterval(i);
		ti.setNodeType(LegoTreeNodeType.measurementInterval);
		//interval is displayed on the measurement concept, so no node to add.
		treeView.contentChanged(ti);
		Event.fireEvent(ti, new TreeItem.TreeModificationEvent<String>(TreeItem.valueChangedEvent(), ti));
	}

	private void removeInterval(Measurement m, LegoTreeItem ti)
	{
		m.setInterval(null);
		ti.setNodeType(LegoTreeNodeType.measurementEmpty);
		Event.fireEvent(ti, new TreeItem.TreeModificationEvent<String>(TreeItem.valueChangedEvent(), ti));
		//interval doesn't have its own node, so nothing to remove.
		treeView.contentChanged(ti);
	}

	private void addPoint(Measurement m, LegoTreeItem ti)
	{
		m.setPoint(new PointMeasurementConstant());
		ti.setNodeType(LegoTreeNodeType.measurementPoint);
		treeView.contentChanged(ti);
		//point doesn't have its own node, so nothing to add
		Event.fireEvent(ti, new TreeItem.TreeModificationEvent<String>(TreeItem.valueChangedEvent(), ti));
	}

	private void removePoint(Measurement m, LegoTreeItem ti)
	{
		m.setPoint(null);
		ti.setNodeType(LegoTreeNodeType.measurementEmpty);
		Event.fireEvent(ti, new TreeItem.TreeModificationEvent<String>(TreeItem.valueChangedEvent(), ti));
		//No point node to remove
		treeView.contentChanged(ti);
	}

	private void addBound(Measurement m, LegoTreeItem ti)
	{
		Bound b = new Bound();
		m.setBound(b);
		ti.setNodeType(LegoTreeNodeType.measurementBound);
		treeView.contentChanged(ti);
		//no bound node to add
		Event.fireEvent(ti, new TreeItem.TreeModificationEvent<String>(TreeItem.valueChangedEvent(), ti));
	}

	private void removeBound(Measurement m, LegoTreeItem ti)
	{
		m.setBound(null);
		ti.setNodeType(LegoTreeNodeType.measurementEmpty);
		Event.fireEvent(ti, new TreeItem.TreeModificationEvent<String>(TreeItem.valueChangedEvent(), ti));
		//no bound node to remove
		treeView.contentChanged(ti);
	}

	private void addMeasurement(Object parent, LegoTreeNodeType pointBoundOrInterval, String unitsSCTID, LegoTreeItem ti)
	{
		Measurement m = new Measurement();
		String label = "Measurement";

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
			label = "Timing";
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
			if (LegoTreeNodeType.measurementPoint == pointBoundOrInterval)
			{
				m.setPoint(new PointMeasurementConstant()); // purposefully don't set a value inside this
			}
			else if (LegoTreeNodeType.measurementBound == pointBoundOrInterval)
			{
				m.setBound(new Bound());
			}
			else if (LegoTreeNodeType.measurementInterval == pointBoundOrInterval)
			{
				m.setInterval(new Interval());
			}
		}

		LegoTreeItem lti = new LegoTreeItem(m, label);
		ti.getChildren().add(lti);

		FXCollections.sort(ti.getChildren(), new LegoTreeItemComparator(true));
		Utility.expandAll(ti);
		treeView.contentChanged(lti);
		Event.fireEvent(ti, new TreeItem.TreeModificationEvent<String>(TreeItem.valueChangedEvent(), ti));
	}

	private void addConcept(Object object, String sctUUID, LegoTreeItem ti)
	{
		Concept c = new Concept();
		c.setUuid(sctUUID);

		Expression expression;
		boolean addExpression = false;

		LegoTreeNodeType type = ti.getNodeType();
		LegoTreeItem lti = null;
		
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
			if (addExpression)
			{
				lti = new LegoTreeItem(expression, type);
				ti.getChildren().add(lti);
			}
			//No new tree item for just a single concept - it is rendered on the expression node.
			//The tree modification event fired below should take care of it.
		}
		else // We know it has a concept
		{
			// Becomes optional when on a conjunction. Value is already optional, so don't need to switch that one.
			if (type == LegoTreeNodeType.expressionDiscernible || type == LegoTreeNodeType.expressionQualifier)
			{
				type = LegoTreeNodeType.expressionOptional;
			}

			// Not a conjunction yet - convert to one
			if (expression.getConcept() != null)
			{
				// need to convert to a conjunction
				Concept currentConcept = expression.getConcept();

				//A single concept has no tree item, it is rendered on the expression node.

				Expression e = new Expression();
				e.setConcept(currentConcept);
				expression.setConcept(null);
				expression.getExpression().add(e);

				LegoTreeItem conjunction = new LegoTreeItem(e, type);
				ti.getChildren().add(conjunction);
				Utility.expandAll(conjunction);
			}

			// Add to the conjunction
			Expression newExpression = new Expression();
			newExpression.setConcept(c);
			expression.getExpression().add(newExpression);
			lti = new LegoTreeItem(newExpression, type);
			ti.getChildren().add(lti);
			Utility.expandAll(lti);
		}
		FXCollections.sort(ti.getChildren(), new LegoTreeItemComparator(true));
		ti.setExpanded(true);
		treeView.contentChanged(lti == null ? ti : lti);
		Event.fireEvent(ti, new TreeItem.TreeModificationEvent<String>(TreeItem.valueChangedEvent(), ti));
	}

	private void removeConcept(LegoTreeItem ti)
	{
		if (ti.getExtraData() instanceof Measurement)
		{
			((Measurement)ti.getExtraData()).setUnits(null);
			Event.fireEvent(ti, new TreeItem.TreeModificationEvent<String>(TreeItem.valueChangedEvent(), ti));
			treeView.contentChanged(ti);
		}
		else
		{
			logger.error("Unhandled concept remove request: " + ti.getExtraData());
			return;
		}
	}

	private void pasteExpression(Expression oldExpression, LegoTreeItem oldTreeItem)
	{
		Expression expression;
		if (CustomClipboard.containsType(Expression.class))
		{
			expression = CustomClipboard.getExpression();
		}
		else if (CustomClipboard.containsType(Discernible.class))
		{
			expression = CustomClipboard.getDiscernible().getExpression();
		}
		else if (CustomClipboard.containsType(Qualifier.class))
		{
			expression = CustomClipboard.getQualifier().getExpression();
		}
		else
		{
			CustomClipboard.updateBindings();
			LegoGUI.getInstance().showErrorDialog("No Pasteable Expression on Clipboard", "The Clipboard does not contain an Expression", null);
			return;
		}

		boolean expand = oldTreeItem.isExpanded();
		LegoTreeItem parentLegoTreeItem = oldTreeItem.getLegoParent();

		LegoTreeItem newLTI = null;
		if (oldTreeItem.getNodeType() == LegoTreeNodeType.expressionDiscernible)
		{
			Assertion a = (Assertion) parentLegoTreeItem.getExtraData();
			a.getDiscernible().setExpression(expression);
			newLTI = new LegoTreeItem(expression, LegoTreeNodeType.expressionDiscernible);
		}
		else if (oldTreeItem.getNodeType() == LegoTreeNodeType.expressionQualifier)
		{
			Assertion a = (Assertion) parentLegoTreeItem.getExtraData();
			a.getQualifier().setExpression(expression);
			newLTI = new LegoTreeItem(expression, LegoTreeNodeType.expressionQualifier);
		}
		else if (oldTreeItem.getNodeType() == LegoTreeNodeType.expressionValue)
		{
			Value v = (Value) parentLegoTreeItem.getExtraData();
			v.setExpression(expression);
			newLTI = new LegoTreeItem(expression, LegoTreeNodeType.expressionValue);
		}
		else if (oldTreeItem.getNodeType() == LegoTreeNodeType.expressionDestination)
		{
			Relation r = (Relation) parentLegoTreeItem.getExtraData();
			r.getDestination().setExpression(expression);
			newLTI = new LegoTreeItem(expression, LegoTreeNodeType.expressionDestination);
		}
		else if (oldTreeItem.getNodeType() == LegoTreeNodeType.expressionOptional)
		{
			Expression parentExpression = (Expression) parentLegoTreeItem.getExtraData();
			parentExpression.getExpression().remove(oldExpression);
			parentExpression.getExpression().add(expression);
			newLTI = new LegoTreeItem(expression, LegoTreeNodeType.expressionOptional);
		}
		else
		{
			// I think I have all the cases handled....
			logger.error("Unhandled paste type in expression " + parentLegoTreeItem.getExtraData().getClass().getName());
			LegoGUI.getInstance().showErrorDialog("Unhandled paste operation", "Unhandled paste operation", "Please file a bug with your log file attached");
			return;
		}

		// Need to delete the old value tree node
		parentLegoTreeItem.getChildren().remove(oldTreeItem);

		newLTI.setExpanded(expand);
		parentLegoTreeItem.getChildren().add(newLTI);
		FXCollections.sort(parentLegoTreeItem.getChildren(), new LegoTreeItemComparator(true));
		treeView.contentChanged(newLTI);
		Event.fireEvent(parentLegoTreeItem, new TreeItem.TreeModificationEvent<String>(TreeItem.valueChangedEvent(), parentLegoTreeItem));
	}

	private void removeExpression(Expression e, LegoTreeItem ti)
	{
		Object parent = ti.getLegoParent().getExtraData();
		LegoTreeItem treeParent = ti.getLegoParent();
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
				//Don't need to build a node for this - it will be displayed on the expression node
				
				//Move up the relations from the sibling expression
				parentExpression.getRelation().addAll(conjunctionExpression.getRelation());
				for (Relation r : conjunctionExpression.getRelation())
				{
					treeParent.getChildren().add(new LegoTreeItem(r));
				}
				parentExpression.getRelationGroup().addAll(conjunctionExpression.getRelationGroup());
				for (RelationGroup rg : conjunctionExpression.getRelationGroup())
				{
					treeParent.getChildren().add(new LegoTreeItem(rg));
				}

				for (TreeItem<String> sibling : treeParent.getChildren())
				{
					if (((LegoTreeItem) sibling).getExtraData() == conjunctionExpression)
					{
						treeParent.getChildren().remove(sibling);
						break;
					}
				}
				FXCollections.sort(treeParent.getChildren(), new LegoTreeItemComparator(true));
			}
		}
		Event.fireEvent(treeParent, new TreeItem.TreeModificationEvent<String>(TreeItem.valueChangedEvent(), treeParent));
		
		treeParent.getChildren().remove(ti);
		treeView.contentChanged(treeParent);
	}

	private void addRelation(Expression e, String sctUUID, LegoTreeItem ti)
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
		treeView.contentChanged(lti);
		Event.fireEvent(ti, new TreeItem.TreeModificationEvent<String>(TreeItem.valueChangedEvent(), ti));
	}

	private void addRelation(RelationGroup rg, LegoTreeItem ti)
	{
		Relation r = new Relation();
		rg.getRelation().add(r);
		LegoTreeItem lti = new LegoTreeItem(r);
		ti.getChildren().add(lti);
		Utility.expandAll(lti);
		ti.setExpanded(true);
		treeView.contentChanged(lti);
		Event.fireEvent(ti, new TreeItem.TreeModificationEvent<String>(TreeItem.valueChangedEvent(), ti));
	}

	private void removeRelation(Relation rel, LegoTreeItem ti)
	{
		LegoTreeItem treeParent = ti.getLegoParent();
		Object parent = ti.getLegoParent().getExtraData();
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
		treeParent.getChildren().remove(ti);
		treeView.contentChanged(treeParent);
	}

	private void removeRelationGroup(LegoTreeItem ti)
	{
		LegoTreeItem treeParent = ti.getLegoParent();
		Expression e = (Expression) ti.getLegoParent().getExtraData();
		e.getRelationGroup().clear();

		// No need to fire a parent event here - the options on Expression don't change with add/remove of a rel.
		treeParent.getChildren().remove(ti);
		treeView.contentChanged(treeParent);
	}

	private void addRelationshipGroup(Expression e, LegoTreeItem ti)
	{
		RelationGroup rg = new RelationGroup();
		e.getRelationGroup().add(rg);
		LegoTreeItem lti = new LegoTreeItem(rg);
		ti.getChildren().add(lti);
		Utility.expandAll(ti);
		treeView.contentChanged(lti);
		Event.fireEvent(ti, new TreeItem.TreeModificationEvent<String>(TreeItem.valueChangedEvent(), ti));
	}

	private void pasteValue(Assertion parentAssertion, LegoTreeItem parentTreeItem)
	{
		if (CustomClipboard.containsType(Value.class))
		{
			Value v = CustomClipboard.getValue();

			boolean expand = true;
			if (parentAssertion.getValue() != null)
			{
				// Need to delete the old value tree node
				for (TreeItem<String> lti : parentTreeItem.getChildren())
				{
					if (((LegoTreeItem) lti).getExtraData() instanceof Value)
					{
						expand = lti.isExpanded();
						parentTreeItem.getChildren().remove(lti);
						break;
					}
				}
			}
			parentAssertion.setValue(v);
			LegoTreeItem lti = new LegoTreeItem(v);
			parentTreeItem.getChildren().add(lti);
			FXCollections.sort(parentTreeItem.getChildren(), new LegoTreeItemComparator(true));
			lti.setExpanded(expand);
			treeView.contentChanged(lti);
			Event.fireEvent(parentTreeItem, new TreeItem.TreeModificationEvent<String>(TreeItem.valueChangedEvent(), parentTreeItem));
		}
		else
		{
			CustomClipboard.updateBindings();
			LegoGUI.getInstance().showErrorDialog("No Value on Clipboard", "The Clipboard does not contain a Value", null);
		}
	}

	private void addValue(Assertion a, String sctUUID, LegoTreeItem ti)
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
		treeView.contentChanged(lti);
		Event.fireEvent(ti, new TreeItem.TreeModificationEvent<String>(TreeItem.valueChangedEvent(), ti));
	}

	private void addQualifier(Assertion a, String sctUUID, LegoTreeItem ti)
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
		treeView.contentChanged(lti);
		Event.fireEvent(ti, new TreeItem.TreeModificationEvent<String>(TreeItem.valueChangedEvent(), ti));
	}

	private void addDiscernibile(Assertion a, String sctUUID, LegoTreeItem ti)
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
		treeView.contentChanged(lti);
		Event.fireEvent(ti, new TreeItem.TreeModificationEvent<String>(TreeItem.valueChangedEvent(), ti));
	}

	private void addAssertionComponent(Assertion a, LegoTreeItem ti)
	{
		AssertionComponent ac = new AssertionComponent();
		a.getAssertionComponent().add(ac);
		LegoTreeItem lti = new LegoTreeItem(ac);
		ti.getChildren().add(lti);
		FXCollections.sort(ti.getChildren(), new LegoTreeItemComparator(true));
		Utility.expandAll(lti);
		treeView.contentChanged(lti);
		Event.fireEvent(ti, new TreeItem.TreeModificationEvent<String>(TreeItem.valueChangedEvent(), ti));
	}

	private void removeAssertionComponent(AssertionComponent ac, LegoTreeItem ti)
	{
		LegoTreeItem treeParent = ti.getLegoParent();
		Assertion a = (Assertion) ti.getLegoParent().getExtraData();
		a.getAssertionComponent().remove(ac);
		// No need to fire a parent event here - the options on AssertionComponents don't change with add/remove of a component.
		treeParent.getChildren().remove(ti);
		treeView.contentChanged(treeParent);
	}

	private void removeAssertion(Assertion a, LegoTreeItem ti)
	{
		LegoTreeItem treeParent = ti.getLegoParent();
		LegoTreeView ltv = (LegoTreeView) getTreeView();
		for (Assertion assertion : ltv.getLego().getAssertion())
		{
			if (assertion.getAssertionUUID().equals(a.getAssertionUUID()))
			{
				ltv.getLego().getAssertion().remove(assertion);
				break;
			}
		}
		treeParent.getChildren().remove(ti);
		treeView.contentChanged(treeParent);
		// No need to fire event here, there is no parent to notify
	}

	private void addText(Object parent, LegoTreeItem ti)
	{
		LegoTreeItem lti = null;
		if (parent instanceof Value)
		{
			((Value) parent).setText("");
			lti = new LegoTreeItem((Value) parent, LegoTreeNodeType.text);
		}
		else if (parent instanceof Destination)
		{
			((Destination) parent).setText("");
			lti = new LegoTreeItem((Destination) parent, LegoTreeNodeType.text);
		}
		else
		{
			logger.error("Unexpected addText call");
			return;
		}
		ti.getChildren().add(lti);
		treeView.contentChanged(lti);
		Utility.expandAll(ti);
		Event.fireEvent(ti, new TreeItem.TreeModificationEvent<String>(TreeItem.valueChangedEvent(), ti));
	}

	private void removeText(Object parent, LegoTreeItem ti)
	{
		LegoTreeItem treeParent = ti.getLegoParent();
		if (parent instanceof Value)
		{
			((Value) parent).setText(null);
		}
		else if (parent instanceof Destination)
		{
			((Destination) parent).setText(null);
		}
		else
		{
			logger.error("Unexpected removeText call");
		}
		Event.fireEvent(ti.getParent(), new TreeItem.TreeModificationEvent<String>(TreeItem.valueChangedEvent(), ti.getParent()));
		treeParent.getChildren().remove(ti);
		treeView.contentChanged(treeParent);
	}

	private void addBoolean(Object parent, LegoTreeItem ti)
	{
		LegoTreeItem lti = null;
		if (parent instanceof Value)
		{
			((Value) parent).setBoolean(new Boolean(true));
			lti = new LegoTreeItem((Value) parent, LegoTreeNodeType.bool);
		}
		else if (parent instanceof Destination)
		{
			((Destination) parent).setBoolean(new Boolean(true));
			lti = new LegoTreeItem((Destination) parent, LegoTreeNodeType.bool);
		}
		else
		{
			logger.error("Unexpected addBoolean call");
			return;
		}
		ti.getChildren().add(lti);
		treeView.contentChanged(lti);
		FXCollections.sort(ti.getChildren(), new LegoTreeItemComparator(true));
		Utility.expandAll(ti);
		Event.fireEvent(ti, new TreeItem.TreeModificationEvent<String>(TreeItem.valueChangedEvent(), ti));
	}

	private void removeBoolean(Object parent, LegoTreeItem ti)
	{
		LegoTreeItem treeParent = ti.getLegoParent();
		if (parent instanceof Value)
		{
			((Value) parent).setBoolean(null);
		}
		else if (parent instanceof Destination)
		{
			((Destination) parent).setBoolean(null);
		}
		else
		{
			logger.error("Unexpected removeBoolean call");
			return;
		}
		Event.fireEvent(ti.getParent(), new TreeItem.TreeModificationEvent<String>(TreeItem.valueChangedEvent(), ti.getParent()));
		treeParent.getChildren().remove(ti);
		treeView.contentChanged(treeParent);
	}

	private void pasteAssertion()
	{
		if (CustomClipboard.containsType(Assertion.class))
		{
			Assertion a = CustomClipboard.getAssertion();

			LegoTreeView ltv = (LegoTreeView) getTreeView();
			// Change the Assertion UUID
			a.setAssertionUUID(UUID.randomUUID().toString());
			ltv.getLego().getAssertion().add(a);

			LegoTreeItem lti = new LegoTreeItem(a);
			ltv.getRoot().getChildren().add(lti);
			FXCollections.sort(ltv.getRoot().getChildren(), new LegoTreeItemComparator(true));
			treeView.contentChanged(lti);
			lti.setExpanded(true);
		}
		else
		{
			CustomClipboard.updateBindings();
			LegoGUI.getInstance().showErrorDialog("No Assertion on Clipboard", "The Clipboard does not contain an Assertion", null);
		}
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
		treeView.contentChanged(lti);
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
			LegoListByReference llbr = (LegoListByReference) ti.getLegoParent().getLegoParent().getExtraData();

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
					// set everything
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
					// just set the author, time and uuid
					s.setAuthor(up.getAuthor());
					s.setTime(TimeConvert.convert(System.currentTimeMillis()));
					s.setUuid(UUID.randomUUID().toString());
				}
				l.setStamp(s);

				// Change all the assertion UUIDs
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
		
		
		//Not supported yet on the Mac platform by OSX
		//https://csfe.aceworkspace.net/sf/go/artf227798
		if (!isMac)
		{
			mi = new MenuItem("View in Web Browser");
			mi.setOnAction(new EventHandler<ActionEvent>()
			{
				@Override
				public void handle(ActionEvent arg0)
				{
					LegoGUIModel.getInstance().viewLegoListInBrowser(llbr.getLegoListUUID());
				}
			});
		}
		mi.setGraphic(Images.HTML_VIEW_16.createImageView());
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

		mi = new MenuItem("Export...");
		mi.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent arg0)
			{
				LegoGUI.getInstance().showExportDialog(Arrays.asList(new LegoList[] {BDBDataStoreImpl.getInstance().getLegoListByID(llbr.getLegoListUUID())}));
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
				Answer a = LegoGUI.getInstance().showYesNoDialog("Really delete Lego List?",
						"Are you sure that you want to delete the Lego List?  " + "This will delete all contained Legos.");
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
		
		//Not supported yet on the Mac platform by OSX
		//https://csfe.aceworkspace.net/sf/go/artf227798
		if (!isMac && !legoReference.isNew())
		{
			mi = new MenuItem("View in Web Browser");
			mi.setOnAction(new EventHandler<ActionEvent>()
			{
				@Override
				public void handle(ActionEvent arg0)
				{
					LegoGUIModel.getInstance().viewLegoInBrowser(legoReference.getLegoUUID(), legoReference.getStampUUID());
				}
			});
			mi.setGraphic(Images.HTML_VIEW_16.createImageView());
			cm.getItems().add(mi);
		}
		
		mi = new MenuItem("Delete Lego");
		mi.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent arg0)
			{
				Answer a = LegoGUI.getInstance().showYesNoDialog("Really delete Lego?", "Are you sure that you want to delete the Lego?");
				if (a == Answer.YES)
				{
					// From legoReference treeItem, go up past pncsName and pncs value to get the LegoListReference
					try
					{
						LegoGUIModel.getInstance().removeLego((LegoListByReference) lti.getLegoParent().getLegoParent().getLegoParent().getExtraData(),
								legoReference);
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

	private void addMenus(LegoTreeNodeType type, final Object parent, ContextMenu cm, final LegoTreeItem treeItem)
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
								if (result.get(i).getStamp().getTime().toGregorianCalendar().getTimeInMillis() > newest.getStamp().getTime().toGregorianCalendar()
										.getTimeInMillis())
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

		if (a.getValue() != null && a.getValue().getExpression() == null && a.getValue().getMeasurement() == null && a.getValue().getText() == null
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
					addMeasurement(a, LegoTreeNodeType.measurementPoint, null, treeItem);
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
					addMeasurement(a, LegoTreeNodeType.measurementBound, null, treeItem);
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
					addMeasurement(a, LegoTreeNodeType.measurementInterval, null, treeItem);
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
					addMeasurement(a, null, label.getDroppedValue(), treeItem);
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

	/**
	 * This is for the node menu, while the other addMenus(Measurement...) is for the point value boxes.
	 */
	private void addMenus(final Measurement m, final LegoTreeItem treeItem, ContextMenu cm, final DropTargetLabel label)
	{
		MenuItem mi;
		if (treeItem.getNodeType() == LegoTreeNodeType.measurementEmpty)
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

		if (m.getUnits() == null || m.getUnits().getConcept() == null)
		{
			mi = new MenuItem("Add Units");
			mi.setOnAction(new EventHandler<ActionEvent>()
			{
				@Override
				public void handle(ActionEvent arg0)
				{
					addUnits(m, null, treeItem);
				}
			});
			mi.setGraphic(Images.ADD.createImageView());
			cm.getItems().add(mi);
			
			//Timing or Measurement is what value is...
			mi = new MenuItem("Drop as a " + treeItem.getValue() + " Unit");
			mi.setOnAction(new EventHandler<ActionEvent>()
			{
				@Override
				public void handle(ActionEvent arg0)
				{
					addUnits(m, label.getDroppedValue(), treeItem);
					label.setDroppedValue(null);
				}
			});
			mi.setGraphic(Images.ADD.createImageView());
			label.getDropContextMenu().getItems().add(mi);
		}
		
		//Says Timing or Value
		mi = new MenuItem("Remove " + treeItem.getValue());
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
	
	/**
	 * This is for the value boxes, while the other addMenus(Measurement...) is for the node menu.
	 */
	private void addMenus(final Measurement m, final LegoTreeItem treeItem, ContextMenu cm)
	{
		MenuItem mi;
		if (treeItem.getNodeType() == LegoTreeNodeType.measurementPoint)
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
		else if (treeItem.getNodeType() == LegoTreeNodeType.measurementBound)
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
		else if (treeItem.getNodeType() == LegoTreeNodeType.measurementInterval)
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

		if (treeItem.getNodeType() == LegoTreeNodeType.expressionValue || treeItem.getNodeType() == LegoTreeNodeType.expressionDestination
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
					CustomClipboard.set(((Assertion) treeItem.getLegoParent().getExtraData()).getDiscernible());
				}
				else if (treeItem.getNodeType() == LegoTreeNodeType.expressionQualifier)
				{
					CustomClipboard.set(((Assertion) treeItem.getLegoParent().getExtraData()).getQualifier());
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
					LegoGUI.getInstance().showCreateTemplateDialog(((Assertion) treeItem.getLegoParent().getExtraData()).getDiscernible());
				}
				else if (treeItem.getNodeType() == LegoTreeNodeType.expressionQualifier)
				{
					LegoGUI.getInstance().showCreateTemplateDialog(((Assertion) treeItem.getLegoParent().getExtraData()).getQualifier());
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
					LegoGUI.getInstance().showErrorDialog("Unknown Concept", "Can't lookup an invalid concept", null);
				}
			}
		});
		mi.setGraphic(Images.CONCEPT_VIEW.createImageView());
		cm.getItems().add(mi);
		
		mi = new MenuItem("Find Concept in Tree or Pending");
		mi.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent arg0)
			{
				if (cn.isValid())
				{
					LegoGUI.getInstance().getLegoGUIController().findConceptInTree(UUID.fromString(c.getUuid()));
				}
				else
				{
					LegoGUI.getInstance().showErrorDialog("Unknown Concept", "Can't lookup an invalid concept", "");
				}
			}
		});
		mi.setGraphic(Images.ROOT.createImageView());
		cm.getItems().add(mi);
		
		mi = new MenuItem("Copy Text");
		mi.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent arg0)
			{
				CustomClipboard.set(cn.getDisplayedText());
			}
		});
		mi.setGraphic(Images.COPY.createImageView());
		cm.getItems().add(mi);
		
		mi = new MenuItem("Copy UUID");
		mi.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent arg0)
			{
				CustomClipboard.set(c.getUuid());
			}
		});
		mi.setGraphic(Images.COPY.createImageView());
		cm.getItems().add(mi);
		
		mi = new MenuItem("Copy SCTID");
		mi.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent arg0)
			{
				CustomClipboard.set(c.getSctid() + "");
			}
		});
		mi.setGraphic(Images.COPY.createImageView());
		cm.getItems().add(mi);
		
		mi = new MenuItem("Paste");
		mi.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent arg0)
			{
				cn.set(CustomClipboard.getString());
			}
		});
		mi.setGraphic(Images.PASTE.createImageView());
		cm.getItems().add(mi);
		
		if (treeItem.getExtraData() instanceof Measurement)
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
			mi.setGraphic(Images.DELETE.createImageView());
			cm.getItems().add(mi);
		}
	}

	private void addMenus(final Relation r, final LegoTreeItem treeItem, ContextMenu cm, final DropTargetLabel label)
	{
		MenuItem mi;

		if (r.getDestination().getExpression() == null && r.getDestination().getMeasurement() == null && r.getDestination().getText() == null
				&& r.getDestination().isBoolean() == null)
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
					addMeasurement(r, LegoTreeNodeType.measurementPoint, null, treeItem);
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
					addMeasurement(r, LegoTreeNodeType.measurementBound, null, treeItem);
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
					addMeasurement(r, LegoTreeNodeType.measurementInterval, null, treeItem);
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
		if (v.getExpression() == null && v.getMeasurement() == null && v.getText() == null && v.isBoolean() == null)
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
					addMeasurement(v, LegoTreeNodeType.measurementPoint, null, treeItem);
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
					addMeasurement(v, LegoTreeNodeType.measurementBound, null, treeItem);
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
					addMeasurement(v, LegoTreeNodeType.measurementInterval, null, treeItem);
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
				pasteValue((Assertion) (treeItem.getLegoParent()).getExtraData(), treeItem.getLegoParent());
			}
		});
		mi.visibleProperty().bind(CustomClipboard.containsValue);
		mi.setGraphic(Images.PASTE.createImageView());
		cm.getItems().add(mi);

	}

	@Override
	protected void finalize() throws Throwable
	{
		// Help deal with javafx memory leaks
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
