package gov.va.legoEdit.gui.legoTreeView;

import gov.va.legoEdit.gui.util.LLTreeItemComparator;
import gov.va.legoEdit.model.schemaModel.Assertion;
import gov.va.legoEdit.model.schemaModel.AssertionComponent;
import gov.va.legoEdit.model.schemaModel.AssertionComponents;
import gov.va.legoEdit.model.schemaModel.Bound;
import gov.va.legoEdit.model.schemaModel.Concept;
import gov.va.legoEdit.model.schemaModel.Destination;
import gov.va.legoEdit.model.schemaModel.Discernible;
import gov.va.legoEdit.model.schemaModel.Interval;
import gov.va.legoEdit.model.schemaModel.Lego;
import gov.va.legoEdit.model.schemaModel.LegoList;
import gov.va.legoEdit.model.schemaModel.Measurement;
import gov.va.legoEdit.model.schemaModel.Point;
import gov.va.legoEdit.model.schemaModel.Qualifier;
import gov.va.legoEdit.model.schemaModel.Rel;
import gov.va.legoEdit.model.schemaModel.Timing;
import gov.va.legoEdit.model.schemaModel.Type;
import gov.va.legoEdit.model.schemaModel.Value;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;
import javafx.collections.FXCollections;
import javafx.scene.control.TreeItem;

public class LegoTreeItem extends TreeItem<String>
{
    private LegoTreeNodeType tct_ = null;
    private Object extraData_ = null;
    
    public LegoTreeItem()
    {
        setValue("Hidden Root");
    }
    
	public LegoTreeItem(String value)
	{
		setValue(value);
	}
	
	public LegoTreeItem(String value, LegoTreeNodeType tct)
    {
        this.tct_ = tct;
        setValue(value);
    }
	
	public LegoTreeItem(String label, String value, LegoTreeNodeType tct)
	{
		this.tct_ = tct;
		setValue(value);
		extraData_ = label;
	}

    public LegoTreeItem(LegoList ll)
    {
        setValue(ll.getGroupName());
        this.extraData_ = ll;
        this.tct_ = LegoTreeNodeType.legoList;

        // Going to reorganize these under the LEGO list by introducing a PNCS NAME / value hierarchy in-between the
        // LegoList and the individual legos.

        buildPNCSChildren();
    }
    
    public void buildPNCSChildren()
    {
        if (!(getExtraData() instanceof LegoList))
        {
            throw new IllegalArgumentException();
        }
        LegoList ll = (LegoList)getExtraData();
        Hashtable<String, Hashtable<String, List<Lego>>> pncsHier = new Hashtable<>();

        for (Lego l : ll.getLego())
        {
            String pncsName = l.getPncs().getName();
            Hashtable<String, List<Lego>> pncsValueTable = pncsHier.get(pncsName);
            if (pncsValueTable == null)
            {
                pncsValueTable = new Hashtable<String, List<Lego>>();
                pncsHier.put(pncsName, pncsValueTable);
            }
            String pncsValue = l.getPncs().getValue();
            List<Lego> legoList = pncsValueTable.get(pncsValue);
            if (legoList == null)
            {
                legoList = new ArrayList<Lego>();
                pncsValueTable.put(pncsValue, legoList);
            }
            legoList.add(l);
        }

        for (Entry<String, Hashtable<String, List<Lego>>> items : pncsHier.entrySet())
        {
            LegoTreeItem pncsNameTI = new LegoTreeItem(items.getKey(), LegoTreeNodeType.pncsName);
            getChildren().add(pncsNameTI);
            for (Entry<String, List<Lego>> nestedItems : items.getValue().entrySet())
            {
                LegoTreeItem pncsValueTI = new LegoTreeItem(nestedItems.getKey(), LegoTreeNodeType.pncsValue);
                pncsNameTI.getChildren().add(pncsValueTI);
                for (Lego l : nestedItems.getValue())
                {
                    pncsValueTI.getChildren().add(new LegoTreeItem(l));
                }
            }
        }
        FXCollections.sort(getChildren(), new LLTreeItemComparator(true));
        for (TreeItem<String> item : getChildren())
        {
            FXCollections.sort(item.getChildren(), new LLTreeItemComparator(true));
        }
    }

    public LegoTreeItem(Lego l)
    {
        setValue("Lego");
        extraData_ = l;
        tct_ = LegoTreeNodeType.legoListLego;
    }

