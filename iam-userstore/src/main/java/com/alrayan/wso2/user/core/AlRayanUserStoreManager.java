package com.alrayan.wso2.user.core;

import com.alrayan.wso2.common.AlRayanConfiguration;
import com.alrayan.wso2.common.AlRayanConstants;
import com.alrayan.wso2.common.AlRayanError;
import com.alrayan.wso2.common.exception.PINValidationFailedException;
import com.alrayan.wso2.common.exception.StringDecryptionException;
import com.alrayan.wso2.common.exception.UserNameNotFoundException;
import com.alrayan.wso2.common.utils.KeyStoreUtils;
import org.apache.axiom.om.util.Base64;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.user.api.Properties;
import org.wso2.carbon.user.api.Property;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.claim.ClaimManager;
import org.wso2.carbon.user.core.jdbc.JDBCRealmConstants;
import org.wso2.carbon.user.core.jdbc.JDBCRoleContext;
import org.wso2.carbon.user.core.jdbc.JDBCUserStoreConstants;
import org.wso2.carbon.user.core.jdbc.JDBCUserStoreManager;
import org.wso2.carbon.user.core.profile.ProfileConfigurationManager;
import org.wso2.carbon.user.core.util.DatabaseUtil;
import org.wso2.carbon.utils.Secret;
import org.wso2.carbon.utils.UnsupportedSecretTypeException;
import org.wso2.carbon.utils.dbcreator.DatabaseCreator;

import uk.co.alrayan.PinUtils;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;

/**
 * User store manager class to handle user store operations.
 * <p>
 * The username is Salesforce ID while the username entered by the user is a unique claim.
 *
 * @since 1.0.0
 */
public class AlRayanUserStoreManager extends JDBCUserStoreManager {

    private static final Logger log = LoggerFactory.getLogger(AlRayanUserStoreManager.class);

    /**
     * Constructs an instance of {@link AlRayanUserStoreManager}.
     */
    public AlRayanUserStoreManager() {
        super();
    }

    /**
     * Constructs an instance of {@link AlRayanUserStoreManager}.
     *
     * @param realmConfig realm configuration
     * @param tenantId    tenant ID
     * @throws UserStoreException thrown when error on initialising {@link AlRayanUserStoreManager}
     */
    public AlRayanUserStoreManager(RealmConfiguration realmConfig, int tenantId) throws UserStoreException {
        super(realmConfig, tenantId);
    }

    /**
     * Constructs an instance of {@link AlRayanUserStoreManager}.
     *
     * @param ds          data source
     * @param realmConfig realm configuration
     * @param tenantId    tenant ID
     * @param addInitData whether to add initialising data or not
     * @throws UserStoreException thrown when error on initialising {@link AlRayanUserStoreManager}
     */
    public AlRayanUserStoreManager(DataSource ds, RealmConfiguration realmConfig, int tenantId, boolean addInitData)
            throws UserStoreException {
        super(ds, realmConfig, tenantId, addInitData);
    }

    /**
     * Constructs an instance of {@link AlRayanUserStoreManager}.
     *
     * @param dataSource  data source
     * @param realmConfig realm configuration
     * @throws UserStoreException thrown when error on initialising {@link AlRayanUserStoreManager}
     */
    public AlRayanUserStoreManager(DataSource dataSource, RealmConfiguration realmConfig) throws UserStoreException {
        super(dataSource, realmConfig);
    }

    /**
     * Constructs an instance of {@link AlRayanUserStoreManager}.
     *
     * @param realmConfig    realm configuration
     * @param properties     properties
     * @param claimManager   claim manager
     * @param profileManager profile manager
     * @param realm          user realm
     * @param tenantId       tenant ID
     * @throws UserStoreException thrown when error on initialising {@link AlRayanUserStoreManager}
     */
    public AlRayanUserStoreManager(RealmConfiguration realmConfig, Map<String, Object> properties,
                                   ClaimManager claimManager, ProfileConfigurationManager profileManager,
                                   UserRealm realm, Integer tenantId) throws UserStoreException {
        super(realmConfig, properties, claimManager, profileManager, realm, tenantId);
    }

