from livereload import Server
from invoke import task
import os

TARGET_SLIDES = "target/slides"
REVEALJSDIR = "https://cdnjs.cloudflare.com/ajax/libs/reveal.js/3.7.0"
RESOURCES_SRC = "src/main/resources"
SLIDES_SRC = "src/main/slides"


@task
def init(c):
    if not c.run("asciidoctor-revealjs -V"):
        print("Asciidoctor RevealJS is not installed, fix it with: gem install asciidoctor-revealjs")
    if not os.path.isdir(f"{TARGET_SLIDES}"):
        os.makedirs(f"{TARGET_SLIDES}")


@task
def clean(c):
    c.run(f"rm -rf {TARGET_SLIDES}")


@task(init)
def copy_resources(c):
    c.run(f"cp -R --remove-destination {RESOURCES_SRC}/* {TARGET_SLIDES}")


@task(copy_resources)
def render_slides(c):
    c.run(
        f"asciidoctor-revealjs -a revealjsdir={REVEALJSDIR} -r asciidoctor-diagram -D {TARGET_SLIDES} {SLIDES_SRC}/*.adoc")


@task(render_slides)
def livereload(c):
    server = Server()
    server.watch(f"{SLIDES_SRC}/*.adoc", lambda: render_slides(c))
    server.watch(f"{RESOURCES_SRC}/css/*", lambda: copy_resources(c))
    server.watch(f"{RESOURCES_SRC}/images/*", lambda: copy_resources(c))
    server.serve(root=f"{TARGET_SLIDES}", open_url_delay=1)
