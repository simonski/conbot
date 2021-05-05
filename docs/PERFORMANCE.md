# Performance


c2-standard-8
ssh in
gsutil cp gs://conbot-data/setup.sh .
chmod +x setup.sh
./setup.sh


2 million rows/second read with Record(int, row) being created.

./conbot convert -f data/100m.csv.gz -producer conbot.converter.ProducerTestSpeed -consumer csv
	
2.1 millions rows/second read. 
java_args="-Xss3M" ./conbot convert -f data/100m.csv.gz -producer conbot.converter.ProducerTestSpeed -consumer csv

# Actual perf stats on 100m - 743k read+write/second

java_args="-Xss3M" ./conbot convert -f data/100m.csv.gz -consumer csv -readBuffer 8 -threads 7

# When running with the ConsumerCSV but not writing to disk, 1,000,000 read/+1,000,000 write (logic but not writing)


The following benchmarks are run using a reproducable
randomly generated dataset.  This allows you to run the 
same tests on large datasets locally for both comparison
and to be able to accurately estimate your own datasets.

In these tests we will time 

- create reference data
- format conversion 

## Performance Metrics

Let's create our reference file for conversion.

	time ./conbot data -seed 1 -cols 80 -rows 10000000 -optional 0.95 -sparsity 0.99  | gzip --stdout > 10m.csv.gz

This will create a 10m row gzip file of random data where 95% of the columns are optional and 99% of the optional columns will be sparse (empty).  The `-seed` guarantees the data will be the same every time you generate, so you don't have to transfer large files.  If you want different data, specify a different seed.

## Use the reference file to run the metrics

Now we can perform conversion logic on the data

	./conbot convert -f 10m.csv.gz -consumer csv -threads 2 -buffer 256

## GCP Performance Statistics

    gsutil cp gs://conbot-data/10m.csv.gz .
    gsutil cp gs://conbot-data/conbot .
    chmod +x conbot

    sudo apt install openjdk-11-jre-headless -y
    ./conbot convert -f 10m.csv.gz -consumer csv -threads 2..15

|e2-highcpu-16|713k/s|
|c2-standard-8|832k/s|


`openjdk version "13.0.2" 2020-01-14`

### Macbook Pro 13, 2.3 i7, 16GB Ram

`openjdk version "13.0.2" 2020-01-14`

consumer|threads|buffer|rows/sec
--------|--------|------|-------
csv|2|256|354,000
csv|2|512|370,000
csv|4|256|624,000
csv|4|512|600,000
csv|6|256|738,000
csv|6|512|743,000
csv|7|256|851,000
csv|7|512|835,000

### Apple M1 MacMini

`openjdk version 1.8.0_282`

consumer|threads|buffer|rows/sec
--------|--------|------|-------
csv|2|256|306,000
csv|2|512|360,000
csv|4|256|511,000
csv|4|512|566,000
csv|6|256|836,000
csv|6|512|609,000
csv|7|256|1,075,000
csv|7|512|965,000


### Windows 32GB Ram i9700

Using `java -jar`

consumer|threads|buffer|rows/sec
--------|--------|------|-------
csv|2|256|401,000
csv|2|512|396,000
csv|4|256|986,000, 715,000, 735,000
csv|4|512|408,000
csv|6|256|1,385,000
csv|6|512|1,018,000
csv|7|256|1,166,000
csv|7|512|1,316,000

## Run

> Note: if you `make build` you will get an executable "`conbot`" you can call directly.  Otherwise you'll need to `java -jar target/conbot.jar` to run it.

## Convert a file

	./conbot convert -f 10m.csv.gz -consumer csv -rowcount 10000000 -report 1000 -threads 2 -buffer 4

Will convert the source gzip file `10m.csv.gz` using the `CSV` implementation on 2 threads, printing a report every 1s, using a 4k buffer.

## Create your own Consumer

- extend `conbot.Consumer`
- in `conbt.Converter:buildConsumers` add your extension type
- rebuild `make build`








