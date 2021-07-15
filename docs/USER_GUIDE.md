# USER GUIDE

This is the user guide and introduction to `conbot`.  This assumes the `conbot` command is available on your `$PATH`

## Introducting conbot

We will learn how to use conbot though a series of examples.  

## Create some data to simulate conversions

Here we will create some sample data to simulate conversion.

First, create a schema file that is used to generate data

```bash
mkdir -p demo
./conbot schema -cols 80 > demo/schema.csv
```

This has created a file, `demo/schema.csv` that describes an 80-column dataset.

Now we can create some data using this schema. Let's  create 1m rows gzipped from this schema

```bash
./conbot data -schema demo/schema.csv -rows 1000000 | gzip - > demo/1m.csv.gz
```

Now create 10m row file from this.
```bash
for i in `seq 1 10`; do cat demo/1m.csv.gz >> demo/10m.csv.gz; done
```

This file, `demo/10m.csv.gz` is our reference file. It should be aroudn 1.2GB.

## 2. Convert the file

This demo file represents a legacy file compression format (gzip), that is nonsplittable.  Let's perform a conversion using 1, 5 and all threads

```bash
./conbot convert -source demo/10m.csv.gz -target demo/output -consumer csv -threads 1
```
(191,000/sec).


```bash
./conbot convert -source demo/10m.csv.gz -target demo/output -consumer csv -threads 5
```
(450,000/sec).

```bash
./conbot convert -source demo/10m.csv.gz -target demo/output -consumer csv 
```
(383,000/sec).

This will print out progress to STDOUT per second until it completes, finishing with a summary.  The number of rows/second processed is the key metric here.  

Inspect the `demo/output` folder to see `output-n.csv` file(s) and a `report.json` file.

## Summary

We can see splitting the file into a 1 * Producer->N * Consumer arrangement increases throughput, achieving **vertical** scaling.   You should see on your own computer the performance will vary; too many threads can slow the process down.  

## Docker

Let's run the same locally, using docker. Build the image and alias it so we can demonstrate usage locally:

	make docker
    alias dconbot="docker run --rm --label conbot -v $PWD/data:/data conbot:latest $* "

Verify it works

	dconbot

Now run it again using docker

```bash
dconbot convert -source /data/source/data.csv.gz -target /data/output -consumer csv -threads 5
```
(30,000/sec).

But wait! `dconbot` is so slow!.  that's okay - we just wanted to prove our execute call worked properly.

## Read from GCS, process, write to GCS

There is a small file `gs://conbot-data/source/10k.csv.gz` we can use:

```bash
./conbot convert -source gs://conbot-data/source/10k.csv.gz -target gs://conbot-data/output -converter csv
```

This will download, transform, upload.  

## Now run in K8S

```bash
make docker
make push
```

Read the [KUBERNETES.md](KUBERNETES.md) docs next.