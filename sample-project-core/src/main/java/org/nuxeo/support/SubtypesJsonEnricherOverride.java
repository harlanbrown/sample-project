
package org.nuxeo.support;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonGenerator;

import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.query.sql.NXQL;
import org.nuxeo.ecm.core.schema.SchemaManager;
import org.nuxeo.ecm.core.io.registry.reflect.Setup;
import org.nuxeo.ecm.platform.types.SubtypesJsonEnricher;
import org.nuxeo.ecm.platform.types.localconfiguration.UITypesConfigurationConstants;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.services.config.ConfigurationService;

import static org.nuxeo.ecm.core.io.registry.reflect.Instantiations.SINGLETON;
import static org.nuxeo.ecm.core.io.registry.reflect.Priorities.OVERRIDE_REFERENCE;

@Setup(mode = SINGLETON, priority = OVERRIDE_REFERENCE)
public class SubtypesJsonEnricherOverride extends SubtypesJsonEnricher {

    protected static final String CFG_PREFIX = "single.instance.types.";

    @Override
    public void write(JsonGenerator jg, DocumentModel container)
            throws IOException {

        SchemaManager schemaManager =
                Framework.getService(SchemaManager.class);

        Collection<String> allowed =
                UITypesConfigurationConstants.computeSubtypes(container);

        ConfigurationService cs =
                Framework.getService(ConfigurationService.class);

        String csv = cs.getProperty(CFG_PREFIX + container.getType());

        if (csv != null && !csv.isBlank()) {

            Set<String> singleTypes = Arrays.stream(csv.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toCollection(HashSet::new));

            singleTypes.retainAll(allowed);

            CoreSession session = container.getCoreSession();
            String parentId = container.getId();

            if (session != null && parentId != null) {

                Set<String> hide = new HashSet<>();

                for (String type : singleTypes) {

                    String nxql =
                            "SELECT ecm:uuid FROM Document WHERE ecm:parentId = "
                            + NXQL.escapeString(parentId)
                            + " AND ecm:primaryType = "
                            + NXQL.escapeString(type)
                            + " AND ecm:isTrashed = 0";

                    if (!session.queryProjection(nxql, 0, 1).isEmpty()) {
                        hide.add(type);
                    }
                }

                allowed = allowed.stream()
                        .filter(type -> !hide.contains(type))
                        .collect(Collectors.toList());
            }
        }

        jg.writeFieldName(NAME);
        jg.writeStartArray();

        for (String subtype : allowed) {

            jg.writeStartObject();
            jg.writeStringField("type", subtype);

            jg.writeArrayFieldStart("facets");

            for (String facet :
                    schemaManager.getDocumentType(subtype).getFacets()) {
                jg.writeString(facet);
            }

            jg.writeEndArray();
            jg.writeEndObject();
        }

        jg.writeEndArray();
    }
}
