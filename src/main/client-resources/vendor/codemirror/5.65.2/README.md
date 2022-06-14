## Changes from original

At line 289 of `codemirror.css`, the original definition do NOT use the `!important` keyword.
This allows any other stylesheet (like `theme-classic-all-debug_1.css` at `.x-border-box *`) to change configuration for all CodeMirror's elements.

We don't want this, so the definition is now changed to:

```javascript
/* Force content-box sizing for the elements where we expect it */
.CodeMirror-scroll,
.CodeMirror-sizer,
.CodeMirror-gutter,
.CodeMirror-gutters,
.CodeMirror-linenumber {
  -moz-box-sizing: content-box !important;
  box-sizing: content-box !important;
}
```