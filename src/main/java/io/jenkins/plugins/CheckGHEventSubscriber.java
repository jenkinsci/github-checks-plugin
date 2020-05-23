package io.jenkins.plugins;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import edu.umd.cs.findbugs.annotations.Nullable;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.kohsuke.github.GHEvent;
import org.jenkinsci.plugins.github.extension.GHEventsSubscriber;
import org.jenkinsci.plugins.github.extension.GHSubscriberEvent;
import hudson.Extension;
import hudson.model.Item;

import static com.google.common.collect.Sets.immutableEnumSet;

@Extension
public class CheckGHEventSubscriber extends GHEventsSubscriber {

    private static final Logger LOGGER = Logger.getLogger(CheckGHEventSubscriber.class.getName());

    private Map<String, Long> repoFullNameToInstallationId = new HashMap<>();

    public long findInstallationIdByRepository(String fullName) {
        return repoFullNameToInstallationId.getOrDefault(fullName, (long)-1);
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
        return immutableEnumSet(GHEvent.INSTALLATION_REPOSITORIES, GHEvent.CHECK_RUN, GHEvent.CHECK_SUITE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onEvent(GHSubscriberEvent event) {
        switch (event.getGHEvent()) {
            case INSTALLATION_REPOSITORIES:
                LOGGER.log(Level.FINE, "Received Installation Repositories Event...");
                updateInstallations(event); // add (repository_name, installation_id) pair
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + event);
        }
    }

    private void updateInstallations(GHSubscriberEvent event) {
        JsonNode payload = null;
        try {
            payload = new ObjectMapper().readTree(event.getPayload());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        long installationId = payload.get("installation").get("id").asLong();

        // iterate the added repositories
        Iterator<JsonNode> repoIter = payload.get("repositories_added").iterator();
        while (repoIter.hasNext()) {
            repoFullNameToInstallationId.put(repoIter.next().get("full_name").asText(), installationId);
        }

        // iterate the removed repositories
        repoIter = payload.get("repositories_removed").iterator();
        while (repoIter.hasNext()) {
            repoFullNameToInstallationId.remove(repoIter.next().get("full_name").asText());
        }
    }
}
