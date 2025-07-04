package fr.rewrite.server.domain.build.command;

import fr.rewrite.server.domain.datastore.DatastoreId;
import fr.rewrite.server.shared.error.domain.Assert;
import org.jmolecules.architecture.cqrs.Command;

@Command
public record GetClassPath(DatastoreId datastoreId) {
    public GetClassPath {
        Assert.notNull("datastoreId", datastoreId);
    }
    public static GetClassPath from(DatastoreId datastoreId){
        return new GetClassPath(datastoreId);
    }
}
