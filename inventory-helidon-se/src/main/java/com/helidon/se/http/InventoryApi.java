package com.helidon.se.http;

import com.helidon.se.http.service.InventoryService;
import com.helidon.se.persistence.Database;

import io.helidon.config.Config;

public class InventoryApi extends WebApi {

    public InventoryApi(Database database, Config config) {
        super(config, r -> r.register("/inventory", new InventoryService(database)));
    }

}
