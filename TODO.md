+ fix tests under Windows (wrong time format, don't use default
+ ant maven tasks, include
-XX:-UseBiasedLocking -XX:-DoEscapeAnalysis

int Method::fast_exception_handler_bci_for

- add about new
- add about stack and local size

- explain vtable with pictures (invoke interface)
- special vs virtual, ref type vs object type
- zrobić javaagent na bytebuddy
- wywalić jitescript (martwy)

 -XX:CompileCommand=print,*NullCheckFolding.nullCheckFodling

PrintAssembly + JMH, moar code with JMH


= JIT

JIT lifecycle

PrintAssembly i CompileCommand (https://jpbempel.github.io/2016/03/16/compilecommand-jvm-option.html)

dlaczego uruchamiamy z +debug non safepoints 
dlaczego uruchamiamy z -compressed oops

Reading PrintAssembly (https://jpbempel.github.io/2015/12/30/printassembly-output-explained.html, https://jcdav.is/2015/08/30/reading-assembly-from-hotspot/)

NullCheckFolding
NullCheckElimination (https://jpbempel.github.io/2013/09/03/null-check-elimination.html)
UncommonTrap
UncommontrapCHA
LoopUnrolling - https://blogs.oracle.com/javamagazine/post/loop-unrolling
ConstantFolding
Autovectorization

Escape Analisys (https://jpbempel.github.io/2020/08/02/when-escape-analysis-fails-you.html)
LockCoarsenning
LockEllision
PointerCompare
ScalarReplacement
StackAllocation

Exceptions - https://www.baeldung.com/java-exceptions-performance
FastThrow - preallocated exceptions

Intrinsics

CodeCache ?

https://blogs.oracle.com/javamagazine/category/jm-jvm-internals
