package gov.va.legoEdit.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 *
 * @author darmbrust
 */
public class TimeConvert
{
    private static DatatypeFactory datatypeFactory_;
    private static DateFormat dateFormat_ = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    static 
    {
        try
        {
            datatypeFactory_ = DatatypeFactory.newInstance();
        }
        catch (Exception e)
        {
            throw new RuntimeException("Expected to be impossible", e);
        }
    }

    public static XMLGregorianCalendar convert(long time)
    {
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTimeInMillis(time);
        return datatypeFactory_.newXMLGregorianCalendar(gc);
    }
    
    public static long convert(XMLGregorianCalendar gc)
    {
        return gc.toGregorianCalendar().getTimeInMillis();
    }
    
    public static String format(XMLGregorianCalendar gc)
    {
        return format(gc.toGregorianCalendar().getTimeInMillis());
    }
    
    public static String format(long time)
    {
        return dateFormat_.format(new Date(time));
    }
}
