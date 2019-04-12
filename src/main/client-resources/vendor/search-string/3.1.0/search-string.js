(function(){function r(e,n,t){function o(i,f){if(!n[i]){if(!e[i]){var c="function"==typeof require&&require;if(!f&&c)return c(i,!0);if(u)return u(i,!0);var a=new Error("Cannot find module '"+i+"'");throw a.code="MODULE_NOT_FOUND",a}var p=n[i]={exports:{}};e[i][0].call(p.exports,function(r){var n=e[i][1][r];return o(n||r)},p,p.exports,r,e,n,t)}return n[i].exports}for(var u="function"==typeof require&&require,i=0;i<t.length;i++)o(t[i]);return o}return r})()({1:[function(require,module,exports){
'use strict';

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

var _require = require('./utils'),
    getQuotePairMap = _require.getQuotePairMap;

// state tokens


var RESET = 'RESET';
var IN_OPERAND = 'IN_OPERAND';
var IN_TEXT = 'IN_TEXT';
var SINGLE_QUOTE = 'SINGLE_QUOTE';
var DOUBLE_QUOTE = 'DOUBLE_QUOTE';

/**
 * SearchString is a parsed search string which allows you to fetch conditions
 * and text being searched.
 */

var SearchString = function () {
  /**
   * Not intended for public use. API could change.
   */
  function SearchString(conditionArray, textSegments) {
    _classCallCheck(this, SearchString);

    this.conditionArray = conditionArray;
    this.textSegments = textSegments;
    this.string = '';
    this.isStringDirty = true;
  }

  /**
   * @param {String} str to parse e.g. 'to:me -from:joe@acme.com foobar'.
   * @param {Array} transformTextToConditions Array of functions to transform text into conditions
   * @returns {SearchString} An instance of this class SearchString.
   */


  _createClass(SearchString, [{
    key: 'getConditionArray',


    /**
     * @return {Array} conditions, may contain multiple conditions for a particular key.
     */
    value: function getConditionArray() {
      return this.conditionArray;
    }

    /**
     * @return {Object} map of conditions and includes a special key 'excludes'.
     *                  Excludes itself is a map of conditions which were negated.
     */

  }, {
    key: 'getParsedQuery',
    value: function getParsedQuery() {
      var parsedQuery = { exclude: {} };
      this.conditionArray.forEach(function (condition) {
        if (condition.negated) {
          if (parsedQuery.exclude[condition.keyword]) {
            parsedQuery.exclude[condition.keyword].push(condition.value);
          } else {
            parsedQuery.exclude[condition.keyword] = [condition.value];
          }
        } else {
          if (parsedQuery[condition.keyword]) {
            parsedQuery[condition.keyword].push(condition.value);
          } else {
            parsedQuery[condition.keyword] = [condition.value];
          }
        }
      });
      return parsedQuery;
    }

    /**
     * @return {String} All text segments concateted together joined by a space.
     *                  If a text segment is negated, it is preceded by a `-`.
     */

  }, {
    key: 'getAllText',
    value: function getAllText() {
      return this.textSegments ? this.textSegments.map(function (_ref) {
        var text = _ref.text,
            negated = _ref.negated;
        return negated ? '-' + text : text;
      }).join(' ') : '';
    }

    /**
     * @return {Array} all text segment objects, negative or positive
     *                 e.g. { text: 'foobar', negated: false }
     */

  }, {
    key: 'getTextSegments',
    value: function getTextSegments() {
      return this.textSegments;
    }

    /**
     * Removes keyword-negated pair that matches inputted.
     * Only removes if entry has same keyword/negated combo.
     * @param {String} keywordToRemove Keyword to remove.
     * @param {Boolean} negatedToRemove Whether or not the keyword removed is negated.
     */

  }, {
    key: 'removeKeyword',
    value: function removeKeyword(keywordToRemove, negatedToRemove) {
      this.conditionArray = this.conditionArray.filter(function (_ref2) {
        var keyword = _ref2.keyword,
            negated = _ref2.negated;
        return keywordToRemove !== keyword || negatedToRemove !== negated;
      });
      this.isStringDirty = true;
    }

    /**
     * Adds a new entry to search string. Does not dedupe against existing entries.
     * @param {String} keyword  Keyword to add.
     * @param {String} value    Value for respective keyword.
     * @param {Boolean} negated Whether or not keyword/value pair should be negated.
     */

  }, {
    key: 'addEntry',
    value: function addEntry(keyword, value, negated) {
      this.conditionArray.push({
        keyword: keyword,
        value: value,
        negated: negated
      });
      this.isStringDirty = true;
    }

    /**
     * Removes an entry from the search string. If more than one entry with the same settings is found,
     * it removes the first entry matched.
     *
     * @param {String} keyword  Keyword to remove.
     * @param {String} value    Value for respective keyword.
     * @param {Boolean} negated Whether or not keyword/value pair is be negated.
     */

  }, {
    key: 'removeEntry',
    value: function removeEntry(keyword, value, negated) {
      var index = this.conditionArray.findIndex(function (entry) {
        return entry.keyword === keyword && entry.value === value && entry.negated === negated;
      });

      if (index === -1) return;

      this.conditionArray.splice(index, 1);
      this.isStringDirty = true;
    }

    /**
     * @return {SearchString} A new instance of this class based on current data. 
     */

  }, {
    key: 'clone',
    value: function clone() {
      return new SearchString(this.conditionArray.slice(0), this.textSegments.slice(0));
    }

    /**
     * @return {String} Returns this instance synthesized to a string format.
     *                  Example string: `to:me -from:joe@acme.com foobar`
     */

  }, {
    key: 'toString',
    value: function toString() {
      if (this.isStringDirty) {
        // Group keyword, negated pairs as keys
        var conditionGroups = {};
        this.conditionArray.forEach(function (_ref3) {
          var keyword = _ref3.keyword,
              value = _ref3.value,
              negated = _ref3.negated;

          var negatedStr = negated ? '-' : '';
          var conditionGroupKey = '' + negatedStr + keyword;
          if (conditionGroups[conditionGroupKey]) {
            conditionGroups[conditionGroupKey].push(value);
          } else {
            conditionGroups[conditionGroupKey] = [value];
          }
        });
        // Build conditionStr
        var conditionStr = '';
        Object.keys(conditionGroups).forEach(function (conditionGroupKey) {
          var values = conditionGroups[conditionGroupKey];
          var safeValues = values.filter(function (v) {
            return v;
          }).map(function (v) {
            var newV = '';
            var shouldQuote = false;
            for (var i = 0; i < v.length; i++) {
              var char = v[i];
              if (char === '"') {
                newV += '\\"';
              } else {
                if (char === ' ' || char === ',') {
                  shouldQuote = true;
                }
                newV += char;
              }
            }
            return shouldQuote ? '"' + newV + '"' : newV;
          });
          if (safeValues.length > 0) {
            conditionStr += ' ' + conditionGroupKey + ':' + safeValues.join(',');
          }
        });
        this.string = (conditionStr + ' ' + this.getAllText()).trim();
        this.isStringDirty = false;
      }
      return this.string;
    }
  }], [{
    key: 'parse',
    value: function parse(str) {
      var transformTextToConditions = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : [];

      if (!str) str = '';
      var conditionArray = [];
      var textSegments = [];

      var addCondition = function addCondition(key, value, negated) {
        var arrayEntry = { keyword: key, value: value, negated: negated };
        conditionArray.push(arrayEntry);
      };

      var addTextSegment = function addTextSegment(text, negated) {
        var hasTransform = false;
        transformTextToConditions.forEach(function (transform) {
          var _transform = transform(text),
              key = _transform.key,
              value = _transform.value;

          if (key && value) {
            addCondition(key, value, negated);
            hasTransform = true;
          }
        });
        if (!hasTransform) {
          textSegments.push({ text: text, negated: negated });
        }
      };

      var state = void 0;
      var currentOperand = void 0;
      var isNegated = void 0;
      var currentText = void 0;
      var quoteState = void 0;
      var prevChar = void 0;

      var performReset = function performReset() {
        state = RESET;
        quoteState = RESET;
        currentOperand = '';
        currentText = '';
        isNegated = false;
        prevChar = '';
      };

      // Terminology, in this example: 'to:joe@acme.com'
      // 'to' is the operator
      // 'joe@acme.com' is the operand
      // 'to:joe@acme.com' is the condition

      // Possible states:
      var inText = function inText() {
        return state === IN_TEXT;
      }; // could be inside raw text or operator
      var inOperand = function inOperand() {
        return state === IN_OPERAND;
      };
      var inSingleQuote = function inSingleQuote() {
        return quoteState === SINGLE_QUOTE;
      };
      var inDoubleQuote = function inDoubleQuote() {
        return quoteState === DOUBLE_QUOTE;
      };
      var inQuote = function inQuote() {
        return inSingleQuote() || inDoubleQuote();
      };

      performReset();

      var quotePairMap = getQuotePairMap(str);

      for (var i = 0; i < str.length; i++) {
        var char = str[i];
        if (char === ' ') {
          if (inOperand()) {
            if (inQuote()) {
              currentOperand += char;
            } else {
              addCondition(currentText, currentOperand, isNegated);
              performReset();
            }
          } else if (inText()) {
            if (inQuote()) {
              currentText += char;
            } else {
              addTextSegment(currentText, isNegated);
              performReset();
            }
          }
        } else if (char === ',' && inOperand() && !inQuote()) {
          addCondition(currentText, currentOperand, isNegated);
          // No reset here because we are still evaluating operands for the same operator
          currentOperand = '';
        } else if (char === '-' && !inOperand() && !inText()) {
          isNegated = true;
        } else if (char === ':' && !inQuote()) {
          if (inOperand()) {
            // If we're in an operand, just push the string on.
            currentOperand += char;
          } else if (inText()) {
            // Skip this char, move states into IN_OPERAND,
            state = IN_OPERAND;
          }
        } else if (char === '"' && prevChar !== '\\' && !inSingleQuote()) {
          if (inDoubleQuote()) {
            quoteState = RESET;
          } else if (quotePairMap.double[i]) {
            quoteState = DOUBLE_QUOTE;
          } else if (inOperand()) {
            currentOperand += char;
          } else {
            currentText += char;
          }
        } else if (char === "'" && prevChar !== '\\' && !inDoubleQuote()) {
          if (inSingleQuote()) {
            quoteState = RESET;
          } else if (quotePairMap.single[i]) {
            quoteState = SINGLE_QUOTE;
          } else if (inOperand()) {
            currentOperand += char;
          } else {
            currentText += char;
          }
        } else if (char !== '\\') {
          // Regular character..
          if (inOperand()) {
            currentOperand += char;
          } else {
            currentText += char;
            state = IN_TEXT;
          }
        }
        prevChar = char;
      }
      // End of string, add any last entries
      if (inText()) {
        addTextSegment(currentText, isNegated);
      } else if (inOperand()) {
        addCondition(currentText, currentOperand, isNegated);
      }

      return new SearchString(conditionArray, textSegments);
    }
  }]);

  return SearchString;
}();

module.exports = SearchString;
},{"./utils":2}],2:[function(require,module,exports){
'use strict';

function getQuotePairMap(str) {
  if (!str) str = '';
  var quotePairMap = { single: {}, double: {} };

  var prevQuote = { single: -1, double: -1 };
  var prevChar = '';
  for (var i = 0; i < str.length; i++) {
    var char = str[i];
    if (prevChar !== '\\') {
      if (char === '"') {
        if (prevQuote.double >= 0) {
          quotePairMap.double[prevQuote.double] = true;
          quotePairMap.double[i] = true;
          prevQuote.double = -1;
        } else {
          prevQuote.double = i;
        }
      } else if (char === "'") {
        if (prevQuote.single >= 0) {
          quotePairMap.single[prevQuote.single] = true;
          quotePairMap.single[i] = true;
          prevQuote.single = -1;
        } else {
          prevQuote.single = i;
        }
      }
    }
    prevChar = char;
  }

  return quotePairMap;
}

module.exports = {
  getQuotePairMap: getQuotePairMap
};
},{}],3:[function(require,module,exports){

window['SearchString'] = require('./dist/node/searchString');

},{"./dist/node/searchString":1}]},{},[3]);
