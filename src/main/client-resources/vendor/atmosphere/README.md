## Changes from original

At line 1999, the original conditional test was hiding a possible response 401
(FORBIDDEN) status because the code was interested only in requests with 
ready state at level 4 (DONE). The condition is now changed to:

```javascript

if (ajaxRequest.readyState >= 3) {

```
