package gov.va.legoEdit.gui.dialogs;

import gov.va.legoEdit.LegoGUI;
import gov.va.legoEdit.gui.sctTreeView.SimTreeView;
import gov.va.legoEdit.gui.util.CopyableLabel;
import gov.va.legoEdit.gui.util.CustomClipboard;
import gov.va.legoEdit.storage.wb.WBDataStore;
import gov.va.legoEdit.storage.wb.WBUtility;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.UUID;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Callback;
import org.ihtsdo.otf.tcc.api.coordinate.StandardViewCoordinates;
import org.ihtsdo.otf.tcc.api.metadata.binding.Taxonomies;
import org.ihtsdo.otf.tcc.ddo.concept.ConceptChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.attribute.ConceptAttributesChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.attribute.ConceptAttributesVersionDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.description.DescriptionChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.description.DescriptionVersionDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.identifier.IdentifierDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.relationship.RelationshipChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.relationship.RelationshipVersionDdo;
import org.ihtsdo.otf.tcc.ddo.fetchpolicy.RefexPolicy;
import org.ihtsdo.otf.tcc.ddo.fetchpolicy.RelationshipPolicy;
import org.ihtsdo.otf.tcc.ddo.fetchpolicy.VersionPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * SnomedConceptViewController
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 * Copyright 2013
 */
public class SnomedConceptViewController implements Initializable
{
	Logger logger = LoggerFactory.getLogger(SnomedConceptViewController.class);

	@FXML// fx:id="anchorPane"
	private AnchorPane anchorPane; // Value injected by FXMLLoader
	@FXML// fx:id="conceptDefined"
	private Label conceptDefined; // Value injected by FXMLLoader
	@FXML// fx:id="conceptStatus"
	private Label conceptStatus; // Value injected by FXMLLoader
	@FXML// fx:id="descriptions"
	private TableView<StringWithRefList> descriptions; // Value injected by FXMLLoader
	@FXML// fx:id="fsnLabel"
	private Label fsnLabel; // Value injected by FXMLLoader
	@FXML// fx:id="idVBox"
	private VBox idVBox; // Value injected by FXMLLoader
	@FXML// fx:id="sourceRelationships"
	private TableView<StringWithRefList> sourceRelationships; // Value injected by FXMLLoader
	@FXML// fx:id="splitPane"
	private SplitPane splitPane; // Value injected by FXMLLoader
	@FXML// fx:id="splitRight"
	private VBox splitRight; // Value injected by FXMLLoader
	@FXML// fx:id="uuid"
	private Label uuid; // Value injected by FXMLLoader
	@FXML// fx:id="showInTree"
	private Button showInTree; // Value injected by FXMLLoader
	@FXML// fx:id="treeViewProgress"
	private ProgressIndicator treeViewProgress; // Value injected by FXMLLoade
	
	BooleanProperty treeViewSearchRunning = new SimpleBooleanProperty(false);

	SimTreeView sctTree;

	@Override
	// This method is called by the FXMLLoader when initialization is complete
	public void initialize(URL fxmlFileLocation, ResourceBundle resources)
	{
		assert anchorPane != null : "fx:id=\"anchorPane\" was not injected: check your FXML file 'SnomedConceptView.fxml'.";
		assert conceptDefined != null : "fx:id=\"conceptDefined\" was not injected: check your FXML file 'SnomedConceptView.fxml'.";
		assert conceptStatus != null : "fx:id=\"conceptStatus\" was not injected: check your FXML file 'SnomedConceptView.fxml'.";
		assert descriptions != null : "fx:id=\"descriptions\" was not injected: check your FXML file 'SnomedConceptView.fxml'.";
		assert fsnLabel != null : "fx:id=\"fsnLabel\" was not injected: check your FXML file 'SnomedConceptView.fxml'.";
		assert idVBox != null : "fx:id=\"idVBox\" was not injected: check your FXML file 'SnomedConceptView.fxml'.";
		assert sourceRelationships != null : "fx:id=\"sourceRelationships\" was not injected: check your FXML file 'SnomedConceptView.fxml'.";
		assert splitPane != null : "fx:id=\"splitPane\" was not injected: check your FXML file 'SnomedConceptView.fxml'.";
		assert splitRight != null : "fx:id=\"splitRight\" was not injected: check your FXML file 'SnomedConceptView.fxml'.";
		assert uuid != null : "fx:id=\"uuid\" was not injected: check your FXML file 'SnomedConceptView.fxml'.";

	}

