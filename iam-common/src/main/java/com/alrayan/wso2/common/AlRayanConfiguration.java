package com.alrayan.wso2.common;

import com.alrayan.wso2.common.utils.FileUtils;

import java.util.Properties;

/**
 * Enum defining the default configuration properties and values.
 *
 * @since 1.0.0
 */
public enum AlRayanConfiguration {

    /**
     * Maximum number of HTTP connections to have per route. This config is used when invoking external services.
     */
    HTTP_CLIENT_MAX_CONNECTIONS_PER_ROUTE("http.client.max.connections.per.route", "20"),

    /**
     * Maximum number of HTTP connections to have. This config is used when invoking external services.
     */
    HTTP_CLIENT_MAX_CONNECTIONS("http.client.max.connections", "200"),

    /**
     * HTTP client request timeout in seconds. This timeout config will be used when invoking external services.
     */
    HTTP_CLIENT_REQUEST_TIME_OUT_SECONDS("http.client.request.timeout", "30"),

    /**
     * Al Rayan PSU user store domain name.
     */
    AL_RAYAN_USERSTORE_PSU("al.rayan.userstore.psu", "PSU.ALRAYAN.WSO2"),

    /**
     * Digital bank user role name.
     */
    AL_RAYAN_DIGITAL_BANK_USER_ROLE("al.rayan.role.digital.bank", "digital-bank-user"),

    /**
     * VASCO endpoint.
     */
    VASCO_AUTHENTICATION_URL("vasco.authentication.url", null),

    /**
     * Transport key store name (ex: wso2carbon.jks). This is used to get the private key/public key for PIN
     * encryption/decryption.
     */
    INTERNAL_KEY_STORE_PATH("alrayan.internal.keystore.path", null),

    /**
     * Transport key store alias. This is used to get the private key/public key for PIN encryption/decryption.
     */
    INTERNAL_KEY_STORE_ALIAS("alrayan.internal.keystore.alias", null),

    /**
     * Transport key store Password. This is used to get the private key/public key for PIN encryption/decryption.
     */
    INTERNAL_KEY_STORE_PASSWORD("alrayan.internal.keystore.password", null),

    /**
     * Reset credentials endpoint.
     */
    RESET_CREDENTIAL_ENDPOINT("reset.credential.endpoint", null),

    /**
     * Reset credentials endpoint.
     */
    RESET_CREDENTIAL_ENDPOINT_BB("reset.credential.endpoint.bb", null),
    /**
     * Property to enable the symmetric key encryption, this is enabled by default in this use case.
     */
    SYMMETRIC_ENCRYPTION_ENABLED("alrayan.symmetric.encryption.enabled", "True"),

    /**
     * Symmetric key encryption default algorithm is added as AES.
     */
    SYMMETRIC_ENCRYPTION_ALGORITHM("alrayan.symmetricEncryption.Algorithm", "AES"),

    /**
     * Asymmetric key encryption default algorithm is added as RSA.
     */
    ASYMMETRIC_ENCRYPTION_ALGORITHM("alrayan.asymmetricEncryption.Algorithm", "RSA"),

    /**
     * VASCO desktop journey cronto image square size.
     */
    VASCO_CRONTO_IMAGE_SIZE("vasco.cronto.image.size", "10"),

    /**
     * Open-banking URL to access payment charges.
     */
    OPEN_BANKING_PAYMENT_CHARGES_URL("open.banking.payment.charges.endpoint", null),

    /**
     * Open-banking jwt issuer name.
     */
    OPEN_BANKING_JWT_ISSUER("open.banking.jwt.issuer.name", "wso2-ob"),

    /**
     * Open-banking signing enabled for the consents.
     */
    CONSENT_JWT_SIGNING_ENABLED("open.banking.jwt.consent.signing.enabled", "true"),

    /**
     * Open-banking signature verification enabled.
     */
    CONSENT_JWT_SIGNATURE_VERIFICATION_ENABLED("open.banking.jwt.signature.consent.signing.enabled", "true"),

    /**
     * Public cert alias of the dot connect platform.
     */
    DOTCONNECT_PUBLIC_CERT_ALIAS("dotconnect.cert.alias", "alrayan"),

    /**
     * Open-banking user registration signature verification enabled.
     */
    USER_REGISTRATION_SIGNATURE_VALIDATION_ENABLED("open.banking.user.registration.signing.enabled", "true"),

    /**
     * Open-banking user registration signature verification enabled.
     */
    USER_REGISTRATION_ENCRYPTION_ENABLED("open.banking.user.registration.encryption.enabled", "true"),

    /**
     * Open-banking user recovery signature verification enabled.
     */
    USER_RECOVERY_SIGNATURE_VALIDATION_ENABLED("open.banking.user.recovery.signing.enabled", "true"),

    /**
     * Open-banking user registration recovery encryption enabled.
     */
    USER_RECOVERY_ENCRYPTION_ENABLED("open.banking.user.recovery.encryption.enabled", "true"),

    /**
     * Server host URL with proxy port.
     */
    SERVERHOST_WITH_PROXYPORT("server.host.proxy.port", "https://localhost:9446"),

    /**
     * Conditional auth script file name for production.
     */
    CONDITIONAL_AUTH_SCRIPT_FILE_PRODUCTION("conditional.auth.script.production.file.name",
            "conditional.auth.script.prod.js"),

    /**
     * Conditional auth script file name for sandbox.
     */
    CONDITIONAL_AUTH_SCRIPT_FILE_SANDBOX("conditional.auth.script.sandbox.file.name",
            "conditional.auth.script.sandbox.js"),

    /**
     * Federated authenticators for production.
     */
    PRODUCTION_FEDERATED_AUTHENTICATORS("production.federated.authenticators", "ARBMobile"),

