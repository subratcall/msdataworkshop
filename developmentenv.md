Software for development environment is listed below with a download link and verification command for each, however, all of the software necessary for this workshop exists pre-installed as part of the OCI Cloud Shell.


Java 	https://www.oracle.com/technetwork/java/javase/downloads/jdk11-downloads-5066655.html	
java -version

git 	https://git-scm.com/book/en/v2/Getting-Started-Installing-Git	
git --version

Maven 	http://mirrors.sonic.net/apache/maven/maven-3/3.6.3/binaries	
mvn -v

docker 	https://docs.docker.com/install/	
docker version

kubectl 	https://kubernetes.io/docs/tasks/tools/install-kubectl/	
kubectl version

oci 	https://docs.cloud.oracle.com/iaas/Content/API/SDKDocs/cliinstall.htm	
oci -v

helm  https://helm.sh/docs/intro/install/	
helm version

openssl https://www.openssl.org/source/	
openssl version

IDE (optional and can be an online version)		eg Intellij, Eclipse, Visual Studio Code, etc.

OSB (OCI Service Broker)	https://github.com/oracle/oci-service-broker/tree/master/charts/oci-service-broker/docs	we will install this during the workshop

database admin client (optional)	https://www.oracle.com/database/technologies/appdev/sql-developer.html	eg SQLDeveloper

Istio (optional)	https://istio.io/docs/setup/install/	



Component port and protocols used/required to access the OCI hands-on environment. Again these will be accessible when using the Cloud Shell.


ATP instances	1522	tcps

OKE/Kubernetes/kubectl	6443 and 443	informational.. https://kubernetes.io/docs/reference/access-authn-authz/controlling-access/

We will use port-forward for microservice(s) to alleviate the need for LoadBalancers/external IPs/ports (unless they is desired)

We won't be focused on sshing to any nodes, etc. in this lab and so port 22 access, for example, shouldn't be necessary

