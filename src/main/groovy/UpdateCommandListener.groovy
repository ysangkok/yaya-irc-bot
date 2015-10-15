import org.pircbotx.PircBotX;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

class UpdateCommandListener extends ListenerAdapter {

    private static final long NOT_UPDATED = -1
    /** update を実行できる最小の間隔。 2 分。 */
    private static final long MIN_UPDATE_INTERVAL_IN_MS = 2 * 60 * 1000
    private long lastUpdateTimeInMillis = NOT_UPDATED

    /**
     * スレッドセーフ
     */
    @Override
    public void onMessage(MessageEvent event) {
        if (event.getMessage() ==~ /update\s+yaya/) {
            if (!checkUpdateInterval()) {
                event.getChannel().send().notice("更新間隔が短いから今回は更新しませんっ!!")
                return;
            }
            boolean success = update()
            if (success) {
                event.getChannel().send().notice("更新成功 ✧◝(⁰▿⁰)◜✧")
            } else {
                event.getChannel().send().notice("更新失敗です ｡ﾟ(ﾟ∩´﹏`∩ﾟ)ﾟ｡")
            }
        }
    }

    private synchronized boolean checkUpdateInterval() {
        def toBeUpdated = false
        long curTime = System.currentTimeMillis()
        if (lastUpdateTimeInMillis == NOT_UPDATED
                || lastUpdateTimeInMillis + MIN_UPDATE_INTERVAL_IN_MS < curTime) {
            toBeUpdated = true
            lastUpdateTimeInMillis = curTime
        }
        return toBeUpdated
    }

    private boolean update() {
        def proc = """git pull""".execute()
        proc.waitFor()
        return (proc.exitValue() == 0 ? true : false)
    }

}