    /**
     * Constructs an instance of {@link AlRayanUserStoreManager}.
     *
     * @param realmConfig    realm configuration
     * @param properties     properties
     * @param claimManager   claim manager
     * @param profileManager profile manager
     * @param realm          user realm
     * @param tenantId       tenant ID
     * @param skipInitData   whether to skip initialising data
     * @throws UserStoreException thrown when error on initialising {@link AlRayanUserStoreManager}
     */
    public AlRayanUserStoreManager(RealmConfiguration realmConfig, Map<String, Object> properties,
                                   ClaimManager claimManager, ProfileConfigurationManager profileManager,
                                   UserRealm realm, Integer tenantId, boolean skipInitData) throws UserStoreException {
        super(realmConfig, properties, claimManager, profileManager, realm, tenantId, skipInitData);
    }

    @Override
    public void doAddUser(String userName, Object credential, String[] roleList, Map<String, String> claims,
                          String profileName, boolean requirePasswordChange) throws UserStoreException {
        // Add the custom user role.
        roleList = Arrays.copyOf(roleList, roleList.length + 1);
        roleList[roleList.length - 1] = AlRayanConfiguration.AL_RAYAN_DIGITAL_BANK_USER_ROLE.getValue();
        super.doAddUser(userName, credential, roleList, claims, profileName, requirePasswordChange);
    }

