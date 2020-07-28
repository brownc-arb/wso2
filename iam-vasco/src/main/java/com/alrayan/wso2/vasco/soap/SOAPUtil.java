package com.alrayan.wso2.vasco.soap;

import com.alrayan.wso2.common.AlRayanError;
import com.alrayan.wso2.common.utils.HTTPClientUtil;
import com.alrayan.wso2.vasco.VASCOException;
import org.apache.axis2.saaj.MessageFactoryImpl;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.NoSuchElementException;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

/**
 * Util class to help create the SOAP envelop.
 *
 * @since 1.0.0
 */
public class SOAPUtil {

    private static final Logger log = LoggerFactory.getLogger(SOAPUtil.class);

    /**
     * Converts the SOAP message to a string.
     *
     * @return string content of the SOAP message
     */
    public static String convertSOAPMessageToString(SOAPMessage soapMessage) throws IOException, SOAPException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        soapMessage.writeTo(byteArrayOutputStream);
        return new String(byteArrayOutputStream.toByteArray());
    }

    /**
     * Creates and returns a new SOAP message.
     *
     * @return SOAP message
     * @throws SOAPException thrown when error on creating SOAP message
     */
    public static SOAPMessage createSOAPMessage() throws SOAPException {
        MessageFactory messageFactory = new MessageFactoryImpl();
        return messageFactory.createMessage();
    }

    /**
     * Adds the given namespace to the SOAP envelop of the given SOAP message. The SOAP body of the SOAP message will
     * be returned.
     *
     * @param soapMessage        SOAP message
     * @param vascoSOAPNamespace VASCO namespace
     * @return SOAP body of the given SOAP message
     * @throws SOAPException thrown when error on adding namespaces
     */
    public static SOAPBody getSOAPBody(SOAPMessage soapMessage, SOAPNamespace vascoSOAPNamespace) throws SOAPException {
        SOAPPart soapPart = soapMessage.getSOAPPart();
        SOAPEnvelope envelope = soapPart.getEnvelope();
        envelope.addNamespaceDeclaration(vascoSOAPNamespace.getPrefix(), vascoSOAPNamespace.getuRL());
        envelope.addNamespaceDeclaration(SOAPNamespace.XSD.getPrefix(), SOAPNamespace.XSD.getuRL());
        envelope.addNamespaceDeclaration(SOAPNamespace.XSI.getPrefix(), SOAPNamespace.XSI.getuRL());
        return envelope.getBody();
    }

    /**
     * Creates attribute entries for the given attribute map.
     *
     * @param soapElement  parent SOAP element.
     * @param attributeMap attribute map
     * @throws SOAPException thrown when error on creating the SOAP elements
     */
    public static void createAttributeSOAPElement(SOAPElement soapElement, Map<String, SOAPValueElement> attributeMap)
            throws SOAPException {
        for (Map.Entry<String, SOAPValueElement> attribute : attributeMap.entrySet()) {
            SOAPElement attributesElement = soapElement.addChildElement("attributes");
            // Attribute ID
            SOAPElement attributeIDElement = attributesElement.addChildElement("attributeID");
            attributeIDElement.setValue(attribute.getKey());
            // Value
            QName qname = new QName(SOAPNamespace.XSI.getPrefix() + ":type");
            SOAPElement valueElement = attributesElement.addChildElement("value");
            valueElement.addAttribute(qname, attribute.getValue().getType().getInstanceType());
            valueElement.setValue(attribute.getValue().getElementValue());
        }
    }

    public static void createDataFeildSOAPElement(SOAPElement soapElement, Map<String, String> attributeMap)
            throws SOAPException {
        for (Map.Entry<String, String> attribute : attributeMap.entrySet()) {
            SOAPElement attributesElement = soapElement.addChildElement("dataField");
            // Attribute ID
            SOAPElement attributeIDElement = attributesElement.addChildElement("key");
            attributeIDElement.setValue(attribute.getKey());
            // Value
            SOAPElement valueElement = attributesElement.addChildElement("value");
            valueElement.setValue(attribute.getValue());
        }
    }

    /**
     * Returns whether the VASCO response is a VASCO success or not.
     *
     * @param response VASCO HTTP response
     * @return {@code true} if the VASCO response is a success reponse, {@code false} otherwise
     * @throws VASCOException thrown when error on converting response to SOAP message
     */
    public static boolean isStatSuccess(HTTPClientUtil.Response response) throws VASCOException {
        try {
            SOAPMessage soapMessage = SOAPUtil.convertResponseToSOAPMessage(response.getResponseBody());
            SOAPBody soapBody = soapMessage.getSOAPBody();
            // Get secure challenge from SOAP response.
            NodeList statusCodeNodeList = soapBody.getElementsByTagName("statusCodeEnum");
            NodeList returnCodeNodeList = soapBody.getElementsByTagName("returnCodeEnum");
            return statusCodeNodeList.item(0).getTextContent().equals("STAT_SUCCESS") &&
                   returnCodeNodeList.item(0).getTextContent().equals("RET_SUCCESS");
        } catch (SOAPException | IOException e) {
            log.error(AlRayanError.ERROR_CONVERTING_RESPONSE_TO_SOAP.getErrorMessageWithCode(), e);
            throw new VASCOException(AlRayanError.ERROR_CONVERTING_RESPONSE_TO_SOAP.getErrorMessageWithCode(), e);
        }
    }

    /**
     * Returns the attribute value from the SOAP response.
     *
     * @param response HTTP response
     * @return value of the attribute for the given attribute identifier
     * @throws NoSuchElementException thrown when error on obtaining attribute value
     * @throws VASCOException         thrown when error on converting response to SOAP message
     */
    public static String getSOAPBodyAttributeValue(HTTPClientUtil.Response response, String attributeIdentifier)
            throws NoSuchElementException, VASCOException {
        try {
            SOAPMessage soapMessage = SOAPUtil.convertResponseToSOAPMessage(response.getResponseBody());
            SOAPBody soapBody = soapMessage.getSOAPBody();
            // Get secure challenge from SOAP response.
            NodeList nodeList = soapBody.getElementsByTagName("attributeID");
            for (int parentNodeCounter = 0; parentNodeCounter < nodeList.getLength(); parentNodeCounter++) {
                Node node = nodeList.item(parentNodeCounter);
                if (node.getTextContent().equals(attributeIdentifier)) {
                    NodeList childNodeList = node.getParentNode().getChildNodes();
                    for (int childNodeCounter = 0; childNodeCounter < childNodeList.getLength() - 1;
                         childNodeCounter++) {
                        String localName = childNodeList.item(childNodeCounter).getLocalName();
                        if (StringUtils.isNotEmpty(localName) && localName.equals("value")) {
                            return childNodeList.item(childNodeCounter).getTextContent();
                        }
                    }
                }
            }
            throw new NoSuchElementException("Requested element " + attributeIdentifier +
                                             " or its value segment not found in response.");
        } catch (SOAPException | IOException e) {
            log.error(AlRayanError.ERROR_CONVERTING_RESPONSE_TO_SOAP.getErrorMessageWithCode(), e);
            throw new VASCOException(AlRayanError.ERROR_CONVERTING_RESPONSE_TO_SOAP.getErrorMessageWithCode(), e);
        }
    }

    /**
     * Converts the string response to a SOAP message.
     *
     * @param message string response
     * @return SOAP message
     * @throws SOAPException thrown when error on constructing a SOAP message
     * @throws IOException   thrown when error on creating the message
     */
    private static SOAPMessage convertResponseToSOAPMessage(String message) throws SOAPException, IOException {
        InputStream inputStream = new ByteArrayInputStream(message.getBytes());
        return new MessageFactoryImpl().createMessage(null, inputStream);
    }
}
