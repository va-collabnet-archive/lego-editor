package gov.va.legoEdit.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import gov.va.legoEdit.formats.LegoXMLUtils;
import gov.va.legoEdit.model.SchemaEquals;
import gov.va.legoEdit.model.bdbModel.PncsBDB;
import gov.va.legoEdit.model.schemaModel.Assertion;
import gov.va.legoEdit.model.schemaModel.AssertionComponent;
import gov.va.legoEdit.model.schemaModel.Concept;
import gov.va.legoEdit.model.schemaModel.Discernible;
import gov.va.legoEdit.model.schemaModel.Expression;
import gov.va.legoEdit.model.schemaModel.Lego;
import gov.va.legoEdit.model.schemaModel.LegoList;
import gov.va.legoEdit.model.schemaModel.Pncs;
import gov.va.legoEdit.model.schemaModel.Qualifier;
import gov.va.legoEdit.model.schemaModel.Stamp;
import gov.va.legoEdit.model.schemaModel.Value;
import gov.va.legoEdit.storage.util.BDBIterator;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import javax.xml.bind.JAXBException;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * 
 * @author darmbrust
 */
public class BDBDataStoreTest
{
	public static int id = 0;

	@BeforeClass
	public static void oneTimeSetUp() throws Exception
	{
		BDBDataStoreImpl.dbFolderPath = new File("testDB");
		FileUtils.deleteDirectory(BDBDataStoreImpl.dbFolderPath);
	}

	@AfterClass
	public static void oneTimeTearDown() throws Exception
	{
		BDBDataStoreImpl.getInstance().shutdown();
	}

	@Before
	public void setUp() throws Exception
	{
		clearDB();
		assertFalse("Datastore isn't empty!", BDBDataStoreImpl.getInstance().getLegoLists().hasNext());
	}

	@After
	public void tearDown() throws Exception
	{
		clearDB();
	}

	private void clearDB() throws WriteException
	{
		CloseableIterator<LegoList> iterator = BDBDataStoreImpl.getInstance().getLegoLists();
		ArrayList<String> toDelete = new ArrayList<>();
		while (iterator.hasNext())
		{
			LegoList ll = iterator.next();
			toDelete.add(ll.getLegoListUUID());
		}
		for (String s : toDelete)
		{
			BDBDataStoreImpl.getInstance().deleteLegoList(s);
		}
	}

	@Test
	public void testLegoListStorage() throws WriteException
	{
		BDBDataStoreImpl.getInstance().createLegoList(UUID.randomUUID().toString(), "a", "a description", null);
		BDBDataStoreImpl.getInstance().createLegoList(UUID.randomUUID().toString(), "b", "b description", null);
		BDBDataStoreImpl.getInstance().createLegoList(UUID.randomUUID().toString(), "c", "c description", null);

		CloseableIterator<LegoList> iterator = BDBDataStoreImpl.getInstance().getLegoLists();
		int count = 0;
		ArrayList<String> uuids = new ArrayList<>();
		while (iterator.hasNext())
		{
			LegoList ll = iterator.next();
			uuids.add(ll.getLegoListUUID());
			count++;
		}
		org.junit.Assert.assertEquals("Did not get all 3 LegoLists back", count, 3);

		assertNotNull("Did not find legoList by name", BDBDataStoreImpl.getInstance().getLegoListByName("a"));
		assertNotNull("Did not find legoList by name", BDBDataStoreImpl.getInstance().getLegoListByName("b"));
		assertNotNull("Did not find legoList by name", BDBDataStoreImpl.getInstance().getLegoListByName("c"));

		for (String s : uuids)
		{
			assertNotNull("Did not find legoList by UUID", BDBDataStoreImpl.getInstance().getLegoListByID(s));
		}
	}

	@Test
	public void testLegoListSearchByLegoId() throws WriteException
	{
		BDBDataStoreImpl.getInstance().createLegoList(UUID.randomUUID().toString(), "a", "a description", null);
		BDBDataStoreImpl.getInstance().createLegoList(UUID.randomUUID().toString(), "b", "b description", null);
		BDBDataStoreImpl.getInstance().createLegoList(UUID.randomUUID().toString(), "c", "c description", null);

		String aId = BDBDataStoreImpl.getInstance().getLegoListByName("a").getLegoListUUID();

		Lego l1 = new Lego();
		l1.setLegoUUID(UUID.nameUUIDFromBytes("foo".getBytes()).toString());
		l1.setPncs(makeUniquePncs());
		BDBDataStoreImpl.getInstance().commitLego(l1, aId);

		Lego l2 = new Lego();
		l2.setLegoUUID(UUID.nameUUIDFromBytes("bar".getBytes()).toString());
		l2.setPncs(makeUniquePncs());
		BDBDataStoreImpl.getInstance().commitLego(l2, aId);

		List<String> ll = BDBDataStoreImpl.getInstance().getLegoListByLego(UUID.randomUUID().toString());
		assertEquals("This should not find any", ll.size(), 0);

		ll = BDBDataStoreImpl.getInstance().getLegoListByLego(UUID.nameUUIDFromBytes("foo".getBytes()).toString());
		assertEquals("Wrong number of results", ll.size(), 1);
		assertEquals("Didn't find expected Lego List", aId, ll.get(0));

		ll = BDBDataStoreImpl.getInstance().getLegoListByLego(UUID.nameUUIDFromBytes("bar".getBytes()).toString());
		assertEquals("Wrong number of results", ll.size(), 1);
		assertEquals("Didn't find expected Lego List", aId, ll.get(0));

	}

