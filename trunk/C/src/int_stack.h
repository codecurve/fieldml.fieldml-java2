#ifndef H_INT_STACK
#define H_INT_STACK

typedef struct _IntStack IntStack;

IntStack *createIntStack();

void intStackPush( IntStack *stack, int value );

int intStackPop( IntStack *stack );

int intStackPeek( IntStack *stack );

void destroyIntStack( IntStack *stack );

int intStackGetCount( IntStack *stack );

int intStackGet( IntStack *stack, int index );

#endif //H_INT_STACK
