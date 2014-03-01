@Grab(group='org.pircbotx', module='pircbotx', version='2.0.1')
@Grab(group='org.slf4j', module='slf4j-simple', version='1.7.+')

import java.nio.charset.Charset;

import javax.net.ssl.SSLSocketFactory;

import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.UtilSSLSocketFactory;
import org.pircbotx.cap.TLSCapHandler;

// コマンドライン引数
def cli = new CliBuilder(usage: "yaya irc bot")
cli.h(longOpt: 'host', argName: 'hostname', required: true, args: 1, 'host or h')
cli._(longOpt: 'port', argName: 'port', args: 1, 'port number')
cli._(longOpt: 'use-ssl', 'Use SSL')
cli.p(longOpt: 'password', argName: 'password', args: 1, 'password or p' )
cli.c(longOpt: 'channel', argName: 'channels', args: 1, 'channel or c' )
def opt = cli.parse(args)
if (!opt) {
    System.exit 1
}
String  hostname = opt['h']
int     port     = (opt['port'] ? Integer.parseInt(opt['port']) : 6667)
boolean useSsl   = (opt['use-ssl'] ? true : false)
String  password = (opt['p'] ? opt['p'] : '')
String  channels = (opt['c'] ? opt['c'] : '')

//Setup this bot
def confBuilder = new Configuration.Builder<PircBotX>()
    //.setAutoReconnect(true)
    .setServerHostname(hostname)
    .setServerPort(port)
    .setEncoding(Charset.forName("UTF-8"))
    .setName("yaya")
    .setLogin("LQ")
    .setAutoNickChange(true) //Automatically change nick when the current one is in use
    .addListener(new IrcListenerManager())
    .setCapEnabled(true) //Enable CAP features
    .addCapHandler(new TLSCapHandler(new UtilSSLSocketFactory().trustAllCertificates(), true))
if (useSsl) {
    confBuilder.setSocketFactory(SSLSocketFactory.getDefault())
}
if (!password.isEmpty()) confBuilder.setServerPassword(password)
if (!channels.isEmpty()) confBuilder.addAutoJoinChannel(channels)

Configuration configuration = confBuilder.buildConfiguration()

//bot.connect throws various exceptions for failures
try {
    def bot = new PircBotX(configuration);
    bot.startBot();
} //In your code you should catch and handle each exception seperately,
//but here we just lump them all togeather for simpliciy
catch (Exception ex) {
    ex.printStackTrace();
}
