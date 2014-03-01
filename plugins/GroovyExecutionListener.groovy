package info.vividcode.yaya.plugin.listeners

import groovy.transform.ThreadInterrupt

import java.util.concurrent.atomic.AtomicBoolean

import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer

import org.pircbotx.PircBotX
import org.pircbotx.hooks.ListenerAdapter
import org.pircbotx.hooks.events.MessageEvent

class GroovyExecutionListener extends ListenerAdapter<PircBotX> {

    /**
     * スレッドセーフ
     */
    @Override
    synchronized void onMessage(MessageEvent<PircBotX> event) {
        def m = event.getMessage() =~ /\Agroovy:(.+)\z/
        if (m.matches()) {
            def code = m.group(1)
            executeGroovyCodeWithTimeout(code, event, 5000)
        }
    }

    private void executeGroovyCodeWithTimeout(final String code, final MessageEvent<PircBotX> event, final long timeoutInMs) {
        final def lockObj = new Object()
        def timeouted = false
        def tFinished = false
        def t2Finished = false
        final def t = new Thread() {
            @Override void run() {
                try {
                    Binding binding = new Binding()
                    // 標準出力を設定 (println は取れるけど System.out はダメそう)
                    def buf = new StringWriter()
                    binding.setProperty("out", buf)
                    def config = new CompilerConfiguration()
                    config.addCompilationCustomizers(new ASTTransformationCustomizer(ThreadInterrupt))
                    def res = new GroovyShell(binding, config).evaluate(new GroovyCodeSource(code, "RestrictedScript", "/restrictedScript"))
                    if (!buf.toString().isEmpty()) {
                        buf.toString().split("\n").each {
                            event.getChannel().send().notice("${it}".replaceAll("\\s+", " "))
                        }
                    }
                    event.getChannel().send().notice("> ${res}".replaceAll("\\s+", " "))
                } catch (Throwable err) {
                    event.getChannel().send().notice("『${err.message}』 ってエラーが出たよ＞＜".replaceAll("\\s+", " "))
                }
                synchronized(lockObj) {
                    tFinished = true
                }
            }
        }
        t.start()
        final def t2 = new Thread() {
            @Override void run() {
                try {
                    Thread.sleep(timeoutInMs)
                    synchronized(lockObj) {
                        if (!tFinished) {
                            t.interrupt()
                            timeouted = true
                        }
                        t2Finished = true
                    }
                } catch (InterruptedException err) {
                    // 入力された処理が早く終わったらここに来る
                }
            }
        }
        t2.start()
        t.join()
        synchronized(lockObj) {
            if (!t2Finished) t2.interrupt()
        }
        t2.join()
        synchronized(lockObj) {
            if (timeouted) {
                event.getChannel().send().notice("ヽ^ｼ＞ω＜)ﾉｼ 時間かかりすぎてるからタイムアウトさせてあげたの".replaceAll("\\s+", " "))
            }
        }
    }

}