	public LegoTreeItem(Assertion a)
	{
		setValue("Assertion");
		tct_ = LegoTreeNodeType.assertion;
		extraData_ = a;

	    if (a.getAssertionComponents() != null)
	    {
	        getChildren().add(new LegoTreeItem(a.getAssertionComponents()));
	    }
	    if (a.getDiscernible() == null)
	    {
	        a.setDiscernible(new Discernible());
	    }
	    getChildren().add(new LegoTreeItem(a.getDiscernible()));
	    
	    if (a.getQualifier() == null)
	    {
	        a.setQualifier(new Qualifier());
	    }
	    getChildren().add(new LegoTreeItem(a.getQualifier()));
		if (a.getTiming() != null)
		{
		    getChildren().add(new LegoTreeItem(a.getTiming()));
		}
		if (a.getValue() == null)
		{
		    a.setValue(new Value());
		}
		getChildren().add(new LegoTreeItem(a.getValue()));
	}
	
	public LegoTreeItem(Discernible d)
	{
		setValue("Discernibile");
		tct_ = LegoTreeNodeType.discernible;
		if (d != null)
		{
			Concept c = d.getConcept();
			if (c == null)
			{
			    c = new Concept();
			    d.setConcept(c);
			}
			getChildren().add(new LegoTreeItem(c, LegoTreeNodeType.destinationConcept));
		}
	}
	
	public LegoTreeItem(Qualifier q)
	{
		setValue("Qualifier");
		tct_ = LegoTreeNodeType.qualifier;
		if (q != null)
		{
		    Concept c = q.getConcept();
		    if (c == null)
            {
                c = new Concept();
                q.setConcept(c);
            }
			getChildren().add(new LegoTreeItem(c, LegoTreeNodeType.destinationConcept));
		}
	}
	
	public LegoTreeItem(Value value)
	{
		setValue("Value");
		tct_ = LegoTreeNodeType.value;
		extraData_ = value;

		if (value.getConcept() != null)
		{
			getChildren().add(new LegoTreeItem(value.getConcept(), LegoTreeNodeType.valueConcept));
		}
		else if (value.getMeasurement() != null)
		{
			getChildren().add(new LegoTreeItem(value.getMeasurement()));
		}
	}
	
	public LegoTreeItem(Timing t)
	{
		setValue("Timing");
		extraData_ = t;
		tct_ = LegoTreeNodeType.timing;
		if (t.getMeasurement() == null)
		{
		    t.setMeasurement(new Measurement());
		}
		getChildren().add(new LegoTreeItem(t.getMeasurement()));
	}
	
	public LegoTreeItem(Interval i)
	{
		setValue("Interval");
		tct_ = LegoTreeNodeType.interval;
		if (i != null)
		{
		    //lower
		    if (i.getLowerPoint() != null)
		    {
		        getChildren().add(new LegoTreeItem(i.getLowerPoint(), LegoTreeNodeType.lower));
		    }
		    else if (i.getLowerBound() != null)
		    {
		        getChildren().add(new LegoTreeItem(i.getLowerBound(), LegoTreeNodeType.lower));
		    }
		    
		    if (i.getUpperPoint() != null)
		    {
		        getChildren().add(new LegoTreeItem(i.getUpperPoint(), LegoTreeNodeType.upper));
		    }
		    else if (i.getUpperBound() != null)
			{
				getChildren().add(new LegoTreeItem(i.getUpperBound(), LegoTreeNodeType.upper));
			}
		}
	}
	
	public LegoTreeItem(Point p, LegoTreeNodeType tct)
    {
        if (tct == LegoTreeNodeType.upper)
        {
            setValue("less Than");
        }
        else if (tct == LegoTreeNodeType.lower)
        {
            setValue("greater Than");
        }
    }
	
	public LegoTreeItem(Bound b, LegoTreeNodeType tct)
	{
		if (tct == LegoTreeNodeType.upper)
		{
			setValue("less Than");
		}
		else if (tct == LegoTreeNodeType.lower)
		{
			setValue("greater Than");
		}
		
		//TODO redo this mess
		
		//b.isInclusive();
//		getChildren().add(new LTreeItem("Inclusive"));
//		
//		if (b.getNumericPoint() != null)
//		{
//			getChildren().add(new LTreeItem(b.getNumericPoint() + "", TreeContentType.floatVal));
//		}
//		else if (b.getStringPoint() != null)
//		{
//			getChildren().add(new LTreeItem(b.getStringPoint().toString(), TreeContentType.measurementConstant));
//		}
//		else if (b.getPointPair() != null)
//		{
//			getChildren().add(new LTreeItem(b.getPointPair()));
//		}
	}
	
	public LegoTreeItem(Point p)
	{
	    setValue("Point");
	    tct_ = LegoTreeNodeType.point;
	    extraData_ = p;
	}
	