	private Pncs makeUniquePncs()
	{
		Pncs pncs = new Pncs();
		pncs.setId(id++);
		pncs.setValue(pncs.getId() + " value");
		pncs.setName(pncs.getId() + " name");
		return pncs;
	}

	@Test
	public void testLegoSearch() throws WriteException
	{
		BDBDataStoreImpl.getInstance().createLegoList(UUID.randomUUID().toString(), "a", "a description", null);
		BDBDataStoreImpl.getInstance().createLegoList(UUID.randomUUID().toString(), "b", "b description", null);
		BDBDataStoreImpl.getInstance().createLegoList(UUID.randomUUID().toString(), "c", "c description", null);

		String aId = BDBDataStoreImpl.getInstance().getLegoListByName("a").getLegoListUUID();

		Lego l1 = new Lego();
		l1.setLegoUUID(UUID.nameUUIDFromBytes("foo".getBytes()).toString());
		l1.setPncs(makeUniquePncs());
		BDBDataStoreImpl.getInstance().commitLego(l1, aId);

		assertEquals("Failed to find lego", BDBDataStoreImpl.getInstance().getLegos(UUID.nameUUIDFromBytes("foo".getBytes()).toString()).size(), 1);

		// Commit it again - should now exists twice - with different stamp values
		BDBDataStoreImpl.getInstance().commitLego(l1, aId);
		assertEquals("Wrong number of legos found", BDBDataStoreImpl.getInstance().getLegos(UUID.nameUUIDFromBytes("foo".getBytes()).toString()).size(), 2);
	}

	@Test
	public void testLegoSearchBySCTConcept() throws WriteException
	{
		BDBDataStoreImpl.getInstance().createLegoList(UUID.randomUUID().toString(), "a", "a description", null);
		BDBDataStoreImpl.getInstance().createLegoList(UUID.randomUUID().toString(), "b", "b description", null);
		BDBDataStoreImpl.getInstance().createLegoList(UUID.randomUUID().toString(), "c", "c description", null);

		String aId = BDBDataStoreImpl.getInstance().getLegoListByName("a").getLegoListUUID();
		Lego l1 = new Lego();
		l1.setLegoUUID(UUID.nameUUIDFromBytes("foo".getBytes()).toString());
		l1.setPncs(makeUniquePncs());

		Assertion a = new Assertion();
		a.setAssertionUUID(UUID.randomUUID().toString());

		Discernible d = new Discernible();
		Expression e = new Expression();
		Concept c = new Concept();
		c.setDesc("foo");
		c.setSctid(5l);
		e.setConcept(c);
		d.setExpression(e);
		a.setDiscernible(d);

		Value v = new Value();
		Expression e1 = new Expression();
		Concept c1 = new Concept();
		c1.setDesc("bar");
		String knownUUID = UUID.randomUUID().toString();
		c1.setUuid(knownUUID);
		e1.setConcept(c1);
		v.setExpression(e1);
		a.setValue(v);

		Qualifier q = new Qualifier();
		Expression e2 = new Expression();
		Concept c2 = new Concept();
		c2.setDesc("bar");
		c2.setSctid(99l);
		e2.setConcept(c2);
		q.setExpression(e2);
		a.setQualifier(q);

		l1.getAssertion().add(a);

		// TODO TEST need to add more options so that we verify that it finds SCT Ids at any point in the hierarcy where they can be hidden away.

		BDBDataStoreImpl.getInstance().commitLego(l1, aId);

		assertEquals("List not empty", BDBDataStoreImpl.getInstance().getLegosContainingConceptIdentifiers("7").size(), 0);
		assertEquals("List not empty", BDBDataStoreImpl.getInstance().getLegosContainingConceptIdentifiers(UUID.randomUUID().toString()).size(), 0);

		assertEquals("Didn't find lego", BDBDataStoreImpl.getInstance().getLegosContainingConceptIdentifiers(knownUUID).get(0).getLegoUUID(),
				UUID.nameUUIDFromBytes("foo".getBytes()).toString());

		assertEquals("Didn't find lego", BDBDataStoreImpl.getInstance().getLegosContainingConceptIdentifiers("5").get(0).getLegoUUID(),
				UUID.nameUUIDFromBytes("foo".getBytes()).toString());

		assertEquals("Didn't find lego", BDBDataStoreImpl.getInstance().getLegosContainingConceptIdentifiers("99").get(0).getLegoUUID(),
				UUID.nameUUIDFromBytes("foo".getBytes()).toString());
		// TODO TEST add test for multisearch
	}

