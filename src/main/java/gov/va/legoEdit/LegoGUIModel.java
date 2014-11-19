package gov.va.legoEdit;

import edu.stanford.ejalbert.BrowserLauncher;
import gov.va.isaac.util.TaskCompleteCallback;
import gov.va.legoEdit.formats.LegoXMLUtils;
import gov.va.legoEdit.formats.UserPrefsXMLUtils;
import gov.va.legoEdit.gui.legoFilterPane.LegoFilterPaneController;
import gov.va.legoEdit.gui.legoTreeView.LegoTreeItem;
import gov.va.legoEdit.gui.legoTreeView.LegoTreeNodeType;
import gov.va.legoEdit.gui.util.ExpandedNode;
import gov.va.legoEdit.gui.util.LegoTreeItemComparator;
import gov.va.legoEdit.model.LegoListByReference;
import gov.va.legoEdit.model.LegoReference;
import gov.va.legoEdit.model.ModelUtil;
import gov.va.legoEdit.model.schemaModel.Concept;
import gov.va.legoEdit.model.schemaModel.Lego;
import gov.va.legoEdit.model.schemaModel.LegoList;
import gov.va.legoEdit.model.userPrefs.UserPreferences;
import gov.va.legoEdit.storage.AdvancedLegoFilter;
import gov.va.legoEdit.storage.BDBDataStoreImpl;
import gov.va.legoEdit.storage.DataStoreException;
import gov.va.legoEdit.storage.WriteException;
import gov.va.legoEdit.util.UnsavedLegos;
import gov.va.legoEdit.util.Utility;
import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * LegoGUIModel
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 * Copyright 2013
 */
public class LegoGUIModel
{
	Logger logger = LoggerFactory.getLogger(LegoGUIModel.class);
	private static volatile LegoGUIModel instance_;
	private ObservableList<TreeItem<String>> legoLists_ = null;
	private UserPreferences userPreferences_;

	public static LegoGUIModel getInstance()
	{
		if (instance_ == null)
		{
			synchronized (LegoGUIModel.class)
			{
				if (instance_ == null)
				{
					instance_ = new LegoGUIModel();
				}
			}
		}
		return instance_;
	}

	private LegoGUIModel()
	{
		try
		{
			userPreferences_ = UserPrefsXMLUtils.readUserPreferences();
		}
		catch (Exception e)
		{
			userPreferences_ = new UserPreferences();
			userPreferences_.setAuthor(System.getProperty("user.name"));
			userPreferences_.setModule("default module");
			userPreferences_.setPath("default path");
		}
	}

	public UserPreferences getUserPreferences()
	{
		return userPreferences_;
	}

	public LegoList getLegoList(String legoName)
	{
		return BDBDataStoreImpl.getInstance().getLegoListByName(legoName);
	}

	protected LegoTreeItem findTreeItem(String legoUniqueId)
	{
		return findTreeItem(legoLists_, legoUniqueId);
	}

	private LegoTreeItem findTreeItem(List<TreeItem<String>> items, String legoUniqueId)
	{
		for (TreeItem<String> item : items)
		{
			if (item instanceof LegoTreeItem)
			{
				LegoTreeItem lti = (LegoTreeItem) item;
				if (lti.getNodeType() == LegoTreeNodeType.legoReference)
				{
					if (legoUniqueId.equals(((LegoReference) lti.getExtraData()).getUniqueId()))
					{
						return lti;
					}
				}
			}
		}
		for (TreeItem<String> item : items)
		{
			LegoTreeItem result = findTreeItem(item.getChildren(), legoUniqueId);
			if (result != null)
			{
				return result;
			}
		}
		return null;
	}

