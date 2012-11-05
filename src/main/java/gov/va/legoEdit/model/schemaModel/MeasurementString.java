//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.11.05 at 01:50:51 PM CST 
//


package gov.va.legoEdit.model.schemaModel;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for measurementString.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="measurementString">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="DOB"/>
 *     &lt;enumeration value="NOW"/>
 *     &lt;enumeration value="Date of onset (observable entity)"/>
 *     &lt;enumeration value="start active service"/>
 *     &lt;enumeration value="end active service"/>
 *     &lt;enumeration value="several"/>
 *     &lt;enumeration value="Date of event (observable entity)"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "measurementString")
@XmlEnum
public enum MeasurementString {

    DOB("DOB"),
    NOW("NOW"),
    @XmlEnumValue("Date of onset (observable entity)")
    DATE_OF_ONSET_OBSERVABLE_ENTITY("Date of onset (observable entity)"),
    @XmlEnumValue("start active service")
    START_ACTIVE_SERVICE("start active service"),
    @XmlEnumValue("end active service")
    END_ACTIVE_SERVICE("end active service"),
    @XmlEnumValue("several")
    SEVERAL("several"),
    @XmlEnumValue("Date of event (observable entity)")
    DATE_OF_EVENT_OBSERVABLE_ENTITY("Date of event (observable entity)");
    private final String value;

    MeasurementString(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static MeasurementString fromValue(String v) {
        for (MeasurementString c: MeasurementString.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
