package gov.va.legoEdit.storage;

import gov.va.legoEdit.model.LegoReference;
import gov.va.legoEdit.model.schemaModel.Concept;
import gov.va.legoEdit.model.schemaModel.Lego;
import java.util.ArrayList;
import java.util.Iterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AdvancedLegoFilter
 * 
 * @author Dan Armbrust
 *         Copyright 2013
 * 
 */
public class AdvancedLegoFilter
{
	static Logger logger = LoggerFactory.getLogger(AdvancedLegoFilter.class);

	public static void removeNonMatchingRelType(ArrayList<LegoReference> legos, Concept relTypeFilter, Concept relDestFilter, String destModifier,
			String relAppliesToLegoSection)
	{
		if (relTypeFilter == null && relDestFilter == null)
		{
			return;
		}
		logger.debug("Applying Advanced Lego Filter - at start: {} Params: {} : {} : {} : {}", 
				legos.size(), (relTypeFilter == null ? null : relTypeFilter.getDesc()), (relDestFilter == null ? null : relDestFilter.getDesc()), 
						destModifier, relAppliesToLegoSection);
		
		Iterator<LegoReference> legoIter = legos.iterator();
		while (legoIter.hasNext())
		{
			LegoReference lr = legoIter.next();
			Lego l = BDBDataStoreImpl.getInstance().getLego(lr.getLegoUUID(), lr.getStampUUID());
			//TODO implement the filter...
		}
		
		logger.debug("Finished Applying Advanced Lego Filter - at end: {}", legos.size());
	}
}