	/**
	 * 
	 * Assumed to be called on the FXThread, but does the work in the background.  Pass in a boolean property
	 * (which will be set to false) when the background thread completes its work.
	 */
	public void initializeLegoListNames(final TreeView<String> treeView, final Integer pncsFilterId, final String pncsFilterValue, final Concept conceptFilter, 
			final String relAppliesToLegoSection, final Concept relTypeFilter, final String destTypeFilter, final Concept destFilter, 
			final TaskCompleteCallback callback)
	{
		final long startTime = System.currentTimeMillis();
		final ExpandedNode before = gov.va.legoEdit.gui.util.Utility.buildExpandedNodeHierarchy(treeView.getRoot());
		legoLists_ =  treeView.getRoot().getChildren();
		legoLists_.clear();
		
		int targetScrollPos = 0;
		//Now this is a hack....
		//TODO - find a new hack for this
//		if (treeView.getChildrenUnmodifiable().size() > 0)
//		{
//			treeView.scrollTo(arg0);
//			TreeViewSkin<?> tks = ((TreeViewSkin<?>)treeView.getChildrenUnmodifiable().get(0));
//			if (tks.getChildrenUnmodifiable().size() > 0)
//			{
//				VirtualFlow vf = (VirtualFlow)tks.getChildrenUnmodifiable().get(0);
//				IndexedCell<?> first = vf.getFirstVisibleCell();
//				IndexedCell<?> last = vf.getLastVisibleCell();
//				if (first != null && last != null)
//				{
//					targetScrollPos = first.getIndex() + (int)Math.floor(((double)(last.getIndex() - first.getIndex())) / 2.0);
//				}
//			}
//		}
		
		final int scrollTo = (targetScrollPos > 0 ? targetScrollPos : 0);
		
		Runnable r = new Runnable()
		{
			@Override
			public void run()
			{
				ArrayList<LegoReference> legos = new ArrayList<>();
				final HashMap<String, LegoListByReference> legoLists = new HashMap<>();
		
				if (pncsFilterId == null)
				{
					// no concept filter
					if (conceptFilter == null && relTypeFilter == null && destFilter == null)
					{
						// No usable filter - get all - shortcircut the lego processing, just get the lists directly.
						Iterator<LegoList> ll = BDBDataStoreImpl.getInstance().getLegoLists();
						while (ll.hasNext())
						{
							// Throw away the legos, just keep the refs.
							LegoList legoList = ll.next();
							legoLists.put(legoList.getLegoListUUID(), new LegoListByReference(legoList, false));
						}
					}
					else
					{
						boolean alreadyProcessed = false;
						if (conceptFilter != null)
						{
							legos.addAll(LegoReference.convert(BDBDataStoreImpl.getInstance().getLegosContainingConceptIdentifiers(conceptFilter.getSctid() + "",
								conceptFilter.getUuid())));
							
						}
						else if (relTypeFilter != null)
						{
							//start by matching legos anywhere, narrow down below.
							legos.addAll(LegoReference.convert(BDBDataStoreImpl.getInstance().getLegosContainingConceptIdentifiers(relTypeFilter.getSctid() + "",
									relTypeFilter.getUuid())));
						}
						else
						{
							if (LegoFilterPaneController.IS.equals(destTypeFilter))
							{
								//start by matching legos anywhere, narrow down below.
								legos.addAll(LegoReference.convert(BDBDataStoreImpl.getInstance().getLegosContainingConceptIdentifiers(destFilter.getSctid() + "",
									destFilter.getUuid())));
							}
							else
							{
								//with only a child-of, we don't have a shortcut for the starting point.  Just have to get all legos.
								alreadyProcessed = true;
								legos.addAll(AdvancedLegoFilter.findMatchingRelTypes(BDBDataStoreImpl.getInstance().getLegos(), relTypeFilter, destFilter, 
										destTypeFilter, relAppliesToLegoSection));
							}
						}
						
						//Now apply each of the relation filters, removing things that don't match.
						if (!alreadyProcessed)
						{
							AdvancedLegoFilter.removeNonMatchingRelType(legos, relTypeFilter, destFilter, destTypeFilter, relAppliesToLegoSection);
						}
					}
				}
				else
				{
					// we have a pncs filter - start there.
					if (pncsFilterValue == null)
					{
						// id filter, no value
						legos.addAll(LegoReference.convert(BDBDataStoreImpl.getInstance().getLegosForPncs(pncsFilterId)));
					}
					else
					{
						// id filter and value
						legos.addAll(LegoReference.convert(BDBDataStoreImpl.getInstance().getLegosForPncs(pncsFilterId, pncsFilterValue)));
					}
		
					if (conceptFilter != null)
					{
						// Need to remove any legos that don't match the concept filter
						HashSet<String> conceptLegoKeys = new HashSet<>();
						List<Lego> conceptLegos = BDBDataStoreImpl.getInstance().getLegosContainingConceptIdentifiers(conceptFilter.getSctid() + "", conceptFilter.getUuid());
						for (Lego l : conceptLegos)
						{
							conceptLegoKeys.add(ModelUtil.makeUniqueLegoID(l));
						}
						Iterator<LegoReference> iterator = legos.iterator();
						while (iterator.hasNext())
						{
							if (!conceptLegoKeys.contains(iterator.next().getUniqueId()))
							{
								iterator.remove();
							}
						}
					}
					AdvancedLegoFilter.removeNonMatchingRelType(legos, relTypeFilter, destFilter, destTypeFilter, relAppliesToLegoSection);
				}
				// Don't filter unsaved legos - always include them.
				UnsavedLegos unsavedLegos = LegoGUI.getInstance().getLegoGUIController().getUnsavedLegos();
				legos.addAll(LegoReference.convert(unsavedLegos.getLegos(), true));

				if (legos.size() > 0)
				{
					// Need to work backwards from the legos now, and get the legoListRefs - and wire them back together.
					assert legoLists.size() == 0;

					for (LegoReference lr : legos)
					{
						// Could be more than one, usually only one in practice, however
						List<String> legoListIds = BDBDataStoreImpl.getInstance().getLegoListByLego(lr.getLegoUUID());

						// Might also be from the new list...
						String id = unsavedLegos.getLegoListIdForLego(lr.getUniqueId());
						if (id != null)
						{
							legoListIds.add(id);
						}

						for (String legoListId : legoListIds)
						{
							LegoListByReference llbr = legoLists.get(legoListId);
							if (llbr == null)
							{
								llbr = new LegoListByReference(BDBDataStoreImpl.getInstance().getLegoListByID(legoListId), true);
								legoLists.put(legoListId, llbr);
							}
							llbr.getLegoReference().add(lr);
						}
					}
				}
					
				Platform.runLater(new Runnable ()
				{
					@Override
					public void run()
					{
						for (LegoListByReference llbr : legoLists.values())
						{
							legoLists_.add(new LegoTreeItem(llbr));
						}

						legoLists_.add(new LegoTreeItem(LegoTreeNodeType.blankLegoListEndNode));
						FXCollections.sort(legoLists_, new LegoTreeItemComparator(true));
						LegoGUI.getInstance().getLegoGUIController().showLegosForAllOpenTabs();
						if (callback != null)
						{
							callback.taskComplete(startTime, null);
						}
						gov.va.legoEdit.gui.util.Utility.setExpandedStates(before, treeView.getRoot());
						//For some reason, scroll to doesn't work unless you select first
						treeView.getSelectionModel().clearAndSelect(scrollTo);
						treeView.scrollTo(scrollTo);
						treeView.getSelectionModel().clearSelection();
					}
				});
			}
		};
		Utility.tpe.execute(r);
	}

