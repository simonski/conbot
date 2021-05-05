# Kubernetes HOW-TO

## Create a K8S Cluster 

(10 mins)

```bash
export PROJECT_ID=your-gcp-project
export CLUSTER_NAME=your-gke-cluster
alias k=kubectl
````

The cluster has 1 control plane and 1 nodepool.

Container-Optimised OS with containerd

default-pool:
	Container-Optimised OS with containerd
1 x e2-medium


workerpool: 
	size: 0 nodes
	enable autoscaling 0 .. 2
	nodes: Container-Optimised OS with containerd
	c2-standard-8 autoscaling 0...1

kubernetes labels
	poolname: workerpool
	

### Start the cluster

	gcloud beta container --project "$PROJECT_ID" clusters create "$CLUSTER_NAME" --zone "us-central1-c" --no-enable-basic-auth --cluster-version "1.18.16-gke.2100" --release-channel "regular" --machine-type "e2-medium" --image-type "COS_CONTAINERD" --disk-type "pd-standard" --disk-size "100" --metadata disable-legacy-endpoints=true --scopes "https://www.googleapis.com/auth/devstorage.read_only","https://www.googleapis.com/auth/logging.write","https://www.googleapis.com/auth/monitoring","https://www.googleapis.com/auth/servicecontrol","https://www.googleapis.com/auth/service.management.readonly","https://www.googleapis.com/auth/trace.append" --num-nodes "1" --enable-stackdriver-kubernetes --enable-ip-alias --network "projects/$PROJECT_ID/global/networks/default" --subnetwork "projects/$PROJECT_ID/regions/us-central1/subnetworks/default" --default-max-pods-per-node "110" --no-enable-master-authorized-networks --addons HorizontalPodAutoscaling,HttpLoadBalancing,GcePersistentDiskCsiDriver --enable-autoupgrade --enable-autorepair --max-surge-upgrade 1 --max-unavailable-upgrade 0 --enable-shielded-nodes --node-locations "us-central1-c" && gcloud beta container --project "$PROJECT_ID" node-pools create "workerpool" --cluster "$CLUSTER_NAME" --zone "us-central1-c" --machine-type "c2-standard-8" --image-type "COS_CONTAINERD" --disk-type "pd-ssd" --disk-size "99" --node-labels poolname=workerpool --metadata disable-legacy-endpoints=true --scopes "https://www.googleapis.com/auth/devstorage.read_only","https://www.googleapis.com/auth/logging.write","https://www.googleapis.com/auth/monitoring","https://www.googleapis.com/auth/servicecontrol","https://www.googleapis.com/auth/service.management.readonly","https://www.googleapis.com/auth/trace.append" --enable-autoscaling --min-nodes "0" --max-nodes "2" --enable-autoupgrade --enable-autorepair --max-surge-upgrade 1 --max-unavailable-upgrade 0 --node-locations "us-central1-c"

### Connect kubectl to the GKE cluster

```bash
gcloud container clusters get-credentials $CLUSTER_NAME --zone us-central1-c --project $PROJECT_ID
```

### Run on a GKE Cluster

	k run -i demopod --image=conbot/conbot:latest --restart=OnFailure -- schema -cols 10
	k describe pods demopod
	k delete pods demopod

### Run as a job

We want to run `./conbot convert -source gs://conbot-data/source/10k.csv.gz -target gs://conbot-data/output -converter csv` but on k8s

run the job on the default-pool

	k apply -f demojob1-default-pool.yaml 
	k describe jobs demojob1-default-pool
	k delete jobs demojob1-default-pool

run the same job on the workerpool (should autoscale)

	k apply -f demojob1-workerpool.yaml 
	k describe jobs demojob
	k delete jobs demojob