	public void init(ConceptChronicleDdo concept)
	{
		ConceptAttributesChronicleDdo ca = concept.getConceptAttributes();
		final ConceptAttributesVersionDdo cav = ca.getVersions().get(ca.getVersions().size() - 1);
		conceptDefined.setText(cav.isDefined() + "");
		conceptStatus.setText(cav.getStatusString());
		fsnLabel.setText(WBUtility.getDescription(concept));
		CopyableLabel.addCopyMenu(fsnLabel);
		uuid.setText(concept.getPrimordialUuid().toString());
		CopyableLabel.addCopyMenu(uuid);
		LegoGUI.getInstance().getLegoGUIController().updateRecentCodes(concept.getPrimordialUuid().toString());

		uuid.setOnDragDetected(new EventHandler<MouseEvent>()
		{
			public void handle(MouseEvent event)
			{
				/* drag was detected, start a drag-and-drop gesture */
				/* allow any transfer mode */
				Dragboard db = uuid.startDragAndDrop(TransferMode.COPY);

				/* Put a string on a dragboard */
				ClipboardContent content = new ClipboardContent();
				content.putString(uuid.getText());
				db.setContent(content);
				LegoGUI.getInstance().getLegoGUIController().snomedDragStarted();
				event.consume();
			}
		});

		uuid.setOnDragDone(new EventHandler<DragEvent>()
		{
			public void handle(DragEvent event)
			{
				LegoGUI.getInstance().getLegoGUIController().snomedDragCompleted();
			}
		});

		Callback<TableColumn.CellDataFeatures<StringWithRefList, StringWithRef>, ObservableValue<StringWithRef>> cellValueFactory = new Callback<TableColumn.CellDataFeatures<StringWithRefList, StringWithRef>, ObservableValue<StringWithRef>>()
		{
			@Override
			public ObservableValue<StringWithRef> call(CellDataFeatures<StringWithRefList, StringWithRef> param)
			{
				StringWithRefList st = param.getValue();
				return new SimpleObjectProperty<SnomedConceptViewController.StringWithRef>(st.get(Integer.parseInt(param.getTableColumn().getId())));
			}
		};

		Callback<TableColumn<StringWithRefList, StringWithRef>, TableCell<StringWithRefList, StringWithRef>> cellFactory = new Callback<TableColumn<StringWithRefList, StringWithRef>, TableCell<StringWithRefList, StringWithRef>>()
		{
			@Override
			public TableCell<StringWithRefList, StringWithRef> call(TableColumn<StringWithRefList, StringWithRef> param)
			{
				final TableCell<StringWithRefList, StringWithRef> cell = new TableCell<StringWithRefList, StringWithRef>()
				{
					@Override
					public void updateItem(final StringWithRef item, boolean empty)
					{
						super.updateItem(item, empty);
						if (!isEmpty())
						{
							Text text = new Text(item.text);
							text.wrappingWidthProperty().bind(getTableColumn().widthProperty());
							setGraphic(text);
							MenuItem mi = new MenuItem("Copy");
							mi.setOnAction(new EventHandler<ActionEvent>()
							{
								@Override
								public void handle(ActionEvent arg0)
								{
									CustomClipboard.set(item.text);
								}
							});

							ContextMenu cm = new ContextMenu(mi);

							if (item.ref != null)
							{
								mi = new MenuItem("View Concept");
								mi.setOnAction(new EventHandler<ActionEvent>()
								{
									@Override
									public void handle(ActionEvent arg0)
									{
										LegoGUI.getInstance().showSnomedConceptDialog(item.ref);
									}
								});
								cm.getItems().add(mi);
							}
							setContextMenu(cm);
						}
					}
				};
				return cell;
			}
		};

		for (DescriptionChronicleDdo d : concept.getDescriptions())
		{
			DescriptionVersionDdo dv = d.getVersions().get(d.getVersions().size() - 1);
			descriptions.getItems().add(
					new StringWithRefList(new StringWithRef(dv.getTypeReference().getText(), dv.getTypeReference().getUuid()), new StringWithRef(dv.getText())));
		}
		setupTable(new String[] { "Type", "Text" }, descriptions, cellValueFactory, cellFactory);

		for (final IdentifierDdo id : ca.getAdditionalIds())
		{
			HBox hbox = new HBox();
			CopyableLabel l = new CopyableLabel(id.getAuthorityRef().getText());

			MenuItem mi = new MenuItem("View Concept");
			mi.setOnAction(new EventHandler<ActionEvent>()
			{
				@Override
				public void handle(ActionEvent arg0)
				{
					LegoGUI.getInstance().showSnomedConceptDialog(id.getAuthorityRef().getUuid());
				}
			});

			l.getContextMenu().getItems().add(mi);
			l.getStyleClass().add("boldLabel");
			hbox.getChildren().add(l);
			hbox.getChildren().add(new CopyableLabel(id.getDenotation() + ""));
			hbox.setSpacing(5.0);
			idVBox.getChildren().add(hbox);
		}

		for (RelationshipChronicleDdo r : concept.getOriginRelationships())
		{
			RelationshipVersionDdo rv = r.getVersions().get(r.getVersions().size() - 1);
			sourceRelationships.getItems().add(
					new StringWithRefList(new StringWithRef(rv.getTypeReference().getText(), rv.getTypeReference().getUuid()), new StringWithRef(rv
							.getDestinationReference().getText(), rv.getDestinationReference().getUuid())));
		}
		setupTable(new String[] { "Type", "Destination" }, sourceRelationships, cellValueFactory, cellFactory);

		treeViewProgress.visibleProperty().bind(treeViewSearchRunning);
		
		try
		{
			ConceptChronicleDdo fxc = WBDataStore.FxTs().getFxConcept(Taxonomies.SNOMED.getUuids()[0], StandardViewCoordinates.getSnomedStatedLatest(), VersionPolicy.ACTIVE_VERSIONS,
					RefexPolicy.REFEX_MEMBERS, RelationshipPolicy.ORIGINATING_AND_DESTINATION_TAXONOMY_RELATIONSHIPS);

			sctTree = new SimTreeView(fxc, WBDataStore.FxTs());
			splitRight.getChildren().add(sctTree);
			VBox.setVgrow(sctTree, Priority.ALWAYS);
			treeViewSearchRunning.set(true);
			sctTree.showConcept(concept.getPrimordialUuid(), treeViewSearchRunning);
		}
		catch (Exception e)
		{
			logger.error("Error creating tree view", e);
			splitRight.getChildren().add(new Label("Unexpected error building tree"));
		}

		showInTree.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				treeViewSearchRunning.set(true);
				sctTree.showConcept(cav.getConcept().getPrimordialUuid(), treeViewSearchRunning);
			}
		});
	}

	private void setupTable(String[] columns, TableView<StringWithRefList> tableView,
			Callback<TableColumn.CellDataFeatures<StringWithRefList, StringWithRef>, ObservableValue<StringWithRef>> cellValueFactory,
			Callback<TableColumn<StringWithRefList, StringWithRef>, TableCell<StringWithRefList, StringWithRef>> cellFactory)
	{
		for (int i = 0; i < columns.length; i++)
		{
			float colWidth = 1.0f / (float) columns.length;
			TableColumn<StringWithRefList, StringWithRef> tc = new TableColumn<StringWithRefList, StringWithRef>(columns[i]);
			tc.setId(i + "");
			tc.setCellValueFactory(cellValueFactory);
			tc.setCellFactory(cellFactory);
			tc.prefWidthProperty().bind(tableView.widthProperty().multiply(colWidth).subtract(5.0));
			tableView.getColumns().add(tc);
		}
		tableView.setPrefHeight(tableView.getMinHeight() + (20.0 * tableView.getItems().size()));
		tableView.setPlaceholder(new Label());
	}

	public String getTitle()
	{
		return fsnLabel.getText();
	}

	protected class StringWithRefList
	{
		private ArrayList<StringWithRef> items_ = new ArrayList<StringWithRef>();

		StringWithRefList(StringWithRef... items)
		{
			for (StringWithRef swr : items)
			{
				items_.add(swr);
			}
		}

		public StringWithRef get(int index)
		{
			return items_.get(index);
		}
	}

	protected class StringWithRef
	{
		String text;
		UUID ref;

		StringWithRef(String text, UUID ref)
		{
			this.text = text;
			this.ref = ref;
		}

		StringWithRef(String text)
		{
			this.text = text;
		}
	}
}
