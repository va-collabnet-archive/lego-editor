package gov.va.legoEdit.model;

import gov.va.legoEdit.model.schemaModel.Destination;
import gov.va.legoEdit.model.schemaModel.Expression;
import gov.va.legoEdit.model.schemaModel.Measurement;
import gov.va.legoEdit.model.schemaModel.Relation;
import gov.va.legoEdit.model.schemaModel.RelationGroup;
import gov.va.legoEdit.model.schemaModel.Value;

/**
 * This Summary class is used for the very short summaries that are shown in the lego tree when nodes are collapsed.
 * When things get too long, it cuts off. See the SchemaToString for alternate toString methods.
 * SchemaSummary
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 * Copyright 2013
 */
public class SchemaSummary
{

	public static String summary(Measurement m)
	{
		if (m == null)
		{
			return "";
		}
		StringBuilder sb = new StringBuilder();
		if (m.getPoint() != null)
		{
			sb.append(SchemaToString.toString(m.getPoint(), ""));
		}
		else if (m.getBound() != null)
		{
			sb.append(SchemaToString.toString(m.getBound(), "", true));
		}
		else if (m.getInterval() != null)
		{
			sb.append(SchemaToString.toString(m.getInterval(), ""));
		}
		if (m.getUnits() != null && m.getUnits().getConcept() != null && m.getUnits().getConcept().getDesc() != null)
		{
			sb.append(" " + m.getUnits().getConcept().getDesc());
		}
		return sb.toString();
	}

	public static String summary(Value v)
	{
		if (v == null)
		{
			return "";
		}

		if (v.getMeasurement() != null)
		{
			return summary(v.getMeasurement());
		}
		else if (v.isBoolean() != null)
		{
			return v.isBoolean().toString();
		}
		else if (v.getText() != null)
		{
			return v.getText();
		}
		else if (v.getExpression() != null)
		{
			return summary(v.getExpression());
		}
		return "";
	}

	public static String summary(Destination d)
	{
		if (d == null)
		{
			return "";
		}

		if (d.getMeasurement() != null)
		{
			return summary(d.getMeasurement());
		}
		else if (d.isBoolean() != null)
		{
			return d.isBoolean().toString();
		}
		else if (d.getText() != null)
		{
			return d.getText();
		}
		else if (d.getExpression() != null)
		{
			return summary(d.getExpression());
		}
		return "";
	}

	public static String summary(Expression e)
	{
		if (e == null)
		{
			return "";
		}
		StringBuilder sb = new StringBuilder();
		if (e.getConcept() != null && e.getConcept().getDesc() != null)
		{
			sb.append(e.getConcept().getDesc());
		}
		else if (e.getExpression().size() > 0)
		{
			sb.append("Conjunction of " + e.getExpression().size() + " concepts");
		}

		if (e.getRelation().size() > 0)
		{
			sb.append(" + " + e.getRelation().size() + " Rel");
		}

		if (e.getRelationGroup().size() > 0)
		{
			sb.append(" + " + e.getRelationGroup().size() + " Rel Group");
		}
		return sb.toString();
	}

	public static String summary(Relation r)
	{
		if (r == null)
		{
			return "";
		}
		StringBuilder sb = new StringBuilder();
		if (r.getType() != null && r.getType().getConcept() != null && r.getType().getConcept().getDesc() != null)
		{
			sb.append(r.getType().getConcept().getDesc());
		}
		sb.append(" : ");
		sb.append(summary(r.getDestination()));
		return sb.toString();
	}

	public static String summary(RelationGroup rg)
	{
		if (rg == null)
		{
			return "";
		}
		return rg.getRelation().size() + " Relations";
	}
}
