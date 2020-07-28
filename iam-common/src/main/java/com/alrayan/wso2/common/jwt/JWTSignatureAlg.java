package com.alrayan.wso2.common.jwt;

/**
 * JWT builder class.
 *
 * @since 1.0.0
 */
public enum JWTSignatureAlg {

    SHA256_WITH_RSA("RS256"), NONE("none");

    private String jwsCompliantCode;

    JWTSignatureAlg(String s) {
        jwsCompliantCode = s;
    }

    public String getJwsCompliantCode() {
        return jwsCompliantCode;
    }
}
