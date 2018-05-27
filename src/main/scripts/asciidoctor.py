import sh
from os.path import join

SLIDES_SRC = "src/main/slides"
RESOURCES_SRC = "src/main/resources"
SLIDES_TARGET = "target/slides"

REVEAL_JS_VERSION = "3.3.0"
REVEAL_JS_URL = "https://github.com/hakimel/reveal.js/archive/%s.zip" % REVEAL_JS_VERSION
REVEAL_JS_TARGET = "reveal.js-%s" % REVEAL_JS_VERSION

class StdOutLogger:
    def debug(self, line):
        print(line)

def render_slides(logger, source, destination_dir):

    def process_output(logger):
        def _log_output(line):
            logger.debug(line)
        return _log_output

    def _render_slides():
        sh.asciidoctor_revealjs(source, \
            attribute="revealjsdir=/%s" % REVEAL_JS_TARGET, \
            require="asciidoctor-diagram", \
            destination_dir=destination_dir, \
            _out=process_output(logger), \
            _err=process_output(logger))
    return _render_slides

def copy_resources(source_dir, destination_dir):
    def _cpy():
        sh.cp("-R", \
              "--remove-destination", \
              source_dir, \
              destination_dir)
    return _cpy

source = join(SLIDES_SRC, "index.adoc")
destination_dir = join(SLIDES_TARGET)
render_slides(StdOutLogger(), source, destination_dir)()
copy_resources(join(RESOURCES_SRC,"css"),join(SLIDES_TARGET))()
copy_resources(join(RESOURCES_SRC,"images"),join(SLIDES_TARGET))()
for module_name in ["java-io-wars", \
                    "jit-me-baby", \
                    "mutants-xenomorphs-and-bytecode", \
                    "voyeurs-in-jvm-land", \
                    "jmm-and-concurrency","gc-in-openjdk"]:
    source = join("modules", module_name, SLIDES_SRC, "index.adoc")
    destination_dir = join(SLIDES_TARGET, module_name)
    render_slides(StdOutLogger(), source, destination_dir)()
    copy_resources(join("modules", module_name, RESOURCES_SRC,"css"),join(SLIDES_TARGET, module_name))()
    copy_resources(join("modules", module_name, RESOURCES_SRC,"images"),join(SLIDES_TARGET, module_name))()