	@Test
	public void testLegoSearchByUsingAssertion() throws WriteException
	{
		Lego l1 = new Lego();
		l1.setLegoUUID(UUID.nameUUIDFromBytes("foo".getBytes()).toString());
		l1.setPncs(makeUniquePncs());

		Assertion a = createAssertion();
		String knownAssertionId = a.getAssertionUUID();
		a.getAssertionComponent().add(makeAssertionComponent("fred"));
		a.getAssertionComponent().add(makeAssertionComponent("jane"));
		a.getAssertionComponent().add(makeAssertionComponent("mary"));

		l1.getAssertion().add(a);

		BDBDataStoreImpl.getInstance().commitLego(l1,
				BDBDataStoreImpl.getInstance().createLegoList(UUID.randomUUID().toString(), "a", "a description", null).getLegoListUUID());

		Lego l2 = new Lego();
		l2.setLegoUUID(UUID.nameUUIDFromBytes("bar".getBytes()).toString());
		l2.setPncs(makeUniquePncs());

		a = createAssertion();
		a.getAssertionComponent().add(makeAssertionComponent("fred"));
		a.getAssertionComponent().add(makeAssertionComponent("joy"));
		a.getAssertionComponent().add(makeAssertionComponent("james"));

		l2.getAssertion().add(a);

		BDBDataStoreImpl.getInstance().commitLego(l2,
				BDBDataStoreImpl.getInstance().createLegoList(UUID.randomUUID().toString(), "b", "b description", null).getLegoListUUID());
		BDBDataStoreImpl.getInstance().createLegoList(UUID.randomUUID().toString(), "c", "c description", null);

		assertEquals("Didn't find lego", BDBDataStoreImpl.getInstance().getLegosUsingAssertion("jane").get(0).getLegoUUID(), UUID.nameUUIDFromBytes("foo".getBytes())
				.toString());
		assertEquals("Didn't find lego", BDBDataStoreImpl.getInstance().getLegosUsingAssertion("james").get(0).getLegoUUID(), UUID.nameUUIDFromBytes("bar".getBytes())
				.toString());
		assertEquals("Didn't find right number of legos", BDBDataStoreImpl.getInstance().getLegosUsingAssertion("fred").size(), 2);
		assertEquals("Shouldn't have found a lego", BDBDataStoreImpl.getInstance().getLegosUsingAssertion("foo").size(), 0);
		assertEquals("Shouldn't have found a lego", BDBDataStoreImpl.getInstance().getLegosUsingAssertion(knownAssertionId).size(), 0);

		// recommit a lego (new stamp)
		BDBDataStoreImpl.getInstance().commitLego(l2, BDBDataStoreImpl.getInstance().getLegoListByName("b").getLegoListUUID());
		assertEquals("Didn't find lego", BDBDataStoreImpl.getInstance().getLegosUsingAssertion("jane").get(0).getLegoUUID(), UUID.nameUUIDFromBytes("foo".getBytes())
				.toString());
		assertEquals("Didn't find lego", BDBDataStoreImpl.getInstance().getLegosUsingAssertion("james").get(0).getLegoUUID(), UUID.nameUUIDFromBytes("bar".getBytes())
				.toString());
		assertEquals("Didn't find lego", BDBDataStoreImpl.getInstance().getLegosUsingAssertion("james").get(1).getLegoUUID(), UUID.nameUUIDFromBytes("bar".getBytes())
				.toString());
		assertEquals("Didn't find right number of legos", BDBDataStoreImpl.getInstance().getLegosUsingAssertion("fred").size(), 3);
		assertEquals("Shouldn't have found a lego", BDBDataStoreImpl.getInstance().getLegosUsingAssertion("foo").size(), 0);
		assertEquals("Shouldn't have found a lego", BDBDataStoreImpl.getInstance().getLegosUsingAssertion(knownAssertionId).size(), 0);

	}

	private AssertionComponent makeAssertionComponent(String assertionUUID)
	{
		AssertionComponent ac = new AssertionComponent();
		ac.setAssertionUUID(assertionUUID);
		return ac;
	}

	private Assertion createAssertion()
	{
		Assertion a = new Assertion();
		a.setAssertionUUID(UUID.randomUUID().toString());

		Discernible d = new Discernible();
		Expression e = new Expression();
		Concept c = new Concept();
		c.setDesc("foo");
		c.setSctid(5l);
		e.setConcept(c);
		d.setExpression(e);
		a.setDiscernible(d);

		Value v = new Value();
		Expression e1 = new Expression();
		Concept c1 = new Concept();
		c1.setDesc("bar");
		c1.setUuid(UUID.randomUUID().toString());
		e1.setConcept(c1);
		v.setExpression(e1);
		a.setValue(v);

		Qualifier q = new Qualifier();
		Expression e2 = new Expression();
		Concept c2 = new Concept();
		c2.setDesc("me");
		c2.setSctid(99l);
		e2.setConcept(c2);
		q.setExpression(e2);
		a.setQualifier(q);
		return a;
	}

