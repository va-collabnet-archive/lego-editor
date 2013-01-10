//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.01.10 at 10:23:44 AM CST 
//


package gov.va.legoEdit.model.schemaModel;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the gov.va.legoEdit.model.schemaModel package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Text_QNAME = new QName("", "text");
    private final static QName _Boolean_QNAME = new QName("", "boolean");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: gov.va.legoEdit.model.schemaModel
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Expression }
     * 
     */
    public Expression createExpression() {
        return new Expression();
    }

    /**
     * Create an instance of {@link Concept }
     * 
     */
    public Concept createConcept() {
        return new Concept();
    }

    /**
     * Create an instance of {@link Relation }
     * 
     */
    public Relation createRelation() {
        return new Relation();
    }

    /**
     * Create an instance of {@link Type }
     * 
     */
    public Type createType() {
        return new Type();
    }

    /**
     * Create an instance of {@link Destination }
     * 
     */
    public Destination createDestination() {
        return new Destination();
    }

    /**
     * Create an instance of {@link Measurement }
     * 
     */
    public Measurement createMeasurement() {
        return new Measurement();
    }

    /**
     * Create an instance of {@link Units }
     * 
     */
    public Units createUnits() {
        return new Units();
    }

    /**
     * Create an instance of {@link Point }
     * 
     */
    public Point createPoint() {
        return new Point();
    }

    /**
     * Create an instance of {@link Bound }
     * 
     */
    public Bound createBound() {
        return new Bound();
    }

    /**
     * Create an instance of {@link Interval }
     * 
     */
    public Interval createInterval() {
        return new Interval();
    }

    /**
     * Create an instance of {@link RelationGroup }
     * 
     */
    public RelationGroup createRelationGroup() {
        return new RelationGroup();
    }

    /**
     * Create an instance of {@link Stamp }
     * 
     */
    public Stamp createStamp() {
        return new Stamp();
    }

    /**
     * Create an instance of {@link LegoList }
     * 
     */
    public LegoList createLegoList() {
        return new LegoList();
    }

    /**
     * Create an instance of {@link Lego }
     * 
     */
    public Lego createLego() {
        return new Lego();
    }

    /**
     * Create an instance of {@link Pncs }
     * 
     */
    public Pncs createPncs() {
        return new Pncs();
    }

    /**
     * Create an instance of {@link Assertion }
     * 
     */
    public Assertion createAssertion() {
        return new Assertion();
    }

    /**
     * Create an instance of {@link Discernible }
     * 
     */
    public Discernible createDiscernible() {
        return new Discernible();
    }

    /**
     * Create an instance of {@link Qualifier }
     * 
     */
    public Qualifier createQualifier() {
        return new Qualifier();
    }

    /**
     * Create an instance of {@link Value }
     * 
     */
    public Value createValue() {
        return new Value();
    }

    /**
     * Create an instance of {@link Timing }
     * 
     */
    public Timing createTiming() {
        return new Timing();
    }

    /**
     * Create an instance of {@link AssertionComponent }
     * 
     */
    public AssertionComponent createAssertionComponent() {
        return new AssertionComponent();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "text")
    public JAXBElement<String> createText(String value) {
        return new JAXBElement<String>(_Text_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "boolean")
    public JAXBElement<Boolean> createBoolean(Boolean value) {
        return new JAXBElement<Boolean>(_Boolean_QNAME, Boolean.class, null, value);
    }

}