    @Override
    protected void persistUser(String userName, Object credential, String[] roleList,
                               Map<String, String> claims, String profileName, boolean requirePasswordChange)
            throws UserStoreException {
        Connection dbConnection = null;

        Secret credentialObj;
        try {
            credentialObj = Secret.getSecret(credential);
        } catch (UnsupportedSecretTypeException e) {
            throw new UserStoreException("Unsupported credential type", e);
        }

        try {
            dbConnection = getDBConnection();
            String sqlStmt1 = realmConfig.getUserStoreProperty(JDBCRealmConstants.ADD_USER);
            String saltValue = null;
            String password;

            if ("true".equalsIgnoreCase(realmConfig.getUserStoreProperties()
                    .get(JDBCRealmConstants.STORE_SALTED_PASSWORDS))) {
                byte[] bytes = new byte[16];
                random.nextBytes(bytes);
                saltValue = Base64.encode(bytes);
            }

            password = this.preparePassword(credentialObj, saltValue);

            // Get active username claim value.
            String activeUsernameClaim = claims.get(AlRayanConstants.CLAIM_ACTING_USERNAME);
            claims.remove(AlRayanConstants.CLAIM_ACTING_USERNAME);
            if (StringUtils.isEmpty(activeUsernameClaim)) {
                throw new UserStoreException(AlRayanError.ACTING_USERNAME_NOT_SPECIFIED.getErrorMessageWithCode());
            }

            // Get PIN code.
            String pINCode = claims.get(AlRayanConstants.CLAIM_PIN_CODE);
            claims.remove(AlRayanConstants.CLAIM_PIN_CODE);
            if (StringUtils.isEmpty(pINCode)) {
                throw new UserStoreException(AlRayanError.PIN_CODE_NOT_SPECIFIED.getErrorMessageWithCode());
            }

            // Encrypt PIN code using the configured public key.
            PublicKey publicKey = KeyStoreUtils
                    .getPublicKey(AlRayanConfiguration.INTERNAL_KEY_STORE_ALIAS.getValue(),
                            AlRayanConfiguration.INTERNAL_KEY_STORE_PASSWORD.getValue().toCharArray(),
                            AlRayanConfiguration.INTERNAL_KEY_STORE_PATH.getValue());


            if(publicKey == null) {
                 log.error("The public key is not been retrieved. Please check the al-rayan keystore for " +
                         "the existance of the proper public keys");
            }


            byte[] encryptedPINBytes = KeyStoreUtils.encryptFromPublicKey(publicKey, pINCode);
            String encodedEncryptedPIN = Base64.encode(encryptedPINBytes);

            // do all 4 possibilities
            if (sqlStmt1.contains(UserCoreConstants.UM_TENANT_COLUMN) && (saltValue == null)) {
                this.updateStringValuesToDatabase(dbConnection, sqlStmt1, userName, password, "",
                        requirePasswordChange, new Date(), tenantId, activeUsernameClaim, encodedEncryptedPIN);
            } else if (sqlStmt1.contains(UserCoreConstants.UM_TENANT_COLUMN) && (saltValue != null)) {
                this.updateStringValuesToDatabase(dbConnection, sqlStmt1, userName, password, saltValue,
                        requirePasswordChange, new Date(), tenantId, activeUsernameClaim, encodedEncryptedPIN);
            } else if (!sqlStmt1.contains(UserCoreConstants.UM_TENANT_COLUMN) && (saltValue == null)) {
                this.updateStringValuesToDatabase(dbConnection, sqlStmt1, userName, password, "",
                        requirePasswordChange, new Date(), activeUsernameClaim, encodedEncryptedPIN);
            } else {
                this.updateStringValuesToDatabase(dbConnection, sqlStmt1, userName, password, saltValue,
                        requirePasswordChange, new Date(), activeUsernameClaim, encodedEncryptedPIN);
            }

            if (roleList != null && roleList.length > 0) {

                RoleBreakdown breakdown = getSharedRoleBreakdown(roleList);
                String[] roles = breakdown.getRoles();
                // Integer[] tenantIds = breakdown.getTenantIds();

                String[] sharedRoles = breakdown.getSharedRoles();
                Integer[] sharedTenantIds = breakdown.getSharedTenantids();

                String sqlStmt2;
                String type = DatabaseCreator.getDatabaseType(dbConnection);
                if (roles.length > 0) {
                    // Adding user to the non shared roles
                    sqlStmt2 = realmConfig.getUserStoreProperty(JDBCRealmConstants.ADD_ROLE_TO_USER +
                                                                "-" + type);
                    if (sqlStmt2 == null) {
                        sqlStmt2 = realmConfig.getUserStoreProperty(JDBCRealmConstants.ADD_ROLE_TO_USER);
                    }

                    if (sqlStmt2.contains(UserCoreConstants.UM_TENANT_COLUMN)) {
                        if (UserCoreConstants.OPENEDGE_TYPE.equals(type)) {
                            DatabaseUtil.udpateUserRoleMappingInBatchMode(dbConnection, sqlStmt2,
                                    tenantId, roles, tenantId, userName, tenantId);
                        } else {
                            DatabaseUtil.udpateUserRoleMappingInBatchMode(dbConnection, sqlStmt2,
                                    roles, tenantId, userName, tenantId, tenantId);
                        }
                    } else {
                        DatabaseUtil.udpateUserRoleMappingInBatchMode(dbConnection, sqlStmt2, roleList, userName);
                    }

                }
                if (sharedRoles.length > 0) {
                    // Adding user to the shared roles
                    sqlStmt2 =
                            realmConfig.getUserStoreProperty(JDBCRealmConstants.ADD_SHARED_ROLE_TO_USER);
                    DatabaseUtil.udpateUserRoleMappingWithExactParams(dbConnection, sqlStmt2, sharedRoles, userName,
                            sharedTenantIds, tenantId);
                }
            }

            if (claims != null) {
                // add the properties
                if (profileName == null) {
                    profileName = UserCoreConstants.DEFAULT_PROFILE;
                }

                for (Map.Entry<String, String> entry : claims.entrySet()) {
                    String claimURI = entry.getKey();
                    String propName = getClaimAtrribute(claimURI, userName, null);
                    String propValue = entry.getValue();
                    addProperty(dbConnection, userName, propName, propValue, profileName);
                }
            }
            dbConnection.commit();
        } catch (Throwable e) {
            try {
                dbConnection.rollback();
            } catch (SQLException e1) {
                throw new UserStoreException("Error rollbacking add user operation", e1);
            }
            log.error("Error while persisting user : " + userName);
            throw new UserStoreException("Error while persisting user : " + userName, e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection);
        }
    }