    /**
     * Federated authenticators for sandbox.
     */
    SANDBOX_FEDERATED_AUTHENTICATORS("sandbox.federated.authenticators", ""),

    /**
     * Open Banking payable accounts endpoint for sandbox environment.
     */
    SANDBOX_PAYABLE_ACCOUNTS_RETRIEVE_ENDPOINT("sandbox.payable.accounts.retrieve.endpoint",
            "https://localhost:9446/open-banking/services/bankaccounts/bankaccountservice/payable-accounts/"),

    /**
     * Open Banking sharable accounts endpoint for sandbox environment.
     */
    SANDBOX_SHARABLE_ACCOUNTS_RETRIEVE_ENDPOINT("sandbox.sharable.accounts.retrieve.endpoint",
            "https://localhost:9446/open-banking/services/bankaccounts/bankaccountservice/sharable-accounts/"),

    /**
     * Open Banking bank service endpoint for Sandbox environment.
     */
    SANDBOX_BANK_ACCOUNT_SERVICE_ENDPOINT("sandbox.bank.account.service.endpoint",
            "https://localhost:9446/open-banking/services/bankaccounts/bankaccountservice/"),

    /**
     * Open Banking multi auth pending submission service endpoint for sandbox environment.
     */
    SANDBOX_MULTI_AUTH_PENDING_SUBMISSION_SERVICE_ENDPOINT("sandbox.multi.auth.pending.submission.service.endpoint",
            "https://localhost:9446/open-banking/services/MultiAuthSubmissionDS/GetSubmissionStatus/"),

    /**
     * Open Banking multi auth pending submission service endpoint for sandbox environment.
     */
    PRODUCTION_MULTI_AUTH_PENDING_SUBMISSION_SERVICE_ENDPOINT("production.multi.auth.pending.submission.service.endpoint",
            "https://localhost:9446/open-banking/services/MultiAuthSubmissionDS/GetSubmissionStatus/"),

    /**
     * Open Banking bank service endpoint for Production environment.
     */
    PRODUCTION_BANK_ACCOUNT_SERVICE_ENDPOINT("production.bank.account.service.endpoint",
            "https://localhost:9446/open-banking/services/bankaccounts/bankaccountservice/"),

    /**
     * Open Banking bank charges endpoint for sandbox environment.
     */
    SANDBOX_BANK_CHARGES_ENDPOINT("sandbox.bank.charges.endpoint",
            "https://localhost:9446/open-banking/services/bankaccounts/bankaccountservice/payment-charges/"),

    /**
     * VASCO Sandbox URL.
     */
    SANDBOX_VASCO_ENDPOINT("sandbox.vasco.endpoint",
            "https://localhost:9446/open-banking/services/vasco/vascoservice/"),

    /**
     * Mail SMTP auth setting.
     */
    MAIL_SMTP_AUTH("mail.smtp.auth", "true"),

    /**
     * Mail SMTP starttls setting.
     */
    MAIL_SMTP_STARTTLS_ENABLE("mail.smtp.starttls.enable", "true"),

    /**
     * Mail SMTP host.
     */
    MAIL_SMTP_HOST("mail.smtp.host", "smtp.gmail.com"),

    /**
     * Mail SMTP port.
     */
    MAIL_SMTP_PORT("mail.smtp.port", "587"),

    /**
     * User recovery server URL.
     */
    USER_INFO_RECOVERY_SERVER_URL("user.info.recovery.server.url",
            "$config{APIGateway.Environments.Environment.ServerURL}"),

    /**
     * User recovery server username.
     */
    USER_INFO_RECOVERY_SERVER_USERNAME("user.info.recovery.server.username",
            "$config{APIGateway.Environments.Environment.Username}"),

    /**
     * User recovery server password.
     */
    USER_INFO_RECOVERY_SERVER_PASSWORD("user.info.recovery.server.password",
            "$config{APIGateway.Environments.Environment.Password}"),

    /**
     * User recovery AISP role name.
     */
    USER_INFO_RECOVERY_AISP_ROLE_NAME("user.info.recovery.aisp.role.name", "Internal/aispRole"),

    /**
     * User recovery PISP role name.
     */
    USER_INFO_RECOVERY_PISP_ROLE_NAME("user.info.recovery.pisp.role.name", "Internal/pispRole"),

    /**
     * User recovery PIISP role name.
     */
    USER_INFO_RECOVERY_PIISP_ROLE_NAME("user.info.recovery.piisp.role.name", "Internal/piispRole"),

    USER_REDIRECT_STORE_URL("user.login.redirect.url", "localhost"),

    USER_REDIRECT_STORE_PORT("user.login.redirect.port", "9443"),

    /**
     * PIN complex validation check
     */
    PIN_COMPLEXITY_CHECK("alrayan.pin.complexity.check", "true");


    private final String property;

    private String value;

    // Load the overridden values from the configuration.
    static {
        Properties properties = FileUtils.readConfiguration();
        for (final AlRayanConfiguration configuration : values()) {
            final String property = configuration.getProperty();
            final String defaultValue = configuration.getValue();
            configuration.setValue(properties.getProperty(property, defaultValue));
        }
    }

    /**
     * Sets the config property and value.
     *
     * @param property config property
     * @param value    config value
     */
    AlRayanConfiguration(final String property, final String value) {
        this.property = property;
        this.value = value;
    }

    /**
     * Returns the config property.
     *
     * @return config property
     */
    public String getProperty() {
        return property;
    }

    /**
     * Returns the config value.
     *
     * @return config value
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the config value.
     *
     * @param value config value
     */
    public void setValue(String value) {
        this.value = value;
    }
}
