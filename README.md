"Intelligent Event-driven Stateful Microservices with Helidon and Autonomous Database on OCI" 

![demo architecture](demo-arch.png) 
![demo ERD](demo-erd.png) 

Task 1 (Create OCI account, OKE cluster, ATP databases) 
   - Estimated task time 20 minutes
   - Video walk through: https://www.youtube.com/watch?v=0LeyGPw2vAA
   - Get (free) OCI account and tenancy 
        - https://myservices.us.oraclecloud.com/mycloud/signup
        - note tenancy ocid, region name, user ocid
   - Create user api key and note the private key/pem location, fingerprint, and passphrase (be sure to use a passphrase)
        - https://docs.cloud.oracle.com/en-us/iaas/Content/Functions/Tasks/functionssetupapikey.htm
   - Create compartment
        - https://docs.cloud.oracle.com/en-us/iaas/Content/Identity/Tasks/managingcompartments.htm?Highlight=creating%20a%20comparment
        - https://oracle-base.com/articles/vm/oracle-cloud-infrastructure-oci-create-a-compartment#create-compartment
   - Create OCIR repos and auth key
        - Create a meaningful repos name such as `paul.parkinson/msdataworkshop` and note the repo-name you've created as it will be used in Task 4
        - https://docs.cloud.oracle.com/en-us/iaas/Content/Registry/Tasks/registrycreatingarepository.htm
   - Create OKE cluster
        - https://docs.cloud.oracle.com/en-us/iaas/Content/ContEng/Tasks/contengcreatingclusterusingoke.htm
        - https://docs.cloud.oracle.com/en-us/iaas/Content/ContEng/Tasks/contengaccessingclusterkubectl.htm
        - https://docs.cloud.oracle.com/en-us/iaas/Content/ContEng/Tasks/contengdownloadkubeconfigfile.htm
   - Create 2 ATP-S pdbs named `orderdb` and `inventorydb` (for order and all other services)
        - If the pdbs are not named `orderdb` and `inventorydb` the deployment yamls in the examples will need to be modified to use the names given.
        - Select the license included option for license.
        - https://docs.oracle.com/en/cloud/paas/autonomous-data-warehouse-cloud/tutorial-getting-started-autonomous-db/index.html 
        - Note the ocid, compartmentId, name, and admin pw of the databases
        - Download the regional wallet (connection info) and note the wallet password (this is optional depending on setup - todo elaborate)

Task 2 (Use Cloud Shell to access OKE cluster and create `msdataworkshop` namespace)
   - Estimated task time 2 minutes
   - Video walk through: https://www.youtube.com/watch?v=vYD1s6c3a8w
   - Enter Cloud Shell and issue command to export kubeconfig for the OKE cluster created
   - Related blog with quick instructions here: https://blogs.oracle.com/cloud-infrastructure/announcing-oracle-cloud-shell
   - Verify OKE access using command such as `kubectl get pods --all-namespaces`
   - Create `msdataworkshop` namespace using command `kubectl create ns msdataworkshop`
    
Task 3 (Create github account and build microservice image)
   - Estimated task time 2 minutes
   - Video walk through: https://www.youtube.com/watch?v=6g4c2yjbTPg
   - Optionally (if planning to make modifications, for example) 
        - Create github account if needed
        - Fork `https://github.com/paulparkinson/msdataworkshop.git` 
   - From Cloud Shell...
   - Run `git clone https://github.com/paulparkinson/msdataworkshop.git`
        - or if using a fork then `git clone` that fork instead
   - `cd msdataworkshop/frontend-helidon`
   - Run `mvn clean install`

