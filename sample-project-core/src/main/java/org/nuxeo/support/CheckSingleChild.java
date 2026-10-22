package org.nuxeo.support;

import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;

import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.query.sql.NXQL;

@Operation(
  id = CheckSingleChild.ID,
  category = "Support",
  label = "Check Single Child or Fail",
  description = "Throws 409 if a child of the given type already exists under the input parent."
)
public class CheckSingleChild {

  public static final String ID = "support.CheckSingleChild";

  @Context
  protected CoreSession session;

  @Context
  protected OperationContext ctx;

  @Param(name = "type", required = true)
  protected String type;

  @OperationMethod
  public DocumentModel run(DocumentModel container) {
    String nxql =
        "SELECT ecm:uuid FROM Document WHERE ecm:parentId = "
            + NXQL.escapeString(container.getId())
            + " AND ecm:primaryType = " + NXQL.escapeString(type)
            + " AND ecm:isTrashed = 0";

    boolean exists = !session.queryProjection(nxql, 0, 1).isEmpty();
    if (exists) {
      throw new NuxeoException(
          "single‑child rule: already exists " + type + " under this parent", 409);
    }
    return container;
  }
}