function onLoginRequest(context) {
  if (context.request.params.is_registration_flow != null &&
    context.request.params.is_registration_flow[0].equals("true")) {
    executeStep(1, {
      authenticationOptions: [{authenticator: 'alrayanbasicauth'}]
    }, {});
    executeStep(2);
  } else {
    executeStep(1, {
      authenticationOptions: [{authenticator: 'alrayanbasicauth'}, {idp: 'ARBMobile'}],
    }, {
      onSuccess: function (context) {
        if (!context.steps[1].idp.equals("ARBMobile")) {
          executeStep(2);
        }
      }
    });
  }
}
