package gov.va.legoEdit.model;

import gov.va.legoEdit.model.schemaModel.Assertion;
import gov.va.legoEdit.model.schemaModel.AssertionComponent;
import gov.va.legoEdit.model.schemaModel.Bound;
import gov.va.legoEdit.model.schemaModel.Concept;
import gov.va.legoEdit.model.schemaModel.Destination;
import gov.va.legoEdit.model.schemaModel.Discernible;
import gov.va.legoEdit.model.schemaModel.Expression;
import gov.va.legoEdit.model.schemaModel.Interval;
import gov.va.legoEdit.model.schemaModel.Lego;
import gov.va.legoEdit.model.schemaModel.Measurement;
import gov.va.legoEdit.model.schemaModel.Pncs;
import gov.va.legoEdit.model.schemaModel.Point;
import gov.va.legoEdit.model.schemaModel.PointDouble;
import gov.va.legoEdit.model.schemaModel.PointLong;
import gov.va.legoEdit.model.schemaModel.PointMeasurementConstant;
import gov.va.legoEdit.model.schemaModel.Qualifier;
import gov.va.legoEdit.model.schemaModel.Relation;
import gov.va.legoEdit.model.schemaModel.RelationGroup;
import gov.va.legoEdit.model.schemaModel.Stamp;
import gov.va.legoEdit.model.schemaModel.Type;
import gov.va.legoEdit.model.schemaModel.Units;
import gov.va.legoEdit.model.schemaModel.Value;
import gov.va.legoEdit.util.TimeConvert;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SchemaToString
{
    private static Logger logger = LoggerFactory.getLogger(SchemaToString.class);
    public final static String eol = System.getProperty("line.separator"); 
    
    public static String toString(Object o)
    {
        if (o instanceof Lego)
        {
            return toString((Lego)o);
        }
        else if (o instanceof Assertion)
        {
            return toString((Assertion)o, "");
        }
        else if (o instanceof Discernible)
        {
            return toString((Discernible)o, "");
        }
        else if (o instanceof Qualifier)
        {
            return toString((Qualifier)o, "");
        }
        else if (o instanceof Value)
        {
            return toString((Value)o, "");
        }
        else if (o instanceof Expression)
        {
            return toString((Expression)o, "");
        }
        else
        {
            logger.warn("Unsupported use of SchemaToString for " + o);
            return o.toString();
        }
    }
    
    public static String toString(Lego l)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Lego " + l.getLegoUUID());
        if (l.getComment() != null && l.getComment().length() > 0)
        {
            sb.append(eol + "  Comment: " + l.getComment());
        }
        sb.append(eol + toString(l.getPncs(), "  "));
        sb.append(eol + toString(l.getStamp(), "  "));
        for (Assertion a : l.getAssertion())
        {
            sb.append(eol + toString(a, "  "));
        }
        
        return sb.toString();
    }
    
    public static String toString(Pncs pncs, String prefix)
    {
        if (pncs == null)
        {
            return "";
        }
        return prefix + "PNCS: " + pncs.getName() + " (" + pncs.getId() + ") " + pncs.getValue();
    }
    
    public static String toString(Stamp stamp, String prefix)
    {
        if (stamp == null)
        {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append(prefix + "Stamp:");
        sb.append(eol + prefix + "  Status: " + stamp.getStatus());
        sb.append(eol + prefix + "  Time: " + new Date(TimeConvert.convert(stamp.getTime())).toString());
        sb.append(eol + prefix + "  Author: " + stamp.getAuthor());
        sb.append(eol + prefix + "  Module: " + stamp.getModule());
        sb.append(eol + prefix + "  Path: " + stamp.getPath());
        sb.append(eol + prefix + "  UUID: " + stamp.getUuid());
        
        return sb.toString();
    }
    
    public static String toString(Assertion a, String prefix)
    {
        if (a == null)
        {
            return prefix + "<null Assertion>";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append(prefix + "Assertion " + a.getAssertionUUID());
        sb.append(eol + toString(a.getDiscernible(), prefix + "  "));
        sb.append(eol + toString(a.getQualifier(), prefix + "  "));
        String timing = toString(a.getTiming(), prefix + "  ", "Timing");
        if (timing.length() > 0)
        {
            sb.append(eol + timing);
        }
        sb.append(eol + toString(a.getValue(), prefix + "  "));
        if (a.getAssertionComponent().size() > 0)
        {
            sb.append(prefix + "Assertion Components" + eol);
            for (AssertionComponent ac : a.getAssertionComponent())
            {
                sb.append(eol + toString(ac, prefix + "  "));
            }
        }
        sb.append(eol);
        return sb.toString();
    }
    
    public static String toString(AssertionComponent ac, String prefix)
    {
        if (ac == null)
        {
            return "";
        }
        return prefix + "Assertion Component -> " + toString(ac.getType(),"") + " -> " + ac.getAssertionUUID();
    }
    
    public static String toString(Type t, String prefix)
    {
        if (t == null)
        {
            return "";
        }
        if (t.getConcept() != null)
        {
            return prefix + toString(t.getConcept(), "");
        }
        else
        {
            return prefix + "<null Concept>"; 
        }
    }
    
    public static String toString(Discernible d, String prefix)
    {
        if (d == null || d.getExpression() == null)
        {
            return prefix + "<null Discernible>";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append(prefix + "Discernible " + eol);
        sb.append(toString(d.getExpression(), prefix + "  "));
        
        return sb.toString();
    }
    
    public static String toString(Qualifier q, String prefix)
    {
        if (q == null || q.getExpression() == null)
        {
            return prefix + "<null Discernible>";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append(prefix + "Qualifier" + eol);
        sb.append(toString(q.getExpression(), prefix + "  "));
        
        return sb.toString();
    }
    
    public static String toString(Value v, String prefix)
    {
        if (v == null || (v.getExpression() == null && v.getMeasurement() == null && v.getText() == null && v.isBoolean() == null))
        {
            return prefix + "<null Value>";
        }
        
        StringBuilder sb = new StringBuilder();
        if (v.getExpression() != null)
        {
            sb.append(prefix + "Value" + eol);
            sb.append(toString(v.getExpression(), prefix + "  "));
        }
        else if (v.getMeasurement() != null)
        {
            sb.append(prefix + "Value" + eol);
            sb.append(toString(v.getMeasurement(), prefix + "  ", ""));
        }
        else if (v.getText() != null)
        {
            sb.append(prefix + "Value ");
            sb.append(v.getText());
        }
        else if (v.isBoolean() != null)
        {
            sb.append(prefix + "Value ");
            sb.append(v.isBoolean());
        }
        
        return sb.toString();
    }
    
    public static String toString(Measurement m, String prefix, String type)
    {
        if (m == null)
        {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(prefix + (type != null && type.length() > 0 ? type : "Measurement")) ;
        
        sb.append(" ");
        if (m.getBound() != null)
        {
            sb.append(toString(m.getBound(), ""));
        }
        
        else if (m.getInterval() != null)
        {
            sb.append(toString(m.getInterval(), ""));
        }
        
        else if (m.getPoint() != null)
        {
            sb.append(toString(m.getPoint(), ""));
        }
        
        if (m.getUnits() != null)
        {
            sb.append(toString(m.getUnits(), "  "));
        }
        return sb.toString();
    }
    
    public static String toString(Interval interval, String prefix)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(prefix + "(");
        if (interval.getUpperBound() != null && interval.getLowerBound() != null)
        {
            sb.append(toString(interval.getLowerBound(), ""));
            sb.append(" - ");
            sb.append(toString(interval.getUpperBound(), ""));
        }
        else if (interval.getUpperBound() != null)
        {
            sb.append("< ");
            sb.append(toString(interval.getUpperBound(), ""));
        }
        else if (interval.getLowerBound() != null)
        {
            sb.append("> ");
            sb.append(toString(interval.getLowerBound(), ""));
        }
        else
        {
            sb.append("No Interval");
        }
        sb.append(")");
        return sb.toString();
    }
    
    public static String toString(Bound bound, String prefix)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(prefix + "(");
        if (bound.getLowerPoint() != null && bound.getUpperPoint() != null)
        {
            sb.append(toString(bound.getLowerPoint(), ""));
            sb.append(bound.isLowerPointInclusive() ? " <= X " : " < X ");
            sb.append(bound.isUpperPointInclusive() ? " <= " : " < ");
            sb.append(toString(bound.getUpperPoint(), ""));
        }
        else if (bound.getUpperPoint() != null)
        {
            sb.append("X");
            sb.append(bound.isUpperPointInclusive() ? " <= " : " < ");
            sb.append(toString(bound.getUpperPoint(), ""));
        }
        else if (bound.getLowerPoint() != null)
        {
            sb.append("X");
            sb.append(bound.isLowerPointInclusive() ? " >= " : " > ");
            sb.append(toString(bound.getLowerPoint(), ""));
        }
        else
        {
            sb.append("No Bound");
        }
        sb.append(")");
        return sb.toString();
    }
    
    public static String toString(Point p, String prefix)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(prefix);
        if (p != null)
        {
            if (p instanceof PointLong)
            {
                sb.append(((PointLong)p).getValue());
            }
            else if (p instanceof PointDouble)
            {
                sb.append(((PointDouble)p).getValue());
            }
            else if (p instanceof PointMeasurementConstant)
            {
                sb.append(((PointMeasurementConstant)p).getValue().name());
            }
        }
        else
        {
            sb.append("No Point"); //sure there is - the answer is 42.
        }
        return sb.toString();
    }
    
    public static String toString(Units u, String prefix)
    {
        if (u == null)
        {
            return prefix + "";
        }
        else
        {
            return toString(u.getConcept(), prefix);
        }
    }
    
    public static String toString(Concept c, String prefix)
    {
        if (c == null)
        {
            return prefix + "";
        }
        else
        {
            return prefix + (c.getDesc() != null && c.getDesc().length() > 0 ? c.getDesc() 
                        : (c.getSctid() != null ? c.getSctid().toString() 
                                : (c.getUuid() != null ? c.getUuid() : "")));
        }
    }
    
    public static String toString(Expression e, String prefix)
    {
        if (e == null)
        {
            return prefix + "<null Expression>";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append(prefix + "Expression");
        
        if (e.getConcept() != null)
        {
            sb.append(eol + toString(e.getConcept(), prefix + "  "));
        }
        else
        {
            for (Expression e1 : e.getExpression())
            {
                sb.append(eol + toString(e1, prefix + "  "));
            }
        }
        for (Relation r : e.getRelation())
        {
            sb.append(eol + prefix + "  -> ");
            sb.append(toString(r.getType(), ""));
            sb.append(" -> ");
            sb.append(toString(r.getDestination(), prefix + "    ", false));
        }
        for (RelationGroup rg : e.getRelationGroup())
        {
            sb.append(eol + prefix + "  Relation Group");
            for (Relation r : rg.getRelation())
            {
                sb.append(eol + prefix + "    -> ");
                sb.append(toString(r.getType(), ""));
                sb.append(" -> ");
                sb.append(toString(r.getDestination(), prefix + "      ", false));
            }
        }
        return sb.toString();
    }
    
    public static String toString(Destination d, String prefix, boolean usePrefixOnFirstLine)
    {
        if (d == null)
        {
            return (usePrefixOnFirstLine ? prefix : "") + "<null Destination>";
        }
        
        if (d.isBoolean() != null)
        {
            return (usePrefixOnFirstLine ? prefix : "") + d.isBoolean();
        }
        else if (d.getText() != null)
        {
            return (usePrefixOnFirstLine ? prefix : "") + d.getText();
        }
        else if (d.getExpression() != null)
        {
            return eol + toString(d.getExpression(), prefix);
        }
        else if (d.getMeasurement() != null)
        {
            return toString(d.getMeasurement(), (usePrefixOnFirstLine ? prefix : ""), "");
        }
        else 
        {
            return "";
        }
    }
}
