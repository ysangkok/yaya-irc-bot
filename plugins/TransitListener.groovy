package info.vividcode.yaya.plugin.listeners

import org.pircbotx.PircBotX
import org.pircbotx.hooks.ListenerAdapter
import org.pircbotx.hooks.events.MessageEvent

import de.schildbach.pte.dto.QueryTripsResult
import de.schildbach.pte.dto.Location
import de.schildbach.pte.NetworkProvider.Accessibility
import de.schildbach.pte.NetworkProvider.WalkSpeed
import de.schildbach.pte.dto.LocationType
import de.schildbach.pte.dto.Product

import de.schildbach.pte.*

class TransitListener extends ListenerAdapter {
    private def trips = null

    void nextConn(MessageEvent event) {
        def trip = trips.poll()
        if (trip == null) {
            event.getChannel().send().notice("no more connections")
            trips = null
            return
        }
        //println sprintf("From: %s, To: %s", [trip.from, trip.to])

        for (leg in trip.legs) {
                if (leg.getClass().getSimpleName() == "Individual") {
                        event.getChannel().send().notice("Walk")
                        continue
                }
                event.getChannel().send().notice(sprintf("%s %s -> %s", [leg.line.product, leg.line.label, leg.destination.name]))
                event.getChannel().send().notice(sprintf("%s\t%s", [String.format(Locale.US, "%ta %<tR", leg.departureStop.departureTime), leg.departureStop.location.name]))
                event.getChannel().send().notice(sprintf("%s\t%s", [String.format(Locale.US, "%ta %<tR", leg.arrivalStop.plannedArrivalTime), leg.arrivalStop.location.name]))
        }
    }

    @Override
    synchronized void onMessage(MessageEvent event) {
        def nex = event.getMessage() =~ /next connection/
        if (nex.matches() && trips != null) {
            nextConn(event)
            return
        }

        def m = event.getMessage() =~ /from (.+) to (.+) using (.+)/
        if (!m.matches())
            return

        def provider = Class.forName("de.schildbach.pte." + m.group(3) + "Provider").newInstance()

        def sug1 = provider.suggestLocations(m.group(1))
        def sug2 = provider.suggestLocations(m.group(2))

        def result = provider.queryTrips(sug1.suggestedLocations[0].location, null, sug2.suggestedLocations[0].location, new Date(), /*dep*/ true, Product.ALL, null, WalkSpeed.NORMAL, Accessibility.NEUTRAL, null)
        trips = new ArrayDeque(result.trips)
        //println sprintf("From: %1$s, To: %2$s", result.from, result.to)
        nextConn(event)
    }
}
