import org.pircbotx.PircBotX;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

class ReloadCommandListener extends ListenerAdapter<PircBotX> {

    private final Closure callback

    ReloadCommandListener(Closure c) {
        super()
        callback = c
    }

    @Override
    public void onMessage(MessageEvent<PircBotX> event) {
        if (event.getMessage() ==~ /reload yaya/) {
            callback.call()
        }
    }

}
