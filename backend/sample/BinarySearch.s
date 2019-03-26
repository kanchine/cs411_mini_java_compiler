.text
.globl __BS#Start
__BS#Start:
   subq   $8, %rsp
_L_314:
   movq    %rbx, %rax/*t767*/
   movq    %r12, %rax/*t768*/
   movq    %r13, %rax/*t769*/
   movq    %r14, %rax/*t770*/
   movq    %r15, %rax/*t771*/
   movq    %rdi, %rax/*t763*/
   movq    %rsi, %rax/*t764*/
   movq    %rax/*t764*/, %rsi
   movq    %rax/*t763*/, %rdi
   call    __BS#Init
   # movq    %rax, %rax/*t765*/
   movq    %rax/*t763*/, %rdi
   call    __BS#Print
   # movq    %rax, %rax/*t766*/
   movq    $8, %rax/*t840*/
   movq    %rax/*t840*/, %rsi
   movq    %rax/*t763*/, %rdi
   call    __BS#Search
   # movq    %rax, %rax/*t832*/
   xorq    %rax/*t841*/, %rax/*t841*/
   cmpq    %rax/*t841*/, %rax/*t832*/
   je      _L_247
_L_246:
   movq    $1, %rax/*t842*/
   movq    %rax/*t842*/, %rdi
   call    _cs411println
_L_248:
   movq    $19, %rax/*t843*/
   movq    %rax/*t843*/, %rsi
   movq    %rax/*t763*/, %rdi
   call    __BS#Search
   # movq    %rax, %rax/*t833*/
   xorq    %rax/*t844*/, %rax/*t844*/
   cmpq    %rax/*t844*/, %rax/*t833*/
   je      _L_250
_L_249:
   movq    $1, %rax/*t845*/
   movq    %rax/*t845*/, %rdi
   call    _cs411println
_L_251:
   movq    $20, %rax/*t846*/
   movq    %rax/*t846*/, %rsi
   movq    %rax/*t763*/, %rdi
   call    __BS#Search
   # movq    %rax, %rax/*t834*/
   xorq    %rax/*t847*/, %rax/*t847*/
   cmpq    %rax/*t847*/, %rax/*t834*/
   je      _L_253
_L_252:
   movq    $1, %rax/*t848*/
   movq    %rax/*t848*/, %rdi
   call    _cs411println
_L_254:
   movq    $21, %rax/*t849*/
   movq    %rax/*t849*/, %rsi
   movq    %rax/*t763*/, %rdi
   call    __BS#Search
   # movq    %rax, %rax/*t835*/
   xorq    %rax/*t850*/, %rax/*t850*/
   cmpq    %rax/*t850*/, %rax/*t835*/
   je      _L_256
_L_255:
   movq    $1, %rax/*t851*/
   movq    %rax/*t851*/, %rdi
   call    _cs411println
_L_257:
   movq    $37, %rax/*t852*/
   movq    %rax/*t852*/, %rsi
   movq    %rax/*t763*/, %rdi
   call    __BS#Search
   # movq    %rax, %rax/*t836*/
   xorq    %rax/*t853*/, %rax/*t853*/
   cmpq    %rax/*t853*/, %rax/*t836*/
   je      _L_259
_L_258:
   movq    $1, %rax/*t854*/
   movq    %rax/*t854*/, %rdi
   call    _cs411println
_L_260:
   movq    $38, %rax/*t855*/
   movq    %rax/*t855*/, %rsi
   movq    %rax/*t763*/, %rdi
   call    __BS#Search
   # movq    %rax, %rax/*t837*/
   xorq    %rax/*t856*/, %rax/*t856*/
   cmpq    %rax/*t856*/, %rax/*t837*/
   je      _L_262
_L_261:
   movq    $1, %rax/*t857*/
   movq    %rax/*t857*/, %rdi
   call    _cs411println
_L_263:
   movq    $39, %rax/*t858*/
   movq    %rax/*t858*/, %rsi
   movq    %rax/*t763*/, %rdi
   call    __BS#Search
   # movq    %rax, %rax/*t838*/
   xorq    %rax/*t859*/, %rax/*t859*/
   cmpq    %rax/*t859*/, %rax/*t838*/
   je      _L_265
_L_264:
   movq    $1, %rax/*t860*/
   movq    %rax/*t860*/, %rdi
   call    _cs411println
