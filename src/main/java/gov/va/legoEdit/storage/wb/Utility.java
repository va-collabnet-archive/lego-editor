package gov.va.legoEdit.storage.wb;

import gov.va.legoEdit.model.schemaModel.Concept;
import java.io.IOException;
import java.util.UUID;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.id.IdBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Utility
{
    //Should be the first, but isn't... not sure why.
    //TODO file bug
    //private static UUID snomedIdType = UUID.fromString("0418a591-f75b-39ad-be2c-3ab849326da9");  //SNOMED integer id
    private static UUID snomedIdType = UUID.fromString("f7495b58-6630-3499-a44e-2052b5fcf06c");  //user
    private static Integer snomedIdTypeNid = null;
    
    private static Logger logger = LoggerFactory.getLogger(Utility.class);
    
    public static Concept lookupSnomedIdentifier(String identifier)
    {
        Concept c = null;
        ConceptChronicleBI result = null;
        try
        {
            UUID uuid = UUID.fromString(identifier);
            result = WBDataStore.Ts().getConcept(uuid);
        }
        catch (IllegalArgumentException | IOException e)
        {
            //try looking up by ID 
            try
            {
                result = WBDataStore.Ts().getConceptFromAlternateId(snomedIdType, identifier);
            }
            catch (IOException e1)
            {
                // noop
            }
        }
        if (result != null && result.getUUIDs().size() > 0)
        {
            c = new Concept();
            c.setDesc(result.toUserString());
            c.setUuid(result.getUUIDs().get(0).toString());
            try
            {
                for (IdBI x : result.getAdditionalIds())
                {
                    //TODO this will likely get the wrong value, because the DB is returning multiple authorities all with the same type... something hosed.
                    if (x.getAuthorityNid() == getSnomedIdTypeNid())
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
    
    //TODO Keiths getText() method on FxConcept returns a random synonym... need to write a utility method to dig out the FSN instead. 
}
