# DEVELOPER GUIDE

There are a number of ways to install conbot

## Docker

If you are going to run via docker locally, you can use

```bash
mkdir data
alias conbot="docker run --rm --label conbot -v $PWD/data:/data conbot/conbot:latest $* "
```

The command `conbot` is now available via a temporary docker container.

## Build from source

Alternatively you can build from source

    cd $CODE
    git clone https://github.com/simonski/conbot.git 
    cd conbot
    make build
    export PATH=$PATH:.

This will create `conbot` executable and place it on your `$PATH`

