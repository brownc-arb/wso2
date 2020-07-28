package com.alrayan.wso2.vasco.soap;

/**
 * Enum defining instance types of the SOAP element.
 *
 * @since 1.0.0
 */
public enum SOAPElementType {

    STRING("xsd:string"),
    UNSIGNED_INTEGER("xsd:unsignedInt");

    private final String instanceType;

    /**
     * Sets the type of the soap element attribute.
     *
     * @param instanceType instance type
     */
    SOAPElementType(String instanceType) {
        this.instanceType = instanceType;
    }

    /**
     * Returns the instance type.
     *
     * @return instance type
     */
    public String getInstanceType() {
        return instanceType;
    }

    @Override
    public String toString() {
        return "SOAPElementType{" +
               "instanceType='" + instanceType + '\'' +
               '}';
    }
}