_L_266:
   movq    $50, %rax/*t861*/
   movq    %rax/*t861*/, %rsi
   movq    %rax/*t763*/, %rdi
   call    __BS#Search
   # movq    %rax, %rax/*t839*/
   xorq    %rax/*t862*/, %rax/*t862*/
   cmpq    %rax/*t862*/, %rax/*t839*/
   je      _L_268
_L_267:
   movq    $1, %rax/*t863*/
   movq    %rax/*t863*/, %rdi
   call    _cs411println
_L_269:
   movq    $999, %rax/*t864*/
   # movq    %rax/*t864*/, %rax
_bail_245:
   movq    %rax/*t767*/, %rbx
   movq    %rax/*t768*/, %r12
   movq    %rax/*t769*/, %r13
   movq    %rax/*t770*/, %r14
   movq    %rax/*t771*/, %r15
   jmp     _DONE_315
_L_247:
   xorq    %rax/*t865*/, %rax/*t865*/
   movq    %rax/*t865*/, %rdi
   call    _cs411println
   jmp     _L_248
_L_250:
   xorq    %rax/*t866*/, %rax/*t866*/
   movq    %rax/*t866*/, %rdi
   call    _cs411println
   jmp     _L_251
_L_253:
   xorq    %rax/*t867*/, %rax/*t867*/
   movq    %rax/*t867*/, %rdi
   call    _cs411println
   jmp     _L_254
_L_256:
   xorq    %rax/*t868*/, %rax/*t868*/
   movq    %rax/*t868*/, %rdi
   call    _cs411println
   jmp     _L_257
_L_259:
   xorq    %rax/*t869*/, %rax/*t869*/
   movq    %rax/*t869*/, %rdi
   call    _cs411println
   jmp     _L_260
_L_262:
   xorq    %rax/*t870*/, %rax/*t870*/
   movq    %rax/*t870*/, %rdi
   call    _cs411println
   jmp     _L_263
_L_265:
   xorq    %rax/*t871*/, %rax/*t871*/
   movq    %rax/*t871*/, %rdi
   call    _cs411println
   jmp     _L_266
_L_268:
   xorq    %rax/*t872*/, %rax/*t872*/
   movq    %rax/*t872*/, %rdi
   call    _cs411println
   jmp     _L_269
_DONE_315:
   # return sink
   addq   $8, %rsp
   ret

.text
.globl __BS#Search
__BS#Search:
   subq   $8, %rsp
_L_316:
   movq    %rbx, %rax/*t782*/
   movq    %r12, %rax/*t783*/
   movq    %r13, %rax/*t784*/
   movq    %r14, %rax/*t785*/
   movq    %r15, %rax/*t786*/
   movq    %rdi, %rax/*t772*/
   movq    %rsi, %rax/*t773*/
   xorq    %rax/*t875*/, %rax/*t875*/
   # movq    %rax/*t875*/, %rax/*t779*/
   xorq    %rax/*t876*/, %rax/*t876*/
   # movq    %rax/*t876*/, %rax/*t774*/
   # movq    %rax/*t772*/, %rax/*t880*/
   xorq    %rax/*t881*/, %rax/*t881*/
   addq    %rax/*t881*/, %rax/*t880*/
   movq    (%rax/*t880*/), %rax/*t879*/
   # movq    %rax/*t879*/, %rax/*t878*/
   movq    $-8, %rax/*t882*/
   addq    %rax/*t882*/, %rax/*t878*/
   movq    (%rax/*t878*/), %rax/*t877*/
   # movq    %rax/*t877*/, %rax/*t775*/
   # movq    %rax/*t775*/, %rax/*t883*/
   movq    $1, %rax/*t884*/
   subq    %rax/*t884*/, %rax/*t883*/
   # movq    %rax/*t883*/, %rax/*t775*/
   xorq    %rax/*t885*/, %rax/*t885*/
   # movq    %rax/*t885*/, %rax/*t776*/
   movq    $1, %rax/*t886*/
   # movq    %rax/*t886*/, %rax/*t777*/
_L_283:
   xorq    %rax/*t887*/, %rax/*t887*/
   cmpq    %rax/*t887*/, %rax/*t777*/
   je      _L_285
