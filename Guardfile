# encoding: utf-8

BUILD_CMD = [
  "mkdir -p build/slides",
  "bundle exec asciidoctor" \
    " -r asciidoctor-revealjs" \
    " -r asciidoctor-diagram" \
    " -b revealjs" \
    " -a revealjsdir=https://cdn.jsdelivr.net/npm/reveal.js@5.1.0" \
    " -a puppeteer-config=#{Dir.pwd}/slides/puppeteer-config.json" \
    " -D build/slides" \
    " slides/src/main/slides/index.adoc",
  "cp -r slides/src/main/resources/. build/slides/"
].join(" && ").freeze

guard :shell do
  watch(%r{slides/src/main/slides/.+\.adoc$}) do |m|
    puts "==> Rebuilding: #{m[0]}"
    system(BUILD_CMD)
    puts "==> Done"
  end

  watch(%r{slides/src/main/resources/.+}) do |m|
    puts "==> Copying resource: #{m[0]}"
    system("cp -r slides/src/main/resources/. build/slides/")
    puts "==> Done"
  end
end

guard :livereload, host: "localhost", port: 35729, apply_css_live: true do
  watch(%r{build/slides/.+\.(html|css|js)})
end
