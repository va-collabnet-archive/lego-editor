package gov.va.legoEdit.storage;

import java.util.Iterator;

/**
 * An iterator that allows callers to call close() when they are finished with the iterator, 
 * so that the data source for the iterator can release the underlying resources.
 * 
 * A runtime IteratorClosedException is thrown if you try to read a closed iterator.
 * 
 * @author darmbrust
 */
public interface CloseableIterator<E> extends Iterator<E>
{
    /**
     * Clients of the iterator should call close() to release resources in the case where they 
     * don't iterate through all of the items in the iterator.
     */
    public void close();
    
    @Override
    public E next() throws IteratorClosedException;
    
    @Override
    public boolean hasNext() throws IteratorClosedException;
}
