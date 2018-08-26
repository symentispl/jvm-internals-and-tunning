#include <time.h>
#include <stdio.h>

#define CHUNK_SIZE 1

int fibonacci(int n) {
    if (n <= 1) return n;
    else return fibonacci(n-1) + fibonacci(n-2);
}

int main(){

  clock_t start = clock(), diff;
  for (int i = 0; i < 250; ++i) {
    for (int j = 0; j < CHUNK_SIZE; ++j) {
      fibonacci(24);
    }
  }
  diff = clock() - start;

  int msec = diff * 1000 / CLOCKS_PER_SEC;
  printf("Time taken %d seconds %d milliseconds\n", msec/1000, msec%1000);

  return 0;
}
