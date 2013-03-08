package gov.va.legoEdit.gui.legoTreeView;

import gov.va.legoEdit.gui.util.LegoTreeItemComparator;
import gov.va.legoEdit.model.LegoListByReference;
import gov.va.legoEdit.model.LegoReference;
import gov.va.legoEdit.model.schemaModel.Assertion;
import gov.va.legoEdit.model.schemaModel.AssertionComponent;
import gov.va.legoEdit.model.schemaModel.Concept;
import gov.va.legoEdit.model.schemaModel.Destination;
import gov.va.legoEdit.model.schemaModel.Discernible;
import gov.va.legoEdit.model.schemaModel.Expression;
import gov.va.legoEdit.model.schemaModel.Lego;
import gov.va.legoEdit.model.schemaModel.Measurement;
import gov.va.legoEdit.model.schemaModel.Qualifier;
import gov.va.legoEdit.model.schemaModel.Relation;
import gov.va.legoEdit.model.schemaModel.RelationGroup;
import gov.va.legoEdit.model.schemaModel.Stamp;
import gov.va.legoEdit.model.schemaModel.Type;
import gov.va.legoEdit.model.schemaModel.Value;
import gov.va.legoEdit.validators.Validator;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;
import javafx.collections.FXCollections;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.HBox;

/**
 * LegoTreeItem The actual data item for each node in the tree
 * @author Dan Armbrust 
 * Copyright 2013
 * 
 */
public class LegoTreeItem extends TreeItem<String>
{
	private LegoTreeNodeType ltnt_ = null;
	private Object extraData_ = null;
	private Boolean isValid = null;
	private Boolean areChildrenValid = null;
	private String invalidReason_ = null;
	private HBox treeNodeGraphic = null;
	
	public LegoTreeItem getLegoParent()
	{
		return (LegoTreeItem)getParent();
	}

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
		LegoListByReference llbr = (LegoListByReference) getExtraData();
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

