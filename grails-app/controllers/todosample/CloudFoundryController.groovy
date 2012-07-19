package todosample

class CloudFoundryController {
    def cloudFoundryService
    def oauthService

    def index() { redirect action: "listApps" }

    def listApps() {
        def accessToken = cfAccessToken
        if (!accessToken) {
            render status: 403, text: "You are not logged into Cloud Foundry.", layout: "main"
            return
        }

        [apps: cloudFoundryService.listApplications(accessToken.token)]
    }

    def deploy() {
        def accessToken = cfAccessToken
        if (!accessToken) {
            render status: 403, text: "You are not logged into Cloud Foundry.", layout: "main"
            return
        }

        Thread.start {
            def warPath = packageWar()
            if (warPath) {
                eventAsync "deployStart"
                def url = cloudFoundryService.uploadWar(accessToken.token, path)
                eventAsync "deployEnd", url
            }
        }

        render "Deploying app"
    }

    def getCfAccessToken() {
        return session[oauthService.findSessionKeyForAccessToken("uaa")]
    }

    private deployWar(String path) {
        eventAsync "deployStart"
        def accessToken = cfAccessToken
        if (!accessToken) {
            render status: 403, text: "You are not logged into Cloud Foundry.", layout: "main"
            return
        }

        def url = cloudFoundryService.uploadWar(accessToken.token, path)
        eventAsync "deployEnd", url
        render status: 200, text: "Application successfully deployed", layout: "main"
    }

    private String packageWar() {
        eventAsync "packageWarStart"
        def process = ["./grailsw", "--plain-output", "war"].execute()

        Thread.start {
            process.err.withReader { r ->
                def line = r.readLine()
                while (line != null) {
                    eventAsync "packageWarStatus", filterLine(line)
                    line = r.readLine()
                }
            }
        }

        def warPath = null
        process.in.withReader { r ->
            def line = r.readLine()
            while (line != null) {
                def m = line =~ /Done creating WAR\s+(.*\.war)/
                if (m) warPath = m[0][1]
                eventAsync "packageWarStatus", filterLine(line)
                line = r.readLine()
            }
        }

        eventAsync "packageWarEnd"
        return warPath
    }

    protected final String filterLine(String line) {
        return line.startsWith('|') ? line.substring(1) : line
    }
}
