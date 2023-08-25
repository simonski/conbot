# conbot

Welcome to `conbot`!.  Conbot is a utility intended to deal with the corners of data engineering. The more mundane, the better conbot is at it!.

Conbot is built to perform the "heavy lifting" of common tasks a data engineer may conduct.

Conbot tries to be simple, small and as fast as possible.  It can be used as terminal tool on a daily basis through to running horizontally in containers to make the easy simple and the hard possible.

For example:

- file format conversion (for example gzip.csv -> .parquet, .avro)
- schema detection ("give me a parquet schema from a csv")
- data generation (synthetic data for testing)
- transfer and transform (automates the sourcing and adapting of data)
- terminal visualisation of binary formats for simple querying of files 

Conbot doesn't ask why you need to perform some leftfield task, it just tries to provide the capability to achive your task, no questions asked.  It was built to try to accept and formalise some of the repeating glue work performed over the years.

## Install

Installation instructions are in the [INSTALLATION.md](docs/INSTALLATION.md)

## Usage

Refer to the [USER_GUIDE.md](docs/USER_GUIDE.md) for examples on the problems Conbot will solve for you.

## Extending Conbot

Conbot is open-source! - you are very welcome to extend it or participate as a commiter.  Read the [DEVELOPER_GUIDE.md](docs/DEVELOPER_GUIDE.md) for more information.

## Go

Conbot is also available in go, if that is your thing.  In that case, please refer to [https://github.com/simonski/conbot](https://github.com/simonski/conbot).

