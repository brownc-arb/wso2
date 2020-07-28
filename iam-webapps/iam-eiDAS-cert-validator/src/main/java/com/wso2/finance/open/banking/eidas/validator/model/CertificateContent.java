/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 Inc. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein is strictly forbidden, unless permitted by WSO2 in accordance with
 * the WSO2 Commercial License available at http://wso2.com/licenses. For specific
 * language governing the permissions and limitations under this license,
 * please see the license as well as any agreement you’ve entered into with
 * WSO2 governing the purchase of this software and any associated services.
 */

package com.wso2.finance.open.banking.eidas.validator.model;

import org.apache.commons.lang.StringUtils;

import java.util.Date;
import java.util.List;

/**
 * Class that contains extracted PSD2 attributes from the certificate.
 */
public class CertificateContent {

    private String pspAuthorisationNumber;
    private List<String> pspRoles;
    private String name;
    private String ncaName;
    private String ncaId;
    private Date notAfter;
    private Date notBefore;

    public CertificateContent(String pspAuthorisationNumber, List<String> pspRoles, String name, String ncaName,
                              String ncaId, Date notAfter, Date notBefore) {

        this.pspAuthorisationNumber = pspAuthorisationNumber;
        this.pspRoles = pspRoles;
        this.name = name;
        this.ncaName = ncaName;
        this.ncaId = ncaId;
        this.notAfter = new Date(notAfter.getTime());
        this.notBefore = new Date(notBefore.getTime());
    }

    public String getPspAuthorisationNumber() {

        return pspAuthorisationNumber;
    }

    public void setPspAuthorisationNumber(String pspAuthorisationNumber) {

        this.pspAuthorisationNumber = pspAuthorisationNumber;
    }

    public List<String> getPspRoles() {

        return pspRoles;
    }

    public void setPspRoles(List<String> pspRoles) {

        this.pspRoles = pspRoles;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getNcaName() {

        return ncaName;
    }

    public void setNcaName(String ncaName) {

        this.ncaName = ncaName;
    }

    public String getNcaId() {

        return ncaId;
    }

    public void setNcaId(String ncaId) {

        this.ncaId = ncaId;
    }

    public Date getNotAfter() {

        return new Date(notAfter.getTime());
    }

    public void setNotAfter(Date notAfter) {

        this.notAfter = new Date(notAfter.getTime());
    }

    public Date getNotBefore() {

        return new Date(notBefore.getTime());
    }

    public void setNotBefore(Date notBefore) {

        this.notBefore = new Date(notBefore.getTime());
    }

    @Override
    public String toString() {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{");
        if (StringUtils.isNotEmpty(pspAuthorisationNumber)) {
            stringBuilder.append("\"PSP Authorization Number\": \"" + pspAuthorisationNumber + "\"");
            stringBuilder.append(", ");
        }
        if (pspRoles != null && !pspRoles.isEmpty()) {
            stringBuilder.append("\"PSP Roles\":  \"" + String.join(" ", pspRoles) + "\"");
            stringBuilder.append(", ");
        }
        if (StringUtils.isNotEmpty(name)) {
            stringBuilder.append("\"Name\": \"" + name + "\"");
            stringBuilder.append(", ");
        }
        if (StringUtils.isNotEmpty(ncaName)) {
            stringBuilder.append("\"NCA Name\": \"" + ncaName + "\"");
            stringBuilder.append(", ");
        }
        if (StringUtils.isNotEmpty(ncaId)) {
            stringBuilder.append("\"NCA ID\": \"" + ncaId + "\"");
            stringBuilder.append(", ");
        }
        if (notAfter != null) {
            stringBuilder.append("\"Not After\": \"" + notAfter.toString() + "\"");
            stringBuilder.append(", ");
        }
        if (notBefore != null) {
            stringBuilder.append("\"Not Before\": \"" + notBefore.toString() + "\"");
        }
        stringBuilder.append("}");
        return stringBuilder.toString();
    }
}
