package com.alrayan.wso2.vasco.authentication;

import com.alrayan.wso2.common.AlRayanConfiguration;
import com.alrayan.wso2.common.AlRayanError;
import com.alrayan.wso2.common.exception.HTTPClientException;
import com.alrayan.wso2.common.utils.HTTPClientUtil;
import com.alrayan.wso2.vasco.VASCOCommand;
import com.alrayan.wso2.vasco.VASCOException;
import com.alrayan.wso2.vasco.soap.SOAPElementType;
import com.alrayan.wso2.vasco.soap.SOAPNamespace;
import com.alrayan.wso2.vasco.soap.SOAPUtil;
import com.alrayan.wso2.vasco.soap.SOAPValueElement;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import javax.xml.soap.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * This class is responsible for executing the VASCO auth user challenge command.
 *
 * @since 1.0.0
 */
public class AuthUserSigGenCommand implements VASCOCommand<Boolean> {

    private static final Logger log = LoggerFactory.getLogger(AuthUserSigGenCommand.class);
    private String userId;
    private String password;
    private String secureChallengeCode;
    private String appName;

    /**
     * Constructs an instance of {@link AuthUserSigGenCommand}.
     *
     * @param userId              Salesforce ID of the user
     * @param password            CRONTO code shown by the digipass device
     * @param secureChallengeCode secure challenge code received by executing the {@link SecureChallengeCommand}
     * @param appName             service provider application name
     */
    public AuthUserSigGenCommand(String userId, String password, String secureChallengeCode, String appName) {
        this.userId = userId;
        this.password = password;
        this.secureChallengeCode = secureChallengeCode;
        this.appName = appName;
    }

    @Override
    public Boolean execute() throws VASCOException {
        HTTPClientUtil.Response response = null;
        try {
            response = invokeAuthSignGenKey(userId, password, secureChallengeCode,
                    appName);
            return SOAPUtil.isStatSuccess(response);
        } catch (NoSuchElementException e) {
            log.error(AlRayanError.VASCO_AUTH_USER_CHALLENGE_RESPONSE_NOT_FOUND.getErrorMessageWithCode() + " - {" +
                      "response body: " + (response != null ? response.getResponseBody() : "") +
                      "}");
            throw new VASCOException(AlRayanError.VASCO_AUTH_USER_CHALLENGE_RESPONSE_NOT_FOUND
                    .getErrorMessageWithCode(), e);
        }
    }

    /**
     * Obtains the challenge key for the obtained secure code challenge.
     *
     * @param userId              user ID
     * @param secureChallengeCode            user password
     * @param secureChallengeCode challenge key received from {@code getSecureChallenge()} method
     * @param appName             service provider application name
     * @return {@link HTTPClientUtil.Response} containing the secure code challenge
     * @throws VASCOException thrown when error on preparing request or sending the request
     */
    private HTTPClientUtil.Response invokeAuthSignGenKey(String userId, String signature,
                                                         String secureChallengeCode, String appName)
            throws VASCOException {
        String soapMessageString = "";
        String vascoURL = "";
        try {
            SOAPMessage soapMessage = SOAPUtil.createSOAPMessage();
            SOAPBody soapBody = SOAPUtil.getSOAPBody(soapMessage, SOAPNamespace.SIG);

            QName qname = new QName(SOAPNamespace.SIG.getPrefix() + ":authSignature");
            SOAPBodyElement logonElement = soapBody.addBodyElement(qname);

            SOAPElement attributeSetElement = logonElement.addChildElement("attributeSet");
            Map<String, SOAPValueElement> attributeMap = new HashMap<>();
            attributeMap.put("SIGNFLD_COMPONENT_TYPE", new SOAPValueElement("SIGNATURE", SOAPElementType.STRING));
            attributeMap.put("SIGNFLD_USERID", new SOAPValueElement(userId, SOAPElementType.STRING));
            attributeMap.put("SIGNFLD_DOMAIN", new SOAPValueElement("master", SOAPElementType.STRING));
            attributeMap.put("SIGNFLD_SIGNATURE", new SOAPValueElement(signature, SOAPElementType.STRING));
            attributeMap.put("SIGNFLD_REQUEST_KEY", new SOAPValueElement(secureChallengeCode, SOAPElementType.STRING));

            SOAPUtil.createAttributeSOAPElement(attributeSetElement, attributeMap);

            // VASCO gives an error if dataFieldList element is not available.
            SOAPElement dataFieldList = logonElement.addChildElement("dataFieldList");
            Map<String, String> dataFieldMap = new HashMap<>();
            dataFieldMap.put("ID", "9:1:1");
            SOAPUtil.createDataFeildSOAPElement(dataFieldList, dataFieldMap);

            soapMessageString = SOAPUtil.convertSOAPMessageToString(soapMessage);


            vascoURL = appName.endsWith("_SANDBOX") ?
                       AlRayanConfiguration.SANDBOX_VASCO_ENDPOINT.getValue() :
                       AlRayanConfiguration.VASCO_AUTHENTICATION_URL.getValue();
            if (StringUtils.isEmpty(vascoURL)) {
                throw new VASCOException(AlRayanError.VASCO_AUTHENTICATION_URL_NOT_DEFINED.getErrorMessageWithCode());
            }
            return HTTPClientUtil.post(vascoURL)
                    .setEntity(soapMessageString, ContentType.APPLICATION_XML)
                    .execute();
        } catch (HTTPClientException e) {
            log.error(AlRayanError.ERROR_HTTP_REQUEST_TO_VASCO.getErrorMessageWithCode() + " - {" +
                      "VASCO URL: " + vascoURL + "," +
                      "SOAP request string: " + soapMessageString +
                      "}");
            throw new VASCOException(AlRayanError.ERROR_HTTP_REQUEST_TO_VASCO.getErrorMessageWithCode(), e);
        } catch (SOAPException e) {
            log.error(AlRayanError.ERROR_CONSTRUCTING_SOAP_BODY_CONTENT.getErrorMessageWithCode() + " - {" +
                      "VASCO URL: " + vascoURL + "," +
                      "SOAP request string: " + soapMessageString +
                      "}");
            throw new VASCOException(AlRayanError.ERROR_CONSTRUCTING_SOAP_BODY_CONTENT.getErrorMessageWithCode(), e);
        } catch (IOException e) {
            log.error(AlRayanError.ERROR_CONSTRUCTING_SOAP_BODY_CONTENT.getErrorMessageWithCode());
            throw new VASCOException(AlRayanError.ERROR_CONSTRUCTING_SOAP_MESSAGE.getErrorMessageWithCode(), e);
        }
    }
}
