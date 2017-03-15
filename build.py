from pybuilder.core import init, use_plugin, task, use_bldsup

use_plugin("python.install_dependencies")
use_bldsup(build_support_dir="src/main/scripts")

default_task = ["slideon"]

@init
def initialize(project):
    project.depends_on("sh")
    project.depends_on("livereload")
    project.depends_on("requests")
    project.depends_on("clint")

@task
def slideon(logger):
    # check if asciidoctor and reveal.js are setup
    try:
        import livepreview
        livepreview.serve()
    except:
        logger.error("e")

@task
def assembly():
    pass
