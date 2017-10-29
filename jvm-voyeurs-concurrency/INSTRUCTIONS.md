## how to run it?

	./example2 &> /dev/null &

## how to diagnose it?

first check `top`, and see that this application consumes CPU (significant amount of io time, 
user time and sys time should be under control).

High I/O time, suggests crazy disk activity, let's check it.

	pidstat -d 1
	
we will see that one of the applications writes tons of bytes. Let's check where this application writes it's files.

	lsof -E -p [pid]
	
you will see that it writes files in `/tmp` directory.

## so what the second process does?

Let's take a look at io activity

	 sysdig proc.pid=[pid]
	 
it will take time, but you will notice lots of `sendto` syscalls. But where does it connect?

	sysdig -c topconns
	 
So one process is sending bytes to another one over TCP.

## the twist

Kill process `pl.symentis.jvm.example3.Example3c` and run it like this:

	./example3c -c 16 -s 1024 &> /dev/null &
	
What has changed? What can be the reason? (sys time should sky rocket)