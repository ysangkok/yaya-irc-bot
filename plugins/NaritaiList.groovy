package info.vividcode.yaya.plugin.listeners

import org.pircbotx.PircBotX
import org.pircbotx.User
import org.pircbotx.hooks.ListenerAdapter
import org.pircbotx.hooks.events.MessageEvent

class NaritaiListener extends ListenerAdapter {

    private def userToNaritaiMonoListMap = [:]

    /**
     * スレッドセーフ
     */
    @Override
    synchronized void onMessage(MessageEvent event) {
        def m = event.getMessage() =~ /(.*\S)\s*になりたい/
        if (m.matches()) {
            def targetUserName = event.user.nick
            def targetUserId = event.user.userId
            def naritaiMono = m.group(1)
            if (userToNaritaiMonoListMap[targetUserId] == null) {
                userToNaritaiMonoListMap[targetUserId] = []
            }
            userToNaritaiMonoListMap[targetUserId] << naritaiMono
            event.getChannel().send().notice("${targetUserName} さんがなりたいもの...φ(＞ω＜ )ﾒﾓﾒﾓ")
        }

        m = event.getMessage() =~ /(.*\S)\s*になりました/
        if (m.matches()) {
            def targetUserName = event.user.nick
            def targetUserId = event.user.userId
            def naritaiMono = m.group(1)
            def naritaiListForUser = userToNaritaiMonoListMap[targetUserId]
            if (naritaiListForUser != null) {
                int initSize = naritaiListForUser.size()
                userToNaritaiMonoListMap[targetUserId] = naritaiListForUser.dropWhile { it.equals(naritaiMono) }
                if (naritaiListForUser.size() != userToNaritaiMonoListMap[targetUserId].size()) {
                    event.getChannel().send().notice("なりたいものになれておめでたいです ヾ(＞ヮ＜*)")
                }
                if (userToNaritaiMonoListMap[targetUserId].size() == 0) {
                    userToNaritaiMonoListMap[targetUserId] = null
                }
            }
        }

        m = event.getMessage() =~ /(?:(.*?\S)\s*(?:さん)?(?:の|が))?なりたいもの(?:は\?|リスト)?/
        if (m.matches()) {
            def targetUserName = m.group(1) ?: event.user.nick
            def targetUserId = null
            for (User u : event.channel.users) {
                if (u.nick.equals(targetUserName)) {
                    targetUserId = u.userId
                    break
                }
            }
            if (targetUserId != null) {
                if (userToNaritaiMonoListMap[targetUserId] != null) {
                    def res = userToNaritaiMonoListMap[targetUserId].join(" とか ")
                    event.getChannel().send().notice("${targetUserName} さんは ${res} になりたいみたいです。")
                } else {
                    event.getChannel().send().notice("${targetUserName} さんは何になりたいんでしょうか ヽ^ｼ▰╹ヮ╹)ﾉｼ")
                }
            } else {
                event.channel.send().notice("${targetUserName} さんって誰ですか???")
            }
        }
    }

}
