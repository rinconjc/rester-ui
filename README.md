# rester-ui

Test web services 
I use curl a lot but sometimes I keep repeating myself
Don't repeat youself
UI for (rester)[https://github.com/rinconjc/rester], is a web service API testing tool.


## Overview

Do you feel repeating yourself 

Rester-UI allows testing http(s) endpoints, and saving


## Development

To get an interactive development environment run:

    lein fig:build

This will auto compile and send all changes to the browser without the
need to reload. After the compilation process is complete, you will
get a Browser Connected REPL. An easy way to try it is:

    (js/alert "Am I connected?")

and you should see an alert in the browser window.

To clean all compiled files:

	lein clean

To create a production build run:

	lein clean
	lein fig:min


## License

Copyright © 2018 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
