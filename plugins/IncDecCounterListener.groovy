package info.vividcode.yaya.plugin.listeners

import org.pircbotx.PircBotX
import org.pircbotx.hooks.ListenerAdapter
import org.pircbotx.hooks.events.MessageEvent

class IncDecCounterListener extends ListenerAdapter<PircBotX> {

    private def counters = [:]

    /**
     * スレッドセーフ
     */
    @Override
    synchronized void onMessage(MessageEvent<PircBotX> event) {
        def m = event.getMessage() =~ /(\S+)\+\+/
        if (m.matches()) {
            def targetName = m.group(1)
            if (counters[targetName] == null) counters[targetName] = 0
            def count = ++counters[targetName]
            event.getChannel().send().notice("${targetName}: ${count}")
        }
    }

}
