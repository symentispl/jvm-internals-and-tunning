“JIT me baby one more time”

JIT, “just in time”, “hot spot”, tajemniczy, cichy bohater JVM. Dowód na
stwierdzenie, że każda wystarczająco skomplikowana technologia ma w sobie
element magiczny.
Jeśli myślisz, że ta prezentacja będzie wypełniona praktyczna wiedzą,  lub też,  
nie daj Boże, pozwoli Ci zrozumieć jak działa JIT. Jesteś w błędzie.
Będzie to festiwal wiedzy bezużytecznej,  zbędnej i nazbyt odklejonej od naszej
codziennej pracy, podlanej sosem asemblera i ISA Intel x86_64.
Celem tej prezentacji jest chwila zadumy i zachwytu nad pięknem technologii.
Chwila refleksji jak niewiele wiemy i jak wiele skrywa się pod kolejnymi
warstwami abstrakcji, bibliotek i framework’ów.
Prezentacja opierać się będzie na kilku prostych przykładach w języku Java i
analizą zachowania JIT. Dla szybkiego osiągnięcia stanu wspólnej świadomości,
przykłady poprzedzone będą szybkim wprowadzeniem do interpretera bytecode,
kompilatorów C1 i C2, technik “inline” i “escape analisys”,
deoptymalizacji i narzędzi do inspekcji zachowania powyższych mechanizmów.
Podczas prezentacji spotkamy takie techniki wykorzystywane przez JIT jak,
class hierarchy analisys, constant propagration, loop unrolling i intrinsics.
Tą prezentacje sponsoruje -XX:+PrintCompliation, -XX:+PrintInlining,
-XX:+PrintInterpreter oraz hsdis.