    @Override
    public Properties getDefaultUserStoreProperties() {
        Properties properties = super.getDefaultUserStoreProperties();

        List<Property> advancedProperties = new ArrayList(JDBCUserStoreConstants.JDBC_UM_ADVANCED_PROPERTIES);
        Property isDBPUserExistingSQL = new Property(AlRayanConstants.IS_DBP_USER_EXISTING_SQL,
                "SELECT UM_ID FROM UM_USER WHERE DBP_USERNAME=? AND UM_TENANT_ID=?",
                "Is DBP User Existing SQL", null);
        Property getDBPPINCodeForDBPUserNameSQL = new Property(AlRayanConstants.GET_DBP_PIN_CODE_FOR_DBP_USERNAME_SQL,
                "SELECT DBP_PINCODE FROM UM_USER WHERE DBP_USERNAME=? AND UM_TENANT_ID=?",
                "Get DBP PIN Code For DBP Username SQL", null);
        Property getDBPPINCodeForUserNameSQL = new Property(AlRayanConstants.GET_DBP_PIN_CODE_FOR_USERNAME_SQL,
                "SELECT DBP_PINCODE FROM UM_USER WHERE UM_USER_NAME=? AND UM_TENANT_ID=?",
                "Get DBP PIN Code For DBP Username SQL", null);
        Property updateDBPPINCodeForUserNameSQL = new Property(AlRayanConstants.UPDATE_DBP_PIN_CODE_FOR_USERNAME_SQL,
                "UPDATE UM_USER SET DBP_PINCODE=? WHERE UM_USER_NAME=? AND UM_TENANT_ID=?",
                "Update DBP PIN Code For UserName SQL", null);
        Property updateDBPPINCodeForDBPUserNameSQL =
                new Property(AlRayanConstants.UPDATE_DBP_PIN_CODE_FOR_DBP_USERNAME_SQL,
                        "UPDATE UM_USER SET DBP_PINCODE=? WHERE DBP_USERNAME=? AND UM_TENANT_ID=?",
                        "Update DBP PIN Code For DBP UserName SQL", null);
        Property getUserNameForDBPUsernameSQL = new Property(AlRayanConstants.GET_USERNAME_FOR_DBP_USERNAME_SQL,
                "SELECT UM_USER_NAME FROM UM_USER WHERE DBP_USERNAME=? AND UM_TENANT_ID=?",
                "Get User Name For DBP Username SQL", null);
        Property getDBPUsernameForUserNameSQL = new Property(AlRayanConstants.GET_DBP_USERNAME_FOR_USERNAME_SQL,
                "SELECT DBP_USERNAME FROM UM_USER WHERE UM_USER_NAME=? AND UM_TENANT_ID=?",
                "Get DBP Username For User Name SQL", null);

        advancedProperties.add(isDBPUserExistingSQL);
        advancedProperties.add(getDBPPINCodeForDBPUserNameSQL);
        advancedProperties.add(getDBPPINCodeForUserNameSQL);
        advancedProperties.add(updateDBPPINCodeForUserNameSQL);
        advancedProperties.add(updateDBPPINCodeForDBPUserNameSQL);
        advancedProperties.add(getUserNameForDBPUsernameSQL);
        advancedProperties.add(getDBPUsernameForUserNameSQL);
        properties.setAdvancedProperties(advancedProperties.toArray(new Property[advancedProperties.size()]));
        return properties;
    }

