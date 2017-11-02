#!/usr/bin/env python
from livereload import Server
from os.path import join, isdir
import sh
import requests
import tempfile
from clint.textui import progress
import zipfile

SLIDES_SRC = "src/main/slides"
RESOURCES_SRC = "src/main/resources"
SLIDES_TARGET = "target/slides"

REVEAL_JS_VERSION = "3.3.0"
REVEAL_JS_URL = "https://github.com/hakimel/reveal.js/archive/%s.zip" % REVEAL_JS_VERSION
REVEAL_JS_TARGET = "reveal.js-%s" % REVEAL_JS_VERSION

def install_revealjs(logger):
    logger.info("checking for reveal.js")
    if not isdir("target/slides/%s" % REVEAL_JS_TARGET):
        logger.info("downloading reveal.js version %s" % REVEAL_JS_VERSION)
        r = requests.get(REVEAL_JS_URL, stream=True)
        tmp = tempfile.TemporaryFile()
        total_length = int(r.headers.get('content-length'))
        for chunk in progress.bar(r.iter_content(chunk_size=1024), expected_size=(total_length/1024) + 1):
            tmp.write(chunk)
            tmp.flush()
        logger.info("unzipping reveal.js")
        zipf = zipfile.ZipFile(tmp)
        zipf.extractall(path="target/slides/")

def install_asciidoctor(logger):
    logger.info("checking for asciidoctor-revealjs")
    lines = sh.gem("list", "--local").stdout.splitlines()
    packages = [line.decode().partition(" ")[0] for line in lines]
    if not "asciidoctor-revealjs" in packages:
        logger.error("asciidoctor-revealjs not installed, execute `gem install asciidoctor-revealjs`")
        sys.exit(1)

def render_slides(logger):
    def process_output(logger):
        def _log_output(line):
            logger.debug(line)
        return _log_output
        
    def _render_slides():
        sh.asciidoctor_revealjs("src/main/slides/index.adoc", \
            attribute="revealjsdir=%s" % REVEAL_JS_TARGET, \
            require="asciidoctor-diagram", \
            destination_dir=SLIDES_TARGET, \
            _out=process_output(logger), \
            _err=process_output(logger))
    return _render_slides

def copy_resources(resource_dir):
    def _cpy():
        sh.cp("-R", \
              "--remove-destination", \
              join(RESOURCES_SRC, resource_dir), \
              SLIDES_TARGET)
    return _cpy

def init_slides(logger):
    install_revealjs(logger)
    install_asciidoctor(logger)
    sh.mkdir("-p", SLIDES_TARGET)
    render_slides(logger)()
    copy_resources("css")()
    copy_resources("images")()

def serve(logger):
    init_slides(logger)
    server = Server()
    server.watch('src/main/slides/*.adoc', render_slides(logger))
    server.watch('src/main/resources/css/*', copy_resources("css"))
    server.watch('src/main/resources/images/*', copy_resources("images"))
    server.serve(root='target/slides', open_url_delay=1)
