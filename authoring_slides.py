#!/usr/bin/env python
from livereload import Server
from os.path import join
import sh

SLIDES_SRC = "src/main/asciidoc"
SLIDES_TARGET = "target/slides"

def init_slides():
    sh.mkdir("-p", SLIDES_TARGET)
    sh.cp("-R", "--remove-destination", "reveal.js", SLIDES_TARGET)

def render_slides():
    sh.asciidoctor("src/main/asciidoc/*.adoc",
        template_dir="asciidoctor-reveal.js/templates/slim/",
        require="asciidoctor-diagram",
        destination_dir=SLIDES_TARGET,
        _out=process_output,
        _err=process_output)

def process_output(line):
    print line

init_slides()
render_slides()

server = Server()
server.watch('src/main/asciidoc/*.adoc', render_slides)
server.serve(root='target/slides')
