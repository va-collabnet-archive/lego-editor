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
import java.util.TreeSet;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommonlyUsedConcepts
{
    Logger logger = LoggerFactory.getLogger(CommonlyUsedConcepts.class);
    // Type to Concept UUID to usage count
    private HashMap<ConceptUsageType, HashMap<String, Count>> usageCounts_;
    private HashMap<ConceptUsageType, List<ComboBoxConcept>> topLists_;

    public CommonlyUsedConcepts()
    {
        usageCounts_ = new HashMap<>();
        topLists_ = new HashMap<>();
        for (ConceptUsageType cut : ConceptUsageType.values())
        {
            usageCounts_.put(cut, new HashMap<String, Count>());
            topLists_.put(cut, new ArrayList<ComboBoxConcept>());
        }

        initData();
    }

    private void initData()
    {
        Runnable r = new Runnable()
        {
            @Override
            public void run()
            {
                logger.debug("Gathering Snomed Concept Usage Stats");
                Iterator<Lego> iter = BDBDataStoreImpl.getInstance().getLegos();
                while (iter.hasNext())
                {
                    processLego(iter.next());
                }

                // Ok, should have the top concepts used for each category at this point. Create the topLists.
                for (final ConceptUsageType cut : usageCounts_.keySet())
                {
                    final ArrayList<ComboBoxConcept> temp = getTop(cut, null);
                    Platform.runLater(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            topLists_.get(cut).addAll(temp);
                        }
                    });
                }

                // clear the stats gathered from the DB - just track stats from the session going forward.
                for (HashMap<String, Count> items : usageCounts_.values())
                {
                    items.clear();
                }

                logger.debug("Stats gathering finished");
            }
        };

        Thread t = new Thread(r, "CommonCodeLookup");
        t.setDaemon(true);
        t.start();
    }
    
    private void processLego(Lego lego)
    {
        for (Assertion a : lego.getAssertion())
        {
            if (a.getDiscernible() != null)
            {
                process(a.getDiscernible().getExpression(), ConceptUsageType.DISCERNIBLE);
            }
            if (a.getQualifier() != null)
            {
                process(a.getQualifier().getExpression(), ConceptUsageType.QUALIFIER);
            }
            if (a.getValue() != null)
            {
                process(a.getValue().getExpression(), ConceptUsageType.VALUE);
                process(a.getValue().getMeasurement());
            }
        }
    }
    
    private ArrayList<ComboBoxConcept> getTop(ConceptUsageType cut, List<ComboBoxConcept> dupeCheck)
    {
        HashMap<String, Count> countToValueMap = usageCounts_.get(cut);
        TreeSet<Count> sortedCounts = new TreeSet<>(countToValueMap.values());
        ArrayList<ComboBoxConcept> temp = new ArrayList<>();

        for (Count count : sortedCounts)
        {
            if (temp.size() >= 5)
            {
                break;
            }
            ComboBoxConcept cbc = new ComboBoxConcept(count.getDescription(), count.getId());
            if (dupeCheck != null && dupeCheck.contains(cbc))
            {
                continue;
            }
            else
            {
                temp.add(cbc);
            }
        }
        return temp;
    }

    public List<ComboBoxConcept> getSuggestions(ConceptUsageType cut)
    {
        return topLists_.get(cut);
    }
    
    public void legoCommitted(Lego lego)
    {
        processLego(lego);
        
        for (final ConceptUsageType cut : usageCounts_.keySet())
        {
            //This now will return just the top 5 from the committed legos in this session
            final ArrayList<ComboBoxConcept> temp = getTop(cut, topLists_.get(cut));
            Platform.runLater(new Runnable()
            {
                @Override
                public void run()
                {
                    List<ComboBoxConcept> list = topLists_.get(cut);
                    //Remove until the list is size 5 (only things from the DB)
                    while (list.size() > 5)
                    {
                        list.remove(0);
                    }
                    //Add the new top 5 (from the committed legos)
                    list.addAll(0, temp);
                }
            });
            
        }
    }

    private void process(Expression e, ConceptUsageType defaultType)
    {
        if (e == null)
        {
            return;
        }
        index(e.getConcept(), defaultType);

        for (Expression nestedE : e.getExpression())
        {
            process(nestedE, defaultType);
        }

        for (Relation r : e.getRelation())
        {
            process(r);
        }

        for (RelationGroup rg : e.getRelationGroup())
        {
            for (Relation r : rg.getRelation())
            {
                process(r);
            }
        }
    }

    private void process(Relation r)
    {
        if (r == null)
        {
            return;
        }
        if (r.getDestination() != null)
        {
            process(r.getDestination().getExpression(), ConceptUsageType.REL_DESTINATION);
            process(r.getDestination().getMeasurement());
        }
        if (r.getType() != null)
        {
            index(r.getType().getConcept(), ConceptUsageType.TYPE);
        }
    }

    private void process(Measurement m)
    {
        if (m == null)
        {
            return;
        }
        if (m.getUnits() != null)
        {
            index(m.getUnits().getConcept(), ConceptUsageType.UNITS);
        }
    }

    private void index(Concept c, ConceptUsageType type)
    {
        if (c == null || (Utility.isEmpty(c.getUuid()) && c.getSctid() == null))
        {
            return;
        }
        else
        {
            HashMap<String, Count> countMap = usageCounts_.get(type);
            String id = c.getUuid();
            if (Utility.isEmpty(id) && c.getSctid() != null)
            {
                id = c.getSctid() + " ";  //Dan hack - purposely put a space after the ID.  The lookup later will trim it off, but this 
                //will prevent the .equals checks in the combo box drop downs from selecting this item, and doing odd things with it.
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
            //decending order
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