	@Test
	public void testPncsSearch() throws WriteException
	{
		BDBDataStoreImpl.getInstance().createLegoList(UUID.randomUUID().toString(), "a", "a description", null);
		BDBDataStoreImpl.getInstance().createLegoList(UUID.randomUUID().toString(), "b", "b description", null);
		BDBDataStoreImpl.getInstance().createLegoList(UUID.randomUUID().toString(), "c", "c description", null);

		String aId = BDBDataStoreImpl.getInstance().getLegoListByName("a").getLegoListUUID();

		Lego l1 = new Lego();
		l1.setLegoUUID(UUID.nameUUIDFromBytes("foo".getBytes()).toString());
		l1.setPncs(makeUniquePncs());
		Pncs knownPncs1 = l1.getPncs();
		BDBDataStoreImpl.getInstance().commitLego(l1, aId);

		Lego l2 = new Lego();
		l2.setLegoUUID(UUID.nameUUIDFromBytes("foo".getBytes()).toString());
		Pncs knownPncs2 = new Pncs();
		knownPncs2.setId(knownPncs1.getId());
		knownPncs2.setName(knownPncs1.getName());
		knownPncs2.setValue("testVal");
		l2.setPncs(knownPncs2);
		BDBDataStoreImpl.getInstance().commitLego(l2, aId);

		List<Lego> ls = BDBDataStoreImpl.getInstance().getLegosForPncs(knownPncs1.getId());
		assertEquals("Wrong number of legos", ls.size(), 2);
		for (Lego l : ls)
		{
			assertEquals("wrong Lego", l.getLegoUUID(), UUID.nameUUIDFromBytes("foo".getBytes()).toString());
		}

		ls = BDBDataStoreImpl.getInstance().getLegosForPncs(knownPncs1.getId(), "testVal");
		assertEquals("Wrong number of legos", ls.size(), 1);
		for (Lego l : ls)
		{
			assertEquals("wrong Lego", l.getLegoUUID(), UUID.nameUUIDFromBytes("foo".getBytes()).toString());
		}

		assertEquals("Shouldn't have found a lego", 0, BDBDataStoreImpl.getInstance().getLegosForPncs(knownPncs1.getId(), "something").size());
		assertEquals("Shouldn't have found a lego", 0, BDBDataStoreImpl.getInstance().getLegosForPncs(55).size());

		Iterator<Pncs> i = BDBDataStoreImpl.getInstance().getPncs();
		int count = 0;
		while (i.hasNext())
		{
			i.next();
			count++;
		}
		assertEquals(2, count);
	}

	@Test
	public void testPncslookup() throws WriteException
	{
		BDBDataStoreImpl.getInstance().createLegoList(UUID.randomUUID().toString(), "a", "a description", null);
		BDBDataStoreImpl.getInstance().createLegoList(UUID.randomUUID().toString(), "b", "b description", null);
		BDBDataStoreImpl.getInstance().createLegoList(UUID.randomUUID().toString(), "c", "c description", null);

		String aId = BDBDataStoreImpl.getInstance().getLegoListByName("a").getLegoListUUID();

		Lego l1 = new Lego();
		l1.setLegoUUID(UUID.nameUUIDFromBytes("foo".getBytes()).toString());
		l1.setPncs(makeUniquePncs());
		Pncs knownPncs1 = l1.getPncs();
		BDBDataStoreImpl.getInstance().commitLego(l1, aId);

		Lego l2 = new Lego();
		l2.setLegoUUID(UUID.nameUUIDFromBytes("foo".getBytes()).toString());
		Pncs knownPncs2 = new Pncs();
		knownPncs2.setId(knownPncs1.getId());
		knownPncs2.setName(knownPncs1.getName());
		knownPncs2.setValue("testVal");
		l2.setPncs(knownPncs2);
		BDBDataStoreImpl.getInstance().commitLego(l2, aId);

		Lego l3 = new Lego();
		l3.setLegoUUID(UUID.nameUUIDFromBytes("bar".getBytes()).toString());
		l3.setPncs(makeUniquePncs());
		Pncs knownPncs3 = l3.getPncs();
		Stamp l3LegoStamp = BDBDataStoreImpl.getInstance().commitLego(l3, aId);

		assertEquals("Wrong number of PNCS items found", 2, BDBDataStoreImpl.getInstance().getPncs(knownPncs1.getId()).size());
		assertEquals("Wrong number of PNCS items found", 2, BDBDataStoreImpl.getInstance().getPncs(knownPncs2.getId()).size());
		assertEquals("Wrong number of PNCS items found", 1, BDBDataStoreImpl.getInstance().getPncs(knownPncs3.getId()).size());

		Iterator<Pncs> i = BDBDataStoreImpl.getInstance().getPncs();
		int count = 0;
		while (i.hasNext())
		{
			i.next();
			count++;
		}
		assertEquals(3, count);

		BDBDataStoreImpl.getInstance().deleteLego(aId, l3.getLegoUUID(), l3LegoStamp.getUuid());

		assertEquals("Wrong number of PNCS items found", 2, BDBDataStoreImpl.getInstance().getPncs(knownPncs1.getId()).size());
		assertEquals("Wrong number of PNCS items found", 2, BDBDataStoreImpl.getInstance().getPncs(knownPncs2.getId()).size());
		assertEquals("Wrong number of PNCS items found", 0, BDBDataStoreImpl.getInstance().getPncs(knownPncs3.getId()).size());

		i = BDBDataStoreImpl.getInstance().getPncs();
		count = 0;
		while (i.hasNext())
		{
			i.next();
			count++;
		}
		assertEquals(2, count);
	}

