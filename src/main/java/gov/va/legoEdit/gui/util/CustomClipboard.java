package gov.va.legoEdit.gui.util;

import gov.va.legoEdit.model.SchemaClone;
import gov.va.legoEdit.model.SchemaToString;
import gov.va.legoEdit.model.schemaModel.Assertion;
import gov.va.legoEdit.model.schemaModel.Discernible;
import gov.va.legoEdit.model.schemaModel.Expression;
import gov.va.legoEdit.model.schemaModel.Lego;
import gov.va.legoEdit.model.schemaModel.Qualifier;
import gov.va.legoEdit.model.schemaModel.Value;
import java.util.ArrayList;
import javafx.beans.binding.Binding;
import javafx.beans.binding.BooleanBinding;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;


public class CustomClipboard
{
    
    private final static Clipboard clipboard_ = Clipboard.getSystemClipboard();
    private static Object object_ = null;
    private static DataFormat type_ = new DataFormat("type");
    
    private static ArrayList<Binding<Boolean>> bindings_ = new ArrayList<Binding<Boolean>>();
    
    public static BooleanBinding containsAssertion = new BooleanBinding()
    {
        @Override
        protected boolean computeValue()
        {
            return containsType(Assertion.class);
        }
    };
    
    public static BooleanBinding containsString = new BooleanBinding()
    {
        @Override
        protected boolean computeValue()
        {
            return object_ == null && clipboard_.getString() != null;
        }
    };
    
    public static BooleanBinding containsValue = new BooleanBinding()
    {
        @Override
        protected boolean computeValue()
        {
            return containsType(Value.class);
        }
    };
    
    public static BooleanBinding containsExpression = new BooleanBinding()
    {
        @Override
        protected boolean computeValue()
        {
            return containsType(Expression.class);
        }
    };
    
    public static BooleanBinding containsDiscernible = new BooleanBinding()
    {
        @Override
        protected boolean computeValue()
        {
            return containsType(Discernible.class);
        }
    };
    
    public static BooleanBinding containsQualifier = new BooleanBinding()
    {
        @Override
        protected boolean computeValue()
        {
            return containsType(Qualifier.class);
        }
    };
    
    public static BooleanBinding containsLego = new BooleanBinding()
    {
        @Override
        protected boolean computeValue()
        {
            return containsType(Lego.class);
        }
    };
    
    static
    {
        bindings_.add(containsAssertion);
        bindings_.add(containsValue);
        bindings_.add(containsDiscernible);
        bindings_.add(containsQualifier);
        bindings_.add(containsExpression);
        bindings_.add(containsString);
        bindings_.add(containsLego);
    }
    
    public static void set(Lego l)
    {
        set(SchemaClone.clone(l), SchemaToString.toString(l));
    }
    
    public static void set(Assertion a)
    {
        set(SchemaClone.clone(a), SchemaToString.toString(a, ""));
    }
    
    public static void set(Value v)
    {
        set(SchemaClone.clone(v), SchemaToString.toString(v, ""));
    }
    
    public static void set(Expression e)
    {
        set(SchemaClone.clone(e), SchemaToString.toString(e, ""));
    }
    
    public static void set(Discernible d)
    {
        set(SchemaClone.clone(d), SchemaToString.toString(d, ""));
    }
    
    public static void set(Qualifier q)
    {
        set(SchemaClone.clone(q), SchemaToString.toString(q, ""));
    }
    
    private static void set(Object value, String stringValue)
    {
       object_ = value;
       ClipboardContent cc = new ClipboardContent();
       cc.putString(stringValue);
       cc.put(type_, value.getClass().getName());
       clipboard_.setContent(cc);
       updateBindings();
    }
    
    public static void set(String s)
    {
        object_ = null;
        ClipboardContent cc = new ClipboardContent();
        cc.putString(s);
        clipboard_.setContent(cc);
        updateBindings();
    }
    
    public static void updateBindings()
    {
        for (Binding<Boolean> b : bindings_)
        {
            b.invalidate();
        }
    }
    
    
    public static boolean containsType(Class<?> clazz)
    {
        String value = (String)clipboard_.getContent(type_);
        if (value == null)
        {
            return false;
        }
        else
        {
            return value.equals(clazz.getName());
        }
    }
    
    public static String getString()
    {
        if (containsString.get())
        {
            return clipboard_.getString();
        }
        return null;
    }
    
    public static Lego getLego()
    {
        if (containsLego.get())
        {
            return SchemaClone.clone((Lego)object_);
        }

        return null;
    }
    
    public static Assertion getAssertion()
    {
        if (containsAssertion.get())
        {
            return SchemaClone.clone((Assertion)object_);
        }

        return null;
    }
    
    public static Value getValue()
    {
        if (containsValue.get())
        {
            return SchemaClone.clone((Value)object_);
        }

        return null;
    }
    
    public static Expression getExpression()
    {
        if (containsExpression.get())
        {
            return SchemaClone.clone((Expression)object_);
        }

        return null;
    }
    
    public static Discernible getDiscernible()
    {
        if (containsDiscernible.get())
        {
            return SchemaClone.clone((Discernible)object_);
        }

        return null;
    }
    
    public static Qualifier getQualifier()
    {
        if (containsQualifier.get())
        {
            return SchemaClone.clone((Qualifier)object_);
        }

        return null;
    }
}
