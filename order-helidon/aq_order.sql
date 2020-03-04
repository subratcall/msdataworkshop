
-- replace values for connect adminPW, orderuserPW, inventoruserPW, and servicename(s)
--  DBMS_CLOUD.CREATE_CREDENTIAL params, DBMS_CLOUD.GET_OBJECT params, CREATE_DATABASE_LINK parameter values,
connect connect admin/WelcomeOrder123@db202002011726_high
 GRANT PDB_DBA TO orderuser IDENTIFIED BY WelcomeOrder12345;
 GRANT EXECUTE ON DBMS_CLOUD_ADMIN TO orderuser;
 GRANT EXECUTE ON DBMS_CLOUD TO orderuser;
 GRANT CREATE DATABASE LINK TO orderuser;

connect orderuser/
 // as orderuser...
 BEGIN
 DBMS_CLOUD.CREATE_CREDENTIAL (
 'objectstore_cred',
 'paul.parkinson',
 'Q:4qWo:7PYDph9KZomZQ');
 END;
 /

 # had to make bucket public as paul.parkinson/authtoken wasnt working thus no `credential_name => 'objectstore_cred',` arg
 # this can likely be done by any user as it is shared, I did it with admin
 BEGIN
 DBMS_CLOUD.GET_OBJECT(
 object_uri => 'https://objectstorage.us-phoenix-1.oraclecloud.com/n/stevengreenberginc/b/msdataworkshop_bucket/o/cwallet.sso',
 directory_name => 'DATA_PUMP_DIR');
 END;
 /

 BEGIN
 DBMS_CLOUD.CREATE_CREDENTIAL(
 credential_name => 'DB_LINK_CRED_TEST1',
 username => 'INVENTORYUSER',
 password => 'Welcome12345'
 );
 END;
 /

 BEGIN
 DBMS_CLOUD_ADMIN.CREATE_DATABASE_LINK(
 db_link_name => 'TESTLINK1',
 hostname => 'adb.us-phoenix-1.oraclecloud.com',
 port => '1522',
 service_name => 'mnisopbygm56hii_inventorydb_high.atp.oraclecloud.com',
 ssl_server_cert_dn => 'CN=adwc.uscom-east-1.oraclecloud.com,OU=Oracle BMCS US,O=Oracle Corporation,L=Redwood City,ST=California,C=US',
 credential_name => 'DB_LINK_CRED_TEST1',
 directory_name => 'DATA_PUMP_DIR');
 END;
 /

 SELECT count(*) FROM inventory@TESTLINK1;

 CREATE OR REPLACE TYPE payload AS OBJECT(jdata CLOB);
 /
 BEGIN
 dbms_aqadm.create_queue_table(
 queue_table        => 'orderuser.my_dest_queue_table1',
 queue_payload_type => 'payload',
 multiple_consumers => TRUE);
 dbms_aqadm.create_queue(queue_name  => 'orderuser.my_dest_queue1',
 queue_table => 'orderuser.my_dest_queue_table1');
 dbms_aqadm.start_queue(queue_name => 'orderuser.my_dest_queue1');
 END;
 /

 BEGIN
 dbms_aqadm.create_queue_table(
 queue_table        => 'orderuser.my_source_queue_table1',
 queue_payload_type => 'payload',
 multiple_consumers => true);
 dbms_aqadm.create_queue(queue_name  => 'orderuser.my_source_queue1',
 queue_table => 'orderuser.my_source_queue_table1');
 dbms_aqadm.start_queue(queue_name => 'orderuser.my_source_queue1');
 END;
 /

 DECLARE
 sub sys.aq$_agent := sys.aq$_agent(name     => 'dest_subscriber1',
 address  => NULL,
 protocol => NULL);
 BEGIN
 dbms_aqadm.add_subscriber(queue_name     => 'orderuser.my_dest_queue1',
 subscriber     => sub);
 END;
 /

 DECLARE
 sub sys.aq$_agent := sys.aq$_agent(name     => 'dest_subscriber1',
 address  => 'inventoryuser.my_dest_queue1@TESTLINK1',
 protocol => NULL);
 BEGIN
 dbms_aqadm.add_subscriber(queue_name     => 'orderuser.my_source_queue1',
 subscriber     => sub,
 queue_to_queue => TRUE);
 END;
 /

 BEGIN
 dbms_aqadm.schedule_propagation(queue_name  => 'orderuser.my_source_queue1',
 destination => 'TESTLINK1',
 latency     => 10,
 destination_queue => 'orderuser.my_dest_queue1');
 COMMIT;
 END;
 /

 DECLARE
 enqueue_options    dbms_aq.enqueue_options_t;
 message_properties dbms_aq.message_properties_t;
 message_handle     RAW(16);
 my_payload         payload := payload('{"id":1,"First Name":"X","Middle Name" : "y", "Last Name" : "Z"}');
 BEGIN
 dbms_aq.enqueue(queue_name => 'orderuser.my_source_queue1',
 enqueue_options => enqueue_options,
 message_properties => message_properties,
 payload => my_payload,
 msgid => message_handle);
 COMMIT;
 dbms_output.put_line('enqueued message with id ' || RAWTOHEX(message_handle));
 END;
 /




 DONE on inventory DB...

 BEGIN
 dbms_aqadm.create_queue_table(
 queue_table        => 'inventoryuser.my_dest_queue_table1',
 queue_payload_type => 'payload',
 multiple_consumers => TRUE);
 dbms_aqadm.create_queue(queue_name  => 'inventoryuser.my_dest_queue1',
 queue_table => 'inventoryuser.my_dest_queue_table1');
 dbms_aqadm.start_queue(queue_name => 'inventoryuser.my_dest_queue1');
 END;
 /



 DECLARE
 sub sys.aq$_agent := sys.aq$_agent(name     => 'dest_subscriber1',
 address  => NULL,
 protocol => NULL);
 BEGIN
 dbms_aqadm.add_subscriber(queue_name     => 'inventoryuser.my_dest_queue1',
 subscriber     => sub);
 END;
 /

 DONE ON ORDER db...

 BEGIN
 dbms_aqadm.schedule_propagation(queue_name  => 'orderuser.my_source_queue1',
 destination => 'TESTLINK1',
 latency     => 10,
 destination_queue => 'inventoryuser.my_dest_queue1');
 COMMIT;
 END;
 /
 BEGIN
 dbms_aqadm.unschedule_propagation(queue_name  => 'orderuser.my_source_queue1',
 destination => 'TESTLINK1',
 destination_queue => 'inventoryuser.my_dest_queue1');
 COMMIT;
 END;
 /

 DECLARE
 enqueue_options    dbms_aq.enqueue_options_t;
 message_properties dbms_aq.message_properties_t;
 message_handle     RAW(16);
 my_payload         payload := payload('{"id":2,"First Name":"XX","Middle Name" : "yY", "Last Name" : "ZZ"}');
 BEGIN
 dbms_aq.enqueue(queue_name => 'orderuser.my_source_queue1',
 enqueue_options => enqueue_options,
 message_properties => message_properties,
 payload => my_payload,
 msgid => message_handle);
 COMMIT;
 dbms_output.put_line('enqueued message with id ' || RAWTOHEX(message_handle));
 END;
 /

 -- dequeue a message
 DECLARE
 dequeue_options    dbms_aq.dequeue_options_t;
 message_properties dbms_aq.message_properties_t;
 message_handle     RAW(16);
 my_payload         payload;
 BEGIN
 dequeue_options.navigation := dbms_aq.first_message;
 dequeue_options.consumer_name := 'DEST_SUBSCRIBER1';
 dbms_aq.dequeue(queue_name => 'inventoryuser.my_dest_queue1',
 dequeue_options => dequeue_options,
 message_properties => message_properties,
 payload => my_payload,
 msgid => message_handle);
 dbms_output.put_line('dequeued message with id ' || RAWTOHEX(message_handle));
 COMMIT;
 END;
 /

 SELECT count(*) FROM INVENTORYUSER.MY_DEST_QUEUE_TABLE1@TESTLINK1;

 //end of working commands