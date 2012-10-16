package gov.va.legoEdit.util;

import java.util.GregorianCalendar;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 *
 * @author darmbrust
 */
public class TimeConvert
{
    private static DatatypeFactory df;
    static {
        try
        {
            df = DatatypeFactory.newInstance();
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
        return df.newXMLGregorianCalendar(gc);
    }
    
    public static long convert(XMLGregorianCalendar gc)
    {
        return gc.toGregorianCalendar().getTimeInMillis();
    }
}
