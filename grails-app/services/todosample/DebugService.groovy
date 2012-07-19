package todosample

import grails.events.Listener

class DebugService {
    @Listener
    def packageWarStart() {
        println "[DebugService] Packaging started"
    }

    @Listener
    def packageWarStatus(String msg) {
        println "[DebugService] Packaging status: ${msg}"
    }
}
