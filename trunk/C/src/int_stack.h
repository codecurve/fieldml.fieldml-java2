#ifndef H_INT_STACK
#define H_INT_STACK

typedef struct _IntStack IntStack;

IntStack *createIntStack();

void pushInt( IntStack *stack, int value );

int popInt( IntStack *stack );

int peekInt( IntStack *stack );

void destroyIntStack( IntStack *stack );

#endif //H_INT_STACK