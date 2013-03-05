package gov.va.legoEdit.storage;

import gov.va.legoEdit.gui.legoTreeView.ComboBoxConcept;
import gov.va.legoEdit.gui.legoTreeView.ConceptUsageType;
import gov.va.legoEdit.model.schemaModel.Assertion;
import gov.va.legoEdit.model.schemaModel.Concept;
import gov.va.legoEdit.model.schemaModel.Expression;
import gov.va.legoEdit.model.schemaModel.Lego;
import gov.va.legoEdit.model.schemaModel.Measurement;
import gov.va.legoEdit.model.schemaModel.Relation;
import gov.va.legoEdit.model.schemaModel.RelationGroup;
import gov.va.legoEdit.util.Utility;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommonlyUsedConcepts
{
	Logger logger = LoggerFactory.getLogger(CommonlyUsedConcepts.class);
	
	public static int HOW_MANY_EACH_TYPE = 5;
	// Type to Concept UUID to usage count
	private HashMap<ConceptUsageType, HashMap<String, Count>> sessionUsageCounts_;
	private HashMap<ConceptUsageType, List<ComboBoxConcept>> sessionTopLists_;
	private HashMap<ConceptUsageType, List<ComboBoxConcept>> dbTopLists_;

	public CommonlyUsedConcepts()
	{
		sessionUsageCounts_ = new HashMap<>();
		for (ConceptUsageType cut : ConceptUsageType.values())
		{
			sessionUsageCounts_.put(cut, new HashMap<String, Count>());
		}
		sessionTopLists_ = buildTopList(sessionUsageCounts_, false);  //nothing there yet, just need to init it.
		readDB();
	}
	
	private void readDB()
	{
		logger.debug("Recalculating stats request");
		Runnable r = new Runnable()
		{
			@Override
			public void run()
			{
				logger.debug("Gathering Snomed Concept Usage Stats");
				HashMap<ConceptUsageType, HashMap<String, Count>> dbUsageCounts = new HashMap<>();
				for (ConceptUsageType cut : ConceptUsageType.values())
				{
					dbUsageCounts.put(cut, new HashMap<String, Count>());
				}
				
				Iterator<Lego> iter = BDBDataStoreImpl.getInstance().getLegos();
				while (iter.hasNext())
				{
					processLego(iter.next(), dbUsageCounts);
				}
				dbTopLists_ = buildTopList(dbUsageCounts, false);
				dbUsageCounts = null;
				logger.debug("DB Stats gathering finished");
			}
		};

		Utility.tpe.submit(r);
	}

	private void processLego(Lego lego, HashMap<ConceptUsageType, HashMap<String, Count>> usageCounts)
	{
		for (Assertion a : lego.getAssertion())
		{
			if (a.getDiscernible() != null)
			{
				process(a.getDiscernible().getExpression(), ConceptUsageType.DISCERNIBLE, usageCounts);
			}
			if (a.getQualifier() != null)
			{
				process(a.getQualifier().getExpression(), ConceptUsageType.QUALIFIER, usageCounts);
			}
			if (a.getValue() != null)
			{
				process(a.getValue().getExpression(), ConceptUsageType.VALUE, usageCounts);
				process(a.getValue().getMeasurement(), usageCounts);
			}
		}
	}

	private HashMap<ConceptUsageType, List<ComboBoxConcept>> buildTopList(HashMap<ConceptUsageType, HashMap<String, Count>> usageCounts, boolean checkDBForDupes)
	{
		HashMap<ConceptUsageType, List<ComboBoxConcept>> topList = new HashMap<>();
		
		for (Entry<ConceptUsageType, HashMap<String, Count>> entry : usageCounts.entrySet())
		{
			topList.put(entry.getKey(), new ArrayList<ComboBoxConcept>());
			TreeSet<Count> sortedCounts = new TreeSet<>(entry.getValue().values());
			ArrayList<ComboBoxConcept> temp = new ArrayList<>();
			for (Count count : sortedCounts)
			{
				if (temp.size() >= HOW_MANY_EACH_TYPE)
				{
					break;
				}
				ComboBoxConcept cbc = new ComboBoxConcept(count.getDescription(), count.getId());
				if (checkDBForDupes && dbTopLists_.get(entry.getKey()).contains(cbc))
				{
					continue;
				}
				else
				{
					temp.add(cbc);
				}
			}
			topList.put(entry.getKey(), temp);
		}
		return topList;
	}

	public List<ComboBoxConcept> getSuggestions(ConceptUsageType cut)
	{
		ArrayList<ComboBoxConcept> result = new ArrayList<>(HOW_MANY_EACH_TYPE * 2);
		//If you are fast, you can make this request before the dbinit has finished.  Prevent the null pointer.
		if (dbTopLists_ != null)
		{
			result.addAll(dbTopLists_.get(cut));
		}
		result.addAll(sessionTopLists_.get(cut));
		return result;
	}

	public void legoCommitted(Lego lego)
	{
		processLego(lego, sessionUsageCounts_);
		sessionTopLists_ = buildTopList(sessionUsageCounts_, true);
	}
	
	public void rebuildDBStats()
	{
		//I don't have the proper metadata to update the stats for a delete.
		//Just rebuild the DB stats, and ignore the session stats 
		readDB();
	}

	private void process(Expression e, ConceptUsageType defaultType, HashMap<ConceptUsageType, HashMap<String, Count>> usageCounts)
	{
		if (e == null)
		{
			return;
		}
		index(e.getConcept(), usageCounts.get(defaultType));

		for (Expression nestedE : e.getExpression())
		{
			process(nestedE, defaultType, usageCounts);
		}

		for (Relation r : e.getRelation())
		{
			process(r, usageCounts);
		}

		for (RelationGroup rg : e.getRelationGroup())
		{
			for (Relation r : rg.getRelation())
			{
				process(r, usageCounts);
			}
		}
	}

	private void process(Relation r, HashMap<ConceptUsageType, HashMap<String, Count>> usageCounts)
	{
		if (r == null)
		{
			return;
		}
		if (r.getDestination() != null)
		{
			process(r.getDestination().getExpression(), ConceptUsageType.REL_DESTINATION, usageCounts);
			process(r.getDestination().getMeasurement(), usageCounts);
		}
		if (r.getType() != null)
		{
			index(r.getType().getConcept(), usageCounts.get(ConceptUsageType.TYPE));
		}
	}

	private void process(Measurement m, HashMap<ConceptUsageType, HashMap<String, Count>> usageCounts)
	{
		if (m == null)
		{
			return;
		}
		if (m.getUnits() != null)
		{
			index(m.getUnits().getConcept(), usageCounts.get(ConceptUsageType.UNITS));
		}
	}

	private void index(Concept c, HashMap<String, Count> countMap)
	{
		if (c == null || (Utility.isEmpty(c.getUuid()) && c.getSctid() == null))
		{
			return;
		}
		else
		{
			String id = c.getUuid();
			if (Utility.isEmpty(id) && c.getSctid() != null)
			{
				id = c.getSctid() + "";
			}
			Count count = countMap.get(id);
			if (count == null)
			{
				count = new Count(id, c.getDesc());
				countMap.put(id, count);
			}
			count.increment();
		}
	}

	private class Count implements Comparable<Count>
	{
		int count = 0;
		String id;
		String description;

		public Count(String id, String description)
		{
			this.id = id;
			this.description = description;
		}

		@Override
		public int compareTo(Count other)
		{
			// decending order
			int i = other.getCount() - getCount();
			if (i == 0)
			{
				return id.compareTo(other.id);
			}
			return i;
		}

		public void increment()
		{
			count++;
		}

		public int getCount()
		{
			return count;
		}

		public String getId()
		{
			return id;
		}

		public String getDescription()
		{
			return description;
		}
	}
}
