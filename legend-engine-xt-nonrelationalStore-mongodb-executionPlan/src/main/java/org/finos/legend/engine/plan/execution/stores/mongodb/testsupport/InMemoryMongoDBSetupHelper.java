// Copyright 2023 Goldman Sachs
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.finos.legend.engine.plan.execution.stores.mongodb.testsupport;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
import org.bson.Document;
import org.finos.legend.engine.protocol.mongodb.schema.metamodel.data.MongoDBStoreEmbeddedData;

import java.net.InetSocketAddress;

public class InMemoryMongoDBSetupHelper
{
    public final String baseUrl;
    public final int port;
    private final MongoServer mongoServer;
    private final MongoClient mongoClient;


    public InMemoryMongoDBSetupHelper()
    {
        this.mongoServer = new MongoServer(new MemoryBackend());
        InetSocketAddress address = mongoServer.bind();

        this.baseUrl = address.getHostName();
        this.port = address.getPort();
        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            @Override
            public void run()
            {
                mongoServer.shutdown();
            }
        });
        this.mongoClient = MongoClients.create("mongodb://" + this.baseUrl + ":" + String.valueOf(this.port));
    }

    public void cleanUp()
    {
        this.mongoClient.close();
        this.mongoServer.shutdown();
    }

    public void setupData(MongoDBStoreEmbeddedData data)
    {
        String dbName = data.databaseName;
        data.testData.stream().forEach(item ->
        {
            MongoDatabase database = this.mongoClient.getDatabase(dbName);
            MongoCollection<Document> collection = database.getCollection(item.collectionName);
            item.documents.forEach(doc -> collection.insertOne(Document.parse(doc)));
        });
    }
}
