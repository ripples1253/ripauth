package icu.ripley.fusionauthtest1callback.controller;

import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import icu.ripley.fusionauthtest1callback.model.Callback;
import icu.ripley.fusionauthtest1callback.model.SSOConfig;
import icu.ripley.fusionauthtest1callback.service.RedisMessagePublisher;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.api.UserResponse;
import io.fusionauth.domain.oauth2.AccessToken;
import io.fusionauth.domain.oauth2.OAuthError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
public class CallbackController {

    @Autowired
    private SSOConfig ssoConfig;

    private static final Logger logger = LoggerFactory.getLogger(CallbackController.class);

    @Autowired
    private RedisMessagePublisher publisher;

    @GetMapping(path = "/api/v1/auth/callback")
    public ResponseEntity<Callback> oauthCallback(@RequestParam String code, @RequestParam(defaultValue = "en") String locale, @RequestParam String userState) {
        if (!userState.equals("Authenticated")) {
            logger.error("User authentication attempt did NOT return Authenticated. This isn't normal behaviour, did our SSO URL get leaked or get misconfigured?");

            return ResponseEntity.internalServerError().build();
        }

        FusionAuthClient client = new FusionAuthClient(ssoConfig.getApiKey(), ssoConfig.getBaseUrl());

        // grab oauth access token, so we can grab user information.
        ClientResponse<AccessToken, OAuthError> response = client.exchangeOAuthCodeForAccessToken(code,
                ssoConfig.getClientId(),
                ssoConfig.getClientSecret(),
                ssoConfig.getCallbackUrl());

        if(!response.wasSuccessful()){
            logger.error("User authentication attempt failed with error " + response.errorResponse.reason.toString());
            return ResponseEntity.internalServerError().build();
        }

        ClientResponse<UserResponse, Errors> user = client.retrieveUser(response.successResponse.userId);

        if(!user.wasSuccessful()){
            logger.error("User retrieval failed with error(s) " + user.errorResponse.generalErrors);
            return ResponseEntity.internalServerError().build();
        }

        publisher.publishMessage("AUTHENTICATE " + user.successResponse.user.username);

        logger.info("Authenticated " + user.successResponse.user.fullName + ".");

        Callback callback_response = new Callback();

        callback_response.setEmail(user.successResponse.user.email);
        callback_response.setSuccess(true);

        return ResponseEntity.ok(callback_response);
    }

    @GetMapping(path = "/authenticate")
    public ModelAndView authenticateRedirection() {
        return new ModelAndView("redirect:" + ssoConfig.getOauthLoginUrl());
    }

}