Task 4 (Push image, deploy, and access microservice)
   - Estimated task time 8 minutes
   - Video walk through: https://www.youtube.com/watch?v=I10NLrAYjIM
   - From Cloud Shell...
   - Login to OCIR and verify docker
        - https://docs.cloud.oracle.com/en-us/iaas/Content/Registry/Tasks/registrypushingimagesusingthedockercli.htm
        - Run `docker login <region-key>.ocir.io -u <tenancy-namespace>/<username> -p <authtoken>` 
            - `<tenancy-namespace>` is the Object Storage Namespace found under tenancy information
            - example `docker login us-phoenix-1.ocir.io -u ax2mkasdfkkx/msdataworkshopuser -p Q:4qXo:7ADFaf9KZddZQ`
        - Verify with `docker image` command
   - For convenience, vi ~/.bashrc, append the following lines (substituting DOCKER_REGISTRY and MSDATAWORKSHOP_LOCATION values), and `source ~/.bashrc`

         export MSDATAWORKSHOP_LOCATION=~/msdataworkshop
         source $MSDATAWORKSHOP_LOCATION/shortcutaliases
         export PATH=$PATH:$MSDATAWORKSHOP_LOCATION/utils/
         export DOCKER_REGISTRY="<region-key>.ocir.io/<tenancy-namespace>/<repo-name>"
   - `cd $MSDATAWORKSHOP_LOCATION/frontend-helidon`
   - Run `./build.sh` to build the frontend-helidon image and push it to OCIR
   - Mark the image as public in OCIR via Cloud Shell (this avoids the need to do `docker login` in the deployment yaml or git CI/CD)
   - Run `./deploy.sh` to create deployment and service in the msdataworkshop namespace 
   - Check frontend pod is Running by using `kubectl get pods --all-namespaces` or the `pods` shortcut command
   - Check frontend loadbalancer address using `kubectl get services --all-namespaces`  or use `services` shortcut command
   - Access frontend page 
        - via frontend LoadBalancer service, eg http://129.146.99.99:8080
        - or, if the service has been modified to use NodePort instead of LoadBalancer...
            - `kubectl port-forward [frontend pod] -n msdataworkshop 8080:8080`
            - and access http://localhost:8080
   - Run `cd $MSDATAWORKSHOP_LOCATION ; ./build.sh ` to build and push all of the rest of the microservices
   - Mark all of the images as public in OCIR via Cloud Shell as done with the frontend image

Task 5 (Setup OCI Open Service Broker)
   - Estimated task time 5 minutes
   - Video walk through: https://www.youtube.com/watch?v=cb8N1TzNgsk
   - `cd $MSDATAWORKSHOP_LOCATION/osb-atp-and-oss`
   - Set ocid, etc. values in `setupOSB.sh` and run `./setupOSB.sh`
   - Refererences... 
        - https://github.com/oracle/oci-service-broker/blob/master/charts/oci-service-broker/docs/installation.md
        - https://www.youtube.com/watch?v=qW_pw6Nd5hM
   
Task 6 (Using OCI service broker, create binding to 2 existing ATP instances)
   - Estimated task time 5 minutes
   - Video walk through: https://www.youtube.com/watch?v=EGO-bLHrhv0
   - `cd $MSDATAWORKSHOP_LOCATION/osb-atp-and-oss`
   - Set ocid and password values in `setupATP.sh` and run `./setupATP.sh`
   - References...
        - https://github.com/oracle/oci-service-broker/blob/master/charts/oci-service-broker/docs/atp.md
        - https://www.youtube.com/watch?v=qW_pw6Nd5hM
  
Task 7 (Verify and understand ATP connectivity via Helidon microservice deployment in OKE)
   - Estimated task time 5 minutes
   - Video walk through: https://www.youtube.com/watch?v=k44qrpdohTM
   - Notice [atpaqadmin-deployment.yaml](atpaqadmin/atpaqadmin-deployment.yaml) wallet, secret, decode initcontainer, etc. 
   - Notice `atp*` references in [microprofile-config.properties](atpaqadmin/src/main/resources/META-INF/microprofile-config.properties) 
   - Notice @Inject dataSources in [ATPAQAdminResource.java](atpaqadmin/src/main/java/oracle/db/microservices/ATPAQAdminResource.java)
   - `cd $MSDATAWORKSHOP_LOCATION/atpaqadmin`
   - Run `./deploy.sh` to create deployment and service
   - Run `msdataworkshop` command to verify existence of deployment and service and verify pod is in Running state
   - Open the frontend microservice home page and hit the `testdatasources` button 
   - Troubleshooting... 
        - Look at logs... `kubectl logs [podname] -n msdataworkshop`
        - If no request is shown in logs, try accessing the pod directly using port-forward
            - `kubectl port-forward [atpadmin pod] -n msdataworkshop 8080:8080`
            - http://localhost:8080/test
            
