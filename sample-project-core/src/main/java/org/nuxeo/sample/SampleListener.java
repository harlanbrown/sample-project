
package org.nuxeo.sample;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.event.DocumentEventTypes;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.runtime.api.Framework;

public class SampleListener implements EventListener {

    private static final Log log = LogFactory.getLog(SampleListener.class);

    @Override
    public void handleEvent(Event event) {
        EventContext ctx = event.getContext();
        DocumentEventContext docCtx = (DocumentEventContext) ctx;
        DocumentModel doc = docCtx.getSourceDocument();

        if (DocumentEventTypes.DOCUMENT_CREATED.equals(event.getName()) ||
            DocumentEventTypes.DOCUMENT_UPDATED.equals(event.getName())){

            log.info("Hi from SampleListener");
        }
    }
}
