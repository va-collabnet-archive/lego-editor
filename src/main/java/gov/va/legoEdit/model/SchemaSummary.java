package gov.va.legoEdit.model;

import gov.va.legoEdit.model.schemaModel.Bound;
import gov.va.legoEdit.model.schemaModel.Destination;
import gov.va.legoEdit.model.schemaModel.Expression;
import gov.va.legoEdit.model.schemaModel.Measurement;
import gov.va.legoEdit.model.schemaModel.Point;
import gov.va.legoEdit.model.schemaModel.PointDouble;
import gov.va.legoEdit.model.schemaModel.PointLong;
import gov.va.legoEdit.model.schemaModel.PointMeasurementConstant;
import gov.va.legoEdit.model.schemaModel.Relation;
import gov.va.legoEdit.model.schemaModel.RelationGroup;
import gov.va.legoEdit.model.schemaModel.Value;

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
            sb.append(summary(m.getPoint()));
        }
        else if (m.getBound() != null)
        {
            sb.append(summary(m.getBound()));
        }
        else if (m.getInterval() != null)
        {
            if (m.getInterval().getLowerBound() != null && m.getInterval().getUpperBound() != null)
            {
                sb.append(summary(m.getInterval().getLowerBound()));
                sb.append(" <= Y <= ");
                sb.append(summary(m.getInterval().getUpperBound()));
            }
            else if (m.getInterval().getLowerBound() != null)
            {
                sb.append("Y >= ");
                sb.append(summary(m.getInterval().getLowerBound()));
            }
            else if (m.getInterval().getUpperBound() != null)
            {
                sb.append("Y <= ");
                sb.append(summary(m.getInterval().getUpperBound()));
            }
        }
        if (m.getUnits() != null && m.getUnits().getConcept() != null && m.getUnits().getConcept().getDesc() != null)
        {
            sb.append(" " + m.getUnits().getConcept().getDesc());
        }
        return sb.toString();
    }
    
    public static String summary(Point p)
    {
        if (p instanceof PointDouble)
        {
            return ((PointDouble)p).getValue() + "";
        }
        else if (p instanceof PointLong)
        {
            return ((PointLong)p).getValue() + "";
        }
        else if (p instanceof PointMeasurementConstant)
        {
            return ((PointMeasurementConstant)p).getValue().name();
        }
        return "";
    }
    
    public static String summary(Bound b)
    {
        if (b == null)
        {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        if (b.getLowerPoint() != null && b.getUpperPoint() != null)
        {
            sb.append(summary(b.getLowerPoint()));
            sb.append(b.isLowerPointInclusive() ? " <=" : " <");
            sb.append(" X");
            sb.append(b.isUpperPointInclusive() ? " <=" : " <");
            sb.append(summary(b.getUpperPoint()));
        }
        else if (b.getLowerPoint() != null)
        {
            sb.append("X ");
            sb.append(b.isLowerPointInclusive() ? ">=" : ">");
            sb.append(summary(b.getLowerPoint()));
        }
        else if (b.getUpperPoint() != null)
        {
            sb.append("X ");
            sb.append(b.isUpperPointInclusive() ? "<=" : "<");
            sb.append(summary(b.getUpperPoint()));
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
        sb.append(" - ");
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
