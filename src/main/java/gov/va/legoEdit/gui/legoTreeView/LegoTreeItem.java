package gov.va.legoEdit.gui.legoTreeView;

import gov.va.legoEdit.gui.util.LegoTreeItemComparator;
import gov.va.legoEdit.model.LegoListByReference;
import gov.va.legoEdit.model.LegoReference;
import gov.va.legoEdit.model.schemaModel.Assertion;
import gov.va.legoEdit.model.schemaModel.AssertionComponent;
import gov.va.legoEdit.model.schemaModel.Bound;
import gov.va.legoEdit.model.schemaModel.Concept;
import gov.va.legoEdit.model.schemaModel.Destination;
import gov.va.legoEdit.model.schemaModel.Discernible;
import gov.va.legoEdit.model.schemaModel.Expression;
import gov.va.legoEdit.model.schemaModel.Interval;
import gov.va.legoEdit.model.schemaModel.Measurement;
import gov.va.legoEdit.model.schemaModel.Point;
import gov.va.legoEdit.model.schemaModel.Qualifier;
import gov.va.legoEdit.model.schemaModel.Relation;
import gov.va.legoEdit.model.schemaModel.RelationGroup;
import gov.va.legoEdit.model.schemaModel.Stamp;
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
    private LegoTreeNodeType ltnt_ = null;
    private Object extraData_ = null;
    
    public LegoTreeItem()
    {
        setValue("Hidden Root");
    }
    
	public LegoTreeItem(String value)
	{
		setValue(value);
	}
	
	public LegoTreeItem(LegoTreeNodeType tct)
    {
        this.ltnt_ = tct;
        setValue(null);
    }
	
	public LegoTreeItem(String value, LegoTreeNodeType tct)
    {
        this.ltnt_ = tct;
        setValue(value);
    }
	
	public LegoTreeItem(Stamp value, LegoTreeNodeType tct)
    {
        this.ltnt_ = tct;
        extraData_ = value;
        setValue(null);
    }
	
	public LegoTreeItem(String label, String value, LegoTreeNodeType tct)
	{
		this.ltnt_ = tct;
		setValue(value);
		extraData_ = label;
	}

    public LegoTreeItem(LegoListByReference llbr)
    {
        setValue(llbr.getGroupName());
        this.extraData_ = llbr;
        this.ltnt_ = LegoTreeNodeType.legoListByReference;

        // Going to reorganize these under the LEGO list by introducing a PNCS NAME / value hierarchy in-between the
        // LegoList and the individual legos.

        buildPNCSChildren();
    }
    
    public void buildPNCSChildren()
    {
        if (!(getExtraData() instanceof LegoListByReference))
        {
            throw new IllegalArgumentException();
        }
        LegoListByReference llbr = (LegoListByReference)getExtraData();
        Hashtable<String, Hashtable<String, List<LegoReference>>> pncsHier = new Hashtable<>();

        for (LegoReference lr : llbr.getLegoReference())
        {
            String pncsName = lr.getPncs().getName();
            Hashtable<String, List<LegoReference>> pncsValueTable = pncsHier.get(pncsName);
            if (pncsValueTable == null)
            {
                pncsValueTable = new Hashtable<String, List<LegoReference>>();
                pncsHier.put(pncsName, pncsValueTable);
            }
            String pncsValue = lr.getPncs().getValue();
            List<LegoReference> legoList = pncsValueTable.get(pncsValue);
            if (legoList == null)
            {
                legoList = new ArrayList<LegoReference>();
                pncsValueTable.put(pncsValue, legoList);
            }
            legoList.add(lr);
        }

        for (Entry<String, Hashtable<String, List<LegoReference>>> items : pncsHier.entrySet())
        {
            LegoTreeItem pncsNameTI = new LegoTreeItem(items.getKey(), LegoTreeNodeType.pncsName);
            getChildren().add(pncsNameTI);
            for (Entry<String, List<LegoReference>> nestedItems : items.getValue().entrySet())
            {
                LegoTreeItem pncsValueTI = new LegoTreeItem(nestedItems.getKey(), LegoTreeNodeType.pncsValue);
                pncsNameTI.getChildren().add(pncsValueTI);
                for (LegoReference lr : nestedItems.getValue())
                {
                    pncsValueTI.getChildren().add(new LegoTreeItem(lr));
                }
            }
        }
        FXCollections.sort(getChildren(), new LegoTreeItemComparator(true));
        for (TreeItem<String> item : getChildren())
        {
            FXCollections.sort(item.getChildren(), new LegoTreeItemComparator(true));
        }
    }

    public LegoTreeItem(LegoReference lr)
    {
        setValue("Lego");
        extraData_ = lr;
        ltnt_ = LegoTreeNodeType.legoReference;
    }

	public LegoTreeItem(Assertion a)
	{
		setValue("Assertion");
		ltnt_ = LegoTreeNodeType.assertion;
		extraData_ = a;

	    for (AssertionComponent ac : a.getAssertionComponent())
	    {
	        getChildren().add(new LegoTreeItem(ac));
	    }
	    if (a.getDiscernible() == null)
	    {
	        a.setDiscernible(new Discernible());
	    }

        Expression de = a.getDiscernible().getExpression();
        if (de == null)
        {
            de = new Expression();
            a.getDiscernible().setExpression(de);
        }
        getChildren().add(new LegoTreeItem(de, LegoTreeNodeType.expressionDiscernible));
	    
	    if (a.getQualifier() == null)
	    {
	        a.setQualifier(new Qualifier());
	    }

        Expression qe = a.getQualifier().getExpression();
	    if (qe == null)
	    {
            qe = new Expression();
            a.getQualifier().setExpression(qe);
        }
	    getChildren().add(new LegoTreeItem(qe, LegoTreeNodeType.expressionQualifier));	    
	    
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
	
	public LegoTreeItem(Value value)
	{
		setValue("Value");
		ltnt_ = LegoTreeNodeType.value;
		extraData_ = value;

		if (value.getExpression() != null)
		{
			getChildren().add(new LegoTreeItem(value.getExpression(), LegoTreeNodeType.expressionValue));
		}
		else if (value.getMeasurement() != null)
		{
			getChildren().add(new LegoTreeItem(value.getMeasurement()));
		}
		else if (value.getText() != null)
		{
		    new LegoTreeItem("TODO text");  //TODO text
		}
		else if (value.isBoolean() != null)
		{
		    new LegoTreeItem("TODO booelan");  //TODO boolean
		}
	}
	
	public LegoTreeItem(Timing t)
	{
		setValue("Timing");
		extraData_ = t;
		ltnt_ = LegoTreeNodeType.timing;
		if (t.getMeasurement() == null)
		{
		    t.setMeasurement(new Measurement());
		}
		getChildren().add(new LegoTreeItem(t.getMeasurement()));
	}
	
	public LegoTreeItem(Interval i)
	{
		setValue("Interval");
		ltnt_ = LegoTreeNodeType.interval;
		if (i != null)
		{
		    if (i.getLowerBound() != null)
		    {
		        getChildren().add(new LegoTreeItem(i.getLowerBound(), LegoTreeNodeType.lower));
		    }
		    
		    if (i.getUpperBound() != null)
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
		
		//TODO bound mess
		
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
	    ltnt_ = LegoTreeNodeType.point;
	    extraData_ = p;
	}
	
	public LegoTreeItem(Measurement measurement)
	{
		setValue("Measurement");
		ltnt_ = LegoTreeNodeType.measurement;
		extraData_ = measurement;

		if (measurement.getUnits() != null && measurement.getUnits().getConcept() != null)
		{
			getChildren().add(new LegoTreeItem(measurement.getUnits().getConcept(), LegoTreeNodeType.conceptOptional));
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
	
	public LegoTreeItem(AssertionComponent ac)
	{
		setValue("Assertion Component");
		ltnt_ = LegoTreeNodeType.assertionComponent;

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
	    
	    getChildren().add(new LegoTreeItem(c, LegoTreeNodeType.concept));
	    extraData_ = ac;
	}
	
	public LegoTreeItem(Concept concept, LegoTreeNodeType tct)
    {
        extraData_ = concept;
        this.ltnt_ = tct;
    }
	
	public LegoTreeItem(Expression expression, LegoTreeNodeType tct)
	{
		extraData_ = expression;
		this.ltnt_ = tct;
		setValue("Expression");
		
		if (expression.getConcept() == null && expression.getExpression().size() == 0)
        {
		    Concept c = new Concept();
            expression.setConcept(c);
        }
		
		if (expression.getConcept() != null)
		{
		    getChildren().add(new LegoTreeItem(expression.getConcept(), LegoTreeNodeType.concept));
		}
		
		if (expression.getExpression().size() > 0)
		{
		    while (expression.getExpression().size() < 2)
	        {
	            expression.getExpression().add(new Expression());
	        }
	        
	        for (Expression e : expression.getExpression())
	        {
	            getChildren().add(new LegoTreeItem(e, tct));
	        }
		}
		
		if (expression.getRelation() != null)
		{
    		for (Relation r : expression.getRelation())
            {
    		    getChildren().add(new LegoTreeItem(r));
            }
		}
		if (expression.getRelationGroup() != null)
		{
		    for (RelationGroup rg : expression.getRelationGroup())
		    {
		        LegoTreeItem rgti = new LegoTreeItem(rg);
		        getChildren().add(rgti);
		        for (Relation r : rg.getRelation())
		        {
		            rgti.getChildren().add(new LegoTreeItem(r));
		        }
		    }
		}
	}
	
	public LegoTreeItem(RelationGroup relationGroup)
    {
        extraData_ = relationGroup;
        ltnt_ = LegoTreeNodeType.relationshipGroup;
        setValue("Relation Group");
        if (relationGroup.getRelation().size() == 0)
        {
            relationGroup.getRelation().add(new Relation());
        }
        for (Relation r: relationGroup.getRelation())
        {
            getChildren().add(new LegoTreeItem(r));
        }
    }
	
	public LegoTreeItem(Relation r)
	{
		setValue("Relation");
		ltnt_ = LegoTreeNodeType.relation;
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
	    getChildren().add(new LegoTreeItem(c, LegoTreeNodeType.conceptOptional));
	    
	    Destination d = r.getDestination();
	    if (d == null)
	    {
	        d = new Destination();
	        r.setDestination(d);
	    }
	    
		if (d.getExpression() != null)
		{
			getChildren().add(new LegoTreeItem(d.getExpression(), LegoTreeNodeType.expressionDestination));
		}
		else if (d.getMeasurement() != null)
		{
			getChildren().add(new LegoTreeItem(d.getMeasurement()));
		}
		else if (d.getText() != null)
        {
            new LegoTreeItem("TODO text");  //TODO text
        }
        else if (d.isBoolean() != null)
        {
            new LegoTreeItem("TODO booelan");  //TODO boolean
        }
	}
	
	public LegoTreeNodeType getNodeType()
    {
        return ltnt_;
    }
    
    public Object getExtraData()
    {
        return extraData_;
    }
    
    public int getSortOrder()
    {
        if (ltnt_ != null)
        {
            return ltnt_.getSortOrder();
        }
        return 0;
    }
}