Task 8 (Setup DB links between ATP PDBs, AQ, and Queue propagation)...
   - Download connection information zip for ATP instances from console.
   - Upload cwallet.sso to objectstore, obtain and note pre-authorized URL for cwallet.sso
        - (this is for convenience, alternatively a DBMS_CLOUD.CREATE_CREDENTIAL can be used to create a credential that is then used to execute GET_OBJECT in PropagationSetup.java)
   - `cd $MSDATAWORKSHOP_LOCATION/atpaqadmin`
   - Edit `atpaqadmin-deployment.yaml` and provide values in the section marked with `PROVIDE VALUES FOR THE FOLLOWING...`
   - Run `./redeploy.sh` 
   - Run `pods` and verify the atpaqadmin pod is in running state
   - Open the frontend microservice home page and hit the following buttons in order  
        - `createUsers`, `createInventoryTable`, `createDBLinks`, `setupTablesQueuesAndPropagation`
   - If it is necessary to restart or rerun the process or to clean the database...
        - hit the `unschedulePropagation` button only if `setupTablesQueuesAndPropagation` was run and then hti the `deleteUsers` button
        - it may be necessary to run `deletepod admin` first as there may be open connections that need to be be released.
   - If any changes are made to src code or deployment, simply run `./build.sh ; ./redeploy.sh` to rebuild and redeploy
                
Task 9 (Demonstrate Converged database (relational, JSON, spatial, etc.), Event-driven Order/Inventory Saga, Event Sourcing, CQRS, etc. via Order/Inventory store application)
   - `cd $MSDATAWORKSHOP_LOCATION/order-helidon ; ./build.sh ; ./deploy.sh`
   - `cd $MSDATAWORKSHOP_LOCATION/inventory-helidon ; ./build.sh ; ./deploy.sh`
   - `cd $MSDATAWORKSHOP_LOCATION/supplier-helidon-se ; ./build.sh ; ./deploy.sh`
   - Open the frontend microservice home page
   - Hit the `listenForMessages` button 
   - Check inventory for a given item such as a cucumbers and insure (remove if necessary) no inventory exists
   - Place an order for that item and then select show order and notice failed order due to choreography saga compensation
   - Add inventory for the item and place and show the order again noticing the order was successful 
   - This demonstrates atomic exactly once event-driven communication over AQ, 
   - This also demonstrates CQRS as the command is executed on the database while the query is derived from events received.
   - If any changes are made to src code or deployment, simply run `./build.sh ; ./redeploy.sh` to rebuild and redeploy
   - todo add doc links on JSON both in db (eg SODA), Helidon, and in general
   
   
Task 9.5 tbd (Demonstrate spatial service running on Weblogic (operator) via order delivery routing service )
   - ...
   
