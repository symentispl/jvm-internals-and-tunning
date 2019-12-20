# how to diagnose

	export JAVA_OPTS="-agentpath:/home/jarek/projects/presentations/voyeurs-in-jvm-land/honest-profiler/liblagent.so=logPath=/tmp/honest.log"
	
	sysdig proc.pid=6844 and not evt.type=futex and not evt.type=switch