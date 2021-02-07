#include <stdio.h>
#include <strings.h>

/*
 Chain Lists and Friends
 */
#define NULL 0
#define TRUE 1
#define FALSE 0

typedef struct SMALL {
  struct SMALL * Next;
} StrPt;

void chainAppend (StrPt * Head, StrPt * Pt) {
//  StrPt * Head;
//  StrPt * Pt;
//{
  StrPt * StructPt = Head;

  while (StructPt->Next) {
    StructPt = StructPt->Next;
  }

  StructPt->Next = Pt;
}

void freeChain (StrPt * Head) {
//  StrPt * Head;
//{
  StrPt * NextOne;
  StrPt * ThisOne;

  ThisOne = Head;
  while (ThisOne) {
    NextOne = ThisOne->Next;
    free((char *)ThisOne);
    ThisOne = NextOne;
  }
}
#define walkList(a,b) for (b=a;b;b=b->Next)

void chainRemove (StrPt * Head, StrPt * Pt) {
//  StrPt * Head;
//  StrPt * Pt;
//{
  StrPt * LocPt;

  walkList(Head, LocPt) {
    if (LocPt->Next == Pt) {
      LocPt->Next = Pt->Next;
      break;
    }
  }
}

StrPt * dropHead (StrPt * Head) {
//  StrPt * Head;
//{
	if (Head != NULL) {
		if (Head->Next != NULL) {
			StrPt * newHead = Head->Next;
			free((char*)Head);
			return newHead;
		}
	}
	return NULL;
}

int listLength(StrPt * Head) {
//  StrPt * Head;
//{
	int len = 0;
	StrPt * LocPt;

  walkList(Head, LocPt) {
    len += 1;
  }

	return len;
}

int main (int argc, char ** argv) {
  typedef struct LOC {
    struct LOC * Next; // Match the SMALL one (StrPt)
    int a;
    char str[20];
  } LocStruct;

  LocStruct * StA;
  LocStruct * StB;
  LocStruct * StC;
  LocStruct * StD;

  LocStruct * MyStr;

  StA = (LocStruct *) calloc(1, sizeof(LocStruct));
  StB = (LocStruct *) calloc(1, sizeof(LocStruct));
  StC = (LocStruct *) calloc(1, sizeof(LocStruct));
  StD = (LocStruct *) calloc(1, sizeof(LocStruct));

  StA->Next = NULL;
  StB->Next = NULL;
  StC->Next = NULL;
  StD->Next = NULL;

  StA->a = 1;
  StB->a = 2;
  StC->a = 3;
  StD->a = 4;

  strcpy(StA->str, "First");
  strcpy(StB->str, "Second");
  strcpy(StC->str, "Third");
  strcpy(StD->str, "Fourth");

  chainAppend((StrPt *)StA, (StrPt *)StB);
  chainAppend((StrPt *)StA, (StrPt *)StC);
  chainAppend((StrPt *)StA, (StrPt *)StD);

  walkList(StA, MyStr) {
    fprintf(stdout, "a = %d\t", MyStr->a);
    fprintf(stdout, "str = %s\n", MyStr->str);
  }

  chainRemove((StrPt *)StA, (StrPt *)StC);
  free((char*)StC);
  fprintf (stdout, "After remove\n");
  walkList(StA, MyStr) {
    fprintf(stdout, "a = %d\t", MyStr->a);
    fprintf(stdout, "str = %s\n", MyStr->str);
  }

	StA = dropHead((StrPt *)StA);
  fprintf (stdout, "After dropHead\n");
  walkList(StA, MyStr) {
    fprintf(stdout, "a = %d\t", MyStr->a);
    fprintf(stdout, "str = %s\n", MyStr->str);
  }
	fprintf(stdout, "List length is now %d\n", listLength(StA));

  freeChain (StA);
  fprintf (stdout, "Space is now free !\n");

  return 0;
}
