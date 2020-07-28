# rester-ui

A simple tool for quickly testing HTTP(s) URLs. 

## Overview

This is a web interface for [Rester](https://github.com/rinconjc/rester), the data driven API integration testing tool.
The test cases can be defined in any format supported by Rester, and then imported into this tool for running.

## Usage

Rester-ui is a Java executable JAR file. Download the [latest release](https://github.com/rinconjc/rester-ui/releases) archive and double-click (Windows) to execute, or use the following command.

    java -jar rester-ui.jar
    
Then point your browser to http://localhost:4000

To run in a different port use the following:
    
    java -Dport=<port-number> rester-ui.jar

## Development

To get an interactive development environment run:

    lein fig:build

## License

Copyright © 2018 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