	public LegoTreeItem(Lego l, LegoTreeNodeType ltnt)
	{
		if (ltnt == LegoTreeNodeType.comment)
		{
			setValue("Comment");
		}
		else
		{
			throw new IllegalArgumentException();
		}
		extraData_ = l;
		ltnt_ = ltnt;
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
			getChildren().add(new LegoTreeItem(a.getTiming(), LegoTreeNodeType.timingMeasurement));
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
			getChildren().add(new LegoTreeItem(value.getMeasurement(), LegoTreeNodeType.measurement));
		}
		else if (value.getText() != null)
		{
			getChildren().add(new LegoTreeItem(value, LegoTreeNodeType.text));
		}
		else if (value.isBoolean() != null)
		{
			getChildren().add(new LegoTreeItem(value, LegoTreeNodeType.bool));
		}
	}

	public LegoTreeItem(Measurement measurement, LegoTreeNodeType type)
	{
		if (type == LegoTreeNodeType.point)
		{
			setValue("Point");
			ltnt_ = LegoTreeNodeType.point;
			extraData_ = measurement;
		}
		else if (type == LegoTreeNodeType.bound)
		{
			setValue("Bound");
			ltnt_ = LegoTreeNodeType.bound;
			extraData_ = measurement;
		}
		else if (type == LegoTreeNodeType.interval)
		{
			setValue("Interval");
			ltnt_ = LegoTreeNodeType.interval;
			extraData_ = measurement;
		}
		else
		{
			setValue(type == LegoTreeNodeType.timingMeasurement ? "Timing" : "Measurement");
			ltnt_ = type;
			extraData_ = measurement;

			if (measurement.getUnits() != null && measurement.getUnits().getConcept() != null)
			{
				getChildren().add(new LegoTreeItem(measurement.getUnits().getConcept(), LegoTreeNodeType.conceptOptional));
			}
			if (measurement.getInterval() != null)
			{
				getChildren().add(new LegoTreeItem(measurement, LegoTreeNodeType.interval));
			}
			else if (measurement.getPoint() != null)
			{
				getChildren().add(new LegoTreeItem(measurement, LegoTreeNodeType.point));
			}
			else if (measurement.getBound() != null)
			{
				getChildren().add(new LegoTreeItem(measurement, LegoTreeNodeType.bound));
			}
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
				// If we are building a conjunction, and the type is Discernible or qualifier - the expressions become optional
				getChildren().add(
						new LegoTreeItem(e,
								(tct == LegoTreeNodeType.expressionDiscernible || tct == LegoTreeNodeType.expressionQualifier) ? LegoTreeNodeType.expressionOptional
										: tct));
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
		for (Relation r : relationGroup.getRelation())
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
		getChildren().add(new LegoTreeItem(c, LegoTreeNodeType.concept));

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
			getChildren().add(new LegoTreeItem(d.getMeasurement(), LegoTreeNodeType.measurement));
		}
		else if (d.getText() != null)
		{
			getChildren().add(new LegoTreeItem(d, LegoTreeNodeType.text));
		}
		else if (d.isBoolean() != null)
		{
			getChildren().add(new LegoTreeItem(d, LegoTreeNodeType.bool));
		}
	}

	public LegoTreeItem(Value value, LegoTreeNodeType type)
	{
		setValue("");
		ltnt_ = type;
		extraData_ = value;
	}

	public LegoTreeItem(Destination destination, LegoTreeNodeType type)
	{
		setValue("");
		ltnt_ = type;
		extraData_ = destination;
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
	
	public String getInvalidReason()
	{
		return invalidReason_;
	}
	
	public boolean isValid()
	{
		if (isValid == null)
		{
			validate();
		}
		return isValid;
	}
	
	public boolean areChildrenValid()
	{
		if (areChildrenValid == null)
		{
			validateChildren();
		}
		return areChildrenValid;
	}
	
	private void validate()
	{
		if (LegoTreeNodeType.pncsName  == ltnt_ || LegoTreeNodeType.pncsValue == ltnt_ || LegoTreeNodeType.legoListByReference == ltnt_
				|| LegoTreeNodeType.blankLegoEndNode == ltnt_ || LegoTreeNodeType.blankLegoListEndNode == ltnt_ 
				|| LegoTreeNodeType.point == ltnt_ || LegoTreeNodeType.interval == ltnt_ || LegoTreeNodeType.bound == ltnt_
				|| LegoTreeNodeType.legoReference == ltnt_ || LegoTreeNodeType.status == ltnt_ || LegoTreeNodeType.comment == ltnt_)
		{
			invalidReason_ = null;
		}
		else if (extraData_ == null)
		{
			invalidReason_ = null;
		}
		else
		{
			invalidReason_ = Validator.isValid(extraData_, this);
		}
		isValid = invalidReason_ == null;
	}
	
	private void validateChildren()
	{
		boolean newValue = true;
		for (TreeItem<String> ti : getChildren())
		{
			LegoTreeItem lti = (LegoTreeItem)ti;
			if (!lti.isValid() || !lti.areChildrenValid())
			{
				newValue = false;
				break;
			}
		}
		
		areChildrenValid = newValue;
	}
	
	protected void setTreeNodeGraphic(HBox node)
	{
		this.treeNodeGraphic = node;
	}
	
	public void revalidateToRoot()
	{
		validate();
		validateChildren();
		updateValidityImage();
		
		LegoTreeItem parent = getLegoParent();
		if (parent != null)
		{
			parent.revalidateToRoot();
		}
	}
	
	protected void updateValidityImage()
	{
		//add the exclamation - but not on nodes that handle it themselves
		if (treeNodeGraphic != null && LegoTreeNodeType.concept != ltnt_ && LegoTreeNodeType.conceptOptional != ltnt_
				&& LegoTreeNodeType.assertionUUID != ltnt_ && LegoTreeNodeType.text != ltnt_)
		{
			if (isValid() && areChildrenValid())
			{
				if (treeNodeGraphic.getChildren().size() > 0 && treeNodeGraphic.getChildren().get(0) instanceof InvalidNode)
				{
					treeNodeGraphic.getChildren().remove(0);
				}
			}
			else
			{
				if (treeNodeGraphic.getChildren().size() == 0 || !(treeNodeGraphic.getChildren().get(0) instanceof InvalidNode))
				{
					treeNodeGraphic.getChildren().add(0, new InvalidNode(isValid() ? "Error in child" : invalidReason_));
				}
				else
				{
					((InvalidNode)treeNodeGraphic.getChildren().get(0)).setInvalidReason(isValid() ? "Error in child" : invalidReason_);
				}
			}
		}
	}
}