	@Test
	public void testLegoGetAndDelete() throws WriteException
	{
		BDBDataStoreImpl.getInstance().createLegoList(UUID.randomUUID().toString(), "a", "a description", null);
		BDBDataStoreImpl.getInstance().createLegoList(UUID.randomUUID().toString(), "b", "b description", null);
		BDBDataStoreImpl.getInstance().createLegoList(UUID.randomUUID().toString(), "c", "c description", null);

		String aId = BDBDataStoreImpl.getInstance().getLegoListByName("a").getLegoListUUID();
		String bId = BDBDataStoreImpl.getInstance().getLegoListByName("b").getLegoListUUID();
		String cId = BDBDataStoreImpl.getInstance().getLegoListByName("c").getLegoListUUID();

		Lego l1 = new Lego();
		l1.setLegoUUID(UUID.nameUUIDFromBytes("foo".getBytes()).toString());
		l1.setPncs(makeUniquePncs());
		Pncs knownPncs1 = l1.getPncs();
		Stamp l1LegoStamp = BDBDataStoreImpl.getInstance().commitLego(l1, aId);

		Lego l2 = new Lego();
		l2.setLegoUUID(UUID.nameUUIDFromBytes("foo".getBytes()).toString());
		Pncs knownPncs2 = new Pncs();
		knownPncs2.setId(knownPncs1.getId());
		knownPncs2.setName(knownPncs1.getName());
		knownPncs2.setValue("testVal");
		l2.setPncs(knownPncs2);
		Stamp l2LegoStamp = BDBDataStoreImpl.getInstance().commitLego(l2, aId);

		Lego l3 = new Lego();
		l3.setLegoUUID(UUID.nameUUIDFromBytes("bar".getBytes()).toString());
		l3.setPncs(makeUniquePncs());
		Pncs knownPncs3 = l3.getPncs();
		Stamp l3LegoStamp = BDBDataStoreImpl.getInstance().commitLego(l3, bId);

		Lego l = BDBDataStoreImpl.getInstance().getLego(l1.getLegoUUID(), l1LegoStamp.getUuid());
		assertNotNull(l);
		assertTrue(SchemaEquals.equals(knownPncs1, l.getPncs()));

		l = BDBDataStoreImpl.getInstance().getLego(l2.getLegoUUID(), l2LegoStamp.getUuid());
		assertNotNull(l);
		assertTrue(SchemaEquals.equals(knownPncs2, l.getPncs()));

		l = BDBDataStoreImpl.getInstance().getLego(l1.getLegoUUID(), l2LegoStamp.getUuid());
		assertNotNull(l);
		assertTrue(SchemaEquals.equals(knownPncs2, l.getPncs()));

		l = BDBDataStoreImpl.getInstance().getLego(l3.getLegoUUID(), l3LegoStamp.getUuid());
		assertNotNull(l);
		assertTrue(SchemaEquals.equals(knownPncs3, l.getPncs()));

		l = BDBDataStoreImpl.getInstance().getLego(l1.getLegoUUID(), l3LegoStamp.getUuid());
		assertNull(l);

		Iterator<Lego> i = BDBDataStoreImpl.getInstance().getLegos();
		int count = 0;
		while (i.hasNext())
		{
			i.next();
			count++;
		}
		assertEquals(3, count);

		// noop
		BDBDataStoreImpl.getInstance().deleteLego(cId, l1.getLegoUUID(), l1LegoStamp.getUuid());
		i = BDBDataStoreImpl.getInstance().getLegos();
		count = 0;
		while (i.hasNext())
		{
			i.next();
			count++;
		}
		assertEquals(3, count);

		// noop
		BDBDataStoreImpl.getInstance().deleteLego(bId, l1.getLegoUUID(), l1LegoStamp.getUuid());
		i = BDBDataStoreImpl.getInstance().getLegos();
		count = 0;
		while (i.hasNext())
		{
			i.next();
			count++;
		}
		assertEquals(3, count);
		assertEquals(1, BDBDataStoreImpl.getInstance().getLegoListByLego(l1.getLegoUUID()).size());

		BDBDataStoreImpl.getInstance().deleteLego(aId, l1.getLegoUUID(), l1LegoStamp.getUuid());
		i = BDBDataStoreImpl.getInstance().getLegos();
		count = 0;
		while (i.hasNext())
		{
			i.next();
			count++;
		}
		assertEquals(2, count);
		assertNull(BDBDataStoreImpl.getInstance().getLego(l1.getLegoUUID(), l1LegoStamp.getUuid()));

		assertEquals(1, BDBDataStoreImpl.getInstance().getLegoListByLego(l1.getLegoUUID()).size());
		assertEquals(1, BDBDataStoreImpl.getInstance().getLegoListByLego(l2.getLegoUUID()).size());
		assertEquals(1, BDBDataStoreImpl.getInstance().getLegoListByLego(l3.getLegoUUID()).size());

		// noop
		BDBDataStoreImpl.getInstance().deleteLego(aId, l1.getLegoUUID(), l1LegoStamp.getUuid());
		i = BDBDataStoreImpl.getInstance().getLegos();
		count = 0;
		while (i.hasNext())
		{
			i.next();
			count++;
		}
		assertEquals(2, count);

		BDBDataStoreImpl.getInstance().deleteLego(aId, l2.getLegoUUID(), l2LegoStamp.getUuid());
		i = BDBDataStoreImpl.getInstance().getLegos();
		count = 0;
		while (i.hasNext())
		{
			i.next();
			count++;
		}
		assertEquals(1, count);
		assertNull(BDBDataStoreImpl.getInstance().getLego(l2.getLegoUUID(), l2LegoStamp.getUuid()));

		assertEquals(0, BDBDataStoreImpl.getInstance().getLegoListByLego(l1.getLegoUUID()).size());
		assertEquals(0, BDBDataStoreImpl.getInstance().getLegoListByLego(l2.getLegoUUID()).size());
		assertEquals(1, BDBDataStoreImpl.getInstance().getLegoListByLego(l3.getLegoUUID()).size());
		assertNotNull(BDBDataStoreImpl.getInstance().getLego(l3.getLegoUUID(), l3LegoStamp.getUuid()));
	}