_L_284:
   # movq    %rax/*t775*/, %rax/*t888*/
   addq    %rax/*t776*/, %rax/*t888*/
   # movq    %rax/*t888*/, %rax/*t778*/
   movq    %rax/*t778*/, %rsi
   movq    %rax/*t772*/, %rdi
   call    __BS#Div
   # movq    %rax, %rax/*t778*/
   # movq    %rax/*t772*/, %rax/*t892*/
   xorq    %rax/*t893*/, %rax/*t893*/
   addq    %rax/*t893*/, %rax/*t892*/
   movq    (%rax/*t892*/), %rax/*t891*/
   # movq    %rax/*t891*/, %rax/*t890*/
   movq    $-8, %rax/*t894*/
   addq    %rax/*t894*/, %rax/*t890*/
   movq    (%rax/*t890*/), %rax/*t889*/
   cmpq    %rax/*t889*/, %rax/*t778*/
   jge     _L_272
_L_271:
   # movq    %rax/*t772*/, %rax/*t898*/
   xorq    %rax/*t899*/, %rax/*t899*/
   addq    %rax/*t899*/, %rax/*t898*/
   movq    (%rax/*t898*/), %rax/*t897*/
   # movq    %rax/*t897*/, %rax/*t896*/
   movq    $8, %rax/*t901*/
   # movq    %rax/*t901*/, %rax/*t900*/
   imulq   %rax/*t778*/, %rax/*t900*/
   addq    %rax/*t900*/, %rax/*t896*/
   movq    (%rax/*t896*/), %rax/*t895*/
   # movq    %rax/*t895*/, %rax/*t781*/
_L_273:
   # movq    %rax/*t781*/, %rax/*t779*/
   cmpq    %rax/*t779*/, %rax/*t773*/
   jge     _L_275
_L_274:
   # movq    %rax/*t778*/, %rax/*t902*/
   movq    $1, %rax/*t903*/
   subq    %rax/*t903*/, %rax/*t902*/
   # movq    %rax/*t902*/, %rax/*t775*/
_L_276:
   movq    %rax/*t773*/, %rdx
   movq    %rax/*t779*/, %rsi
   movq    %rax/*t772*/, %rdi
   call    __BS#Compare
   # movq    %rax, %rax/*t873*/
   xorq    %rax/*t904*/, %rax/*t904*/
   cmpq    %rax/*t904*/, %rax/*t873*/
   je      _L_278
_L_277:
   xorq    %rax/*t905*/, %rax/*t905*/
   # movq    %rax/*t905*/, %rax/*t777*/
_L_279:
   cmpq    %rax/*t776*/, %rax/*t775*/
   jge     _L_281
_L_280:
   xorq    %rax/*t906*/, %rax/*t906*/
   # movq    %rax/*t906*/, %rax/*t777*/
_L_282:
   jmp     _L_283
_L_272:
   movq    $1, %rax/*t907*/
   movq    %rax/*t907*/, %rdi
   call    _cs411error
   # movq    %rax, %rax/*t781*/
   jmp     _L_273
_L_275:
   movq    $1, %rax/*t909*/
   # movq    %rax/*t909*/, %rax/*t908*/
   addq    %rax/*t778*/, %rax/*t908*/
   # movq    %rax/*t908*/, %rax/*t776*/
   jmp     _L_276
_L_278:
   movq    $1, %rax/*t910*/
   # movq    %rax/*t910*/, %rax/*t777*/
   jmp     _L_279
_L_281:
   xorq    %rax/*t911*/, %rax/*t911*/
   # movq    %rax/*t911*/, %rax/*t780*/
   jmp     _L_282
_L_285:
   movq    %rax/*t773*/, %rdx
   movq    %rax/*t779*/, %rsi
   movq    %rax/*t772*/, %rdi
   call    __BS#Compare
   # movq    %rax, %rax/*t874*/
   xorq    %rax/*t912*/, %rax/*t912*/
   cmpq    %rax/*t912*/, %rax/*t874*/
   je      _L_287
_L_286:
   movq    $1, %rax/*t913*/
   # movq    %rax/*t913*/, %rax/*t774*/
_L_288:
   # movq    %rax/*t774*/, %rax
_bail_270:
   movq    %rax/*t782*/, %rbx
   movq    %rax/*t783*/, %r12
   movq    %rax/*t784*/, %r13
   movq    %rax/*t785*/, %r14
   movq    %rax/*t786*/, %r15
   jmp     _DONE_317
_L_287:
   xorq    %rax/*t914*/, %rax/*t914*/
   # movq    %rax/*t914*/, %rax/*t774*/
   jmp     _L_288
