#ifndef ___UTILS___
#define ___UTILS___

#ifndef _countof
#define _countof(x) (sizeof(x)/sizeof(x[0]))
#endif
#define _cstrlen(x) (_countof(x) - 1)
#define _cstr_len(x) x,_cstrlen(x)
#define _isstrempty(x) (!x || !x[0])

#ifndef max
#define max(a,b) (((a) > (b)) ? (a) : (b))
#define min(a,b) (((a) < (b)) ? (a) : (b))
#endif

#endif
