#!/usr/bin/env python
from livereload import Server
import sh
import webbrowser

SLIDES_SRC = "src/main/asciidoc"
IMAGES_DIR = "%s/%s" % (SLIDES_SRC,"images")
SLIDES_TARGET = "target/slides"

def init_slides():
    sh.mkdir("-p", SLIDES_TARGET)
    sh.cp("-R", "--remove-destination", "reveal.js", SLIDES_TARGET)

def render_slides():
    sh.cp("-R","--remove-destination",IMAGES_DIR,SLIDES_TARGET)
    sh.asciidoctor("src/main/asciidoc/*.adoc",
        template_dir="asciidoctor-reveal.js/templates/slim/",
        require="asciidoctor-diagram",
        destination_dir=SLIDES_TARGET,
        _out=process_output,
        _err=process_output)

def process_output(line):
    print line

if __name__=="__main__":
    init_slides()
    render_slides()

    server = Server()
    server.watch('src/main/asciidoc/*.adoc', render_slides)
    server.serve(root=SLIDES_TARGET)

    webbrowser.open("http://localhost:5500/")
