/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 Inc. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein is strictly forbidden, unless permitted by WSO2 in accordance with
 * the WSO2 Commercial License available at http://wso2.com/licenses. For specific
 * language governing the permissions and limitations under this license,
 * please see the license as well as any agreement you’ve entered into with
 * WSO2 governing the purchase of this software and any associated services.
 *
 */

package com.wso2.solutions.open.banking.services;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Path("/vascoservice/")
public class VASCOService {

    private static final String response =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
            "xmlns:AUTH-TYPES=\"http://www.vasco.com/IdentikeyServer/IdentikeyTypes/Authentication\" " +
            "xmlns:BASIC-TYPES=\"http://www.vasco.com/IdentikeyServer/IdentikeyTypes/BasicTypes.xsd\" " +
            "xmlns:CREDENTIAL-TYPES=\"http://www.vasco.com/IdentikeyServer/IdentikeyTypes/CredentialTypes.xsd\" " +
            "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" " +
            "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
            "   <soapenv:Body>\n" +
            "      <AUTH-TYPES:getSecureChallengeRespond>\n" +
            "         <results xsi:type=\"CREDENTIAL-TYPES:CredentialResults\">\n" +
            "            <resultCodes xsi:type=\"BASIC-TYPES:ResultCodes\">\n" +
            "               <returnCodeEnum>RET_SUCCESS</returnCodeEnum>\n" +
            "               <statusCodeEnum>STAT_SUCCESS</statusCodeEnum>\n" +
            "               <returnCode>0</returnCode>\n" +
            "               <statusCode>0</statusCode>\n" +
            "            </resultCodes>\n" +
            "            <resultAttribute xsi:type=\"CREDENTIAL-TYPES:CredentialAttributeSet\">\n" +
            "               <attributes xsi:type=\"CREDENTIAL-TYPES:CredentialAttribute\">\n" +
            "                  <value xsi:type=\"xsd:string\">user1</value>\n" +
            "                  <attributeID>CREDFLD_USERID</attributeID>\n" +
            "               </attributes>\n" +
            "               <attributes xsi:type=\"CREDENTIAL-TYPES:CredentialAttribute\">\n" +
            "                  <value xsi:type=\"xsd:string\">master</value>\n" +
            "                  <attributeID>CREDFLD_DOMAIN</attributeID>\n" +
            "               </attributes>\n" +
            "               <attributes xsi:type=\"CREDENTIAL-TYPES:CredentialAttribute\">\n" +
            "                  <value xsi:type=\"xsd:string\">1481259140</value>\n" +
            "                  <attributeID>CREDFLD_CHALLENGE_KEY</attributeID>\n" +
            "               </attributes>\n" +
            "               <attributes xsi:type=\"CREDENTIAL-TYPES:CredentialAttribute\">\n" +
            "                  <value xsi:type=\"xsd:string\">VDS0000001-7</value>\n" +
            "                  <attributeID>CREDFLD_SERIAL_NO</attributeID>\n" +
            "               </attributes>\n" +
            "               <attributes xsi:type=\"CREDENTIAL-TYPES:CredentialAttribute\">\n" +
            "                  <value xsi:type=\"xsd:string\">0041C3E4000004E4D1F2E9BF3F1DA9D1619A69E7BDDC3B04E" +
            "8563F3CAEC7D522DB3361E673D9594B9754A7650E35C1FDF2D59175EF3330AA2ABD4DA322E4</value>\n" +
            "                  <attributeID>CREDFLD_REQUEST_MESSAGE</attributeID>\n" +
            "               </attributes>\n" +
            "            </resultAttribute>\n" +
            "            <errorStack xsi:type=\"BASIC-TYPES:ErrorStack\" />\n" +
            "         </results>\n" +
            "      </AUTH-TYPES:getSecureChallengeRespond>\n" +
            "   </soapenv:Body>\n" +
            "</soapenv:Envelope>";


