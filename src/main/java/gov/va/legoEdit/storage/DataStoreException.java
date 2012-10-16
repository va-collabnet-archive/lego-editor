package gov.va.legoEdit.storage;

/**
 *
 * @author darmbrust
 */
public class DataStoreException extends RuntimeException
{

    public DataStoreException()
    {
        super();
    }

    public DataStoreException(String message)
    {
        super(message);
    }

    public DataStoreException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public DataStoreException(Throwable cause)
    {
        super(cause);
    }
}
