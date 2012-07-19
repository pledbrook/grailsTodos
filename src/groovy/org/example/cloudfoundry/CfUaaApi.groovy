package org.example.cloudfoundry

import org.scribe.builder.api.DefaultApi20
import org.scribe.exceptions.OAuthException
import org.scribe.extractors.AccessTokenExtractor
import org.scribe.model.OAuthConfig
import org.scribe.model.OAuthConstants
import org.scribe.model.OAuthRequest
import org.scribe.model.Response
import org.scribe.model.Token
import org.scribe.model.Verb
import org.scribe.model.Verifier
import org.scribe.oauth.OAuth20ServiceImpl
import org.scribe.oauth.OAuthService
import org.scribe.utils.OAuthEncoder
import org.scribe.utils.Preconditions


class CfUaaApi extends DefaultApi20 {

    private static final String AUTHORIZE_URL = "http://uaa.cf01.qa.las01.vcsops.com/oauth/authorize?response_type=code&client_id=%s&redirect_uri=%s"
    private static final String SCOPED_AUTHORIZE_URL = AUTHORIZE_URL + "&scope=%s"

    String getAccessTokenEndpoint() { return "http://uaa.cf01.qa.las01.vcsops.com/oauth/token" }
    
    @Override
    public AccessTokenExtractor getAccessTokenExtractor() {
        return { String response ->
            println ">>> UAA response: $response"
            Preconditions.checkEmptyString(response, "Response body is incorrect. Can't extract a token from an empty string")

            def matcher = response =~ /"access_token"\s*:\s*"([^&"]+)"/
            if (matcher) {
                return new Token(OAuthEncoder.decode(matcher.group(1)), "", response)
            } 
            else {
                throw new OAuthException("Response body is incorrect. Can't extract a token from this: '" + response + "'", null)
            }
        } as AccessTokenExtractor
    }

    String getAuthorizationUrl(OAuthConfig config) {
        def str
        // Append scope if present
        if (config.hasScope()) {
            str = String.format(SCOPED_AUTHORIZE_URL, config.apiKey,
                    OAuthEncoder.encode(config.callback),
                    OAuthEncoder.encode(config.scope))
        } else {
            str = String.format(AUTHORIZE_URL, config.apiKey, OAuthEncoder.encode(config.callback))
        }
        println ">> Auth URL: $str"

        return str
    }
    
    Verb getAccessTokenVerb() {
        return Verb.POST
    }
    
    OAuthService createService(OAuthConfig config) {
        return new UaaOAuth2Service(this, config)
    }
}

class UaaOAuth2Service extends OAuth20ServiceImpl {

    private static final String GRANT_TYPE_AUTHORIZATION_CODE = "authorization_code"
    private static final String GRANT_TYPE = "grant_type"
    private DefaultApi20 api
    private OAuthConfig config

    public UaaOAuth2Service(DefaultApi20 api, OAuthConfig config) {
        super(api, config)
        this.api = api
        this.config = config
    }
    
    public Token getAccessToken(Token requestToken, Verifier verifier) {
        println ">>> Request token: $requestToken"
        OAuthRequest request = new OAuthRequest(api.getAccessTokenVerb(), api.getAccessTokenEndpoint())
        request.addHeader "Authorization", "Basic ${'grails-getstarted:hc3N3b3JkIiwicmVhZCIsIndyaXRl'.bytes.encodeBase64()}"

        switch (api.accessTokenVerb) {
        case Verb.POST:
            request.addBodyParameter(OAuthConstants.CLIENT_ID, config.getApiKey())
            request.addBodyParameter(OAuthConstants.CLIENT_SECRET, config.getApiSecret())
            request.addBodyParameter(OAuthConstants.CODE, verifier.getValue())
            request.addBodyParameter(OAuthConstants.REDIRECT_URI, config.getCallback())
            if(config.hasScope()) request.addBodyParameter(OAuthConstants.SCOPE, config.getScope())
            request.addBodyParameter(GRANT_TYPE, GRANT_TYPE_AUTHORIZATION_CODE)
            println ">>> Request: $request"
            println ">>> Callback: ${config.callback}"
            break
        case Verb.GET:
        default:
            request.addQuerystringParameter(OAuthConstants.CLIENT_ID, config.getApiKey())
            request.addQuerystringParameter(OAuthConstants.CLIENT_SECRET, config.getApiSecret())
            request.addQuerystringParameter(OAuthConstants.CODE, verifier.getValue())
            request.addQuerystringParameter(OAuthConstants.REDIRECT_URI, config.getCallback())
            if(config.hasScope()) request.addQuerystringParameter(OAuthConstants.SCOPE, config.getScope())
        }
        Response response = request.send()
        return api.accessTokenExtractor.extract(response.body)
    }
}