	@Test
	public void testDelete() throws WriteException
	{
		BDBDataStoreImpl.getInstance().createLegoList(UUID.randomUUID().toString(), "a", "a description", null);
		BDBDataStoreImpl.getInstance().createLegoList(UUID.randomUUID().toString(), "b", "b description", null);
		BDBDataStoreImpl.getInstance().createLegoList(UUID.randomUUID().toString(), "c", "c description", null);

		String legoListAId = BDBDataStoreImpl.getInstance().getLegoListByName("a").getLegoListUUID();
		String legoListBId = BDBDataStoreImpl.getInstance().getLegoListByName("b").getLegoListUUID();
		Lego l1 = new Lego();
		l1.setLegoUUID(UUID.nameUUIDFromBytes("foo".getBytes()).toString());
		l1.setPncs(makeUniquePncs());
		Pncs knownPncs = l1.getPncs();

		Assertion a = new Assertion();
		a.setAssertionUUID(UUID.randomUUID().toString());

		String knownAssertionId = a.getAssertionUUID();

		Discernible d = new Discernible();
		Expression e = new Expression();
		Concept c = new Concept();
		c.setDesc("foo");
		c.setSctid(5l);
		e.setConcept(c);
		d.setExpression(e);
		a.setDiscernible(d);

		Value v = new Value();
		Expression e1 = new Expression();
		Concept c1 = new Concept();
		c1.setDesc("bar");
		String knownUUID = UUID.randomUUID().toString();
		c1.setUuid(knownUUID);
		e1.setConcept(c1);
		v.setExpression(e1);
		a.setValue(v);

		Qualifier q = new Qualifier();
		Expression e2 = new Expression();
		Concept c2 = new Concept();
		c2.setDesc("bar");
		c2.setSctid(99l);
		e2.setConcept(c2);
		q.setExpression(e2);
		a.setQualifier(q);

		l1.getAssertion().add(a);

		assertEquals("assertion shouldn't exist", BDBDataStoreImpl.getInstance().getLegosContainingAssertion((knownAssertionId)).size(), 0);
		assertNull("pncs should not exist",
				((BDBDataStoreImpl) BDBDataStoreImpl.getInstance()).getPncsByUniqueId(PncsBDB.makeUniqueId(knownPncs.getId(), knownPncs.getValue())));
		BDBDataStoreImpl.getInstance().commitLego(l1, legoListAId);
		assertEquals("assertion should exist", BDBDataStoreImpl.getInstance().getLegosContainingAssertion((knownAssertionId)).size(), 1);

		assertEquals("Lego should exist once", BDBDataStoreImpl.getInstance().getLegos(UUID.nameUUIDFromBytes("foo".getBytes()).toString()).size(), 1);
		assertNotNull("pncs should exist",
				((BDBDataStoreImpl) BDBDataStoreImpl.getInstance()).getPncsByUniqueId(PncsBDB.makeUniqueId(knownPncs.getId(), knownPncs.getValue())));

		// This lego is now committed twice
		BDBDataStoreImpl.getInstance().commitLego(l1, legoListAId);

		assertNotNull("pncs should exist",
				((BDBDataStoreImpl) BDBDataStoreImpl.getInstance()).getPncsByUniqueId(PncsBDB.makeUniqueId(knownPncs.getId(), knownPncs.getValue())));

		// the same pncs is now used in two different lego lists.
		Lego l2 = l1;
		l2.setLegoUUID(UUID.nameUUIDFromBytes("bar".getBytes()).toString());
		l2.getAssertion().get(0).setAssertionUUID(UUID.randomUUID().toString());
		BDBDataStoreImpl.getInstance().commitLego(l2, legoListBId);

		assertNotNull("pncs should exist",
				((BDBDataStoreImpl) BDBDataStoreImpl.getInstance()).getPncsByUniqueId(PncsBDB.makeUniqueId(knownPncs.getId(), knownPncs.getValue())));

		BDBDataStoreImpl.getInstance().deleteLegoList(legoListAId);
		assertEquals("assertion shouldn't exist", BDBDataStoreImpl.getInstance().getLegosContainingAssertion((knownAssertionId)).size(), 0);
		assertEquals("Lego shouldn't exist", BDBDataStoreImpl.getInstance().getLegos(UUID.nameUUIDFromBytes("foo".getBytes()).toString()).size(), 0);
		assertNotNull("pncs should exist",
				((BDBDataStoreImpl) BDBDataStoreImpl.getInstance()).getPncsByUniqueId(PncsBDB.makeUniqueId(knownPncs.getId(), knownPncs.getValue())));
		BDBDataStoreImpl.getInstance().deleteLegoList(legoListBId);
		assertNull("pncs should not exist",
				((BDBDataStoreImpl) BDBDataStoreImpl.getInstance()).getPncsByUniqueId(PncsBDB.makeUniqueId(knownPncs.getId(), knownPncs.getValue())));

		// TODO TEST validate correct behavior with STAMPs used by multiple legos (probably via import from XML instead, since that doesn't modify the
		// STAMP)
		// LegoList with multiple legos, each with the same stamp, same uuid.
		// TODO TEST ConceptConjunctions - searching them, etc.
		// TODO TEST relationGroups - searching them, etc

	}

