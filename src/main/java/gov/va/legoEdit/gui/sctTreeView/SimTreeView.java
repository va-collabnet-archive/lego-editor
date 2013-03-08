package gov.va.legoEdit.gui.sctTreeView;

import gov.va.legoEdit.gui.util.Images;
import javafx.event.EventHandler;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.util.Callback;
import org.ihtsdo.fxmodel.FxTaxonomyReferenceWithConcept;
import org.ihtsdo.fxmodel.concept.FxConcept;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimTreeView extends TreeView<FxTaxonomyReferenceWithConcept>
{
	Logger logger = LoggerFactory.getLogger(SimTreeView.class);
	private TerminologyStoreDI ts_;
	
	protected static volatile boolean shutdownRequested = false;

	public SimTreeView(FxConcept rootFxConcept, TerminologyStoreDI ts)
	{
		super();
		ts_ = ts;

		getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

		setCellFactory(new Callback<TreeView<FxTaxonomyReferenceWithConcept>, TreeCell<FxTaxonomyReferenceWithConcept>>()
		{
			@Override
			public TreeCell<FxTaxonomyReferenceWithConcept> call(TreeView<FxTaxonomyReferenceWithConcept> p)
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

		//connect to 

		FxTaxonomyReferenceWithConcept hiddenRootConcept = new FxTaxonomyReferenceWithConcept();
		SimTreeItem hiddenRootItem = new SimTreeItem(hiddenRootConcept, ts_);
		setShowRoot(false);
		setRoot(hiddenRootItem);
		
		FxTaxonomyReferenceWithConcept visibleRootConcept = new FxTaxonomyReferenceWithConcept();
		visibleRootConcept.setConcept(rootFxConcept);

		SimTreeItem visibleRootItem = new SimTreeItem(visibleRootConcept, Images.ROOT.createImageView(), ts_);

		hiddenRootItem.getChildren().add(visibleRootItem);
		visibleRootItem.addChildren();

		// put this event handler on the root
		visibleRootItem.addEventHandler(TreeItem.branchCollapsedEvent(), new EventHandler<TreeItem.TreeModificationEvent<Object>>() 
		{
			@Override
			public void handle(TreeItem.TreeModificationEvent<Object> t)
			{
				// remove grandchildren
				SimTreeItem sourceTreeItem = (SimTreeItem) t.getSource();
				sourceTreeItem.removeGrandchildren();
			}
		});
		
		visibleRootItem.addEventHandler(TreeItem.branchExpandedEvent(), new EventHandler<TreeItem.TreeModificationEvent<Object>>()
		{
			@Override
			public void handle(TreeItem.TreeModificationEvent<Object> t)
			{
				// add grandchildren
				SimTreeItem sourceTreeItem = (SimTreeItem) t.getSource();
				ProgressIndicator p2 = new ProgressIndicator();

				p2.setSkin(new TaxonomyProgressIndicatorSkin(p2));
				p2.setPrefSize(16, 16);
				p2.setProgress(-1);
				sourceTreeItem.setProgressIndicator(p2);
				sourceTreeItem.addChildrenConceptsAndGrandchildrenItems(p2);
			}
		});
	}
	
	/**
	 * rebuild the tree from the root down.  Useful when the requested description type changes.
	 */
	public void rebuild()
	{
		getRoot().getChildren().get(0).getChildren().clear();
		((SimTreeItem)getRoot().getChildren().get(0)).addChildren();
	}
	
	/**
	 * Tell the sim tree to stop whatever threading operations it has running, as the application is exiting.
	 */
	public static void shutdown()
	{
	    shutdownRequested = true;
	}
}
