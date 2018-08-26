#!/usr/bin/env python
from livereload import Server
from os.path import join
import sh

SLIDES_SRC = "slides"
SLIDES_TARGET = "target/slides"

def init_slides():
    sh.mkdir("-p", SLIDES_TARGET)
    sh.cp("-R", "--remove-destination", "reveal.js", SLIDES_TARGET)

def render_slides():
    sh.cp("-R", join(SLIDES_SRC, "images"), SLIDES_TARGET)
    sh.cp("-R", join(SLIDES_SRC, "css"), SLIDES_TARGET)
    sh.asciidoctor("slides/slides.adoc",
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
server.watch('slides/*.adoc', render_slides)
server.serve(root='target/slides')
