package info.vividcode.yaya.plugin.listeners

import java.util.List
import java.util.regex.Matcher
import java.util.regex.Pattern

import org.pircbotx.PircBotX
import org.pircbotx.hooks.ListenerAdapter
import org.pircbotx.hooks.events.MessageEvent

class SimpleResponse extends ListenerAdapter<PircBotX> {

    private static class ResponseDefinition {
        public Pattern pattern;
        public List<String> responseMessages;
    }

    private static final RESPONSE_DEF_LIST = [
        new ResponseDefinition(pattern: ~/寒い|さむい/, responseMessages: [
            "お布団の中でぬくぬくしましょう c(╹ω╹*c[＿＿＿]",
        ]),
    ]

    /**
     * スレッドセーフ
     */
    @Override
    synchronized void onMessage(MessageEvent<PircBotX> event) {
        for (ResponseDefinition d : RESPONSE_DEF_LIST) {
            Matcher m = d.pattern.matcher(event.message)
            if (m.find()) {
                String msg = d.responseMessages[0]
                event.channel.send().notice(msg)
            }
        }
    }

}