	public void updateLegoLists()
	{
		// Long way around to get back to the method above... but I need the filter params.
		LegoGUI.getInstance().getLegoGUIController().getLegoFilterPaneController().reloadOptions();
		LegoGUI.getInstance().getLegoGUIController().getLegoFilterPaneController().updateLegoList();
	}

	public void importLegoList(LegoList ll) throws WriteException
	{
		BDBDataStoreImpl.getInstance().importLegoList(ll);
		LegoGUI.getInstance().getLegoGUIController().getCommonlyUsedConcept().rebuildDBStats();
		updateLegoLists();
	}

	public void updateLegoList(LegoListByReference llbr, TreeItem<String> ti, String name, String description, String comments) throws DataStoreException, WriteException
	{
		BDBDataStoreImpl.getInstance().updateLegoListMetadata(llbr.getLegoListUUID(), name, description, comments);
		llbr.setGroupName(name);
		llbr.setComments(comments);
		llbr.setDescription(description);
		if (ti != null)
		{
			Event.fireEvent(ti, new TreeItem.TreeModificationEvent<String>(TreeItem.valueChangedEvent(), ti));
		}
	}

	public void removeLegoList(LegoListByReference legoListByReference) throws WriteException
	{
		for (LegoReference lr : legoListByReference.getLegoReference())
		{
			LegoGUI.getInstance().getLegoGUIController().closeTabIfOpen(lr);
			// clear the new list too.
			LegoGUI.getInstance().getLegoGUIController().removeNewLego(lr.getUniqueId());
		}
		BDBDataStoreImpl.getInstance().deleteLegoList(legoListByReference.getLegoListUUID());
		LegoGUI.getInstance().getLegoGUIController().getCommonlyUsedConcept().rebuildDBStats();
		updateLegoLists();
	}

