package info.vividcode.yaya.plugin.listeners

import org.pircbotx.PircBotX
import org.pircbotx.hooks.ListenerAdapter
import org.pircbotx.hooks.events.MessageEvent

class NaritaiListener extends ListenerAdapter<PircBotX> {

    private def userToNaritaiMonoListMap = [:]

    /**
     * スレッドセーフ
     */
    @Override
    synchronized void onMessage(MessageEvent<PircBotX> event) {
        def m = event.getMessage() =~ /(.*\S)\s*になりたい/
        if (m.matches()) {
            def targetUserName = event.user.realName
            def naritaiMono = m.group(1)
            if (userToNaritaiMonoListMap[targetUserName] == null) {
                userToNaritaiMonoListMap[targetUserName] = []
            }
            userToNaritaiMonoListMap[targetUserName] << naritaiMono
            event.getChannel().send().notice("${targetUserName} さんがなりたいもの...φ(＞ω＜ )ﾒﾓﾒﾓ")
        }

        m = event.getMessage() =~ /(.*\S)\s*になりました/
        if (m.matches()) {
            def targetUserName = event.user.realName
            def naritaiMono = m.group(1)
            def naritaiListForUser = userToNaritaiMonoListMap[targetUserName]
            if (naritaiListForUser != null) {
                int initSize = naritaiListForUser.size()
                userToNaritaiMonoListMap[targetUserName] = naritaiListForUser.dropWhile { it.equals(naritaiMono) }
                if (naritaiListForUser.size() != userToNaritaiMonoListMap[targetUserName].size()) {
                    event.getChannel().send().notice("なりたいものになれておめでたいです ヾ(＞ヮ＜*)")
                }
                if (userToNaritaiMonoListMap[targetUserName].size() == 0) {
                    userToNaritaiMonoListMap[targetUserName] = null
                }
            }
        }

        m = event.getMessage() =~ /(?:(.*?\S)\s*(?:さん)?(?:の|が))?なりたいもの(?:は\?|リスト)?/
        if (m.matches()) {
            def targetUserName = m.group(1) ?: event.user.realName
            if (userToNaritaiMonoListMap[targetUserName] != null) {
                def res = userToNaritaiMonoListMap[targetUserName].join(" とか ")
                event.getChannel().send().notice("${targetUserName} さんは ${res} になりたいみたいです。")
            } else {
                event.getChannel().send().notice("${targetUserName} さんがなりたいものを知らないです＞＜")
            }
        }
    }

}
