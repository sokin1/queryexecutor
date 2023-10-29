package com.grepr.apiserver;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Connection;

import org.joda.time.Instant;

import com.codahale.metrics.health.HealthCheck;
import com.grepr.apiserver.resources.HelloWorldResource;
import com.grepr.apiserver.resources.SQLQueryResource;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class sqlexecutorApplication extends Application<sqlexecutorConfiguration> {

    Connection connection;
    JedisPool pool;

    public static void main(final String[] args) throws Exception {
        new sqlexecutorApplication().run(args);
    }

    @Override
    public String getName() {
        return "sqlexecutor";
    }

    @Override
    public void initialize(final Bootstrap<sqlexecutorConfiguration> bootstrap) {
        try {
            connection = DriverManager.getConnection(
                "jdbc:mariadb://localhost:3306/test-database",
                "jaekuk",
                "grepr");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        pool = new JedisPool("localhost", 6379);
    }

    @Override
    public void run(final sqlexecutorConfiguration configuration,
                    final Environment environment) {
        environment.jersey().register(new HelloWorldResource());
        environment.jersey().register(new SQLQueryResource(connection, pool));
        environment.healthChecks().register("tempHealthChecker", new HealthCheck() {

            @Override
            protected Result check() throws Exception {
                return Result.healthy();
            }
            
        });
    }

}
