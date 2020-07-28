/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.alrayan.wso2.webapp.management.serviceclient.beans;

import org.wso2.carbon.identity.mgt.beans.User;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Store attributes required for password reset
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"user", "code", "password"})
@XmlRootElement(name = "userPassword")
public class UserPassword implements Serializable {
    @XmlElement(required = true)
    private User user;

    @XmlElement(required = true)
    private String password;

    @XmlElement(required = true)
    private String code;

    public UserPassword() {
    }

    public UserPassword(User user, String password, String code) {
        this.user = user;
        this.password = password;
        this.code = code;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