    private static final String response2 =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
                    "xmlns:SIGN-SCENARIO=\"http://www.vasco.com/IdentikeyServer/Scenarios/Signature\" " +
                    "xmlns:SIGN-TYPES=\"http://www.vasco.com/IdentikeyServer/IdentikeyTypes/Signature\" " +
                    "xmlns:CREDENTIAL-TYPES=\"http://www.vasco.com/IdentikeyServer/IdentikeyTypes/CredentialTypes.xsd\" " +
                    "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" " +
                    "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                    "<soapenv:Body>" +
                    "<SIGN-TYPES:genRequestResponse>" +
                    "<results xsi:type=\"SIGNATURE-TYPES:SignatureResults\">"+
                    "<resultCodes xsi:type=\"BASIC-TYPES:ResultCodes\">"+
                    "<returnCodeEnum xsi:type=\"BASIC-TYPES:ReturnCodeEnum\">RET_SUCCESS</returnCodeEnum>"+
                    "<statusCodeEnum xsi:type=\"BASIC-TYPES:StatusCodeEnum\">STAT_SUCCESS</statusCodeEnum>"+
                    "<returnCode xsi:type=\"xsd:int\">0</returnCode>"+
                    "<statusCode xsi:type=\"xsd:int\">0</statusCode>"+
                    "</resultCodes>"+
                    "<resultAttribute xsi:type=\"SIGNATURE-TYPES:SignatureAttributeSet\">"+
                    "<attributes xsi:type=\"SIGNATURE-TYPES:SignatureAttribute\">"+
                        "<value xsi:type=\"xsd:string\">sfdc-pc000878363</value>"+
                        "<attributeID xsi:type=\"SIGNATURE-TYPES:SignatureAttributeIDEnum\">SIGNFLD_USERID</attributeID>"+
                    "</attributes>"+
                    "<attributes xsi:type=\"SIGNATURE-TYPES:SignatureAttribute\">"+
                        "<value xsi:type=\"xsd:string\">master</value>"+
                        "<attributeID xsi:type=\"SIGNATURE-TYPES:SignatureAttributeIDEnum\">SIGNFLD_DOMAIN</attributeID>"+
                    "</attributes>"+
                    "<attributes xsi:type=\"SIGNATURE-TYPES:SignatureAttribute\">"+
                        "<value xsi:type=\"xsd:string\">FDT0276028</value>"+
                        "<attributeID xsi:type=\"SIGNATURE-TYPES:SignatureAttributeIDEnum\">SIGNFLD_SERIAL_NO</attributeID>"+
                    "</attributes>"+
                    "<attributes xsi:type=\"SIGNATURE-TYPES:SignatureAttribute\">"+
                        "<value xsi:type=\"xsd:string\">7069426004</value>"+
                        "<attributeID xsi:type=\"SIGNATURE-TYPES:SignatureAttributeIDEnum\">SIGNFLD_REQUEST_KEY</attributeID>"+
                    "</attributes>"+
                    "<attributes xsi:type=\"SIGNATURE-TYPES:SignatureAttribute\">"+
                        "<value xsi:type=\"xsd:string\">00C15FE504363C6072588D3D489F52881A80452FCC2FFAF09CDF8961822040384ED2F9A6D56EA07BB7BA4E66D4AB7F1D554E194F3614EBFBD35F6A97</value>"+
                        "<attributeID xsi:type=\"SIGNATURE-TYPES:SignatureAttributeIDEnum\">SIGNFLD_REQUEST_MESSAGE</attributeID>"+
                    "</attributes>"+
                "</resultAttribute>"+
                "<errorStack xsi:type=\"BASIC-TYPES:ErrorStack\"></errorStack>"+
           "</results>"+
        "</SIGN-TYPES:genRequestResponse>"+
    "</soapenv:Body>"+
                    "</soapenv:Envelope>";

    @POST
    @Path("/")
    @Produces("application/xml")
    @Consumes("application/xml")
    public Response iASAuthentication(String request) {
        return Response.status(200).entity(response2).build();
    }
}