_DONE_317:
   # return sink
   addq   $8, %rsp
   ret

.text
.globl __BS#Div
__BS#Div:
_L_318:
   movq    %rbx, %rax/*t792*/
   movq    %r12, %rax/*t793*/
   movq    %r13, %rax/*t794*/
   movq    %r14, %rax/*t795*/
   movq    %r15, %rax/*t796*/
   movq    %rdi, %rax/*t787*/
   movq    %rsi, %rax/*t788*/
   xorq    %rax/*t915*/, %rax/*t915*/
   # movq    %rax/*t915*/, %rax/*t789*/
   xorq    %rax/*t916*/, %rax/*t916*/
   # movq    %rax/*t916*/, %rax/*t790*/
   # movq    %rax/*t788*/, %rax/*t917*/
   movq    $1, %rax/*t918*/
   subq    %rax/*t918*/, %rax/*t917*/
   # movq    %rax/*t917*/, %rax/*t791*/
_L_290:
   cmpq    %rax/*t791*/, %rax/*t790*/
   jge     _L_292
_L_291:
   movq    $1, %rax/*t920*/
   # movq    %rax/*t920*/, %rax/*t919*/
   addq    %rax/*t789*/, %rax/*t919*/
   # movq    %rax/*t919*/, %rax/*t789*/
   movq    $2, %rax/*t922*/
   # movq    %rax/*t922*/, %rax/*t921*/
   addq    %rax/*t790*/, %rax/*t921*/
   # movq    %rax/*t921*/, %rax/*t790*/
   jmp     _L_290
_L_292:
   # movq    %rax/*t789*/, %rax
_bail_289:
   movq    %rax/*t792*/, %rbx
   movq    %rax/*t793*/, %r12
   movq    %rax/*t794*/, %r13
   movq    %rax/*t795*/, %r14
   movq    %rax/*t796*/, %r15
_DONE_319:
   # return sink
   ret

.text
.globl __BS#Compare
__BS#Compare:
_L_320:
   movq    %rbx, %rax/*t802*/
   movq    %r12, %rax/*t803*/
   movq    %r13, %rax/*t804*/
   movq    %r14, %rax/*t805*/
   movq    %r15, %rax/*t806*/
   movq    %rdi, %rax/*t797*/
   movq    %rsi, %rax/*t798*/
   movq    %rdx, %rax/*t799*/
   xorq    %rax/*t923*/, %rax/*t923*/
   # movq    %rax/*t923*/, %rax/*t800*/
   movq    $1, %rax/*t925*/
   # movq    %rax/*t925*/, %rax/*t924*/
   addq    %rax/*t799*/, %rax/*t924*/
   # movq    %rax/*t924*/, %rax/*t801*/
   cmpq    %rax/*t799*/, %rax/*t798*/
   jge     _L_298
_L_297:
   xorq    %rax/*t926*/, %rax/*t926*/
   # movq    %rax/*t926*/, %rax/*t800*/
_L_299:
   # movq    %rax/*t800*/, %rax
_bail_293:
   movq    %rax/*t802*/, %rbx
   movq    %rax/*t803*/, %r12
   movq    %rax/*t804*/, %r13
   movq    %rax/*t805*/, %r14
   movq    %rax/*t806*/, %r15
   jmp     _DONE_321
_L_298:
   cmpq    %rax/*t801*/, %rax/*t798*/
   jge     _L_294
_L_295:
   movq    $1, %rax/*t927*/
   # movq    %rax/*t927*/, %rax/*t800*/
_L_296:
   jmp     _L_299
_L_294:
   xorq    %rax/*t928*/, %rax/*t928*/
   # movq    %rax/*t928*/, %rax/*t800*/
   jmp     _L_296
_DONE_321:
   # return sink
   ret

.text
.globl __BS#Print
__BS#Print:
   subq   $8, %rsp
_L_322:
   movq    %rbx, %rax/*t810*/
   movq    %r12, %rax/*t811*/
   movq    %r13, %rax/*t812*/
   movq    %r14, %rax/*t813*/
   movq    %r15, %rax/*t814*/
   movq    %rdi, %rax/*t807*/
   movq    $1, %rax/*t929*/
   # movq    %rax/*t929*/, %rax/*t808*/