	@Test
	public void testCommitErrorCheck() throws WriteException
	{
		BDBDataStoreImpl.getInstance().createLegoList(UUID.randomUUID().toString(), "a", "a description", null);
		BDBDataStoreImpl.getInstance().createLegoList(UUID.randomUUID().toString(), "b", "a description", null);

		String legoListAId = BDBDataStoreImpl.getInstance().getLegoListByName("a").getLegoListUUID();
		String legoListBId = BDBDataStoreImpl.getInstance().getLegoListByName("b").getLegoListUUID();

		Lego l1 = new Lego();
		l1.setLegoUUID(UUID.nameUUIDFromBytes("foo".getBytes()).toString());
		l1.setPncs(makeUniquePncs());

		Assertion a = new Assertion();
		a.setAssertionUUID(UUID.randomUUID().toString());

		Discernible d = new Discernible();
		Expression e = new Expression();
		Concept c = new Concept();
		c.setDesc("foo");
		c.setSctid(5l);
		e.setConcept(c);
		d.setExpression(e);
		a.setDiscernible(d);

		Value v = new Value();
		Expression e1 = new Expression();
		Concept c1 = new Concept();
		c1.setDesc("bar");
		String knownUUID = UUID.randomUUID().toString();
		c1.setUuid(knownUUID);
		e1.setConcept(c1);
		v.setExpression(e1);
		a.setValue(v);

		Qualifier q = new Qualifier();
		Expression e2 = new Expression();
		Concept c2 = new Concept();
		c2.setDesc("bar");
		c2.setSctid(99l);
		e2.setConcept(c2);
		q.setExpression(e2);
		a.setQualifier(q);

		l1.getAssertion().add(a);

		// Should fail bad legoListId
		try
		{
			BDBDataStoreImpl.getInstance().commitLego(l1, "foo");
			fail("This should have failed");
		}
		catch (WriteException ex)
		{
			// expected
		}

		// Should work
		BDBDataStoreImpl.getInstance().commitLego(l1, legoListAId);

		// Should work (new stamp, same details)
		BDBDataStoreImpl.getInstance().commitLego(l1, legoListAId);

		// Should fail (same lego UUID in a different legoList)
		try
		{
			BDBDataStoreImpl.getInstance().commitLego(l1, legoListBId);
			fail("This should have failed");
		}
		catch (WriteException ex)
		{
			// expected
		}

		// Fix the lego UUID
		l1.setLegoUUID(UUID.randomUUID().toString());
		// Should fail (because assertionUUID reused in another LegoList)
		try
		{
			BDBDataStoreImpl.getInstance().commitLego(l1, legoListBId);
			fail("This should have failed");
		}
		catch (WriteException ex)
		{
			// expected
		}

		// Change the assertion id - should work now
		a.setAssertionUUID(UUID.randomUUID().toString());
		BDBDataStoreImpl.getInstance().commitLego(l1, legoListAId);
	}

