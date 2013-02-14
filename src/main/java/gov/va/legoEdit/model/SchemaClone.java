package gov.va.legoEdit.model;

import gov.va.legoEdit.formats.LegoXMLUtils;
import gov.va.legoEdit.model.schemaModel.Assertion;
import gov.va.legoEdit.model.schemaModel.Discernible;
import gov.va.legoEdit.model.schemaModel.Expression;
import gov.va.legoEdit.model.schemaModel.Lego;
import gov.va.legoEdit.model.schemaModel.Qualifier;
import gov.va.legoEdit.model.schemaModel.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SchemaClone
{
    static Logger logger = LoggerFactory.getLogger(SchemaClone.class);
    
    public static Lego clone(Lego l)
    {
        try
        {
            return  LegoXMLUtils.readLego(LegoXMLUtils.toXML(l));
        }
        catch (Exception e) 
        {
            logger.error("Unexpected error during clone", e);
            throw new RuntimeException("Copy Failure");
        }
    }
    
    public static Assertion clone(Assertion a)
    {
        try
        {
            return  LegoXMLUtils.readAssertion(LegoXMLUtils.toXML(a));
        }
        catch (Exception e) 
        {
            logger.error("Unexpected error during clone", e);
            throw new RuntimeException("Copy Failure");
        }
    }
    
    public static Value clone(Value v)
    {
        try
        {
            return  LegoXMLUtils.readValue(LegoXMLUtils.toXML(v));
        }
        catch (Exception e) 
        {
            logger.error("Unexpected error during clone", e);
            throw new RuntimeException("Copy Failure");
        }
    }
    
    public static Expression clone(Expression e)
    {
        try
        {
            return  LegoXMLUtils.readExpression(LegoXMLUtils.toXML(e));
        }
        catch (Exception e1) 
        {
            logger.error("Unexpected error during clone", e1);
            throw new RuntimeException("Copy Failure");
        }
    }
    
    public static Discernible clone(Discernible d)
    {
        try
        {
            return  LegoXMLUtils.readDiscernible(LegoXMLUtils.toXML(d));
        }
        catch (Exception e1) 
        {
            logger.error("Unexpected error during clone", e1);
            throw new RuntimeException("Copy Failure");
        }
    }
    
    public static Qualifier clone(Qualifier q)
    {
        try
        {
            return  LegoXMLUtils.readQualifier(LegoXMLUtils.toXML(q));
        }
        catch (Exception e1) 
        {
            logger.error("Unexpected error during clone", e1);
            throw new RuntimeException("Copy Failure");
        }
    }
}
