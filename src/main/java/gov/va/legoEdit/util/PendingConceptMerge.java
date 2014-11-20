package gov.va.legoEdit.util;

import gov.va.isaac.init.SystemInit;
import gov.va.legoEdit.model.PendingConcept;
import gov.va.legoEdit.model.PendingConcepts;
import gov.va.legoEdit.model.schemaModel.Concept;
import gov.va.legoEdit.storage.wb.WBDataStore;
import gov.va.legoEdit.storage.wb.WBUtility;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import javafx.util.Pair;

/**
 * PendingConceptMerge
 *
 * Hack code to read in a pending concept file, and merge it into the primary pending concept file, 
 * adding remaps as necessary.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 * Copyright 2013
 */
public class PendingConceptMerge extends PendingConcepts
{
	public PendingConceptMerge() throws Exception
	{
		super();
		highestInUseId = 12000;  //make our remaps go higher than anything existing
		//File pendingToMerge = new File("pendingMerge/collabnet/pendingConcepts.tsv");
		//File pendingToMerge = new File("pendingMerge/DS_pendingConcepts.tsv");
		File pendingToMerge = new File("pendingMerge/HS_pendingConcepts.tsv");
		
		System.out.println("Currently have " + getPendingConcepts().size() + " pending concepts");
		
		System.out.println("Reading " + pendingToMerge.getAbsolutePath());
		
		int added = 0;
		int split = 0;
		
		HashMap<Long, Long> incompleteRemaps = new HashMap<>();  //Old ID to new ID for the incoming entries
		HashMap<Long, Pair<Long, String>> incomingParents = new HashMap<>();  //conceptID to the conceptID,description of the parent
		
		List<String> lines = Files.readAllLines(pendingToMerge.toPath(), StandardCharsets.UTF_8);
		for (String s : lines)
		{
			if (s.startsWith("#") || s.length() == 0)
			{
				continue;
			}
			String[] parts = s.split("\t");
			if (parts.length > 1)
			{
				Long id;
				String description;
				Pair<Long, String> parentID = null;
				
				try
				{
					id = Long.parseLong(parts[0]);
				}
				catch (NumberFormatException e)
				{
					System.err.println("Invalid ID in pending concepts file - line '" + s + "'");
					continue;
				}
				
				description = parts[1];
				
				if (parts.length > 2)
				{
					try
					{
						long temp = Long.parseLong(parts[2]);
						String desc = null;
						if (parts.length > 3)
						{
							desc = parts[3];
						}
						parentID = new Pair<>(temp, desc);
					}
					catch (NumberFormatException e)
					{
						System.err.println("Invalid Parent ID in pending concepts file - line '" + s + "'");
					}
				}
				
				
				PendingConcept currentPC = getConcept(id + "", description);
				if (currentPC == null)
				{
					//It still might have already been remapped, but with a different description.  make sure we don't add an ID that is already in the
					//remap list
					
					if (pcr_.hasRemap(id))
					{
						//It has a remap, but the description didn't match ours.  Make yet another remap for this entry.
						
						Long remappedId = getUnusedId();
						addConcept(remappedId, description, null);
						split++;
						if (parentID != null)
						{
							incomingParents.put(remappedId, parentID);
						}
						registerRemap(id, description, remappedId);
						incompleteRemaps.put(id, remappedId);
						
					}
					else
					{
						//not in the current pending concepts list.  Add.  (don't put in parents yet - need to deal with out of order issues)
						addConcept(id, description, null);
						added++;
						if (parentID != null)
						{
							incomingParents.put(id, parentID);
						}
					}
				}
				else
				{
					if (currentPC.getSctid().longValue() != id.longValue())
					{
						//If we did a lookup on one ID, and got back another - it has already been remapped.  Nothing to do.
						checkParents(getParentConcept(currentPC.getSctid()), currentPC, parentID, incomingParents);
					}
					else
					{
						//Got back one with the same ID.
						if (currentPC.getDesc().equals(description))
						{
							//perfect match - nothing to do
							checkParents(getParentConcept(currentPC.getSctid()), currentPC, parentID, incomingParents);
						}
						else
						{
							//oops.  Need to split this into two new concepts.
							System.out.println("Splitting " + currentPC.getSctid() + " - " + currentPC.getDesc() + " - vs - " 
									+ description);
							remap(currentPC.getSctid(), currentPC.getDesc());
							split++;
							
							Long newID = getUnusedId();
							addConcept(newID, description, null);  //Don't put in parent yet
							if (parentID != null)
							{
								incomingParents.put(newID, parentID);
							}
							registerRemap(id, description, newID);
							incompleteRemaps.put(id, newID);
						}
					}
				}
			}
			else
			{
				System.err.println("Skipping invalid line: '" + s + "'");
			}
		}
		
		for (Entry<Long, Long> x : incompleteRemaps.entrySet())
		{
			fixParentMapping(x.getKey(), x.getValue());
		}
		
		for (Entry<Long, Pair<Long, String>> x : incomingParents.entrySet())
		{
			Long pendingID = x.getKey();
			Pair<Long, String> parentInfo = x.getValue();
			Long parentID = parentInfo.getKey();
			
			//Is there a remap for the parent?
			ArrayList<Pair<Long, String>> parentRemap = pcr_.getRemap(parentInfo.getKey());
			
			if (parentRemap != null)
			{
				boolean found = false;
				for (Pair<Long, String> oldEntry : parentRemap)
				{
					if (oldEntry.getValue().equals(parentInfo.getValue()))
					{
						//Use this remap
						parentID = oldEntry.getKey();
						found = true;
						break;
					}
				}
				if (!found)
				{
					//uh oh - its supposed to be remapped, but I don't know which one to use.  I _think_ it will always be the last one in the list.
					System.err.println("Picking random remap for parent ID" + parentID);
					parentID = parentRemap.get(parentRemap.size() - 1).getKey();
				}
			}
			
			Concept parent = WBUtility.lookupSnomedIdentifier(parentID + "", null);
			if (parent == null)
			{
				System.err.println("Couldn't lookup incoming parent concept " + parentID);
			}
			else
			{
				addParent(pendingID, parent);
			}
		}
		
		rewritePendingConceptsFile();
		
		WBDataStore.shutdown();
		
		System.out.println("Merge complete");
		System.out.println("Added: " + added);
		System.out.println("Split: " + split);
	}
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception
	{
		SystemInit.doBasicSystemInit();
		new PendingConceptMerge();
	}

	private void checkParents(Concept currentParent, PendingConcept currentPC, Pair<Long, String> incomingParentID, HashMap<Long, Pair<Long, String>> incomingParents)
	{
		if (currentParent != null && incomingParentID != null && currentParent.getSctid().longValue() != incomingParentID.getKey())
		{
			//See if we have a remap for the incoming parent
			long remappedParent = pcr_.getNewID(incomingParentID.getKey(), incomingParentID.getValue());
			
			if (currentParent.getSctid() != remappedParent)
			{
				System.err.println("Different parent concepts specified for '" + currentPC.getSctid() + "' "
					+ "Please manually resolve: " + currentParent.getSctid() + " - " + incomingParentID.getValue());
			}
		}
		else if (currentParent == null && incomingParentID != null)
		{
			//will just add this - but later - so remaps can be handled
			incomingParents.put(currentPC.getSctid(), incomingParentID);
		}
	}
}
