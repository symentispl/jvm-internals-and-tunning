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
        with tmp as f:
            total_length = int(r.headers.get('content-length'))
            for chunk in progress.bar(r.iter_content(chunk_size=1024), expected_size=(total_length/1024) + 1):
                if chunk:
                    f.write(chunk)
                    f.flush()
            logger.info("unzipping reveal.js")
            zipf = zipfile.ZipFile(tmp)
            zipf.extractall(path="target/slides/")

def install_asciidoctor(logger):
    logger.info("checking for asciidoctor-revealjs")
    lines = sh.gem("list", "--local").stdout.splitlines()
    packages = [line.partition(" ")[0] for line in lines]
    if not "asciidoctor-revealjs" in packages:
        logger.error("asciidoctor-revealjs not installed")
        sys.exit(1)

def init_slides():
    sh.mkdir("-p", SLIDES_TARGET)

def render_slides():
    sh.asciidoctor_revealjs("src/main/slides/index.adoc", \
        attribute="revealjsdir=%s" % REVEAL_JS_TARGET, \
        require="asciidoctor-diagram", \
        destination_dir=SLIDES_TARGET, \
        _out=process_output, \
        _err=process_output)

def copy_resources(resource_dir):
    def _cpy():
        print("kopiuje ")
        sh.cp("-R", \
              "--remove-destination", \
              join(RESOURCES_SRC, resource_dir), \
              SLIDES_TARGET)
    return _cpy

def process_output(line):
    print(line)

def serve(logger):
    # install_revealjs(logger)
    # install_asciidoctor(logger)
    init_slides()
    copy_resources("css")()
    copy_resources("images")()
    server = Server()
    server.watch('src/main/slides/*.adoc', render_slides)
    server.watch('src/main/resources/css/*', copy_resources("css"))
    server.watch('src/main/resources/images/*', copy_resources("images"))
    server.serve(root='target/slides', open_url_delay=1)
