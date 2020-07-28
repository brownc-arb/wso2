function onLoginRequest(context) {
  reportingData(context, "AuthenticationAttempted");
  if (context.request.params.is_registration_flow != null &&
      context.request.params.is_registration_flow[0].equals("true")) {
    executeStep(1,

        {
          authenticationOptions: [{
            authenticator: 'alrayanbasicauth'
          }]
        }, {
          onSuccess: function(context) {
            reportingData(context, "AuthenticationSuccessful");
          },
          onFail: function(context) {
            reportingData(context, "AuthenticationFailed");
            executeAllSteps(context);
          }
        });
    executeStep(2, {
      onSuccess: function(context) {
        reportingData(context, "AuthenticationSuccessful");
      },
      onFail: function(context) {
        reportingData(context, "AuthenticationFailed");
        executeAllSteps(context);
      }
    });
  } else {
    executeStep(1,

        {
          authenticationOptions: [{
            authenticator: 'alrayanbasicauth'
          }, {
            idp: 'ARBMobile'
          }],
        }, {
          onSuccess: function(context) {
            reportingData(context, "AuthenticationSuccessful");
            if (!context.steps[1].idp.equals("ARBMobile"))

            {
              executeStep(2, {
                onSuccess: function(context) {
                  reportingData(context, "AuthenticationSuccessful");
                },
                onFail: function(context) {
                  reportingData(context, "AuthenticationFailed");
                  executeAllSteps(context);
                }
              });
            }
          },
          onFail: function(context) {
            reportingData(context, "AuthenticationFailed");
          }
        });

  }
}