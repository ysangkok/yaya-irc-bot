import java.nio.file.Files
import java.nio.file.Paths

import java.util.ArrayList

import org.pircbotx.PircBotX
import org.pircbotx.hooks.Event
import org.pircbotx.hooks.Listener

class IrcListenerManager implements Listener {

    private final GroovyClassLoader loader
    private final ReloadCommandListener reloadCommandListener
    private final UpdateCommandListener updateCommandListener
    // 複数スレッドから同時に変更されることがないようにすること
    private List<Listener> listeners

    IrcListenerManager() {
        loader = new GroovyClassLoader()
        reloadPluginedListeners()
        reloadCommandListener = new ReloadCommandListener({
            reloadPluginedListeners()
        })
        updateCommandListener = new UpdateCommandListener()
    }

    private synchronized void reloadPluginedListeners() {
        listeners = new ArrayList<Listener>()
        loader.clearCache()
        def ds = Files.newDirectoryStream(Paths.get("plugins"))
        for (def entry : ds) {
            if (entry.fileName ==~ /.+\.groovy\z/) {
                try {
                    listeners.add(loader.parseClass(entry.toFile()).newInstance())
                } catch (Exception err) {
                    err.printStackTrace()
                }
            }
        }
    }

    /**
     * スレッドセーフ (プラグインされた各リスナのスレッドセーフ性はプラグイン側で保証されているものとする)
     */
    @Override
    void onEvent(Event event) {
        updateCommandListener.onEvent(event)
        final List<Listener<PircBotX>> ll
        // 必要であればプラグインを読み込み直してプラグイン一覧を取得。
        // 読み込み直しは同期して行う。
        synchronized(this) {
            reloadCommandListener.onEvent(event)
            ll = listeners
        }
        for (def listener : ll) {
            try {
                listener.onEvent(event)
            } catch (Exception err) {
                err.printStackTrace()
            }
        }
    }

}
