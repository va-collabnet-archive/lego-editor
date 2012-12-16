//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.12.13 at 04:17:30 PM CST 
//


package gov.va.legoEdit.model.schemaModel;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import com.sleepycat.persist.model.Persistent;


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
 *         &lt;choice minOccurs="0">
 *           &lt;element name="lowerBound" type="{}bound"/>
 *           &lt;element name="lowerPoint" type="{}point"/>
 *         &lt;/choice>
 *         &lt;choice minOccurs="0">
 *           &lt;element name="upperBound" type="{}bound"/>
 *           &lt;element name="upperPoint" type="{}point"/>
 *         &lt;/choice>
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
    "lowerBound",
    "lowerPoint",
    "upperBound",
    "upperPoint"
})
@XmlRootElement(name = "interval")
@Persistent
public class Interval {

    protected Bound lowerBound;
    protected Point lowerPoint;
    protected Bound upperBound;
    protected Point upperPoint;

    /**
     * Gets the value of the lowerBound property.
     * 
     * @return
     *     possible object is
     *     {@link Bound }
     *     
     */
    public Bound getLowerBound() {
        return lowerBound;
    }

    /**
     * Sets the value of the lowerBound property.
     * 
     * @param value
     *     allowed object is
     *     {@link Bound }
     *     
     */
    public void setLowerBound(Bound value) {
        this.lowerBound = value;
    }

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
     * Gets the value of the upperBound property.
     * 
     * @return
     *     possible object is
     *     {@link Bound }
     *     
     */
    public Bound getUpperBound() {
        return upperBound;
    }

    /**
     * Sets the value of the upperBound property.
     * 
     * @param value
     *     allowed object is
     *     {@link Bound }
     *     
     */
    public void setUpperBound(Bound value) {
        this.upperBound = value;
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

}
