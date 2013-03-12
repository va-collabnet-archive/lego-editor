package gov.va.legoEdit;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.scene.control.TreeItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author darmbrust
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
	public void initializeLegoListNames(final TreeItem<String> treeRoot, final Integer pncsFilterId, final String pncsFilterValue, final Concept conceptFilter, 
			final String relAppliesToLegoSection, final Concept relTypeFilter, final String destTypeFilter, final Concept destFilter, 
			final BooleanProperty setFalseWhenFinished)
	{
		final ExpandedNode before = gov.va.legoEdit.gui.util.Utility.buildExpandedNodeHierarchy(treeRoot);
		legoLists_ = treeRoot.getChildren();
		legoLists_.clear();

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
						if (setFalseWhenFinished != null)
						{
							setFalseWhenFinished.set(false);
						}
						gov.va.legoEdit.gui.util.Utility.setExpandedStates(before, treeRoot);
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
}
