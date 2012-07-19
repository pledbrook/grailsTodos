package todosample

class AppTagLib {
    def oauthService

    def hasAccessToken = { attrs, body ->
        if (!attrs.provider) throwTagError "Tag [g:hasAccessToken] is missing required attribute [provider]."

        if (hasAccessToken(attrs.provider)) {
            out << body()
        }
    }

    def lacksAccessToken = { attrs, body ->
        if (!attrs.provider) throwTagError "Tag [g:hasAccessToken] is missing required attribute [provider]."

        if (!hasAccessToken(attrs.provider)) {
            out << body()
        }
    }

    def cloudFoundry = { attrs, body ->
        if (runningOnCloudFoundry) {
            out << body()
        }
    }

    def notCloudFoundry = { attrs, body ->
        if (!runningOnCloudFoundry) {
            out << body()
        }
    }

    protected final boolean hasAccessToken(String provider) {
        return session[oauthService.findSessionKeyForAccessToken(provider)]
    }

    protected final boolean isRunningOnCloudFoundry() {
        return System.getenv("VCAP_SERVICES")
    }
}
