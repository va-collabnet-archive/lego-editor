package gov.va.legoEdit.storage.wb;

import gov.va.legoEdit.model.schemaModel.Concept;
import gov.va.legoEdit.util.Utility;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.ihtsdo.fxmodel.concept.FxConcept;
import org.ihtsdo.fxmodel.concept.component.description.FxDescriptionChronicle;
import org.ihtsdo.fxmodel.concept.component.description.FxDescriptionVersion;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.StandardViewCoordinates;
import org.ihtsdo.tk.api.description.DescriptionChronicleBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.id.IdBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WBUtility
{
    //TODO WB BUG - fix after https://csfe.aceworkspace.net/sf/go/artf227370
    //private static UUID snomedIdType = UUID.fromString("0418a591-f75b-39ad-be2c-3ab849326da9");  //SNOMED integer id
    private static UUID snomedIdType = UUID.fromString("f7495b58-6630-3499-a44e-2052b5fcf06c");  //user
    private static Integer snomedIdTypeNid = null;
    private static UUID FSN_UUID = UUID.fromString("00791270-77c9-32b6-b34f-d932569bd2bf");
    private static Integer FSNTypeNid = null;
    private static UUID ACTIVE_VALUE_UUID = UUID.fromString("d12702ee-c37f-385f-a070-61d56d4d0f1f");
    private static Integer ActiveValueTypeNid = null;
    
    
    private static Logger logger = LoggerFactory.getLogger(Utility.class);
    
    
    private static BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>();
    private static ThreadPoolExecutor tpe = new ThreadPoolExecutor(5, 5, 60, TimeUnit.SECONDS, workQueue, new ThreadFactory()
    {
        @Override
        public Thread newThread(Runnable r)
        {
            Thread t = Executors.defaultThreadFactory().newThread(r);
            t.setDaemon(true);
            return t;
        }
    });
    
    
    public static void lookupSnomedIdentifier(final String identifier, final ConceptLookupCallback callback)
    {
        Runnable r = new Runnable()
        {
            @Override
            public void run()
            {
                Concept c = lookupSnomedIdentifier(identifier);
                callback.lookupComplete(c);
            }
        };
        tpe.execute(r);
    }
    
    public static Concept lookupSnomedIdentifier(String identifier)
    {
        Concept c = null;
        ConceptVersionBI result = lookupSnomedIdentifierAsCV(identifier);
        if (result != null && result.getUUIDs().size() > 0)
        {
            c = new Concept();
            c.setDesc(getFSN(result));
            c.setUuid(result.getUUIDs().get(0).toString());
            try
            {
                for (IdBI x : result.getAdditionalIds())
                {
                    if (x.getAuthorityNid() == getSnomedIdTypeNid() && Utility.isLong(x.getDenotation().toString()))
                    {
                        c.setSctid(Long.parseLong(x.getDenotation().toString()));
                        break;
                    }
                }
            }
            catch (Exception e)
            {
                // noop
            }
        }
        return c;
    }
   
    public static ConceptVersionBI lookupSnomedIdentifierAsCV(String identifier)
    {
        if (identifier == null)
        {
            return null;
        }
        
        ConceptVersionBI result = null;
        try
        {
            UUID uuid = UUID.fromString(identifier.trim());
            result = WBDataStore.Ts().getConceptVersion(StandardViewCoordinates.getSnomedLatest(), uuid);
            if (result.getUUIDs().size() == 0)
            {
                //This is garbage that the moronic WB API invented.  Nothing like an undocumented getter which, rather than returning null when the thing
                //you are asking for doesn't exist - it goes off and returns essentially a new, empty, useless node.  Sigh.
                throw new IllegalArgumentException();
            }
        }
        catch (IllegalArgumentException | IOException e)
        {
            //try looking up by ID 
            try
            {
                result = WBDataStore.Ts().getConceptVersionFromAlternateId(StandardViewCoordinates.getSnomedLatest(), snomedIdType, identifier.trim());
                if (result.getUUIDs().size() == 0)
                {
                    //This is garbage that the moronic WB API invented.  Nothing like an undocumented getter which, rather than returning null when the thing
                    //you are asking for doesn't exist - it goes off and returns essentially a new, empty, useless node.  Sigh.
                    result = null;
                }
            }
            catch (IOException e1)
            {
                // noop
            }
        }
        return result;
    }
    
    private static int getSnomedIdTypeNid()
    {
        if (snomedIdTypeNid == null)
        {
            try
            {
                snomedIdTypeNid = WBDataStore.Ts().getNidForUuids(snomedIdType);
            }
            catch (IOException e)
            {
                logger.error("Couldn't find nid for snomed id UUID", e);
                snomedIdTypeNid = -1;
            }
        }
        return snomedIdTypeNid;
    }
    
    private static int getFSNTypeNid()
    {
        if (FSNTypeNid == null)
        {
            try
            {
                FSNTypeNid = WBDataStore.Ts().getNidForUuids(FSN_UUID);
            }
            catch (IOException e)
            {
                logger.error("Couldn't find nid for FSN UUID", e);
                FSNTypeNid = -1;
            }
        }
        return FSNTypeNid;
    }
    
    private static int getActiveValueTypeNid()
    {
        if (ActiveValueTypeNid == null)
        {
            try
            {
                ActiveValueTypeNid = WBDataStore.Ts().getNidForUuids(ACTIVE_VALUE_UUID);
            }
            catch (IOException e)
            {
                logger.error("Couldn't find nid for Active Value UUID", e);
                ActiveValueTypeNid = -1;
            }
        }
        return ActiveValueTypeNid;
    }
    
    /**
     * Note, this method isn't smart enough to work with multiple versions properly....
     * assumes you only pass in a concept with current values
     */
    public static String getFSN(ConceptVersionBI concept)
    {
        String bestFound = null;
        try
        {
            for (DescriptionChronicleBI desc : concept.getDescs())
            {
                DescriptionVersionBI<?> descVer = desc.getVersions().toArray(new DescriptionVersionBI[desc.getVersions().size()])[desc.getVersions().size() - 1];
                
                if (descVer.getTypeNid() == getFSNTypeNid())
                {
                    if (descVer.getStatusNid() == getActiveValueTypeNid())
                    {
                        return descVer.getText();
                    }
                    else
                    {
                        bestFound = descVer.getText();
                    }
                }
            }
        }
        catch (IOException  e)
        {
            //noop
        }
        return (bestFound == null ? concept.toUserString() : bestFound);
    }
    
    public static String getFSN(FxConcept concept)
    {
        //Go hunting for a FSN
        if (concept.getDescriptions() == null)
        {
            return concept.getConceptReference().getText();
        }
        String bestFound = null;
        for (FxDescriptionChronicle d : concept.getDescriptions())
        {
            FxDescriptionVersion dv = d.getVersions().get(d.getVersions().size() - 1);
            if (dv.getTypeReference().getUuid().equals(FSN_UUID))
            {
                if (dv.getStatusReference().getUuid().equals(ACTIVE_VALUE_UUID))
                {
                    return dv.getText();
                }
                else
                {
                    bestFound = dv.getText();
                }
            }
        }
        return (bestFound == null ? concept.getConceptReference().getText() : bestFound);
    }
}