    /**
     * Changes the PIN code by validating the current PIN.
     *
     * @param username   salesforce ID
     * @param currentPIN current PIN code
     * @param newPINCode new PIN code
     * @throws UserStoreException           thrown when internal server error on changing PIN code
     * @throws PINValidationFailedException thrown when current PIN validation failed against the PIN code in the
     *                                      database
     * @throws StringDecryptionException    thrown when error on obtaining key
     * @throws UserNameNotFoundException    thrown when error on obtaining key
     * @throws UnrecoverableKeyException    thrown when error on obtaining key
     * @throws CertificateException         thrown when error on obtaining key
     * @throws NoSuchAlgorithmException     thrown when error on obtaining key
     * @throws KeyStoreException            thrown when error on obtaining key
     * @throws IOException                  thrown when error on locating the keystore
     */
    public void changePIN(String username, String currentPIN, String newPINCode)
            throws UserStoreException, PINValidationFailedException, StringDecryptionException,
            UserNameNotFoundException, UnrecoverableKeyException, CertificateException, NoSuchAlgorithmException,
            KeyStoreException, IOException {
        // Current PIN validation.
        String userPIN = getUserPINForUserName(username, getTenantId());
        if (userPIN == null) {
            throw new UserNameNotFoundException(AlRayanError.USER_DOES_NOT_EXISTS.getErrorMessageWithCode());
        }
        PrivateKey privateKey = KeyStoreUtils
                .getPrivateKey(AlRayanConfiguration.INTERNAL_KEY_STORE_ALIAS.getValue(),
                        AlRayanConfiguration.INTERNAL_KEY_STORE_PASSWORD.getValue().toCharArray(),
                        AlRayanConfiguration.INTERNAL_KEY_STORE_PATH.getValue());
        String decryptedPIN = KeyStoreUtils.decryptFromPrivateKey(privateKey, Base64.decode(userPIN));

        if (StringUtils.isEmpty(currentPIN) || !currentPIN.equals(decryptedPIN) || PinUtils.checkPin(decryptedPIN) < 0) {
            throw new PINValidationFailedException(AlRayanError.CHANGE_PIN_FAILED_INCORRECT_CREDENTIALS
                    .getErrorMessageWithCode());
        }

        // Change PIN code.
        updatePIN(username, newPINCode);
    }

    /**
     * Changes the PIN code by validating the current PIN.
     *
     * @param username   salesforce ID
     * @param newPINCode new PIN code
     * @throws UserStoreException           thrown when internal server error on changing PIN code
     */
    public void changePIN(String username, String newPINCode)
            throws UserStoreException {
        updatePIN(username, newPINCode);
    }

    /**
     * Checks whether the DBP user exists in the database.
     *
     * @param dbpUsername Digital Banking Username
     * @param tenantId    tenant ID
     * @return {@code true} if the DBP user ID exists, {@code false} otherwise
     * @throws UserStoreException thrown when error on checking Digital Bank User existence
     */
    public boolean isDBPUserExist(String dbpUsername, int tenantId) throws UserStoreException {
        String value = getUMTableValue(AlRayanConstants.IS_DBP_USER_EXISTING_SQL, dbpUsername, tenantId,
                "Error on checking if the DBP username exists.");
        return value != null;
    }

    /**
     * Returns the user PIN code for the given digital banking username.
     *
     * @param dbpUsername digital banking username
     * @param tenantId    tenant ID
     * @return digital banking user PIN code
     * @throws UserStoreException thrown when error on obtaining digital banking PIN code
     */
    public String getUserPINForDBPUserName(String dbpUsername, int tenantId) throws UserStoreException {
        return getUMTableValue(AlRayanConstants.GET_DBP_PIN_CODE_FOR_DBP_USERNAME_SQL, dbpUsername, tenantId,
                "Error on obtaining user PIN code.");
    }

    /**
     * Returns the user PIN code for the given salesforce ID.
     *
     * @param username salesforce ID
     * @param tenantId tenant ID
     * @return digital banking user PIN code
     * @throws UserStoreException thrown when error on obtaining digital banking PIN code
     */
    public String getUserPINForUserName(String username, int tenantId) throws UserStoreException {
        return getUMTableValue(AlRayanConstants.GET_DBP_PIN_CODE_FOR_USERNAME_SQL, username, tenantId,
                "Error on obtaining user PIN code.");
    }

