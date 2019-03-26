.text
.globl __Foo#doit
__Foo#doit:
   subq   $8, %rsp
_L_334:
   movq    %rbx, %rax/*t997*/
   movq    %r12, %rax/*t998*/
   movq    %r13, %rax/*t999*/
   movq    %r14, %rax/*t1000*/
   movq    %r15, %rax/*t1001*/
   movq    %rdi, %rax/*t993*/
   movq    %rsi, %rax/*t994*/
   movq    %rdx, %rax/*t995*/
   movq    %rax/*t995*/, %rdx
   movq    %rax/*t994*/, %rsi
   movq    %rax/*t993*/, %rdi
   call    __Foo#ordered
   # movq    %rax, %rax/*t1016*/
   xorq    %rax/*t1017*/, %rax/*t1017*/
   cmpq    %rax/*t1017*/, %rax/*t1016*/
   je      _L_331
_L_330:
   movq    $99, %rax/*t1018*/
   # movq    %rax/*t1018*/, %rax/*t996*/
_L_332:
   # movq    %rax/*t996*/, %rax
_bail_329:
   movq    %rax/*t997*/, %rbx
   movq    %rax/*t998*/, %r12
   movq    %rax/*t999*/, %r13
   movq    %rax/*t1000*/, %r14
   movq    %rax/*t1001*/, %r15
   jmp     _DONE_335
_L_331:
   movq    $22, %rax/*t1019*/
   # movq    %rax/*t1019*/, %rax/*t996*/
   jmp     _L_332
_DONE_335:
   # return sink
   addq   $8, %rsp
   ret

.text
.globl __Foo#ordered
__Foo#ordered:
_L_336:
   movq    %rbx, %rax/*t1006*/
   movq    %r12, %rax/*t1007*/
   movq    %r13, %rax/*t1008*/
   movq    %r14, %rax/*t1009*/
   movq    %r15, %rax/*t1010*/
   movq    %rdi, %rax/*t1002*/
   movq    %rsi, %rax/*t1003*/
   movq    %rdx, %rax/*t1004*/
   xorq    %rax/*t1020*/, %rax/*t1020*/
   # movq    %rax/*t1020*/, %rax/*t1005*/
   cmpq    %rax/*t1004*/, %rax/*t1003*/
   movq    $1, %rax/*t1021*/
   cmovl     %rax/*t1021*/, %rax/*t1005*/
   # movq    %rax/*t1005*/, %rax
_bail_333:
   movq    %rax/*t1006*/, %rbx
   movq    %rax/*t1007*/, %r12
   movq    %rax/*t1008*/, %r13
   movq    %rax/*t1009*/, %r14
   movq    %rax/*t1010*/, %r15
_DONE_337:
   # return sink
   ret

.text
.globl _cs411main
_cs411main:
   subq   $8, %rsp
_L_338:
   movq    %rbx, %rax/*t1011*/
   movq    %r12, %rax/*t1012*/
   movq    %r13, %rax/*t1013*/
   movq    %r14, %rax/*t1014*/
   movq    %r15, %rax/*t1015*/
   xorq    %rax/*t1026*/, %rax/*t1026*/
   movq    %rax/*t1026*/, %rdi
   call    _cs411newobject
   # movq    %rax, %rax/*t1023*/
   movq    $20, %rax/*t1027*/
   movq    %rax/*t1027*/, %rdx
   movq    $10, %rax/*t1028*/
   movq    %rax/*t1028*/, %rsi
   movq    %rax/*t1023*/, %rdi
   call    __Foo#doit
   # movq    %rax, %rax/*t1022*/
   movq    %rax/*t1022*/, %rdi
   call    _cs411println
   xorq    %rax/*t1029*/, %rax/*t1029*/
   movq    %rax/*t1029*/, %rdi
   call    _cs411newobject
   # movq    %rax, %rax/*t1025*/
   movq    $10, %rax/*t1030*/
   movq    %rax/*t1030*/, %rdx
   movq    $20, %rax/*t1031*/
   movq    %rax/*t1031*/, %rsi
   movq    %rax/*t1025*/, %rdi
   call    __Foo#doit
   # movq    %rax, %rax/*t1024*/
   movq    %rax/*t1024*/, %rdi
   call    _cs411println
_bail_328:
   movq    %rax/*t1011*/, %rbx
   movq    %rax/*t1012*/, %r12
   movq    %rax/*t1013*/, %r13
   movq    %rax/*t1014*/, %r14
   movq    %rax/*t1015*/, %r15
_DONE_339:
   # return sink
   addq   $8, %rsp
   ret

   .ident	"minijavac: cs411 course project 2017w2"
