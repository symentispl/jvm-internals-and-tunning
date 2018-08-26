# Polyglot persistence

## Setting up for slides authoring

In order to fully enjoy slides authoring you will have to install few small bits
of software, here and there.

First of all make sure that you have all necessary asciidoctor packages:

	sudo gem install asciidoctor asciidoctor-diagram pygments.rb slim

and next make sure that your have all necessary Python packages so you can enjoy
live preview:

	sudo pip install livereload sh

Once this is installed on your machine, just lunch live preview script:

	./livepreview.py

and point your browser at <http://localhost:5500/slides.html> and open `slides/slides.adoc` in editor of your choice,
every time your save the file, it will be automatically processed by asciidoctor and slides will be reloaded in browser,saves a couple of keystrokes. Enjoy!

## Setting up dev environment

This is easy as this, under one condition, you have `docker` and `docker-compose` installed,
if this true, all you need is:

	./mvnw package && docker-compose build && docker-compose up