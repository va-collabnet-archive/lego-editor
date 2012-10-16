//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.10.16 at 08:28:41 AM CDT 
//


package gov.va.legoEdit.model.schemaModel;

import com.sleepycat.persist.model.Persistent;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="assertionUUID" type="{}UUID"/>
 *         &lt;element name="typeConcept" type="{}concept"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "assertionUUID",
    "typeConcept"
})
@XmlRootElement(name = "assertionComponent")
@Persistent
public class AssertionComponent {

    @XmlElement(required = true)
    protected String assertionUUID;
    @XmlElement(required = true)
    protected Concept typeConcept;

    /**
     * Gets the value of the assertionUUID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAssertionUUID() {
        return assertionUUID;
    }

    /**
     * Sets the value of the assertionUUID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAssertionUUID(String value) {
        this.assertionUUID = value;
    }

    /**
     * Gets the value of the typeConcept property.
     * 
     * @return
     *     possible object is
     *     {@link Concept }
     *     
     */
    public Concept getTypeConcept() {
        return typeConcept;
    }

    /**
     * Sets the value of the typeConcept property.
     * 
     * @param value
     *     allowed object is
     *     {@link Concept }
     *     
     */
    public void setTypeConcept(Concept value) {
        this.typeConcept = value;
    }

}