	public void removeLego(LegoListByReference legoListReference, LegoReference legoReference) throws WriteException
	{
		LegoGUI.getInstance().getLegoGUIController().closeTabIfOpen(legoReference);
		BDBDataStoreImpl.getInstance().deleteLego(legoListReference.getLegoListUUID(), legoReference.getLegoUUID(), legoReference.getStampUUID());
		LegoGUI.getInstance().getLegoGUIController().getCommonlyUsedConcept().rebuildDBStats();
		LegoGUI.getInstance().getLegoGUIController().removeNewLego(legoReference.getUniqueId());
		updateLegoLists();
	}
	
	
	public void viewLegoListInBrowser(final String legoListUUID)
	{
		Runnable r = new Runnable()
		{
			@Override
			public void run()
			{
				File f = null;
				try
				{
					LegoList ll = BDBDataStoreImpl.getInstance().getLegoListByID(legoListUUID);
					f = File.createTempFile(ll.getGroupName() + "-", ".html");
					FileOutputStream fos = new FileOutputStream(f);
					
					TransformerFactory tf = TransformerFactory.newInstance();
					Transformer transformer = tf.newTransformer(new StreamSource(LegoXMLUtils.class.getResourceAsStream("/xslTransforms/LegoListToXHTML.xslt")));
					
					LegoXMLUtils.transform(ll, fos, transformer, true);
					
					try
					{
						BrowserLauncher bl = new BrowserLauncher();
						bl.openURLinBrowser(f.toURI().toURL().toString());
					}
					catch (Exception e)
					{
						logger.debug("Error launching web browser with Browser Launcher, trying java native", e);
						//This is the sun way... but it spews crap all over the console - so I used the BrowserLauncher instead (where it works).
						Desktop.getDesktop().browse(f.toURI());
					}
					
					Thread.sleep(10000);
				}
				catch (Exception e)
				{
					LegoGUI.getInstance().showErrorDialog("Error launching browser", "There was an error launching the web browser", null);
					logger.error("Error launching web browser", e);
				}
				finally
				{
					if (f != null)
					{
						f.delete();
					}
				}
			}
		};
		Utility.tpe.execute(r);
	}
	
	public void viewLegoInBrowser(final String legoUUID, final String stampUUID)
	{
		Runnable r = new Runnable()
		{
			@Override
			public void run()
			{
				File f = null;
				try
				{
					Lego l = BDBDataStoreImpl.getInstance().getLego(legoUUID, stampUUID);
					f = File.createTempFile(l.getLegoUUID() + "-", ".html");
					FileOutputStream fos = new FileOutputStream(f);
					
					TransformerFactory tf = TransformerFactory.newInstance();
					Transformer transformer = tf.newTransformer(new StreamSource(LegoXMLUtils.class.getResourceAsStream("/xslTransforms/LegoToXHTML.xslt")));
					
					LegoXMLUtils.transform(l, fos, transformer, true);
					
					//TODO replace both of these with Application.getHostServices.showDocument(...)?
					try
					{
						BrowserLauncher bl = new BrowserLauncher();
						bl.openURLinBrowser(f.toURI().toURL().toString());
					}
					catch (Exception e)
					{
						logger.debug("Error launching web browser with Browser Launcher, trying java native", e);
						//This is the sun way... but it spews crap all over the console - so I used the BrowserLauncher instead (where it works).
						Desktop.getDesktop().browse(f.toURI());
					}
					Thread.sleep(10000);
				}
				catch (Exception e)
				{
					LegoGUI.getInstance().showErrorDialog("Error launching browser", "There was an error launching the web browser", null);
					logger.error("Error launching web browser", e);
				}
				finally
				{
					if (f != null)
					{
						f.delete();
					}
				}
			}
		};
		Utility.tpe.execute(r);
	}
}
