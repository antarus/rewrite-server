package fr.rewrite.server.infrastructure.secondary.log;

import fr.rewrite.server.domain.datastore.DatastoreId;
import fr.rewrite.server.domain.log.LogLevel;

record SubscriberInfo(LogSubscriber subscriber, LogLevel minLevel, DatastoreId datastoreId) {}