    /**
     * Returns the username (salesforce ID) of the given digital banking username.
     *
     * @param digitalBankingUsername digital banking username
     * @param tenantId               tenant ID
     * @return username (salesforce ID)
     * @throws UserStoreException thrown when error on obtaining username (salesforce ID)
     */
    public String getUserNameForDBPUserName(String digitalBankingUsername, int tenantId) throws UserStoreException {
        return getUMTableValue(AlRayanConstants.GET_USERNAME_FOR_DBP_USERNAME_SQL, digitalBankingUsername, tenantId,
                "Error on obtaining user name (salesforce ID) for digital banking username.");
    }

    /**
     * Returns the digital banking username for the given digital salesforce ID.
     *
     * @param salesforceId salesforce ID
     * @param tenantId     tenant ID
     * @return username (digital banking username)
     * @throws UserStoreException thrown when error on obtaining digital banking username
     */
    public String getDBPUserNameForUsername(String salesforceId, int tenantId) throws UserStoreException {
        return getUMTableValue(AlRayanConstants.GET_DBP_USERNAME_FOR_USERNAME_SQL, salesforceId, tenantId,
                "Error on obtaining digital banking username for Salesforce ID.");
    }

    /**
     * Updates the current PIN with the new PIN.
     *
     * @param username   salesforce ID
     * @param newPINCode new PIN code
     * @throws UserStoreException thrown when error on updating PIN
     */
    private void updatePIN(String username, String newPINCode) throws UserStoreException {
        PreparedStatement preparedStatement = null;
        Connection dbConnection = null;

        try {
            // Encrypt PIN
            PublicKey publicKey = KeyStoreUtils
                    .getPublicKey(AlRayanConfiguration.INTERNAL_KEY_STORE_ALIAS.getValue(),
                            AlRayanConfiguration.INTERNAL_KEY_STORE_PASSWORD.getValue().toCharArray(),
                            AlRayanConfiguration.INTERNAL_KEY_STORE_PATH.getValue());
            byte[] encryptedPINBytes = KeyStoreUtils.encryptFromPublicKey(publicKey, newPINCode);
            String encodedEncryptedPIN = Base64.encode(encryptedPINBytes);

            // Do DB transaction
            dbConnection = getDBConnection();
            String sqlStatement = this.realmConfig
                    .getUserStoreProperty(AlRayanConstants.UPDATE_DBP_PIN_CODE_FOR_USERNAME_SQL);
            preparedStatement = dbConnection.prepareStatement(sqlStatement);
            preparedStatement.setString(1, encodedEncryptedPIN);
            preparedStatement.setString(2, username);
            preparedStatement.setInt(3, tenantId);

            int count = preparedStatement.executeUpdate();
            if (count == 0) {
                log.debug("Change PIN request - no rows updated.");
            }
            dbConnection.commit();
        } catch (StringDecryptionException | IOException | CertificateException | NoSuchAlgorithmException |
                UnrecoverableKeyException | KeyStoreException e) {
            throw new UserStoreException(AlRayanError.PIN_ENCRYPTION_ERROR.getErrorMessageWithCode(), e);
        } catch (Exception e) {
            throw new UserStoreException(AlRayanError.PIN_CHANGE_INTERNAL_SERVER_ERROR.getErrorMessageWithCode(), e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection, preparedStatement);
        }
    }

