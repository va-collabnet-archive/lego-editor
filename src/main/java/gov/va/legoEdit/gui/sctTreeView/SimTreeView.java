package gov.va.legoEdit.gui.sctTreeView;

import gov.va.legoEdit.LegoGUI;
import gov.va.legoEdit.gui.util.Images;
import gov.va.legoEdit.storage.wb.WBDataStore;
import gov.va.legoEdit.storage.wb.WBUtility;
import gov.va.legoEdit.util.Utility;
import java.util.ArrayList;
import java.util.UUID;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.event.EventHandler;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.util.Callback;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.coordinate.StandardViewCoordinates;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.ddo.TaxonomyReferenceWithConcept;
import org.ihtsdo.otf.tcc.ddo.concept.ConceptChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.relationship.RelationshipChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.relationship.RelationshipVersionDdo;
import org.ihtsdo.otf.tcc.ddo.fetchpolicy.RefexPolicy;
import org.ihtsdo.otf.tcc.ddo.fetchpolicy.RelationshipPolicy;
import org.ihtsdo.otf.tcc.ddo.fetchpolicy.VersionPolicy;
import org.ihtsdo.otf.tcc.ddo.store.FxTerminologyStoreDI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimTreeView extends TreeView<TaxonomyReferenceWithConcept>
{
	Logger logger = LoggerFactory.getLogger(SimTreeView.class);
	private FxTerminologyStoreDI ts_;
	SimTreeItem visibleRootItem;

	protected static volatile boolean shutdownRequested = false;

	public SimTreeView(ConceptChronicleDdo rootFxConcept, FxTerminologyStoreDI ts)
	{
		super();
		ts_ = ts;

		getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

		setCellFactory(new Callback<TreeView<TaxonomyReferenceWithConcept>, TreeCell<TaxonomyReferenceWithConcept>>()
		{
			@Override
			public TreeCell<TaxonomyReferenceWithConcept> call(TreeView<TaxonomyReferenceWithConcept> p)
			{
				return new SimTreeCell(ts_);
			}
		});

		// sctTree.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
		// @Override
		// public void changed(ObservableValue observable, Object oldValue, Object newValue) {
		// if (newValue instanceof SimTreeItem) {
		// SimTreeItem simTreeItem = (SimTreeItem) newValue;
		// //
		// // getConceptService.setConceptUuid(simTreeItem.getValue().getConcept().getPrimordialUuid());
		// // getConceptService.restart();
		// }
		// }
		// });

		// connect to

		TaxonomyReferenceWithConcept hiddenRootConcept = new TaxonomyReferenceWithConcept();
		SimTreeItem hiddenRootItem = new SimTreeItem(hiddenRootConcept, ts_);
		setShowRoot(false);
		setRoot(hiddenRootItem);

		TaxonomyReferenceWithConcept visibleRootConcept = new TaxonomyReferenceWithConcept();
		visibleRootConcept.setConcept(rootFxConcept);

		visibleRootItem = new SimTreeItem(visibleRootConcept, Images.ROOT.createImageView(), ts_);

		hiddenRootItem.getChildren().add(visibleRootItem);
		visibleRootItem.addChildren();

		// put this event handler on the root
		visibleRootItem.addEventHandler(TreeItem.<TaxonomyReferenceWithConcept>branchCollapsedEvent(), 
				new EventHandler<TreeItem.TreeModificationEvent<TaxonomyReferenceWithConcept>>()
		{
			@Override
			public void handle(TreeItem.TreeModificationEvent<TaxonomyReferenceWithConcept> t)
			{
				// remove grandchildren
				SimTreeItem sourceTreeItem = (SimTreeItem) t.getSource();
				sourceTreeItem.removeGrandchildren();
			}
		});

		visibleRootItem.addEventHandler(TreeItem.<TaxonomyReferenceWithConcept>branchExpandedEvent(), 
				new EventHandler<TreeItem.TreeModificationEvent<TaxonomyReferenceWithConcept>>()
		{
			@Override
			public void handle(TreeItem.TreeModificationEvent<TaxonomyReferenceWithConcept> t)
			{
				// add grandchildren
				SimTreeItem sourceTreeItem = (SimTreeItem) t.getSource();
				ProgressIndicator p2 = new ProgressIndicator();

				//p2.setSkin(new TaxonomyProgressIndicatorSkin(p2));
				p2.setPrefSize(16, 16);
				p2.setProgress(-1);
				sourceTreeItem.setProgressIndicator(p2);
				sourceTreeItem.addChildrenConceptsAndGrandchildrenItems(p2);
			}
		});

		setOnDragDetected(new EventHandler<MouseEvent>()
		{
			public void handle(MouseEvent event)
			{
				/* drag was detected, start a drag-and-drop gesture */
				/* allow any transfer mode */
				Dragboard db = startDragAndDrop(TransferMode.COPY);

				/* Put a string on a dragboard */
				TreeItem<TaxonomyReferenceWithConcept> dragItem = getSelectionModel().getSelectedItem();
				if (dragItem == null)
				{
					// Don't know why, but I've seen this happen...
					return;
				}

				ClipboardContent content = new ClipboardContent();
				content.putString(dragItem.getValue().getConcept().getPrimordialUuid().toString());
				db.setContent(content);
				LegoGUI.getInstance().getLegoGUIController().snomedDragStarted();
				event.consume();
			}
		});

		setOnDragDone(new EventHandler<DragEvent>()
		{
			public void handle(DragEvent event)
			{
				LegoGUI.getInstance().getLegoGUIController().snomedDragCompleted();
			}
		});
	}

	public void showConcept(final UUID uuid, final BooleanProperty setFalseWhenDone)
	{
		Runnable r = new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					final ArrayList<UUID> pathToRoot = new ArrayList<>();
					pathToRoot.add(uuid);
					UUID current = uuid;
					
					while (true)
					{
						//FX API is broken if you ask for a concept that doesn't exist, need to  handle it ourselves...
						ConceptVersionBI cv = WBUtility.lookupSnomedIdentifierAsCV(current.toString());
						if (cv == null)
						{
							//Must be a pending concept. 
							Platform.runLater(new Runnable()
							{
								@Override
								public void run()
								{
									LegoGUI.getInstance().getLegoGUIController().findPendingConcept(uuid.toString());
								}
							});
							
							return;
						}

						ConceptChronicleDdo fxc = new ConceptChronicleDdo(WBDataStore.Ts().getSnapshot(StandardViewCoordinates.getSnomedStatedLatest()), cv,
								VersionPolicy.ACTIVE_VERSIONS, RefexPolicy.REFEX_MEMBERS, RelationshipPolicy.ORIGINATING_RELATIONSHIPS);

						boolean found = false;
						for (RelationshipChronicleDdo rel : fxc.getOriginRelationships())
						{
							RelationshipVersionDdo rv = rel.getVersions().get(rel.getVersions().size() - 1);
							if (rv.getTypeReference().getUuid().equals(Snomed.IS_A))
							{
								pathToRoot.add(rv.getDestinationReference().getUuid());
								current = rv.getDestinationReference().getUuid();
								found = true;
								break;
							}
						}
						if (!found)
						{
							break;
						}
					}
					
					SimTreeItem currentTreeNode = (SimTreeItem)visibleRootItem; 
					SimTreeItem lastFound = null;
					
					for (int i = pathToRoot.size() - 1; i >=0; i--)
					{
						currentTreeNode = findChild(currentTreeNode, pathToRoot.get(i), (i == 0));
						if (currentTreeNode == null)
						{
							break;
						}
						lastFound = currentTreeNode;
					}
					
					if (lastFound != null)
					{
						final SimTreeItem temp = lastFound;
						Platform.runLater(new Runnable()
						{
							@Override
							public void run()
							{
								scrollTo(getRow(temp));
								getSelectionModel().clearAndSelect(getRow(temp));
							}
						});
					}
				}
				catch (Exception e)
				{
					logger.error("Unexpected error trying to find concept in Tree", e);
				}
				finally
				{
					if (setFalseWhenDone != null)
					{
						Platform.runLater(new Runnable()
						{
							@Override
							public void run()
							{
								setFalseWhenDone.set(false);
							}
						});
					}
				}
			}
		};
		
		Utility.tpe.execute(r);
	}

	/**
	 * Ugly nasty threading code to try to get a handle on waiting until children are populated before requesting them.
	 * The first call you make to this should pass in the root node, and its children should already be populated.
	 * After that you can call it repeatedly to walk down the tree.
	 * 
	 * @return the found child, or null, if not found. found child will have already been told to expand and fetch
	 *         its children.
	 */
	private SimTreeItem findChild(final SimTreeItem item, final UUID targetChild, final boolean isLast)
	{
		final ArrayList<SimTreeItem> answer = new ArrayList<>(1);
		Runnable r = new Runnable()
		{
			@Override
			public void run()
			{
				synchronized (answer)
				{
					if (item.getValue().getConcept().getPrimordialUuid().equals(targetChild))
					{
						answer.add((SimTreeItem) item);
					}
					else
					{
						for (TreeItem<TaxonomyReferenceWithConcept> child : item.getChildren())
						{
							if (child != null && child.getValue() != null && child.getValue().getConcept() != null
									&& child.getValue().getConcept().getPrimordialUuid().equals(targetChild))
							{
								answer.add((SimTreeItem) child);
								break;
							}
						}
					}
					if (answer.size() == 0)
					{
						answer.add(null);
					}
					else
					{
						scrollTo(getRow(answer.get(0)));
						if (!isLast)
						{
							// start fetching the next level.
							answer.get(0).setExpanded(true);
							answer.get(0).addChildren();
						}
					}
					answer.notify();
				}
			}
		};

		item.blockUntilChildrenReady();
		synchronized (answer)
		{
			Platform.runLater(r);
			while (answer.size() == 0)
			{
				try
				{
					answer.wait();
				}
				catch (InterruptedException e)
				{
					// noop
				}
			}
		}
		return answer.get(0);
	}

	/**
	 * rebuild the tree from the root down. Useful when the requested description type changes.
	 */
	public void rebuild()
	{
		getRoot().getChildren().get(0).getChildren().clear();
		((SimTreeItem) getRoot().getChildren().get(0)).addChildren();
	}

	/**
	 * Tell the sim tree to stop whatever threading operations it has running, as the application is exiting.
	 */
	public static void shutdown()
	{
		shutdownRequested = true;
	}
}
