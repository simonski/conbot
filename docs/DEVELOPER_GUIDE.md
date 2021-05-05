# DEVELOPER GUIDE

This document is for people who want to extend `conbot`.  

## Dependencies

- git
- Make
- maven3
- jdk 11
- docker

## Get the source

    cd $CODE
    git clone https://github.com/simonski/conbot.git 
    cd conbot
    mvn clean install
    make build
    export PATH=$PATH:.

This will create `conbot` executable and place it on your `$PATH` as well as install into your `~/.m2/repository`.

## Extending

You can eiher fork and build directly, or extend your applciations own `pom.xml` file to depend on `com.simonski.conbot/conbot:1.0`.