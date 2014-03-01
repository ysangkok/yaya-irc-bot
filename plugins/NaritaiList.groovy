package info.vividcode.yaya.plugin.listeners

import org.pircbotx.PircBotX
import org.pircbotx.hooks.ListenerAdapter
import org.pircbotx.hooks.events.MessageEvent

class NaritaiListener extends ListenerAdapter<PircBotX> {

    def userToNaritaiMonoListMap = [:]

    /**
     * スレッドセーフ
     */
    @Override
    synchronized void onMessage(MessageEvent<PircBotX> event) {
        def m = event.getMessage() =~ /(\S+)になりたい/
        if (m.matches()) {
            def targetUserName = event.user.realName
            def naritaiMono = m.group(1)
            if (userToNaritaiMonoListMap[targetUserName] == null) {
                userToNaritaiMonoListMap[targetUserName] = []
            }
            userToNaritaiMonoListMap[targetUserName] << naritaiMono
            event.getChannel().send().notice("${targetUserName} さんがなりたいもの...φ(＞ω＜ )ﾒﾓﾒﾓ")
        }

        m = event.getMessage() =~ /なりたいものリスト/
        if (m.matches()) {
            def targetUserName = event.user.realName
            if (userToNaritaiMonoListMap[targetUserName] != null) {
                def res = userToNaritaiMonoListMap[targetUserName].join(" とか ")
                event.getChannel().send().notice("${targetUserName} さんは ${res} になりたいみたいです。")
            } else {
                event.getChannel().send().notice("${targetUserName} さんがなりたいものを知らないです＞＜")
            }
        }
    }

}
