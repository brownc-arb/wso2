package com.alrayan.wso2.vasco.soap;

/**
 * Enum defining SOAP namespaces.
 *
 * @since 1.0.0
 */
public enum SOAPNamespace {

    ADM("adm", "http://www.vasco.com/IdentikeyServer/IdentikeyTypes/Administration"),
    AUT("aut", "http://www.vasco.com/IdentikeyServer/IdentikeyTypes/Authentication"),
    SIG("sig", "http://www.vasco.com/IdentikeyServer/IdentikeyTypes/Signature"),
    XSD("xsd", "http://www.w3.org/2001/XMLSchema"),
    XSI("xsi", "http://www.w3.org/2001/XMLSchema-instance");

    private final String prefix;
    private final String uRL;

    /**
     * Sets the namespace prefix and URL.
     *
     * @param prefix namespace prefix
     * @param uRL    namespace URL
     */
    SOAPNamespace(String prefix, String uRL) {
        this.prefix = prefix;
        this.uRL = uRL;
    }

    /**
     * Returns the namespace prefix.
     *
     * @return namespace prefix
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Returns the namespace URL.
     *
     * @return namespace URL
     */
    public String getuRL() {
        return uRL;
    }

    @Override
    public String toString() {
        return "SOAPNamespace{" +
               "prefix='" + prefix + '\'' +
               ", uRL='" + uRL + '\'' +
               '}';
    }
}
