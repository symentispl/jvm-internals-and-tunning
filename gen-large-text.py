from loremipsum import Generator

dictionary = []

with open('/usr/share/dict/words', 'r') as dictionary_txt:
    dictionary = [x.strip() for x in dictionary_txt.readlines()]

gen = Generator(None, dictionary)
while(True):
    print(gen.generate_paragraph()[2])