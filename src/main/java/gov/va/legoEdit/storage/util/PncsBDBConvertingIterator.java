package gov.va.legoEdit.storage.util;

import gov.va.legoEdit.model.bdbModel.PncsBDB;
import gov.va.legoEdit.model.schemaModel.Pncs;
import gov.va.legoEdit.storage.CloseableIterator;

import java.util.concurrent.ScheduledExecutorService;

import com.sleepycat.persist.EntityCursor;

/**
 *
 * @author darmbrust
 */
public class PncsBDBConvertingIterator implements CloseableIterator<Pncs>
{

    private BDBIterator<PncsBDB> iter;

    public PncsBDBConvertingIterator(ScheduledExecutorService sec, EntityCursor<PncsBDB> c)
    {
        iter = new BDBIterator<>(sec, c);
    }

    @Override
    public Pncs next()
    {
        return iter.next().toSchemaPncs();
    }

    @Override
    public boolean hasNext()
    {
        return iter.hasNext();
    }

    @Override
    public void remove()
    {
        iter.remove();
    }

    @Override
    public void close()
    {
        iter.close();
    }
}
