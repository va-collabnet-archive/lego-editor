//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.01.10 at 10:23:44 AM CST 
//


package gov.va.legoEdit.model.schemaModel;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import com.sleepycat.persist.model.Persistent;


/**
 * <p>Java class for bound complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="bound">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="lowerPoint" type="{}point" minOccurs="0"/>
 *         &lt;element name="upperPoint" type="{}point" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="lowerPointInclusive" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="upperPointInclusive" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "bound", propOrder = {
    "lowerPoint",
    "upperPoint"
})
@Persistent
public class Bound {

    protected Point lowerPoint;
    protected Point upperPoint;
    @XmlAttribute(name = "lowerPointInclusive")
    protected Boolean lowerPointInclusive;
    @XmlAttribute(name = "upperPointInclusive")
    protected Boolean upperPointInclusive;

    /**
     * Gets the value of the lowerPoint property.
     * 
     * @return
     *     possible object is
     *     {@link Point }
     *     
     */
    public Point getLowerPoint() {
        return lowerPoint;
    }

    /**
     * Sets the value of the lowerPoint property.
     * 
     * @param value
     *     allowed object is
     *     {@link Point }
     *     
     */
    public void setLowerPoint(Point value) {
        this.lowerPoint = value;
    }

    /**
     * Gets the value of the upperPoint property.
     * 
     * @return
     *     possible object is
     *     {@link Point }
     *     
     */
    public Point getUpperPoint() {
        return upperPoint;
    }

    /**
     * Sets the value of the upperPoint property.
     * 
     * @param value
     *     allowed object is
     *     {@link Point }
     *     
     */
    public void setUpperPoint(Point value) {
        this.upperPoint = value;
    }

    /**
     * Gets the value of the lowerPointInclusive property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isLowerPointInclusive() {
        return lowerPointInclusive;
    }

    /**
     * Sets the value of the lowerPointInclusive property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setLowerPointInclusive(Boolean value) {
        this.lowerPointInclusive = value;
    }

    /**
     * Gets the value of the upperPointInclusive property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isUpperPointInclusive() {
        return upperPointInclusive;
    }

    /**
     * Sets the value of the upperPointInclusive property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setUpperPointInclusive(Boolean value) {
        this.upperPointInclusive = value;
    }

}
