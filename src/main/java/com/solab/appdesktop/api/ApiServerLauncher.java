package com.solab.appdesktop.api;

import com.solab.appdesktop.repository.SQLiteUtil;

public class ApiServerLauncher {

    public static void main(String[] args) throws Exception {
        SQLiteUtil.crearTablas();

        ApiHttpServer server = new ApiHttpServer();
        server.start();

        Thread.currentThread().join();
    }
}
