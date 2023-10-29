package com.grepr.apiserver.resources;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Path("/sqlquery")
public class SQLQueryResource {

    private Connection conn;
    private JedisPool pool;

    private static int PAGE_SIZE = 50;

    public SQLQueryResource(Connection conn, JedisPool pool) {
        this.conn = conn;
        this.pool = pool;
    }

    /* Request handle
     * @Description Request handle of request with timeout(60s) to let user to use it to get results
     *              handle is type of uuid and stored in redis queue with current offset starting from 0
     */
    @Path("/requestHandle")
    @GET
    public String getHandle() {
        UUID uuid = UUID.randomUUID();
        try (Jedis jedis = pool.getResource()) {
            jedis.set(uuid.toString(), "0");
            jedis.expire(uuid.toString(), 60);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return uuid.toString();
    }

    /* Getting offset from handle
     * @Param Required handle   : UUID handle of request which is generate from /requestHandle endpoint
     * @Description validating handle and get current offset of it
     */
    private int validateAndGetOffset(String handle) {
        try (Jedis jedis = pool.getResource()) {
            if (!jedis.exists(handle)) return -1;
            return Integer.valueOf(jedis.get(handle));
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    /* SELECT QUERY with HANDLE
     * @Param Required handle   : UUID handle of request which is generate from /requestHandle endpoint
     * @Param Optional size     : size of results, if not set, 50 is used
     * @Description This endpoint is running select query with handle. If handle is not expired(60s), this query will
     *              retrieve next available amount of record which is set by user or default(50)
     */
    @Path("/select")
    @GET
    public String selectQuery(@QueryParam("handle") String handle, @QueryParam("size") Optional<String> pageSize) {
        int offset = validateAndGetOffset(handle);
        if (offset == -1) return "Handle is expired. Try to get new handle from /sqlquery/requestHandle";

        int pSize = PAGE_SIZE;
        if (pageSize.isPresent()) pSize = Integer.valueOf(pageSize.get());

        StringBuilder rtn = new StringBuilder();
        try (PreparedStatement statement = conn.prepareStatement("SELECT * FROM products_tbl WHERE product_id > ? LIMIT ?")) {
            statement.setInt(1, offset);
            statement.setInt(2, pSize);

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt(1);
                String name = resultSet.getString(2);
                String manuf = resultSet.getString(3);
                Date date = resultSet.getDate(4);

                rtn.append(id + ", ");
                rtn.append(name + ", ");
                rtn.append(manuf + ", ");
                rtn.append(date + "\n");
            }

            try (Jedis jedis = pool.getResource()) {
                jedis.set(handle, String.valueOf(offset+pSize));
                jedis.expire(handle, 60);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        return rtn.toString();
    }

    /* SELECT QUERY with HANDLE
     * @Param Required num   : number of records to insert
     * @Description This is inserting records to table to run tests
     */
    @Path("/insert")
    @GET
    public String insertQuery(@QueryParam("num") int num) {
        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO products_tbl(product_name, product_manufacturer, submission_date) VALUES (?, ?, ?)")) {
            for (int i = 0; i < num; i++) {

                ps.setString(1, "name"+i);
                ps.setString(2, "manuf"+i);
                ps.setDate(3, new java.sql.Date(1234567));
                
                ps.addBatch();
            }

            ps.executeBatch();
        } catch(Exception e) {
            e.printStackTrace();
        }

        return "done";
    }
    
}
