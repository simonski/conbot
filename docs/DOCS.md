## Use Cases

In this case we will simulate some legacy data and attempt a rapid conversion.

## Performance Metrics

Create a dataset of 10m records. This will act as our reference file.

	time ./conbot data -seed 1 -cols 80 -rows 10000000 -optional 0.95 -sparsity 0.99  | gzip --stdout > 10m.csv.gz

This will create a 10m row gzip file of random data where 95% of the columns are optional and 99% of the optional columns will be sparse (empty).  The `-seed` guarantees the data will be the same every time you generate, so you don't have to transfer large files.  If you want different data, specify a different seed.

## Use the reference file to run the metrics

Now we can perform conversion logic on the data

	./conbot convert -f 10m.csv.gz -consumer csv -threads 2 -buffer 256

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
- in `conbot.Converter:buildConsumers` add your extension type
- rebuild `make build`


## Extending Conbot
Programmer and operator friendly, `conbot` comes "batteries included" with out of the box converters
however is built for modding through `Schema` and `Consumer` classes.