Task 10 (OSS streaming service)
   - Insure Task 4 is complete and refer to https://github.com/oracle/oci-service-broker and specifically...
        - https://github.com/oracle/oci-service-broker/blob/master/charts/oci-service-broker/docs/oss.md
   - In Cloud Shell and streaming policy
        - add a group for user if one does not exist
        - add policy for that group to allow streaming (eg name `StreamingPolicy`, description `Allow to manage streams`)
            - Policy statement `Allow group <SERVICE_BROKER_GROUP> to manage streams in compartment <COMPARTMENT_NAME>`
            - eg `Allow group msdataworkshop-admins to manage streams in compartment msdataworkshop-sandbox)`' 
   - todo... Currently hitting some issues with the following and resorting to manually setting 
   - cd to `oci-service-broker` directory such as oci-service-broker-1.3.3
   - `cp samples/oss/create-oss-instance.yaml create-oss-instance-order.yaml`
   - Modify `create-oss-instance-order.yaml` 
        - change name to `teststreamorder` provide compartmentID and specify `1` partition
   - Run `kubectl create -f create-oss-instance-order.yaml -n msdataworkshop`
   - `cp samples/oss/create-oss-binding.yaml create-oss-binding-order.yaml`
   - Modify `create-oss-binding-order.yaml` 
        - change name to `test-stream-binding-order` and change instanceRef name to `teststream-order'
   - Run `kubectl create -f create-oss-binding-order.yaml -n msdataworkshop`
   - Run `kubectl get secrets test-stream-binding-order -o yaml -n msdataworkshop`
   - Demonstrate streaming orders in frontend app by hitting `producerstream` button
   - If any changes are made to src code or deployment, simply run `./build.sh ; ./redeploy.sh` to rebuild and redeploy

Task 11 (Helidon/OKE health liveness/readiness) 
   - eg order service is not ready until some data load (from view or eventsourcing or lazily) is done
   - show src and probes in deployment
   - documentaion references...
       - https://kubernetes.io/docs/tasks/configure-pod-container/configure-liveness-readiness-startup-probes/
       - https://github.com/oracle/helidon/blob/master/docs/src/main/docs/health/02_health_in_k8s.adoc
       - https://github.com/oracle/helidon/blob/master/docs/src/main/docs/guides/07_health_se_guide.adoc
       - https://dmitrykornilov.net/2019/08/08/helidon-brings-microprofile-2-2-support/
   - If any changes are made to src code or deployment, simply run `./build.sh ; ./redeploy.sh` to rebuild and redeploy
    
Task 12 (Demonstrate OKE horizontal pod scaling)
   - `cd $MSDATAWORKSHOP_LOCATION`
   - Run `./installMetricsServer.sh` 
   - `cd $MSDATAWORKSHOP_LOCATION/order-helidon`
   - uncomment `resources` section `Task 12` in `order-helidon-deployment.yaml`
   - Run `./redeploy.sh`
   - Run `toppod order` and notice CPU and memory usage
   - Run `kubectl autoscale deployment order-helidon --cpu-percent=50 --min=1 --max=2 -n msdataworkshop`
   - Run `hpa` and notice output
   - Open the frontend microservice home page and hit the `startCPUStress` button
   - increase cpu, notice cpu increase and scale to 2 pods
   - `kubectl delete hpa order-helidon -n msdataworkshop` to clean up
   - alternate demo...
       - `kubectl run oraclelinux77-hpa-demo --image=phx.ocir.io/oraclegilsonmel/oraclelinux77-demo:latest --requests=cpu=200m --limits=cpu=500m tailf /dev/null -n msdataworkshop`
       - `pods | grep linux`
       - `kubectl autoscale deployment oraclelinux77-hpa-demo --cpu-percent=50 --min=1 --max=10 -n msdataworkshop`
       - `hpa ; toppod linux ; k get deployment oraclelinux77-hpa-demo -n msdataworkshop  ; pods |grep linux ;echo ----------------`
       - `podshell linux`
       - `stress --cpu 4 &` and exit
       - `hpa ; toppod linux ; k get deployment oraclelinux77-hpa-demo -n msdataworkshop  ; pods |grep linux ;echo ----------------` 
       - repeated above command as necessary noting changes and additional pods
       - cleanup...
       - `k delete deployment oraclelinux77-hpa-demo -n msdataworkshop`
       - `k delete hpa oraclelinux77-hpa-demo -n msdataworkshop`
   - If any changes are made to src code or deployment, simply run `./build.sh ; ./redeploy.sh` to rebuild and redeploy

Task 13 (Tracing)
   - If any changes are made to src code or deployment, simply run `./build.sh ; ./redeploy.sh` to rebuild and redeploy

Future here to end...

(Demonstrate metrics prometheus and grafana (monitoring and alert)

(tracing)
   - install istio, demonstrate tracing (jaeger and kiali)
   - @Traced annotation
   
(LRA)

(security, vault, etc) 

(sharding)

(data flow) 
    - fully managed Spark service that lets you Run Spark applications with almost no administrative overhead.
    - for demo: fog computing of IoT
    
(data science)
    - enables data scientists to easily build, train, and manage machine learning models on Oracle Cloud, using Python and open source machine learning libraries
    - for demo: predictive analytics of orders to inventory/delivery locations

(data catalog)
    - enables data consumers to easily find, understand, govern, and track Oracle Cloud data assets across the enterprise using an organized inventory of data assets
    - what data is available where in the organization and how trustworthy and fit-for-use they are
    - for demo: analytics report of order info from streaming + atp 