_L_304:
   # movq    %rax/*t807*/, %rax/*t931*/
   movq    $8, %rax/*t932*/
   addq    %rax/*t932*/, %rax/*t931*/
   movq    (%rax/*t931*/), %rax/*t930*/
   cmpq    %rax/*t930*/, %rax/*t808*/
   jge     _L_306
_L_305:
   # movq    %rax/*t807*/, %rax/*t936*/
   xorq    %rax/*t937*/, %rax/*t937*/
   addq    %rax/*t937*/, %rax/*t936*/
   movq    (%rax/*t936*/), %rax/*t935*/
   # movq    %rax/*t935*/, %rax/*t934*/
   movq    $-8, %rax/*t938*/
   addq    %rax/*t938*/, %rax/*t934*/
   movq    (%rax/*t934*/), %rax/*t933*/
   cmpq    %rax/*t933*/, %rax/*t808*/
   jge     _L_302
_L_301:
   # movq    %rax/*t807*/, %rax/*t942*/
   xorq    %rax/*t943*/, %rax/*t943*/
   addq    %rax/*t943*/, %rax/*t942*/
   movq    (%rax/*t942*/), %rax/*t941*/
   # movq    %rax/*t941*/, %rax/*t940*/
   movq    $8, %rax/*t945*/
   # movq    %rax/*t945*/, %rax/*t944*/
   imulq   %rax/*t808*/, %rax/*t944*/
   addq    %rax/*t944*/, %rax/*t940*/
   movq    (%rax/*t940*/), %rax/*t939*/
   # movq    %rax/*t939*/, %rax/*t809*/
_L_303:
   movq    %rax/*t809*/, %rdi
   call    _cs411println
   movq    $1, %rax/*t947*/
   # movq    %rax/*t947*/, %rax/*t946*/
   addq    %rax/*t808*/, %rax/*t946*/
   # movq    %rax/*t946*/, %rax/*t808*/
   jmp     _L_304
_L_302:
   movq    $1, %rax/*t948*/
   movq    %rax/*t948*/, %rdi
   call    _cs411error
   # movq    %rax, %rax/*t809*/
   jmp     _L_303
_L_306:
   movq    $99999, %rax/*t949*/
   movq    %rax/*t949*/, %rdi
   call    _cs411println
   xorq    %rax/*t950*/, %rax/*t950*/
   # movq    %rax/*t950*/, %rax
_bail_300:
   movq    %rax/*t810*/, %rbx
   movq    %rax/*t811*/, %r12
   movq    %rax/*t812*/, %r13
   movq    %rax/*t813*/, %r14
   movq    %rax/*t814*/, %r15
_DONE_323:
   # return sink
   addq   $8, %rsp
   ret

.text
.globl __BS#Init
__BS#Init:
   subq   $8, %rsp
_L_324:
   movq    %rbx, %rax/*t822*/
   movq    %r12, %rax/*t823*/
   movq    %r13, %rax/*t824*/
   movq    %r14, %rax/*t825*/
   movq    %r15, %rax/*t826*/
   movq    %rdi, %rax/*t815*/
   movq    %rsi, %rax/*t816*/
   # movq    %rax/*t815*/, %rax/*t953*/
   movq    $8, %rax/*t954*/
   addq    %rax/*t954*/, %rax/*t953*/
   movq    %rax/*t816*/, (%rax/*t953*/)
   # movq    %rax/*t815*/, %rax/*t955*/
   xorq    %rax/*t956*/, %rax/*t956*/
   addq    %rax/*t956*/, %rax/*t955*/
   # movq    %rax/*t955*/, %rax/*t952*/
   movq    %rax/*t816*/, %rdi
   call    _cs411newarray
   # movq    %rax, %rax/*t951*/
   movq    %rax/*t951*/, (%rax/*t952*/)
   movq    $1, %rax/*t957*/
   # movq    %rax/*t957*/, %rax/*t817*/
   movq    $1, %rax/*t959*/
   # movq    %rax/*t959*/, %rax/*t958*/
   # movq    %rax/*t815*/, %rax/*t961*/
   movq    $8, %rax/*t962*/
   addq    %rax/*t962*/, %rax/*t961*/
   movq    (%rax/*t961*/), %rax/*t960*/
   addq    %rax/*t960*/, %rax/*t958*/
   # movq    %rax/*t958*/, %rax/*t818*/
