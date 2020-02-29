"Intelligent Event-driven Stateful Microservices with Helidon and Autonomous Database on OCI" 

WORKSHOP - NOTE THAT THIS IS A WORK IN PROGRESS AND WILL BE COMPLETE BY MID MARCH 2020

![demo architecture](demo-arch.png) 

Task 1 (create OCI account, OKE cluster, ATP databases, and access OKE from cloud shell)
   - Get (free) OCI account and tenancy 
        - https://myservices.us.oraclecloud.com/mycloud/signup
        - note tenancy ocid, region name, user ocid
   - Create user api key and note the private key/pem location, fingerprint, and passphrase foobar 
        - https://docs.cloud.oracle.com/en-us/iaas/Content/Functions/Tasks/functionssetupapikey.htm
   - Create compartment
        - https://docs.cloud.oracle.com/en-us/iaas/Content/Identity/Tasks/managingcompartments.htm?Highlight=creating%20a%20comparment
        - https://oracle-base.com/articles/vm/oracle-cloud-infrastructure-oci-create-a-compartment#create-compartment
   - Create OCIR repos and auth key
        - https://docs.cloud.oracle.com/en-us/iaas/Content/Registry/Tasks/registrycreatingarepository.htm
        - https://docs.cloud.oracle.com/en-us/iaas/Content/Registry/Tasks/registrypushingimagesusingthedockercli.htm (login etc. steps can be done in later tasks)
   - Create OKE cluster
        - https://docs.cloud.oracle.com/en-us/iaas/Content/ContEng/Tasks/contengcreatingclusterusingoke.htm
        - https://docs.cloud.oracle.com/en-us/iaas/Content/ContEng/Tasks/contengaccessingclusterkubectl.htm
        - https://docs.cloud.oracle.com/en-us/iaas/Content/ContEng/Tasks/contengdownloadkubeconfigfile.htm
   - Create 2 atps pdbs: inventorypdb and orderpdb (for order and all other services)
        - https://docs.oracle.com/en/cloud/paas/autonomous-data-warehouse-cloud/tutorial-getting-started-autonomous-db/index.html
        - note the ocid, compartmentId, name, and admin pw of the databases
        - download the wallet (connection info) and note the wallet password (this is optional depending on setup - todo elaborate)
   - Enter cloud shell and issue command to export kubeconfig for the OKE cluster created
        - related blog with quick instructions here: https://blogs.oracle.com/cloud-infrastructure/announcing-oracle-cloud-shell
        - Verify OKE access using command such as `kubectl get pods --all-namespaces`
    
    
Task 2 (create github account and build microservice image)
   - Create github account
        - http://github.com
   - From cloud shell...
   - run `git clone https://github.com/paulparkinson/msdataworkshop.git`
        - optionally (if planning to make modifications, for example) fork this repos and run `git clone` on the forked repos
   - `cd msdataworkshop`
   - todo mvn install soda and aqapi jars from objectstore
   - run `mvn install`


Task 3 (push image, deploy, and access microservice)
   - Setup OCIR, create authkey
   - From cloud shell...
   - `docker login` 
        - https://docs.cloud.oracle.com/en-us/iaas/Content/Registry/Tasks/registrypushingimagesusingthedockercli.htm
        - example `docker login` user: datademotenancy/datademouser password: [authtoken]
   - Modify the following files... (todo get this from single location such as DEMOREGISTRY env var)
        - export DEMOREGISTRY setting it to OCIR repos location such as us-phoenix-1.ocir.io/stevengreenberginc/paul.parkinson/msdataworkshop
        - edit pom.xml and replace <docker.image.prefix>us-phoenix-1.ocir.io/stevengreenberginc/paul.parkinson/msdataworkshop</docker.image.prefix>
        - edit `./deploy.sh` and replace us-phoenix-1.ocir.io/stevengreenberginc/paul.parkinson/msdataworkshop/frontend-helidon:0.1
   - run `./build.sh` in frontend-helidon dir to push imagine to OCIR
   - run `kubectl create ns datademo` 
   - run `./deploy.sh` to create deployment and service to namespace datademo created in previous step
   - check frontend pod is running by using `kubectl get pods --all-namespaces`
   - check frontend loadbalancer address using `kubectl get services --all-namespaces`
   - access frontend page via frontend loadbalancer service, eg http://129.146.94.70:8080
   - todo give nodeport/port-forward example


