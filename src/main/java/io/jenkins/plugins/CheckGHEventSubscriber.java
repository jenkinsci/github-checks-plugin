package io.jenkins.plugins;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

import org.kohsuke.github.GHEvent;
import org.jenkinsci.plugins.github.extension.GHEventsSubscriber;
import org.jenkinsci.plugins.github.extension.GHSubscriberEvent;
import hudson.Extension;
import hudson.model.Item;

import static com.google.common.collect.Sets.immutableEnumSet;

@Extension
public class CheckGHEventSubscriber extends GHEventsSubscriber {

    private static final Logger LOGGER = Logger.getLogger(CheckGHEventSubscriber.class.getName());

    private List<GHSubscriberEvent> checkSuiteEvents = new LinkedList<>();
    private List<GHSubscriberEvent> checkRunEvents = new LinkedList<>();

    public List<GHSubscriberEvent> getCheckSuiteEvents() {
        return checkSuiteEvents;
    }

    public List<GHSubscriberEvent> getCheckRunEvents() {
        return checkRunEvents;
    }

    public static CheckGHEventSubscriber getInstance() {
        return GHEventsSubscriber.all().get(CheckGHEventSubscriber.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isApplicable(@Nullable Item project) {
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * @return set with CHECK_SUITE and CHECK_RUN event
     */
    @Override
    protected Set<GHEvent> events() {
        return immutableEnumSet(GHEvent.CHECK_RUN, GHEvent.CHECK_SUITE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onEvent(GHSubscriberEvent event) {
        switch (event.getGHEvent()) {
            case CHECK_SUITE:
                LOGGER.log(Level.FINE, "Received Check Run Event...");
                checkSuiteEvents.add(event);
                break;
            case CHECK_RUN:
                LOGGER.log(Level.FINE, "Received Check Suite Event...");
                checkRunEvents.add(event);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + event);
        }
    }
}
