package fr.rewrite.server.domain.build.command;

import fr.rewrite.server.domain.datastore.DatastoreId;
import fr.rewrite.server.shared.error.domain.Assert;
import org.jmolecules.architecture.cqrs.Command;

@Command
public record BuildProject(DatastoreId datastoreId) {
    public BuildProject {
        Assert.notNull("datastoreId", datastoreId);
    }
    public static BuildProject from(DatastoreId datastoreId){
        return new  BuildProject(datastoreId);
    }
}