_L_311:
   # movq    %rax/*t815*/, %rax/*t964*/
   movq    $8, %rax/*t965*/
   addq    %rax/*t965*/, %rax/*t964*/
   movq    (%rax/*t964*/), %rax/*t963*/
   cmpq    %rax/*t963*/, %rax/*t817*/
   jge     _L_313
_L_312:
   # movq    %rax/*t817*/, %rax/*t966*/
   movq    $2, %rax/*t967*/
   imulq   %rax/*t967*/, %rax/*t966*/
   # movq    %rax/*t966*/, %rax/*t820*/
   # movq    %rax/*t818*/, %rax/*t968*/
   movq    $3, %rax/*t969*/
   subq    %rax/*t969*/, %rax/*t968*/
   # movq    %rax/*t968*/, %rax/*t819*/
   # movq    %rax/*t815*/, %rax/*t973*/
   xorq    %rax/*t974*/, %rax/*t974*/
   addq    %rax/*t974*/, %rax/*t973*/
   movq    (%rax/*t973*/), %rax/*t972*/
   # movq    %rax/*t972*/, %rax/*t971*/
   movq    $-8, %rax/*t975*/
   addq    %rax/*t975*/, %rax/*t971*/
   movq    (%rax/*t971*/), %rax/*t970*/
   cmpq    %rax/*t970*/, %rax/*t817*/
   jge     _L_309
_L_308:
   # movq    %rax/*t815*/, %rax/*t978*/
   xorq    %rax/*t979*/, %rax/*t979*/
   addq    %rax/*t979*/, %rax/*t978*/
   movq    (%rax/*t978*/), %rax/*t977*/
   # movq    %rax/*t977*/, %rax/*t976*/
   movq    $8, %rax/*t981*/
   # movq    %rax/*t981*/, %rax/*t980*/
   imulq   %rax/*t817*/, %rax/*t980*/
   addq    %rax/*t980*/, %rax/*t976*/
   # movq    %rax/*t819*/, %rax/*t982*/
   addq    %rax/*t820*/, %rax/*t982*/
   movq    %rax/*t982*/, (%rax/*t976*/)
_L_310:
   movq    $1, %rax/*t984*/
   # movq    %rax/*t984*/, %rax/*t983*/
   addq    %rax/*t817*/, %rax/*t983*/
   # movq    %rax/*t983*/, %rax/*t817*/
   # movq    %rax/*t818*/, %rax/*t985*/
   movq    $1, %rax/*t986*/
   subq    %rax/*t986*/, %rax/*t985*/
   # movq    %rax/*t985*/, %rax/*t818*/
   jmp     _L_311
_L_309:
   movq    $1, %rax/*t987*/
   movq    %rax/*t987*/, %rdi
   call    _cs411error
   # movq    %rax, %rax/*t821*/
   jmp     _L_310
_L_313:
   xorq    %rax/*t988*/, %rax/*t988*/
   # movq    %rax/*t988*/, %rax
_bail_307:
   movq    %rax/*t822*/, %rbx
   movq    %rax/*t823*/, %r12
   movq    %rax/*t824*/, %r13
   movq    %rax/*t825*/, %r14
   movq    %rax/*t826*/, %r15
_DONE_325:
   # return sink
   addq   $8, %rsp
   ret

.text
.globl _cs411main
_cs411main:
   subq   $8, %rsp
_L_326:
   movq    %rbx, %rax/*t827*/
   movq    %r12, %rax/*t828*/
   movq    %r13, %rax/*t829*/
   movq    %r14, %rax/*t830*/
   movq    %r15, %rax/*t831*/
   movq    $16, %rax/*t991*/
   movq    %rax/*t991*/, %rdi
   call    _cs411newobject
   # movq    %rax, %rax/*t990*/
   movq    $20, %rax/*t992*/
   movq    %rax/*t992*/, %rsi
   movq    %rax/*t990*/, %rdi
   call    __BS#Start
   # movq    %rax, %rax/*t989*/
   movq    %rax/*t989*/, %rdi
   call    _cs411println
_bail_244:
   movq    %rax/*t827*/, %rbx
   movq    %rax/*t828*/, %r12
   movq    %rax/*t829*/, %r13
   movq    %rax/*t830*/, %r14
   movq    %rax/*t831*/, %r15
_DONE_327:
   # return sink
   addq   $8, %rsp
   ret

   .ident	"minijavac: cs411 course project 2017w2"
