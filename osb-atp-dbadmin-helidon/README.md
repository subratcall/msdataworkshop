- Resources...

    OCI OSB project... https://github.com/oracle/oci-service-broker    
    
    Demo of previously provisioned (e.g. either via OSB or OCI Console) Oracle ATP database being attached to by OSB and creation of binding/secrets for it as well as kubernetes deployment of Helidon MP and Helidon SE microservices that use the binding/secrets... https://www.youtube.com/watch?v=qW_pw6Nd5hM&feature=youtu.be
    
    Demo of Oracle ATP database provisioned with OCI Open Service Broker and creation of binding/secrets for it... https://www.youtube.com/watch?v=kNhrU7oi8sM
    
    Blog about creation of binding/secrets for a pre-existing provisioned ATP database... https://blogs.oracle.com/cloud-infrastructure/integrating-oci-service-broker-with-autonomous-transaction-processing-in-the-real-world

	
- The basic steps are as follows (steps 1 through 4 are required only if code mods are to be made and steps from 5 on are related to deployment in kubernetes environment)…

		1. set/export DEMOREGISTRY to the value of your registry (eg docker.io/paulparkinson)  
		2. login to the registry 
		3. edit registry values in pom.xml as they are currently hardcoded to docker.io/paulparkinson
    	4. run ./build.sh (to build the project and image and push the image to your registry)
    	5. refer to the helidon-mp-atp-deployment.yaml file as an example and make mods to TNS_NAME, url, secretKeyRef, mountPath, secretName, etc. as appropriate for your ATP instance 
    	6. change the image/repos location in helidon-mp-atp-deployment.yaml 
    	7. run ./deploy.sh to install the deployment and service 
    	8. you can access the app by calling http://host:port/getConnectionMetaData (eg curl http://localhost:8080/getConnectionMetaData)
    	    -  if calling from a pod within the cluster (eg kubectl run curl --image=radial/busyboxplus:curl -i --tty --rm), it will be http://helidonatp:8080/getConnectionMetaData
    	    -  TODO complete... if calling directly on node that is public ... k get nodes (ip)... k get service
    	    -  TODO complete... if calling directly on node that is not public ... k get nodes (ip)... kubectl port-forward -n datademo $(kubectl get pod -l app=helidonatp -o jsonpath='{.items[0].metadata.name}') 8080
    	    -  TODO complete... if calling via istio gateway ... gatewayurl...