Task 4 (Setup OCI Open Service Broker, binding to 2 existing atp instances, and verify with test/admin app for both)
   - Refer to https://github.com/oracle/oci-service-broker and specifically...
        - https://github.com/oracle/oci-service-broker/blob/master/charts/oci-service-broker/docs/installation.md
        - https://github.com/oracle/oci-service-broker/blob/master/charts/oci-service-broker/docs/atp.md
        - https://www.youtube.com/watch?v=qW_pw6Nd5hM&t=12s
   - run ./installOSB.sh
   - If not already created, create user API Key with password...
        - https://docs.cloud.oracle.com/en-us/iaas/Content/Functions/Tasks/functionssetupapikey.htm
   - modify `./ocicredentialsSecret` supplying values from Task 1 
   - run `./ocicredentialsSecret`
   - run ./installOCIOSB.sh
   - run `svcat get brokers` 
        - recheck until broker is shown in ready state
   - run `svcat get classes` and `svcat get plans` 
   - Do the following for each/both ATP instances...
        - modify `oci-service-broker/samples/atp/atp-existing-instance.yaml` # provide class and plan name and pdb ocid and compartmentID
        - run `kubectl create -f charts/oci-service-broker/samples/atp/atp-existing-instance.yaml`
        - run `svcat get instances` '
            - verify in ready state
        - modify `oci-service-broker/samples/atp/atp-binding-plain.yaml` 
            - provide wallet password (either new or existing, for example if downloaded from console previously)
        - run `kubectl create -f charts/oci-service-broker/samples/atp/atp-binding-plain.yaml`
        - run `svcat get bindings` # verify in ready state
        - run `kubectl get secrets atp-demo-binding -o yaml` 
        - modify `oci-service-broker/samples/atp/atp-demo-secret.yaml` 
            - provide admin password and wallet password (use `echo -n value | base64` to encode)
        - run `kubectl create -f oci-service-broker/samples/atp/atp-demo-secret.yaml`
   - If not already done (eg as part of Task 2) run `git clone https://github.com/paulparkinson/msdataworkshop.git`
   - `cd `
   - Show ref in micro-profile
    

Task 5 (setup AQ, order and inventory, saga, and CQRS)...
   - setup AQ, queue-progation
   - todo from frontpage app select create orderuser
   - todo from frontpage app select create inventoryuser
   - mvn install SODA and AQ jars
   - create order, inventory, and supplier deployments and services
   - todo from frontpage app select create ordertoinventory propagation
   - todo from frontpage app select create inventorytoorder propagation
   - demonstrate placeorder for choreography saga (success and fail/compensate)
   - demonstrate showorder for CQRS
   
Task 6 (setup streaming)
   - todo use OSB or just do manual streaming setup/secret?
   - create streaming deployment and service
   - demonstrate streaming

Task 7 (demonstrate health/readiness) 
   - eg order service is not ready until some data load (from view or eventsourcing or lazily) is done
   - show src and probes in deployment
   - https://kubernetes.io/docs/tasks/configure-pod-container/configure-liveness-readiness-startup-probes/
   - http://heidloff.net/article/implementing-health-checks-microprofile-istio
   - https://github.com/oracle/helidon/blob/master/docs/src/main/docs/health/02_health_in_k8s.adoc
   - https://github.com/oracle/helidon/blob/master/docs/src/main/docs/guides/07_health_se_guide.adoc
   - https://dmitrykornilov.net/2019/08/08/helidon-brings-microprofile-2-2-support/
    
