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
import gov.va.legoEdit.storage.wb.WBUtility;
import gov.va.legoEdit.util.Utility;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommonlyUsedConcepts
{
    Logger logger = LoggerFactory.getLogger(CommonlyUsedConcepts.class);
    // Type to Concept UUID to usage count
    private HashMap<ConceptUsageType, HashMap<String, Count>> usageCounts_;
    private HashMap<ConceptUsageType, ObservableList<ComboBoxConcept>> topLists_;

    public CommonlyUsedConcepts()
    {
        usageCounts_ = new HashMap<>();
        topLists_ = new HashMap<>();
        for (ConceptUsageType cut : ConceptUsageType.values())
        {
            usageCounts_.put(cut, new HashMap<String, Count>());
            topLists_.put(cut, FXCollections.observableArrayList(new ComboBoxConcept[] {}));
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
                    Lego lego = iter.next();
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

                // Ok, should have the top concepts used for each category at this point. Create the topLists.
                for (final ConceptUsageType cut : usageCounts_.keySet())
                {
                    HashMap<String, Count> countToValueMap = usageCounts_.get(cut);
                    TreeSet<Count> sortedCounts = new TreeSet<>(countToValueMap.values());
                    final ArrayList<ComboBoxConcept> temp = new ArrayList<>();

                    for (Count count : sortedCounts)
                    {
                        if (temp.size() >= 5)
                        {
                            break;
                        }
                        Concept c = WBUtility.lookupSnomedIdentifier(count.getItem());

                        if (c != null)
                        {
                            temp.add(new ComboBoxConcept(c.getDesc(), c.getUuid()));
                        }
                    }
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
                usageCounts_.clear();

                logger.debug("Stats gathering finished");
            }
        };

        Thread t = new Thread(r, "CommonCodeLookup");
        t.setDaemon(true);
        t.start();
    }

    // TODO track session stats, update as appropriate
    public ObservableList<ComboBoxConcept> getSuggestions(ConceptUsageType cut)
    {
        return topLists_.get(cut);
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
        if (c == null || Utility.isEmpty(c.getUuid()))
        {
            return;
        }
        else
        {
            HashMap<String, Count> countMap = usageCounts_.get(type);
            Count count = countMap.get(c.getUuid());
            if (count == null)
            {
                count = new Count(c.getUuid());
                countMap.put(c.getUuid(), count);
            }
            count.increment();
        }
    }

    private class Count implements Comparable<Count>
    {
        int count = 0;
        String item;

        @Override
        public int compareTo(Count other)
        {
            //decending order
            int i = other.getCount() - getCount();
            if (i == 0)
            {
                return item.compareTo(other.item);
            }
            return i;
        }

        public Count(String item)
        {
            this.item = item;
        }

        public void increment()
        {
            count++;
        }

        public int getCount()
        {
            return count;
        }

        public String getItem()
        {
            return item;
        }
    }
}
