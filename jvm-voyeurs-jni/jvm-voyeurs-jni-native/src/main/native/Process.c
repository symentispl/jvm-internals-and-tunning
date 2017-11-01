#include <sys/types.h>
#include <unistd.h>
#include <signal.h>
#include <stdio.h>
#include <stdlib.h>
#include "pl_symentis_jvm_example0_Process.h"

JNIEXPORT jint JNICALL Java_pl_symentis_jvm_example0_Process_getPid
  (JNIEnv *env, jobject obj){

  int daughter_status;
  pid_t pid = fork();
  
  if(pid!=0){
	// bring your daughter to the slaughter
	sleep(5);
  	int killed = kill(pid,9);
	if(killed==0){
		printf("process %d was killed\n", pid);
		waitpid(pid,&daughter_status,0);
		printf("process %d was terminated with status %d\n", pid, daughter_status);
	} else{
		perror("cannot kill your daughter, she becomes zombie\n");
	}
  } else {
	// your dauther is going slightly mad and becomes random numbers generator
	while(1){
		long int r = random();
		printf("next random number I am thinking of is %ld\n",r);
		sleep(1);	
	}
  }
  return pid;
}


