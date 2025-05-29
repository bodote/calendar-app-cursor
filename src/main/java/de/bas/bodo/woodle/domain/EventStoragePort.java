package de.bas.bodo.woodle.domain;

import de.bas.bodo.woodle.model.EventData;
import java.io.IOException;

public interface EventStoragePort {
    String storeEvent(EventData eventData) throws IOException;

    EventData retrieveEvent(String eventId) throws IOException;
}