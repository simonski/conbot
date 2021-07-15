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
    git clone https://github.com/simonski/conbotj.git 
    cd conbotj
    make build
    export PATH=$PATH:.

This will create a `conbot` executable and place it on your `$PATH` as well as install into your `~/.m2/repository`.  

> **Note** the executable is really just a jarfile with a shellscript at the start.

## Extending

You can eiher fork and build directly, or extend your applications own `pom.xml` file to depend on `conbot/conbot:1.0`.
