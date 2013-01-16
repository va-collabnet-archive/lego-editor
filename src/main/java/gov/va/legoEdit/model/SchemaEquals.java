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
import gov.va.legoEdit.model.schemaModel.LegoList;
import gov.va.legoEdit.model.schemaModel.Measurement;
import gov.va.legoEdit.model.schemaModel.MeasurementConstant;
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
import java.util.List;
import javax.xml.datatype.XMLGregorianCalendar;

public class SchemaEquals
{
    public static boolean equals(LegoList a, LegoList b)
    {
        if (a == null && b == null)
        {
            return true;
        }
        else if ((a == null && b != null) || (a != null && b == null))
        {
            return false;
        }
        return (equals(a.getComment(), b.getComment()) && equals(a.getGroupDescription(), b.getGroupDescription()) && equals(a.getGroupName(), b.getGroupName())
                && equals(a.getLegoListUUID(), b.getLegoListUUID()) && equals(a.getLego(), b.getLego()));
    }
    
    public static boolean equals(Lego a, Lego b)
    {
        if (a == null && b == null)
        {
            return true;
        }
        else if ((a == null && b != null) || (a != null && b == null))
        {
            return false;
        }

        return equals(a.getPncs(), b.getPncs()) && equals(a.getStamp(), b.getStamp()) && equals(a.getComment(), b.getComment())
                && equals(a.getLegoUUID(), b.getLegoUUID()) && equals(a.getAssertion(), b.getAssertion());
    }

    public static boolean equals(Pncs a, Pncs b)
    {
        if (a == null && b == null)
        {
            return true;
        }
        else if ((a == null && b != null) || (a != null && b == null))
        {
            return false;
        }
        else
        {
            return a.getId() == b.getId() && equals(a.getName(), b.getName()) && equals(a.getValue(), b.getValue());
        }
    }

    public static boolean equals(Stamp a, Stamp b)
    {
        if (a == null && b == null)
        {
            return true;
        }
        else if ((a == null && b != null) || (a != null && b == null))
        {
            return false;
        }
        else
        {
            return equals(a.getAuthor(), b.getAuthor()) && equals(a.getModule(), b.getModule())
                    && equals(a.getPath(), b.getPath()) && equals(a.getStatus(), b.getStatus())
                    && equals(a.getTime(), b.getTime()) && equals(a.getUuid(), b.getUuid());
        }
    }

    public static boolean equals(Assertion a, Assertion b)
    {
        if (a == null && b == null)
        {
            return true;
        }
        else if ((a == null && b != null) || (a != null && b == null))
        {
            return false;
        }
        else
        {
            return a.getAssertionUUID().equals(b.getAssertionUUID()) && equals(a.getDiscernible(), b.getDiscernible())
                    && equals(a.getQualifier(), b.getQualifier()) && equals(a.getTiming(), b.getTiming())
                    && equals(a.getValue(), b.getValue())
                    && equals(a.getAssertionComponent(), b.getAssertionComponent());
        }
    }

    public static boolean equals(Type a, Type b)
    {
        if (a == null && b == null)
        {
            return true;
        }
        else if ((a == null && b != null) || (a != null && b == null))
        {
            return false;
        }

        return equals(a.getConcept(), b.getConcept());
    }

    public static boolean equals(Concept a, Concept b)
    {
        if (a == null && b == null)
        {
            return true;
        }
        else if ((a == null && b != null) || (a != null && b == null))
        {
            return false;
        }

        return equals(a.getDesc(), b.getDesc()) && equals(a.getUuid(), b.getUuid())
                && equals(a.getSctid(), b.getSctid());
    }

    public static boolean equals(Discernible a, Discernible b)
    {
        if (a == null && b == null)
        {
            return true;
        }
        else if ((a == null && b != null) || (a != null && b == null))
        {
            return false;
        }
        return equals(a.getExpression(), b.getExpression());
    }

    public static boolean equals(Qualifier a, Qualifier b)
    {
        if (a == null && b == null)
        {
            return true;
        }
        else if ((a == null && b != null) || (a != null && b == null))
        {
            return false;
        }
        return equals(a.getExpression(), b.getExpression());
    }

    public static boolean equals(Value a, Value b)
    {
        if (a == null && b == null)
        {
            return true;
        }
        else if ((a == null && b != null) || (a != null && b == null))
        {
            return false;
        }
        return equals(a.getExpression(), b.getExpression()) && equals(a.getText(), b.getText())
                && equals(a.getMeasurement(), b.getMeasurement()) && equals(a.isBoolean(), b.isBoolean());
    }

    public static boolean equals(Expression a, Expression b)
    {
        if (a == null && b == null)
        {
            return true;
        }
        else if ((a == null && b != null) || (a != null && b == null))
        {
            return false;
        }

        return equals(a.getConcept(), b.getConcept()) && equals(a.getExpression(), b.getExpression())
                && equals(a.getRelation(), b.getRelation()) && equals(a.getRelationGroup(), b.getRelationGroup());
    }

    public static boolean equals(Measurement a, Measurement b)
    {
        if (a == null && b == null)
        {
            return true;
        }
        else if ((a == null && b != null) || (a != null && b == null))
        {
            return false;
        }

        return equals(a.getInterval(), b.getInterval()) && equals(a.getPoint(), b.getPoint())
                && equals(a.getUnits(), b.getUnits());
    }

