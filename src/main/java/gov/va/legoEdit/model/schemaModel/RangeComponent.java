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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for rangeComponent complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="rangeComponent">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;choice>
 *           &lt;element name="numericValue" type="{http://www.w3.org/2001/XMLSchema}float"/>
 *           &lt;element name="stringValue" type="{}measurementString"/>
 *           &lt;element ref="{}relaxedRangeBound"/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *       &lt;attribute name="inclusive" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "rangeComponent", propOrder = {
    "numericValue",
    "stringValue",
    "relaxedRangeBound"
})
@Persistent
public class RangeComponent {

    protected Float numericValue;
    protected MeasurementString stringValue;
    protected RelaxedRangeBound relaxedRangeBound;
    @XmlAttribute(name = "inclusive")
    protected Boolean inclusive;

    /**
     * Gets the value of the numericValue property.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getNumericValue() {
        return numericValue;
    }

    /**
     * Sets the value of the numericValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setNumericValue(Float value) {
        this.numericValue = value;
    }

    /**
     * Gets the value of the stringValue property.
     * 
     * @return
     *     possible object is
     *     {@link MeasurementString }
     *     
     */
    public MeasurementString getStringValue() {
        return stringValue;
    }

    /**
     * Sets the value of the stringValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link MeasurementString }
     *     
     */
    public void setStringValue(MeasurementString value) {
        this.stringValue = value;
    }

    /**
     * Gets the value of the relaxedRangeBound property.
     * 
     * @return
     *     possible object is
     *     {@link RelaxedRangeBound }
     *     
     */
    public RelaxedRangeBound getRelaxedRangeBound() {
        return relaxedRangeBound;
    }

    /**
     * Sets the value of the relaxedRangeBound property.
     * 
     * @param value
     *     allowed object is
     *     {@link RelaxedRangeBound }
     *     
     */
    public void setRelaxedRangeBound(RelaxedRangeBound value) {
        this.relaxedRangeBound = value;
    }

    /**
     * Gets the value of the inclusive property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isInclusive() {
        return inclusive;
    }

    /**
     * Sets the value of the inclusive property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setInclusive(Boolean value) {
        this.inclusive = value;
    }

}
