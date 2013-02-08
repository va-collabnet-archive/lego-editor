package gov.va.legoEdit.model;

import gov.va.legoEdit.model.schemaModel.Assertion;
import gov.va.legoEdit.model.schemaModel.AssertionComponent;
import gov.va.legoEdit.model.schemaModel.Discernible;
import gov.va.legoEdit.model.schemaModel.Expression;
import gov.va.legoEdit.model.schemaModel.Measurement;
import gov.va.legoEdit.model.schemaModel.Qualifier;
import gov.va.legoEdit.model.schemaModel.Type;
import gov.va.legoEdit.model.schemaModel.Value;

public class SchemaToString
{
    public final static String eol = System.getProperty("line.separator"); 
    
    public static String toString(Assertion a, String prefix)
    {
        if (a == null)
        {
            return prefix + "<null Assertion>";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append(prefix + "Assertion " + a.getAssertionUUID() +eol);
        sb.append(prefix + toString(a.getDiscernible(), prefix + "  "));
        sb.append(prefix + toString(a.getQualifier(), prefix + "  "));
        sb.append(prefix + toString(a.getTiming(), prefix + "  ", "Timing"));
        sb.append(prefix + toString(a.getValue(), prefix + "  "));
        if (a.getAssertionComponent().size() > 0)
        {
            sb.append(prefix + "Assertion Components" + eol);
            for (AssertionComponent ac : a.getAssertionComponent())
            {
                sb.append(toString(ac, prefix + "  "));
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
        return prefix + "Assertion Component" + ac.getAssertionUUID() + toString(ac.getType(),"");
    }
    
    public static String toString(Type t, String prefix)
    {
        if (t == null)
        {
            return "";
        }
        if (t.getConcept() != null)
        {
            return prefix + "Type " + t.getConcept().getDesc() + " (" + t.getConcept().getSctid() + ") [" + t.getConcept().getUuid() + "]";
        }
        else
        {
            return prefix + "Type <null Concept>"; 
        }
    }
    
    public static String toString(Discernible d, String prefix)
    {
        if (d == null || d.getExpression() == null)
        {
            return prefix + "<null Discernible>";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append(prefix + "Discernible " + toString(d.getExpression(), prefix) + eol);
        
        return sb.toString();
    }
    
    public static String toString(Qualifier q, String prefix)
    {
        if (q == null || q.getExpression() == null)
        {
            return prefix + "<null Discernible>";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append(prefix + "Qualifier " + toString(q.getExpression(), prefix) + eol);
        
        return sb.toString();
    }
    
    public static String toString(Value v, String prefix)
    {
        if (v == null || (v.getExpression() == null && v.getMeasurement() == null && v.getText() == null && v.isBoolean() == null))
        {
            return prefix + "<null Value>";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append(prefix + "Value ");
        if (v.getExpression() != null)
        {
            sb.append(toString(v.getExpression(), prefix) + eol);
        }
        else if (v.getMeasurement() != null)
        {
            sb.append(toString(v.getMeasurement(), prefix, "") + eol);
        }
        else if (v.getText() != null)
        {
            sb.append(v.getText() + eol);
        }
        else if (v.isBoolean() != null)
        {
            sb.append(v.isBoolean() + eol);
        }
        
        return sb.toString();
    }
    
    public static String toString(Measurement m, String prefix, String type)
    {
        if (m == null)
        {
            return "";
        }
        
        //TODO
        return prefix + "Measurement TODO";
    }
    
    public static String toString(Expression e, String prefix)
    {
        if (e == null)
        {
            return prefix + "<null Expression>";
        }
        
      //TODO
        return prefix + "Expression TODO";
    }
}
