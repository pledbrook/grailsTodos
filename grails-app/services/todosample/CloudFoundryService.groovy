package todosample

import grails.util.Metadata
import java.util.zip.ZipFile
import org.cloudfoundry.client.lib.CloudApplication
import org.cloudfoundry.client.lib.CloudFoundryClient
import org.cloudfoundry.client.lib.CloudService
import org.cloudfoundry.client.lib.archive.ZipApplicationArchive
import org.springframework.beans.factory.InitializingBean
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException

class CloudFoundryService implements InitializingBean {
    def appName
    def cloudFoundryDomain
    def grailsApplication

    void afterPropertiesSet() {
        appName = Metadata.current.getApplicationName()
        cloudFoundryDomain = grailsApplication.config.app.cf.domain
    }

    def listApplications(String accessToken) {
        def client = createClient(accessToken)
        return client.applications
    }

    def uploadWar(String accessToken, String warPath) {
        def client = createClient(accessToken)
        def app = getOrCreateApplication(client)
        client.uploadApplication(appName, warPath)

        if (!app.services) {
            client.createService(mysqlConfig)
            client.bindService(appName, "mysql-${appName}")
            client.createService(rabbitConfig)
            client.bindService(appName, "rabbitmq-${appName}")
            client.startApplication(appName)
        }
        else {
            client.restartApplication(appName)
        }

        return app.uris[0]
    }

    protected final createClient(String accessToken) {
        return new CloudFoundryClient("Bearer $accessToken", "http://api.${cloudFoundryDomain}")
    }

    protected final String generateAppUrl() {
        return appName + "-" + UUID.randomUUID().toString().encodeAsSHA1()[0..7] + '.' + cloudFoundryDomain
    }

    protected final CloudService getMysqlConfig() {
        return new CloudService(
                name: "mysql-${appName}".toString(),
                type: "database",
                vendor: "mysql",
                version: "5.1",
                tier: "free")
    }

    protected final CloudService getRabbitConfig() {
        return new CloudService(
                name: "rabbitmq-${appName}".toString(),
                type: "generic",
                vendor: "rabbitmq",
                version: "2.4",
                tier: "free")
    }

    protected final CloudApplication getOrCreateApplication(client) {
        try {
            return client.getApplication(appName)
        }
        catch (HttpClientErrorException e) {
            if (e.statusCode != HttpStatus.NOT_FOUND) {
                throw e
            }
        }

        client.createApplication(appName, "grails", 512, [generateAppUrl()], [])
        return client.getApplication(appName)
    }
}
