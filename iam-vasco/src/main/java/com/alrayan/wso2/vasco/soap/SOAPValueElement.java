package com.alrayan.wso2.vasco.soap;

/**
 * Holds the data-structure related to a SOAP element that has a instance type.
 *
 * @since 1.0.0
 */
public class SOAPValueElement {

    private final String elementValue;
    private final SOAPElementType type;

    /**
     * Constructs a {@link SOAPValueElement} instance.
     *
     * @param elementValue SOAP element value
     * @param type         Instance type of the SOAP element
     */
    public SOAPValueElement(String elementValue, SOAPElementType type) {
        this.elementValue = elementValue;
        this.type = type;
    }

    /**
     * Returns the value of the SOAP value element.
     *
     * @return SOAP value element value
     */
    public String getElementValue() {
        return elementValue;
    }

    /**
     * Returns the instance type of the SOAP value element.
     *
     * @return SOAP value element's instance type
     */
    public SOAPElementType getType() {
        return type;
    }
}
