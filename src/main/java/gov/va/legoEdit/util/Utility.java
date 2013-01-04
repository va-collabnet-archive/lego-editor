package gov.va.legoEdit.util;

public class Utility
{
    public static boolean isLong(String string)
    {
        try
        {
            Long.parseLong(string);
            return true;
        }
        catch (NumberFormatException e)
        {
             return false;
        }
    }
    
    public static boolean isEmpty(String string)
    {
        if (string == null || string.length() == 0)
        {
            return true;
        }
        return false;
    }
    
    public static boolean isEqual(String a, String b)
    {
        if (a == null)
        {
            return (b == null ? true : false);
        }
        if (b == null)
        {
            return (a == null ? true : false);
        }
        return a.equals(b);
    }
}
