# Author CB
# All these need to be run to add the OB jars into your local repo
# the -Dfile should point to where the open banking fiance jars have been downloaded to
# should be an env var ideally
#
# added a comment 1


mvn install:install-file -Dfile=C:\WSO2\wum\wso2-obkm-1.3.0\repository\components\plugins\com.wso2.finance.open.banking.common_1.3.0.jar -DgroupId=com.wso2.finance -DartifactId=com.wso2.finance.open.banking.common -Dversion=1.3.0 -Dpackaging=jar -DlocalRepositoryPath=C:\d\svn\wso2\iam-openbanking\local-maven-repo

mvn install:install-file -Dfile=C:\WSO2\wum\wso2-obkm-1.3.0\repository\components\plugins\com.wso2.finance.open.banking.event.notifications.uk.300.common_1.3.0.jar -DgroupId=com.wso2.finance -DartifactId=com.wso2.finance.open.banking.event.notifications.uk.300.common -Dversion=1.3.0 -Dpackaging=jar -DlocalRepositoryPath=C:\d\svn\wso2\iam-openbanking\local-maven-repo

mvn install:install-file -Dfile=C:\WSO2\wum\wso2-obkm-1.3.0\repository\components\plugins\com.wso2.finance.open.banking.event.notifications.uk.300.mgt_1.3.0.jar -DgroupId=com.wso2.finance -DartifactId=com.wso2.finance.open.banking.event.notifications.uk.300.mgt -Dversion=1.3.0 -Dpackaging=jar -DlocalRepositoryPath=C:\d\svn\wso2\iam-openbanking\local-maven-repo


mvn install:install-file -Dfile=C:\WSO2\wum\wso2-obkm-1.3.0\repository\components\dropins\com.wso2.finance.open.banking.eidas.certificate.extractor-1.3.0.jar -DgroupId=com.wso2.finance -DartifactId=com.wso2.finance.open.banking.eidas.certificate.extractor -Dversion=1.3.0 -Dpackaging=jar -DlocalRepositoryPath=C:\d\svn\wso2\iam-openbanking\local-maven-repo


mvn install:install-file -Dfile=C:\WSO2\wum\wso2-obkm-1.3.0\repository\components\dropins\com.wso2.finance.open.banking.mtls.authenticator-1.3.0.jar -DgroupId=com.wso2.finance -DartifactId=com.wso2.finance.open.banking.mtls.authenticator -Dversion=1.3.0 -Dpackaging=jar -DlocalRepositoryPath=C:\d\svn\wso2\iam-openbanking\local-maven-repo

mvn install:install-file -Dfile=C:\WSO2\wum\wso2-obkm-1.3.0\repository\components\dropins\com.wso2.finance.open.banking.multiple.authorization.mgmt-1.3.0.jar -DgroupId=com.wso2.finance -DartifactId=com.wso2.finance.open.banking.multiple.authorization.mgmt -Dversion=1.3.0 -Dpackaging=jar -DlocalRepositoryPath=C:\d\svn\wso2\iam-openbanking\local-maven-repo


mvn install:install-file -Dfile=C:\WSO2\wum\wso2-obkm-1.3.0\repository\components\dropins\com.wso2.finance.open.banking.reporting.data.common-1.3.0.jar -DgroupId=com.wso2.finance -DartifactId=com.wso2.finance.open.banking.reporting.data.common -Dversion=1.3.0 -Dpackaging=jar -DlocalRepositoryPath=C:\d\svn\wso2\iam-openbanking\local-maven-repo

mvn install:install-file -Dfile=C:\WSO2\wum\wso2-obkm-1.3.0\repository\components\dropins\com.wso2.finance.open.banking.reporting.data.retriever-1.3.0.jar -DgroupId=com.wso2.finance -DartifactId=com.wso2.finance.open.banking.reporting.data.retriever -Dversion=1.3.0 -Dpackaging=jar -DlocalRepositoryPath=C:\d\svn\wso2\iam-openbanking\local-maven-repo

mvn install:install-file -Dfile=C:\WSO2\wum\wso2-obkm-1.3.0\repository\components\plugins\com.wso2.finance.open.banking.uk.consent.mgt_1.3.0.jar -DgroupId=com.wso2.finance -DartifactId=com.wso2.finance.open.banking.uk.consent.mgt-Dversion=1.3.0 -Dpackaging=jar -DlocalRepositoryPath=C:\d\svn\wso2\iam-openbanking\local-maven-repo

mvn install:install-file -Dfile=C:\WSO2\wum\wso2-obkm-1.3.0\repository\components\plugins\com.wso2.finance.open.banking.consent.mgt.stet.v140_1.3.0.jar -DgroupId=com.wso2.finance -DartifactId=com.wso2.finance.open.banking.consent.mgt.stet.v140 -Dversion=1.3.0 -Dpackaging=jar -DlocalRepositoryPath=C:\d\svn\wso2\iam-openbanking\local-maven-repo

mvn install:install-file -Dfile=C:\WSO2\wum\wso2-obkm-1.3.0\repository\components\plugins\com.wso2.finance.open.banking.consent.mgt.berlin.v100_1.3.0.jar -DgroupId=com.wso2.finance -DartifactId=om.wso2.finance.open.banking.consent.mgt.berlin.v100 -Dversion=1.3.0 -Dpackaging=jar -DlocalRepositoryPath=C:\d\svn\wso2\iam-openbanking\local-maven-repo



mvn install:install-file -Dfile=C:\WSO2\wum\wso2-obkm-1.3.0\repository\components\plugins\com.wso2.finance.open.banking.common-1.3.0.jar -DgroupId=com.wso2.finance -DartifactId=com.wso2.finance.open.banking.common -Dversion=1.3.0 -Dpackaging=jar -DlocalRepositoryPath=C:\d\svn\wso2\iam-openbanking\local-maven-repo
