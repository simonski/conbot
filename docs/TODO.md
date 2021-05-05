# TODO for conbot

Report (STDOUT+File)
gs -> local -> gs
gs -> local -> bq
gs -> local
gs -> local
exit codes
 -> local -> report
 -> local - report on gs
 -> local - report on gs

Docker multistage
----------------

This mostly works but products 470mb images over the openjdk8 producing 100mb images.
Write up a paragraph and then leave it.

Extending
----------------

Cite a 'foreign' jar for a Consumer.
How to extend a Consumer (e.g. modify more columns etc.)


Writing
----------------

Write results to gs:// 
	-overwrite_target yes|no

Write results to bq://table_name with -bq_schema




Misc
----------------
At convert start, pass the CLI to the App, Producer, Consumer to validate the CLI 

	Producer/Consumer.validate(CLI);

Purpose is to ensure everything there is good.



read/write from s3
------------------

?write to bigquery?
------------------

?write to relational db?
----------------------



DONE -threads default to max-1

DOING introduce -source and -target
DOING (GS copying working)	gs/local -> gs/local


databases as targets
	bigquery
	relational

./convbot convert -source protocol://path/to target protocol://path/to
e.g. https, local, gs, s3

make a happy-path docker container that already has permission to
receive SOURCE_FILE, SOURCE_PATH, TARGET_PATH, THREADS, CONSUMER, REPORT as env vars.
process the work then copy to the target paths and exit with success, fail etc.
	local: 
	gsutil cp SOURCE_PATH/SOURCE_FILE temp_dir
	conbot convert
	gsutil cp CONVERT_DIR/* TARGET_GS_PATH

- docker conbot
	google distroless images
	read from env vars 
	copy source
	adapt
	upload source to gs if necessary

user guide

conbot convert -f source_file -temp_dir foo -threads 5 -report_file report.txt -target_output gs://foo/bar 
	-action_on_success 0
	-action_on_failure 1

s3:// path
https:// path
gs:// path  source or target

source credentials (file or key/pair, project etc)
target credentials (file or key/pair, project etc)

# Or a shellscript in the container

- parallel conbot
	conbotnode1
	conbotnode2
	conbotnode3
	producer: sends to a queue for each consumer
	round-robins across nodes
		some local
		some remote, doesn't matter

- get work
	1. get data 
	source file location
	schema
	target table
	metadata
	
	2. get source file
	LOG>transfer start
	download to disk from http link
	LOG>transfer complete
	
	3. transform
	LOG> transform start
	transform to local disk
	LOG> transform complete

	4. ingest
	LOG > ingest start
	gsutil cp output_files to gs://temp_storage_for_upload
	LOG > ingest copied to gcs
	LOG > ingest to bigquery start
	bq upload from gs://
	LOG > ingest to bq complete

	5. tidy
	LOG > tidy start
	gsutil rm source data
	LOG > tidy complete
	LOG > job complete
	LOG > Total job complete
	



	




Docker

when I run `docker run conbot/conbot <Command> I get a rubbish error. Let's look at "conbot: error, I don't understand what yu want."

- high performance read stats (no consumer at all, just an empty producer)
- select the producer wiht -Producer, default to regular Producer
- mvn coverage :https://www.baeldung.com/jacoco

- 'inspect' read a file and attempt to print out salient facts like size, rows, columns, schema, variance by row etc.
- read schema or file from http/s, s3, gcs, et-cet-er-a
- move to new conbot repo, main, develop, features/xxxxx, tag releases
- docker example as-is
- report is incorrect on ETA and printing times
- rename to conbot
- docker and publish with example for simple approach
- go client to call docker? << so a thin client that woudl then ??docker run??
- unit tests
- roadmap
- performance metrics captured
- create sample data helper
- create sample schema helper
- infer schema
- convert to avro
- convert to parquet
- Write compressed (gzip, bzip2)
- Read tgz
- USER_GUIDE covering customer `Consumer` implementations


In the schema the VALUE and RANGE types
Possibly DECLARE a value type and overriding probabilities of types?
