package fr.rewrite.server.infrastructure.secondary.build;

import fr.rewrite.server.domain.datastore.Datastore;

public interface Tool {
    void build(Datastore datastore);
}
