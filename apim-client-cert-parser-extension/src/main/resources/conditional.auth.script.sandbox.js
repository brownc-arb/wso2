function onLoginRequest(context) {
  var supportedAcrValues = ['urn:openbanking:psd2:sca', 'urn:openbanking:psd2:ca'];
  var selectedAcr = selectAcrFrom(context, supportedAcrValues);
  reportingData(context, "AuthenticationAttempted");

  if (isACREnabled()) {
    if (isTRAEnabled()) {
      if (selectedAcr !== 'urn:openbanking:psd2:sca') {
        executeTRAFunction(context);
      } else {
        executeAllSteps(context);
      }
    } else {
      executeAllSteps(context);
    }

  } else {
    if (isTRAEnabled()) {
      executeTRAFunction(context);
    } else {
      executeAllSteps(context);
    }
  }
}


function executeAllSteps(context) {
  executeStep(1, {
    onSuccess: function(context) {
      reportingData(context, "AuthenticationSuccessful");
      executeStep(2, {
        onSuccess: function(context) {
          reportingData(context, "AuthenticationSuccessful");
        },
        onFail: function(context) {
          reportingData(context, "AuthenticationFailed");
          executeAllSteps(context);
        }
      });
    },
    onFail: function(context) {
      reportingData(context, "AuthenticationFailed");
      executeAllSteps(context);
    }
  });
}

function executeTRAFunction(context) {

  var payload = {};
  var metaData = {};

  var traResonse = isSCAEnforced(context);
  var traJSONResponse = JSON.parse(traResonse);
  var scaEnforced = traJSONResponse.isSCAEnforced;
  var resourceType = traJSONResponse.resourceType;
  var consentId = traJSONResponse.consentId;
  var timestamp = traJSONResponse.timestamp;
  var reason = traJSONResponse.reason;
  //Define Application and InputStream name in meta data
  var siddhiAppName, inStreamName;
  if ("ACCOUNTS" === resourceType) {
    siddhiAppName = "SCADataForAccountsApp";
    inStreamName = "SCAAccountStream";
    //Define payload data
    payload = {
      consentId: consentId,
      isSCAApplied: scaEnforced,
      applicationId: "null",
      userId: "null",
      accountResource: "null",
      timestamp: timestamp,
      exemption: reason
    };
  } else if ("PAYMENTS" === resourceType) {
    siddhiAppName = "SCADataForTransactionsApp";
    inStreamName = "SCATransactionStream";

    payload = {
      consentId: consentId,
      isSCAApplied: scaEnforced,
      applicationId: "null",
      userId: "null",
      amount: 0.00,
      timestamp: timestamp,
      exemption: reason
    };

  }
  metaData = {
    Application: siddhiAppName,
    InputStream: inStreamName
  };

  if (!scaEnforced) {
    if (consentId) {
      executeStep(1, {
        onSuccess: function(context) {
          reportingData(context, "AuthenticationSuccessful");
          publishToAnalytics(metaData, payload, context);
        },
        onFail: function(context) {
          reportingData(context, "AuthenticationFailed");
          executeTRAFunction(context);
        }

      });

    }
  } else {
    if (consentId) {
      executeStep(2, {
        onSuccess: function(context) {
          reportingData(context, "AuthenticationSuccessful");
          publishToAnalytics(metaData, payload, context);
        },
        onFail: function(context) {
          reportingData(context, "AuthenticationFailed");
          executeTRAFunction(context);
        }
      });
    }
  }

}