	public LegoTreeItem(Measurement measurement)
	{
		setValue("Measurement");
		tct_ = LegoTreeNodeType.measurement;
		extraData_ = measurement;

		if (measurement.getUnits() != null && measurement.getUnits().getConcept() != null)
		{
			getChildren().add(new LegoTreeItem(measurement.getUnits().getConcept(), LegoTreeNodeType.unitsConcept));
		}
		if (measurement.getInterval() != null)
		{
			getChildren().add(new LegoTreeItem(measurement.getInterval()));
		}
		else if (measurement.getPoint() != null)
		{
		    Point p = measurement.getPoint();
		    getChildren().add(new LegoTreeItem(p));
		}

	}
	
//	public LegoTreeItem(Pncs pncs)
//	{
//		setValue("PNCS");
//		tct_ = LegoTreeNodeType.pncs;
//		if (pncs != null)
//		{
//			getChildren().add(new LegoTreeItem("Name", pncs.getName(), LegoTreeNodeType.labeledUneditableString));
//			getChildren().add(new LegoTreeItem("Value", pncs.getValue(), LegoTreeNodeType.labeledUneditableString));
//			getChildren().add(new LegoTreeItem("ID", pncs.getId() + "", LegoTreeNodeType.labeledUneditableString));
//		}
//	}
//	
//	public LegoTreeItem(Stamp stamp)
//	{
//		setValue("Stamp");
//		tct_ = LegoTreeNodeType.stamp;
//		if (stamp != null)
//		{
//			getChildren().add(new LegoTreeItem(stamp.getStatus(), LegoTreeNodeType.status));
//			getChildren().add(new LegoTreeItem("Author", stamp.getAuthor(), LegoTreeNodeType.labeledUneditableString));
//			getChildren().add(new LegoTreeItem("Module", stamp.getModule(), LegoTreeNodeType.labeledUneditableString));
//			getChildren().add(new LegoTreeItem("Path", stamp.getPath(), LegoTreeNodeType.labeledUneditableString));
//			getChildren().add(new LegoTreeItem("Date", new Date(TimeConvert.convert(stamp.getTime())).toString(), LegoTreeNodeType.labeledUneditableString));
//		}
//	}
	
	public LegoTreeItem(AssertionComponents acs)
	{
		setValue("Assertion Components");
		tct_ = LegoTreeNodeType.assertionComponents;
		if (acs != null)
		{
			for (AssertionComponent cac : acs.getAssertionComponent())
			{
				getChildren().add(new LegoTreeItem(cac));
			}
		}
		extraData_ = acs;
	}
	
	public LegoTreeItem(AssertionComponent ac)
	{
		setValue("Assertion Component");
		tct_ = LegoTreeNodeType.assertionComponent;

		getChildren().add(new LegoTreeItem(ac.getAssertionUUID(), LegoTreeNodeType.assertionUUID));

		Type t = ac.getType();
	    if (t == null)
	    {
	        t = new Type();
	        ac.setType(t);
	    }
	    
	    Concept c = t.getConcept();
	    if (c == null)
	    {
	        c = new Concept();
	        t.setConcept(c);
	    }
	    
	    getChildren().add(new LegoTreeItem(c, LegoTreeNodeType.assertionTypeConcept));
	    extraData_ = ac;
	}
	
	public LegoTreeItem(Concept concept, LegoTreeNodeType tct)
	{
		extraData_ = concept;
		this.tct_ = tct;
		
		for (Rel r : concept.getRel())
        {
            getChildren().add(new LegoTreeItem(r));
        }
	}
	
	public LegoTreeItem(Rel r)
	{
		setValue("Relation");
		tct_ = LegoTreeNodeType.relation;
		extraData_ = r;

		Type t = r.getType();
		if (t == null)
		{
		    t = new Type();
		    r.setType(t);
		}
		Concept c = t.getConcept();
		if (c == null)
		{
		    c = new Concept();
		    t.setConcept(c);
		}
	    getChildren().add(new LegoTreeItem(r.getType().getConcept(), LegoTreeNodeType.typeConcept));
	    
	    Destination d = r.getDestination();
	    if (d == null)
	    {
	        d = new Destination();
	        r.setDestination(d);
	    }
	    
		if (d.getConcept() != null)
		{
			getChildren().add(new LegoTreeItem(d.getConcept(), LegoTreeNodeType.destinationConcept));
		}
		else if (d.getMeasurement() != null)
		{
			getChildren().add(new LegoTreeItem(d.getMeasurement()));
		}
	}
	
	protected LegoTreeNodeType getNodeType()
    {
        return tct_;
    }
    
    protected Object getExtraData()
    {
        return extraData_;
    }
}
