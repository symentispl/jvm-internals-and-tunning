#!/usr/bin/env python
from livereload import Server, shell
import sh



sh.mkdir("-p","target/slides")
sh.cp("-R","--remove-destination","reveal.js","target/slides")

server = Server()
server.watch('src/main/asciidoc/*.adoc', shell('asciidoctor -T asciidoctor-reveal.js/templates/slim/ -r asciidoctor-diagram src/main/asciidoc/*.adoc -D target/slides/'))
server.serve(root='target/slides')
