package com.alrayan.wso2.vasco.authentication;

import com.alrayan.wso2.common.AlRayanConfiguration;
import com.alrayan.wso2.common.AlRayanError;
import com.alrayan.wso2.common.exception.HTTPClientException;
import com.alrayan.wso2.common.utils.HTTPClientUtil;
import com.alrayan.wso2.vasco.VASCOCommand;
import com.alrayan.wso2.vasco.VASCOException;
import com.alrayan.wso2.vasco.model.SecureChallengeResult;
import com.alrayan.wso2.vasco.soap.SOAPElementType;
import com.alrayan.wso2.vasco.soap.SOAPNamespace;
import com.alrayan.wso2.vasco.soap.SOAPUtil;
import com.alrayan.wso2.vasco.soap.SOAPValueElement;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;
import org.json.JSONArray;
import org.apache.axiom.util.base64.Base64Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import javax.xml.soap.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * This class is responsible for executing the VASCO secure challenge command.
 *
 * @since 1.0.0
 */
public class SignGenCommand implements VASCOCommand<SecureChallengeResult> {

    private static final Logger log = LoggerFactory.getLogger(SignGenCommand.class);
    private String userId;
    private String message;
    private String appName;
    private String dataset;
    private String type;

    /**
     * Constructs an instance of {@link SignGenCommand}.
     *
     * @param userId  Salesforce ID of the user
     * @param message message to embed in the CRONTO image
     * @param appName service provider application name
     */
    public SignGenCommand(String userId, String message, String appName, String dataset, String type) {
        this.userId = userId;
        this.message = message;
        this.appName = appName;
        this.dataset = dataset;
        this.type = type;
    }

    @Override
    public SecureChallengeResult execute() throws VASCOException {
        HTTPClientUtil.Response response = null;
        try {
            response = invokeGetSignatureChallenge(userId, message, appName,dataset,type);
            boolean isSuccess = SOAPUtil.isStatSuccess(response);
            if (!isSuccess) {
                return getErrorSecureChallengeResult(response);
            }
            String requestMessage = SOAPUtil.getSOAPBodyAttributeValue(response, "SIGNFLD_REQUEST_MESSAGE")
                    .replace("\n", "");
            String challengeKey = SOAPUtil.getSOAPBodyAttributeValue(response, "SIGNFLD_REQUEST_KEY");
            return new SecureChallengeResult(challengeKey, requestMessage);
        } catch (NoSuchElementException e) {
            log.error(AlRayanError.VASCO_SECURE_CHALLENGE_RESPONSE_NOT_FOUND.getErrorMessageWithCode() + " - {" +
                      "response body: " + (response != null ? response.getResponseBody() : "") +
                      "}");
            throw new VASCOException(AlRayanError.VASCO_SECURE_CHALLENGE_RESPONSE_NOT_FOUND
                    .getErrorMessageWithCode(), e);
        }
    }

    /**
     * Returns the erroneous secure challenge result.
     *
     * @param response HTTP response
     * @return erroneous secure challenge result
     */
    private SecureChallengeResult getErrorSecureChallengeResult(HTTPClientUtil.Response response) {
        SecureChallengeResult secureChallengeResult = new SecureChallengeResult(null, null);
        secureChallengeResult.setError(true);
        try {
            String error = SOAPUtil.getSOAPBodyAttributeValue(response, "SIGNFLD_STATUS_MESSAGE");
            secureChallengeResult.setErrorMessage(error);
        } catch (VASCOException | NoSuchElementException e) {
            log.error("Error on getting VASCO secure challenge error message - {" +
                      "response body: " + response.getResponseBody() +
                      "}");
        }
        return secureChallengeResult;
    }


    /**
     * Obtains the secure code challenge.
     *
     * @param userId  user ID to obtain the secure code challenge
     * @param message message to embed in the CRONTO image
     * @param appName service provider application name
     * @return {@link HTTPClientUtil.Response} containing the secure code challenge
     * @throws VASCOException thrown when error on preparing request or sending the request
     */
    private HTTPClientUtil.Response invokeGetSignatureChallenge(String userId, String message, String appName,
                                                                String dataset,String type)
            throws VASCOException {
        String vascoURL = "";
        String soapMessageString = "";
        try {
            SOAPMessage soapMessage = SOAPUtil.createSOAPMessage();
            SOAPBody soapBody = SOAPUtil.getSOAPBody(soapMessage, SOAPNamespace.SIG);

            QName qname = new QName(SOAPNamespace.SIG.getPrefix() + ":genRequest");
            SOAPBodyElement logonElement = soapBody.addBodyElement(qname);
            SOAPElement attributeSetElement = logonElement.addChildElement("attributeSet");

            Map<String, SOAPValueElement> attributeMap = new HashMap<>();
            attributeMap.put("SIGNFLD_COMPONENT_TYPE", new SOAPValueElement("SIGNATURE", SOAPElementType.STRING));
            attributeMap.put("SIGNFLD_USERID", new SOAPValueElement(userId, SOAPElementType.STRING));
            attributeMap.put("SIGNFLD_DOMAIN", new SOAPValueElement("master", SOAPElementType.STRING));
            SOAPUtil.createAttributeSOAPElement(attributeSetElement, attributeMap);

            SOAPElement dataFieldList = logonElement.addChildElement("dataFieldList");
            Map<String, String> dataFieldMap = new HashMap<>();
            // DotConnect uses this ID to identify requests coming from WSO2
            dataFieldMap.put("ID", "9:1:1");

            //Differentiating Payments with COF and Accounts
            if(StringUtils.isEmpty(dataset) || StringUtils.isEmpty(type)){
                // this means Accounts or COF
            } else {
                String paymentInitiationData = new String(Base64Utils.decode(dataset));
                if(!StringUtils.isEmpty(paymentInitiationData)) {
                    JSONArray jaPaymentInitiationData = new JSONArray(paymentInitiationData);
                    for (int i = 0; i < jaPaymentInitiationData.length(); i++) {
                        // Trying to reduce the character count as much as possible in data fields. Otherwise cronto image generation would fail.
                        if(jaPaymentInitiationData.getString(i).contains("Payment Type")) {
                            dataFieldMap.put("TYPE", jaPaymentInitiationData.getString(i).split(":")[1].replace("Payments","").replace("International","Int'l").trim());
                        }
                        else if(jaPaymentInitiationData.getString(i).contains("Instructed Amount") &&
                                jaPaymentInitiationData.getString(i).split(":")[1] != null) {
                            dataFieldMap.put("AMT",  jaPaymentInitiationData.getString(i).split(":")[1].trim());
                        }
                        else if(jaPaymentInitiationData.getString(i).contains("Instructed Currency") &&
                                jaPaymentInitiationData.getString(i).split(":")[1] != null) {
                            dataFieldMap.put("CCY", jaPaymentInitiationData.getString(i).split(":")[1].trim());
                        }
                        else if(jaPaymentInitiationData.getString(i).contains("Creditor Account ID ")) {
                            dataFieldMap.put("A/C", jaPaymentInitiationData.getString(i).split(":")[1]);
                        }
                    }
                }
            }
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
            log.error(AlRayanError.ERROR_CONSTRUCTING_SOAP_MESSAGE.getErrorMessageWithCode());
            throw new VASCOException(AlRayanError.ERROR_CONSTRUCTING_SOAP_MESSAGE.getErrorMessageWithCode(), e);
        } catch (Exception e) {
            throw new VASCOException(AlRayanError.ERROR_CONSTRUCTING_SOAP_MESSAGE.getErrorMessageWithCode(), e);
        }
    }

}