    /**
     * Returns the value from the UM_USER table matching the given query value.
     * <p>
     * Please note that the query value should be unique (primary key or unique constraint).
     *
     * @param advanceUserStorePropertyName advance user store property name
     * @param queryValue                   value to obtain the matching result
     * @param tenantId                     tenant ID
     * @param error                        exception error message
     * @return value matching the given query value in the database table
     * @throws UserStoreException thrown when error on quering the database
     */
    private String getUMTableValue(String advanceUserStorePropertyName, String queryValue, int tenantId, String error)
            throws UserStoreException {
        Connection dbConnection = null;

        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        String value = null;
        try {
            dbConnection = getDBConnection();
            String sqlStatement = this.realmConfig.getUserStoreProperty(advanceUserStorePropertyName);
            preparedStatement = dbConnection.prepareStatement(sqlStatement);
            preparedStatement.setString(1, queryValue);
            preparedStatement.setInt(2, tenantId);

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                value = resultSet.getString(1);
            }
            return value;
        } catch (SQLException e) {
            throw new UserStoreException(error, e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection, resultSet, preparedStatement);
        }
    }

    /**
     * Updates string values to the database.
     *
     * @param dbConnection DB connector
     * @param sqlStmt      SQL statement
     * @param params       SQL statement parameters
     * @throws UserStoreException thrown when error on updating string values to database
     */
    private void updateStringValuesToDatabase(Connection dbConnection, String sqlStmt,
                                              Object... params) throws UserStoreException {
        PreparedStatement prepStmt = null;
        boolean localConnection = false;
        try {
            if (dbConnection == null) {
                localConnection = true;
                dbConnection = getDBConnection();
            }
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            if (params != null && params.length > 0) {
                for (int i = 0; i < params.length; i++) {
                    Object param = params[i];
                    if (param == null) {
                        throw new UserStoreException("Invalid data provided");
                    } else if (param instanceof String) {
                        prepStmt.setString(i + 1, (String) param);
                    } else if (param instanceof Integer) {
                        prepStmt.setInt(i + 1, (Integer) param);
                    } else if (param instanceof Date) {
                        prepStmt.setTimestamp(i + 1, new Timestamp(System.currentTimeMillis()));
                    } else if (param instanceof Boolean) {
                        prepStmt.setBoolean(i + 1, (Boolean) param);
                    }
                }
            }
            int count = prepStmt.executeUpdate();
            if (count == 0) {
                log.debug("No rows were updated");
            }
            log.debug("Executed query is " + sqlStmt + " and number of updated rows :: " + count);
            if (localConnection) {
                dbConnection.commit();
            }
        } catch (SQLException e) {
            throw new UserStoreException("Error occurred while updating string values to database.", e);
        } finally {
            if (localConnection) {
                DatabaseUtil.closeAllConnections(dbConnection);
            }
            DatabaseUtil.closeAllConnections(null, prepStmt);
        }
    }

    /**
     * Break the provided role list based on whether roles are shared or not.
     *
     * @param rolesList roles array
     * @return role break down (shared or not) information
     */
    private RoleBreakdown getSharedRoleBreakdown(String[] rolesList) {
        List<String> roles = new ArrayList<>();
        List<Integer> tenantIds = new ArrayList<>();
        List<String> sharedRoles = new ArrayList<>();
        List<Integer> sharedTenantIds = new ArrayList<>();

        for (String role : rolesList) {
            String[] deletedRoleNames = role.split(CarbonConstants.DOMAIN_SEPARATOR);
            if (deletedRoleNames.length > 1) {
                role = deletedRoleNames[1];
            }

            JDBCRoleContext ctx = (JDBCRoleContext) createRoleContext(role);
            role = ctx.getRoleName();
            int roleTenantId = ctx.getTenantId();
            boolean isShared = ctx.isShared();
            if (isShared) {
                sharedRoles.add(role);
                sharedTenantIds.add(roleTenantId);
            } else {
                roles.add(role);
                tenantIds.add(roleTenantId);
            }
        }

        RoleBreakdown breakdown = new RoleBreakdown();

        // Non shared roles and tenant ids
        breakdown.setRoles(roles.toArray(new String[roles.size()]));
        breakdown.setTenantIds(tenantIds.toArray(new Integer[tenantIds.size()]));

        // Shared roles and tenant ids
        breakdown.setSharedRoles(sharedRoles.toArray(new String[sharedRoles.size()]));
        breakdown.setSharedTenantids(sharedTenantIds.toArray(new Integer[sharedTenantIds.size()]));
        return breakdown;
    }
}