    public static boolean equals(Point a, Point b)
    {
        if (a == null && b == null)
        {
            return true;
        }
        else if ((a == null && b != null) || (a != null && b == null))
        {
            return false;
        }
        
        if (a instanceof PointDouble && b instanceof PointDouble)
        {
            return ((PointDouble)a).getValue() == ((PointDouble)b).getValue();
        }
        else if (a instanceof PointLong && b instanceof PointLong)
        {
            return ((PointLong)a).getValue() == ((PointLong)b).getValue();
        }
        else if (a instanceof PointMeasurementConstant && b instanceof PointMeasurementConstant)
        {
            return ((PointMeasurementConstant)a).getValue().equals(((PointMeasurementConstant)b).getValue());
        }
        //different types, or an unsupported type
        return false;
    }

    public static boolean equals(MeasurementConstant a, MeasurementConstant b)
    {
        if (a == null && b == null)
        {
            return true;
        }
        else if ((a == null && b != null) || (a != null && b == null))
        {
            return false;
        }

        return a.equals(b);
    }

    public static boolean equals(Units a, Units b)
    {
        if (a == null && b == null)
        {
            return true;
        }
        else if ((a == null && b != null) || (a != null && b == null))
        {
            return false;
        }
        return equals(a.getConcept(), b.getConcept());
    }

    public static boolean equals(Interval a, Interval b)
    {
        if (a == null && b == null)
        {
            return true;
        }
        else if ((a == null && b != null) || (a != null && b == null))
        {
            return false;
        }

        return equals(a.getLowerBound(), b.getLowerBound()) && equals(a.getUpperBound(), b.getUpperBound());
    }

    public static boolean equals(Bound a, Bound b)
    {
        if (a == null && b == null)
        {
            return true;
        }
        else if ((a == null && b != null) || (a != null && b == null))
        {
            return false;
        }
        return equals(a.isLowerPointInclusive(), b.isLowerPointInclusive()) &&
                equals(a.isUpperPointInclusive(), b.isUpperPointInclusive()) &&
                equals(a.getLowerPoint(), b.getLowerPoint()) && 
                equals(a.getUpperPoint(), b.getUpperPoint());
    }

    public static boolean equals(AssertionComponent a, AssertionComponent b)
    {
        if (a == null && b == null)
        {
            return true;
        }
        else if ((a == null && b != null) || (a != null && b == null))
        {
            return false;
        }
        return equals(a.getType(), b.getType()) && equals(a.getAssertionUUID(), b.getAssertionUUID());
    }

    public static boolean equals(Relation a, Relation b)
    {
        if (a == null && b == null)
        {
            return true;
        }
        else if ((a == null && b != null) || (a != null && b == null))
        {
            return false;
        }

        return equals(a.getType(), b.getType()) && equals(a.getDestination(), b.getDestination());
    }

    public static boolean equals(Destination a, Destination b)
    {
        if (a == null && b == null)
        {
            return true;
        }
        else if ((a == null && b != null) || (a != null && b == null))
        {
            return false;
        }

        return equals(a.isBoolean(), b.isBoolean()) && equals(a.getExpression(), b.getExpression())
                && equals(a.getMeasurement(), b.getMeasurement()) && equals(a.getText(), b.getText());
    }

    public static boolean equals(RelationGroup a, RelationGroup b)
    {
        if (a == null && b == null)
        {
            return true;
        }
        else if ((a == null && b != null) || (a != null && b == null))
        {
            return false;
        }

        return equals(a.getRelation(), b.getRelation());
    }

    public static boolean equals(List<?> a, List<?> b)
    {
        if (a.size() != b.size())
        {
            return false;
        }

        for (Object aItem : a)
        {
            boolean found = false;
            for (Object bItem : b)
            {
                if (listItemEquals(aItem, bItem))
                {
                    found = true;
                    break;
                }
            }
            if (!found)
            {
                return false;
            }
        }
        return true;
    }

    public static boolean listItemEquals(Object a, Object b)
    {
        if (!a.getClass().equals(b.getClass()))
        {
            return false;
        }
        if (a instanceof Assertion)
        {
            return equals((Assertion) a, (Assertion) b);
        }
        else if (a instanceof AssertionComponent)
        {
            return equals((AssertionComponent) a, (AssertionComponent) b);
        }
        else if (a instanceof Expression)
        {
            return equals((Expression) a, (Expression) b);
        }
        else if (a instanceof Relation)
        {
            return equals((Relation) a, (Relation) b);
        }
        else if (a instanceof RelationGroup)
        {
            return equals((RelationGroup) a, (RelationGroup) b);
        }
        else if (a instanceof Lego)
        {
            return equals((Lego) a, (Lego) b);
        }

        throw new RuntimeException("Unhandled equal type");
    }

    public static boolean equals(Float a, Float b)
    {
        if (a == null && b == null)
        {
            return true;
        }
        return a != null && a.equals(b);
    }

    public static boolean equals(String a, String b)
    {
        if (a == null && b == null)
        {
            return true;
        }
        return a != null && a.equals(b);
    }

    public static boolean equals(Long a, Long b)
    {
        if (a == null && b == null)
        {
            return true;
        }
        return a != null && a.equals(b);
    }

    public static boolean equals(Boolean a, Boolean b)
    {
        if (a == null && b == null)
        {
            return true;
        }
        return a != null && a.equals(b);
    }

    public static boolean equals(XMLGregorianCalendar a, XMLGregorianCalendar b)
    {
        if (a == null && b == null)
        {
            return true;
        }
        else
        {
            return a != null && a.equals(b);
        }
    }
}