	// TODO TEST write a test to verify that import works properly (mock up a full XML file, do various tests to make sure everything exists as
	// expected)

	@Test
	public void testIterators() throws WriteException, InterruptedException
	{
		int count = 0;
		Iterator<LegoList> i = BDBDataStoreImpl.getInstance().getLegoLists();
		while (i.hasNext())
		{
			i.next();
			count++;
		}
		assertEquals("Should have been 0!", count, 0);

		BDBDataStoreImpl.getInstance().createLegoList(UUID.randomUUID().toString(), "a", "a description", null);
		BDBDataStoreImpl.getInstance().createLegoList(UUID.randomUUID().toString(), "b", "b description", null);
		BDBDataStoreImpl.getInstance().createLegoList(UUID.randomUUID().toString(), "c", "c description", null);

		count = 0;
		i = BDBDataStoreImpl.getInstance().getLegoLists();
		while (i.hasNext())
		{
			i.next();
			count++;
		}
		assertEquals("Should have been 3!", count, 3);

		// Test the timeout
		try
		{
			count = 0;
			BDBIterator.timeoutInSeconds = 2;
			i = BDBDataStoreImpl.getInstance().getLegoLists();
			while (i.hasNext())
			{
				i.next();
				count++;
				Thread.sleep(3000);

			}
			fail("Didn't close the iterator on timeout!");
		}
		catch (IteratorClosedException e)
		{
			// expected.
			assertEquals("Should have been 1!", count, 1);
		}

		count = 0;
		BDBIterator.timeoutInSeconds = 2;
		i = BDBDataStoreImpl.getInstance().getLegoLists();
		while (i.hasNext())
		{
			i.next();
			count++;
			Thread.sleep(1500);
		}
		assertEquals("Should have been 3!", count, 3);
	}

	@Test
	public void testStoreRetreive() throws WriteException, JAXBException, IOException
	{
		LegoList initial = LegoXMLUtils.readLegoList(BDBDataStoreTest.class.getResource("/badDay.xml").openStream());
		BDBDataStoreImpl.getInstance().importLegoList(initial);

		// reread, just incase
		initial = LegoXMLUtils.readLegoList(BDBDataStoreTest.class.getResource("/badDay.xml").openStream());
		LegoList readBack = BDBDataStoreImpl.getInstance().getLegoListByID(initial.getLegoListUUID());

		assertTrue(SchemaEquals.equals(initial, readBack));
	}

	@Test
	public void testPncsNameChangeNotAllowed() throws WriteException
	{
		BDBDataStoreImpl.getInstance().createLegoList(UUID.randomUUID().toString(), "a", "a description", null);

		String aId = BDBDataStoreImpl.getInstance().getLegoListByName("a").getLegoListUUID();

		Lego l1 = new Lego();
		l1.setLegoUUID(UUID.nameUUIDFromBytes("foo".getBytes()).toString());
		l1.setPncs(makeUniquePncs());
		Pncs knownPncs1 = l1.getPncs();
		BDBDataStoreImpl.getInstance().commitLego(l1, aId);
		
		Pncs pncs = BDBDataStoreImpl.getInstance().getPncs(knownPncs1.getId(), knownPncs1.getValue());
		assertTrue(pncs.getName().equals(knownPncs1.getName()));

		Lego l2 = new Lego();
		l2.setLegoUUID(UUID.nameUUIDFromBytes("foo".getBytes()).toString());
		Pncs knownPncs2 = new Pncs();
		knownPncs2.setId(knownPncs1.getId());
		knownPncs2.setName("fred");
		knownPncs2.setValue(knownPncs1.getValue());
		l2.setPncs(knownPncs2);
		try
		{
			BDBDataStoreImpl.getInstance().commitLego(l2, aId);
			fail("Should have failed");
		}
		catch (WriteException e)
		{
			// expected
			knownPncs2.setId(42);
			BDBDataStoreImpl.getInstance().commitLego(l2, aId);
		}
		
		pncs = BDBDataStoreImpl.getInstance().getPncs(knownPncs1.getId(), knownPncs1.getValue());
		assertTrue(pncs.getName().equals(knownPncs1.getName()));
	}
	
}
