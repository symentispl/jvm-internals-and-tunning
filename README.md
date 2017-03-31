# because live is to short to use power point

in few easy steps:

  hg clone https://bitbucket.org/kcrimson/slideon [presentation_name]
  mkvirtualenv --no-site-packages -p  /usr/bin/python3.5 [presentation_name]
  workon [presentation_name]
  pip install pybuilder
  pyb install_dependencies
  pyb slideon

Happy slides !!!