Task 8 (demonstrate metrics prometheus and grafana (maybe monitoring and alert)
   - show compute auto-scaling in console before explaining horizontal scaling of pods.
        - for reference re compute instance scaling... https://docs.cloud.oracle.com/en-us/iaas/Content/Compute/Tasks/autoscalinginstancepools.htm
   - https://medium.com/oracledevs/how-to-keep-your-microservices-available-by-monitoring-its-metrics-d88900298025
   - https://learnk8s.io/autoscaling-apps-kubernetes
   - high level: https://itnext.io/kubernetes-monitoring-with-prometheus-in-15-minutes-8e54d1de2e13
   - https://github.com/coreos/prometheus-operator/blob/master/Documentation/user-guides/getting-started.md
   - helm repo update ;  helm install stable/prometheus-operator --name prometheus-operator --namespace monitoring
   - https://medium.com/oracledevs/deploying-and-monitoring-a-redis-cluster-to-oracle-container-engine-oke-5f210b91b800
   - helm install --namespace monitoring stable/prometheus-operator --name prom-operator --set kubeDns.enabled=true --set prometheus.prometheusSpec.serviceMonitorSelectorNilUsesHelmValues=false --set coreDns.enabled=false --set kubeControllerManager.enabled=false --set kubeEtcd.enabled=false --set kubeScheduler.enabled=false
   - kubectl get pods  -n monitoring
   - k create -f OrderServiceServiceMonitor.yaml -n datademo
   - kubectl port-forward -n monitoring prometheus-prometheus-operator-prometheus-0 9090
   - kubectl -n monitoring get pods | grep grafana
   - kubectl -n monitoring port-forward [podname] 3000:3000
   - Login with admin/prom-operator
   - https://kubernetes.io/docs/tasks/run-application/horizontal-pod-autoscale-walkthrough/#autoscaling-on-multiple-metrics-and-custom-metrics
   - https://github.com/helm/charts/tree/master/stable/prometheus-adapter
   - helm install --name my-release stable/prometheus-adapter

Task 9 (demonstrate OKE horizontal pod scaling)
   - install metrics-server
        - DOWNLOAD_URL=$(curl -Ls "https://api.github.com/repos/kubernetes-sigs/metrics-server/releases/latest" | jq -r .tarball_url)
        - DOWNLOAD_VERSION=$(grep -o '[^/v]*$' <<< $DOWNLOAD_URL)
        - curl -Ls $DOWNLOAD_URL -o metrics-server-$DOWNLOAD_VERSION.tar.gz
        - mkdir metrics-server-$DOWNLOAD_VERSION
        - tar -xzf metrics-server-$DOWNLOAD_VERSION.tar.gz --directory metrics-server-$DOWNLOAD_VERSION --strip-components 1
        - kubectl apply -f metrics-server-$DOWNLOAD_VERSION/deploy/1.8+/
   - kubectl get pods -n datademo |grep order-helidon
   - kubectl top pod order-helidon-74f848d85c-gxfq7 -n datademo --containers 
   - kubectl autoscale deployment order-helidon --cpu-percent=50 --min=1 --max=2 -n datademo
   - kubectl get hpa -n datademo
            NAME            REFERENCE                  TARGETS         MINPODS   MAXPODS   REPLICAS   AGE
            order-helidon   Deployment/order-helidon   <unknown>/50%   1         2         0          16s
   - increase cpu, notice cpu increase and scale to 2 pods

Task 10 (tracing)
   - install istio, demonstrate tracing (jaeger and kiali)
   - @Traced annotation

Task 11 (demonstrate scaling with pdb)
    - autoscaling
    - sharding
  
Task 12 (future)  
   - analytics - OSE server visualization
   - message to logic/endpoint mapping
   - kafka streams not just in chunks theres no end
   - rehydation / retention and compacted queues (most recent not all events) time windows
   - comes from functional aspect, no sharding orders based on phone number, more memory ?)_
   - https://medium.com/oracledevs/how-to-keep-your-microservices-available-by-monitoring-its-metrics-d88900298025
   - nice to have: 
        - messaging when available, JPA, JTA
        - fn
        - cloud developer service
        - apex report/callout to helidon
        - ORDS
        - Grafana of OCI https://blogs.oracle.com/cloudnative/data-source-grafana
        - graph route planning
        kms key monitoring

Task 13 (data flow) 
    - fully managed Spark service that lets you run Spark applications with almost no administrative overhead.
    - for demo: fog computing of IoT
    
Task 14 (data science)
    - enables data scientists to easily build, train, and manage machine learning models on Oracle Cloud, using Python and open source machine learning libraries
    - for demo: predictive analytics of orders to inventory/delivery locations

Task 15 (data catalog)
    - enables data consumers to easily find, understand, govern, and track Oracle Cloud data assets across the enterprise using an organized inventory of data assets
    - what data is available where in the organization and how trustworthy and fit-for-use they are
    - for demo: analytics report of order info from streaming + atp 

