package io.helidon.data.examples;

import oracle.jdbc.OracleConnection;
import oracle.jdbc.aq.*;

import java.sql.SQLException;

public class QueueUtils {
    public static String sendMessage(OracleConnection conn, Object message, String queueName, String aggentName,
                                     String aggentAddress) throws SQLException {
        AQMessageProperties msgprop = AQFactory.createAQMessageProperties();
        // Specify an agent as the sender:
        AQAgent sender = AQFactory.createAQAgent();
        sender.setName(aggentName);
        sender.setAddress(aggentAddress);
        msgprop.setSender(sender);
        // add recipients
        // AQAgent recipient = AQFactory.createAQAgent();
        // recipient.setName("PROXY");
        // msgprop.setRecipientList(new AQAgent[] { recipient });
        // Create the actual AQMessage instance:
        AQMessage mesg = AQFactory.createAQMessage(msgprop);
        mesg.setPayload(new oracle.sql.RAW(JsonUtils.writeValueAsBytes(message)));
        // We want to retrieve the message id after enqueue:
        AQEnqueueOptions opt = new AQEnqueueOptions();
        opt.setRetrieveMessageId(true);
        // execute the actual enqueue operation:
        conn.enqueue(queueName, opt, mesg);
        String id = byteBufferToHexString(mesg.getMessageId(), 20);
//        log.debug("Sent to {} message {}, id {}", queueName, message, id);
        return id;
    }

    public static String getMessage(OracleConnection conn, String queueName) throws SQLException {
        AQDequeueOptions deqopt = new AQDequeueOptions();
        deqopt.setRetrieveMessageId(true);
        AQMessage msg = conn.dequeue(queueName, deqopt, "RAW");
        byte[] msgId = msg.getMessageId();
        String messageId = null;
        if (msgId != null) {
            messageId = QueueUtils.byteBufferToHexString(msgId, 20);
        }
        byte[] payload = msg.getPayload();
        String payloadStr = new String(payload);
//        log.debug("Received from {} message {}, id {}", queueName, payloadStr, messageId);
        return payloadStr;
    }

    public static String byteBufferToHexString(byte[] buffer, int maxNbOfBytes) {
        if (buffer == null)
            return null;
        int offset = 0;
        boolean isFirst = true;
        StringBuilder sb = new StringBuilder();
        while (offset < buffer.length && offset < maxNbOfBytes) {
            if (!isFirst)
                sb.append(' ');
            else
                isFirst = false;
            String hexrep = Integer.toHexString((int) buffer[offset] & 0xFF);
            if (hexrep.length() == 1)
                hexrep = "0" + hexrep;
            sb.append(hexrep);
            offset++;
        }
        return sb.toString();
    }
}