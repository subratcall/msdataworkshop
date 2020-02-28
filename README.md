"Intelligent Event-driven Stateful Microservices with Helidon and Autonomous Database on OCI" 

WORKSHOP

Task 1
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
- Create github account
    - http://github.com
- Enter cloud console and issue command to export kubeconfig for the OKE cluster created
    - related blog with quick instructions here: https://blogs.oracle.com/cloud-infrastructure/announcing-oracle-cloud-shell
    - Verify OKE access using command such as `kubectl get pods --all-namespaces`
    
Task 2
- From cloud console...
- run `git clone https://github.com/paulparkinson/msdataworkshop.git`
    - optionally (if planning to make modifications, for example) fork this repos and run `git clone` on the forked repos
- `cd msdataworkshop/frontend-helidon/`
- mvn install

Task 3
- Setup OCIR, create authkey
- From cloud shell...
- login and modify the following files... (todo get this from DEMOREGISTRY env var)
    - export DEMOREGISTRY setting it to OCIR repos location such as us-phoenix-1.ocir.io/stevengreenberginc/paul.parkinson/msdataworkshop
    - edit pom.xml and replace <docker.image.prefix>us-phoenix-1.ocir.io/stevengreenberginc/paul.parkinson/msdataworkshop</docker.image.prefix>
    - edit `./deploy.sh` and replace us-phoenix-1.ocir.io/stevengreenberginc/paul.parkinson/msdataworkshop/frontend-helidon:0.1
- run `./build.sh` in frontend-helidon dir to push imagine to OCIR
- run `./deploy.sh` to create deployment and service
- check frontend pod is running by using `kubectl get pods --all-namespaces`
- check frontend loadbalancer address using `kubectl get services --all-namespaces`
- access frontend page via frontend loadbalancer service, eg http://129.146.94.70:8080
- todo give nodeport/port-forward example



2nd hr (OCI Open Service Broker)... 
- Setup OSB, binding to 2 existing atp instances, and verify with test app for both...
- Refer to https://github.com/oracle/oci-service-broker specifically...
    - https://github.com/oracle/oci-service-broker/blob/master/charts/oci-service-broker/docs/installation.md
    - https://github.com/oracle/oci-service-broker/blob/master/charts/oci-service-broker/docs/atp.md
- run ./installOSB.sh
- If not already created, create user API Key with password https://docs.cloud.oracle.com/en-us/iaas/Content/Functions/Tasks/functionssetupapikey.htm
- modify and run ./ocicredentialsSecret
- run ./installOCIOSB.sh
- run `svcat get brokers` # recheck until broker is shown in ready state
- run `svcat get classes` and `svcat get plans` 
- Do the following for each/both ATP instances...
    - modify oci-service-broker/samples/atp/atp-existing-instance.yaml # provide class and plan name and pdb ocid and compartmentID
    - run `kubectl create -f charts/oci-service-broker/samples/atp/atp-existing-instance.yaml`
    - run `svcat get instances` #  verify in ready state
    - modify oci-service-broker/samples/atp/atp-binding-plain.yaml # provide wallet pw (either new or existing)
    - run `kubectl create -f charts/oci-service-broker/samples/atp/atp-binding-plain.yaml`
    - run `svcat get bindings` # verify in ready state
    - run `kubectl get secrets atp-demo-binding -o yaml` 
    - modify oci-service-broker/samples/atp/atp-demo-secret.yaml # provide admin password and wallet password (use `echo -n value | base64` to encode)
    - run `kubectl create -f oci-service-broker/samples/atp/atp-demo-secret.yaml`
 - Next steps, adding reference to binding and secrets in kubernetes deployment yaml (see helidon-atp)
 - Show ref in micro-profile
    

3rd hr (setup AQ, order and inventory, saga, CQRS, and streaming)...
- setup AQ, queue-progation
- https://docs.oracle.com/en/cloud/paas/atp-cloud/atpug/database-links.html#GUID-84FB6B85-D60D-4EDC-BB3C-6485B2E5DF4D
- deploy order and inventory and all other services, demonstrate working app for single orders

4th hr (health, monitoring, horizontal scaling)...
- demonstrate health/readiness 
    - eg order service is not ready until some data load (from view or eventsourcing or lazily) is done
    - show src and probes in deployment
    - https://kubernetes.io/docs/tasks/configure-pod-container/configure-liveness-readiness-startup-probes/
    - http://heidloff.net/article/implementing-health-checks-microprofile-istio
    - https://github.com/oracle/helidon/blob/master/docs/src/main/docs/health/02_health_in_k8s.adoc
    - https://github.com/oracle/helidon/blob/master/docs/src/main/docs/guides/07_health_se_guide.adoc
    - https://dmitrykornilov.net/2019/08/08/helidon-brings-microprofile-2-2-support/
- demonstrate metrics (maybe monitoring and alert, grafana prometheus)
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
- demonstrate horizontal scaling
    - install metrics-server
        DOWNLOAD_URL=$(curl -Ls "https://api.github.com/repos/kubernetes-sigs/metrics-server/releases/latest" | jq -r .tarball_url)
        DOWNLOAD_VERSION=$(grep -o '[^/v]*$' <<< $DOWNLOAD_URL)
        curl -Ls $DOWNLOAD_URL -o metrics-server-$DOWNLOAD_VERSION.tar.gz
        mkdir metrics-server-$DOWNLOAD_VERSION
        tar -xzf metrics-server-$DOWNLOAD_VERSION.tar.gz --directory metrics-server-$DOWNLOAD_VERSION --strip-components 1
        kubectl apply -f metrics-server-$DOWNLOAD_VERSION/deploy/1.8+/
    - kubectl get pods -n datademo |grep order-helidon
    - kubectl top pod order-helidon-74f848d85c-gxfq7 -n datademo --containers 
    - kubectl autoscale deployment order-helidon --cpu-percent=50 --min=1 --max=2 -n datademo
    - kubectl get hpa -n datademo
            NAME            REFERENCE                  TARGETS         MINPODS   MAXPODS   REPLICAS   AGE
            order-helidon   Deployment/order-helidon   <unknown>/50%   1         2         0          16s
    - increase cpu, notice cpu increase and scale to 2 pods

5th hr (tracing)
- install istio, demonstrate tracing (jaeger and kiali)
- @Traced annotation

6th hr...
- demonstrate scaling with pdb (comes from functional aspect, no sharding orders based on phone number, more memory ?)_
- https://medium.com/oracledevs/how-to-keep-your-microservices-available-by-monitoring-its-metrics-d88900298025
- nice to have: 
    - messaging when available, JPA, JTA
    - fn
    - cloud developer service
    - apex report/callout to helidon
    - ORDS
    - Grafana of OCI https://blogs.oracle.com/cloudnative/data-source-grafana



Future services to be added...

Data Flow 
    - fully managed Spark service that lets you run Spark applications with almost no administrative overhead.
    - for demo: fog computing of IoT
    
Data Science 
    - enables data scientists to easily build, train, and manage machine learning models on Oracle Cloud, using Python and open source machine learning libraries
    - for demo: predictive analytics of orders to inventory/delivery locations

Data Catalog 
    - enables data consumers to easily find, understand, govern, and track Oracle Cloud data assets across the enterprise using an organized inventory of data assets
    - what data is available where in the organization and how trustworthy and fit-for-use they are
    - for demo: analytics report of order info from streaming + atp 

