# Asciidoctor RevealJS Slides

Presentation slides built with [Asciidoctor RevealJS](https://docs.asciidoctor.org/reveal.js-converter/latest/), managed by [mise](https://mise.jdx.dev/).

## Prerequisites

- [mise](https://mise.jdx.dev/getting-started.html) — manages Ruby, Java, and tasks
- [Graphviz](https://graphviz.org/download/) — for diagram support (`dot` must be on PATH)

Ruby and Java versions are defined in `.mise.toml` and installed automatically by mise.

## Getting Started

```bash
git clone https://github.com/yourusername/yourrepository.git
cd slides-bootstrap
mise install        # install Ruby and Java
mise run install    # install Ruby gems
```

## Tasks

| Command | Description |
|---|---|
| `mise run build` | Build slides → `build/slides/index.html` |
| `mise run serve` | Build, serve at http://localhost:4000, live reload |
| `mise run clean` | Remove `build/slides/` |
| `mise run build-code` | Build the `code/` Java subproject |
| `mise run test-code` | Test the `code/` Java subproject |
| `mise run clean-code` | Clean the `code/` Java subproject |

## Project Structure

```
slides-bootstrap/
├── .mise.toml                    # Tool versions and task definitions
├── Gemfile                       # Ruby gem dependencies
├── slides/
│   └── src/
│       ├── main/
│       │   ├── slides/
│       │   │   ├── index.adoc            # Main presentation
│       │   │   └── revealjs-plugins.js   # Reveal.js plugin config
│       │   └── resources/
│       │       ├── css/custom.css        # Custom styles
│       │       ├── images/               # Slide images
│       │       └── plugins/copycode/     # CopyCode plugin
└── code/                         # Java code examples (built via Gradle)
```

Build output goes to `build/slides/` (gitignored).

## Creating and Editing Slides

Edit `slides/src/main/slides/index.adoc`. Each `==` heading creates a new slide:

```asciidoc
= My Presentation Title
:revealjs_theme: night
:revealjs_history: true
:source-highlighter: highlightjs

== First Slide

Content for the first slide

== Second Slide

Content for the second slide

=== Sub-slide

Content for a sub-slide
```

### Themes

Set `:revealjs_theme:` in your AsciiDoc file. Available themes: `black`, `white`, `league`, `beige`, `sky`, `night`, `serif`, `simple`, `solarized`, `blood`, `moon`.

### Adding Resources

Place images, CSS, and JavaScript in `slides/src/main/resources/`. They are copied into the build output automatically.

### Diagrams

Graphviz and PlantUML diagrams are supported via [Asciidoctor Diagram](https://docs.asciidoctor.org/diagram-extension/latest/):

```asciidoc
[graphviz]
----
digraph {
  A -> B -> C
}
----

[plantuml]
----
Alice -> Bob: Hello
----
```

## Continuous Integration

GitHub Actions builds the slides on every push using `.github/workflows/slides-build.yml`. The built artifact is uploaded as `slides`.

## Additional Resources

- [Asciidoctor RevealJS Documentation](https://docs.asciidoctor.org/reveal.js-converter/latest/)
- [RevealJS Documentation](https://revealjs.com/)
- [AsciiDoc Syntax Quick Reference](https://docs.asciidoctor.org/asciidoc/latest/syntax-quick-reference/)
- [mise Documentation](https://mise.jdx.dev/)

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Additional repositories with code and samples

https://bitbucket.org/symentis/shoppy-java - demo application used for exercises during trainings, wicket+spring+JPA, works under Jetty, Tomcat and Wildfly

https://bitbucket.org/kcrimson/jvm-internals - lots about bytecode and generation, not so much about remaning stuff, maybe someday, but it is still usefull