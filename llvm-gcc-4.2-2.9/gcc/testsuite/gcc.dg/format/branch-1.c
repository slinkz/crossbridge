/* Test for format checking of conditional expressions.  */
/* Origin: Joseph Myers <jsm28@cam.ac.uk> */
/* { dg-do compile } */
/* { dg-options "-std=gnu99 -Wformat" } */

#include "format.h"

void
foo (long l, int nfoo)
{
  printf ((nfoo > 1) ? "%d foos" : "%d foo", nfoo);
  printf ((l > 1) ? "%d foos" : "%d foo", l); /* { dg-warning "int" "wrong type in conditional expr" } */
  printf ((l > 1) ? "%ld foos" : "%d foo", l); /* { dg-warning "int" "wrong type in conditional expr" } */
  printf ((l > 1) ? "%d foos" : "%ld foo", l); /* { dg-warning "int" "wrong type in conditional expr" } */
  /* Should allow one case to have extra arguments.  */
  printf ((nfoo > 1) ? "%d foos" : "1 foo", nfoo);
  printf ((nfoo > 1) ? "many foos" : "1 foo", nfoo); /* { dg-warning "too many" "too many args in all branches" } */
  printf ((nfoo > 1) ? "%d foos" : "", nfoo);
  printf ((nfoo > 1) ? "%d foos" : ((nfoo > 0) ? "1 foo" : "no foos"), nfoo);
  printf ((nfoo > 1) ? "%d foos" : ((nfoo > 0) ? "%d foo" : "%d foos"), nfoo);
  printf ((nfoo > 1) ? "%d foos" : ((nfoo > 0) ? "%d foo" : "%ld foos"), nfoo); /* { dg-warning "long int" "wrong type" } */
  printf ((nfoo > 1) ? "%ld foos" : ((nfoo > 0) ? "%d foo" : "%d foos"), nfoo); /* { dg-warning "long int" "wrong type" } */
  printf ((nfoo > 1) ? "%d foos" : ((nfoo > 0) ? "%ld foo" : "%d foos"), nfoo); /* { dg-warning "long int" "wrong type" } */
  /* Extra arguments to NULL should be complained about.  */
  /* APPLE LOCAL 6821124 string arg is not non-null */
  /* removed lines */
}
