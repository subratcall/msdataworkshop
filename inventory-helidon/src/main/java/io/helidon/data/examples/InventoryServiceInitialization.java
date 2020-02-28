package io.helidon.data.examples;

import oracle.AQ.AQQueueTable;
import oracle.AQ.AQQueueTableProperty;
import oracle.jms.AQjmsDestination;
import oracle.jms.AQjmsDestinationProperty;
import oracle.jms.AQjmsFactory;
import oracle.jms.AQjmsSession;
import oracle.ucp.jdbc.PoolDataSource;

import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.Session;
import javax.sql.DataSource;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

// this could also go in an initContainer(s) or some such but is convenient here
public class InventoryServiceInitialization {

    String queueOwner = "aquser1";
    String queueOwnerPW = "xxxxx";

    public Object createInventoryTable(Connection connection) throws SQLException {
        System.out.println("Order createInventoryTable...");
        connection.createStatement().executeUpdate(
                "create table inventory (inventoryid varchar(16), inventorylocation varchar(32), inventorycount integer)");
        System.out.println("inventory table created successfully");
        return "inventory table created successfully";
    }

    public Object createQueue(DataSource aqDataSource, String queueOwner, String queueName, String msgType) throws Exception {
        System.out.println("create queue queueName:" + queueName);
        QueueConnectionFactory q_cf = AQjmsFactory.getQueueConnectionFactory(aqDataSource);
        QueueConnection q_conn = q_cf.createQueueConnection();
        Session session = q_conn.createQueueSession(true, Session.CLIENT_ACKNOWLEDGE);
        AQQueueTable q_table = null;
        AQQueueTableProperty qt_prop =
                new AQQueueTableProperty(
                        msgType.equals("map") ? "SYS.AQ$_JMS_MAP_MESSAGE":"SYS.AQ$_JMS_TEXT_MESSAGE" )  ;
        q_table = ((AQjmsSession) session).createQueueTable(queueOwner, queueName, qt_prop);
        Queue queue = ((AQjmsSession) session).createQueue(q_table, queueName, new AQjmsDestinationProperty());
        ((AQjmsDestination)queue).start(session, true, true);
        System.out.println("create queue successful for queue:" + queue.toString());
        return "createOrderQueue successful for queue:" + queue.toString();
    }

    Object createAQUser(Connection sysDBAConnection) throws SQLException {
        sysDBAConnection.createStatement().execute("grant dba to " + queueOwner + " identified by " + queueOwnerPW);
        sysDBAConnection.createStatement().execute("grant unlimited tablespace to " + queueOwner);
        sysDBAConnection.createStatement().execute("grant connect, resource TO " + queueOwner);
        sysDBAConnection.createStatement().execute("grant aq_user_role TO " + queueOwner);
        sysDBAConnection.createStatement().execute("GRANT EXECUTE ON sys.dbms_aqadm TO " + queueOwner);
        sysDBAConnection.createStatement().execute("GRANT EXECUTE ON sys.dbms_aq TO " + queueOwner);
        sysDBAConnection.createStatement().execute("GRANT EXECUTE ON sys.dbms_aq TO " + queueOwner);
        //    sysDBAConnection.createStatement().execute("create table tracking (state number)");
        return "createAQUser successful";
    }

}
