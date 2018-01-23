/*
 * WebTop Services is a Web Application framework developed by Sonicle S.r.l.
 * Copyright (C) 2018 Sonicle S.r.l.
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License version 3 as published by
 * the Free Software Foundation with the addition of the following permission
 * added to Section 15 as permitted in Section 7(a): FOR ANY PART OF THE COVERED
 * WORK IN WHICH THE COPYRIGHT IS OWNED BY SONICLE, SONICLE DISCLAIMS THE
 * WARRANTY OF NON INFRINGEMENT OF THIRD PARTY RIGHTS.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301 USA.
 *
 * You can contact Sonicle S.r.l. at email address sonicle@sonicle.com
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License version 3.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License
 * version 3, these Appropriate Legal Notices must retain the display of the
 * Sonicle logo and Sonicle copyright notice. If the display of the logo is not
 * reasonably feasible for technical reasons, the Appropriate Legal Notices must
 * display the words "Copyright (C) 2018 Sonicle S.r.l.".
 */
Ext.define('Sonicle.webtop.core.ux.MessageBody', {
	alternateClassName: 'WTA.ux.MessageBody',
	extend: 'Ext.container.Container',
	alias: ['widget.wtmessagebody'],
	
	plaintextTpl: '<html><body><pre>{body}</pre></body></html>',
	
	emailPattern: /((mailto:)[\w@,;.?=&%:///+ ]+)|([\w-\._\+%]+@(?:[\w-]+\.)+[\w]*)/gi,
	
	linkPattern: /((?:http|ftp)s?:\/\/|www.)([\w\.\-]+)\.(\w{2,6})([\w\/\-\_\+\.\,\?\=\&\!\:\;\%\#\|]+)*/gi,
	
	layout: 'fit',
	anchor: '100%',
	autoScroll: true,
	border : false,
	
	initComponent: function() {
		var me = this;
		me.autoEl = {
			tag: 'iframe',
			cls: 'wt-html-preview-iframe',
			frameborder: 0,
			src: Ext.SSL_SECURE_URL
		};
		me.callParent(arguments);
	},
	
	onRender: function() {
		var me = this;
		me.callParent(arguments);
		me.wrap = me.el.wrap({cls: 'wt-html-preview-body'});
		me.resizeEl = me.positionEl = me.wrap;
	},
	
	update: function(body) {
		console.log(body);
		var me = this,
				el = me.getEl(),
				ifw = el.dom.contentWindow,
				ifd = ifw.document,
				ifdEl = new Ext.Element(ifd),
				html;
		
		ifd.open();
		ifd.write('<!DOCTYPE html><html><body>');
		ifd.write(body);
		ifd.write('</body></html>');
		ifd.close();
		
		// Disable drag and drop
		el.on(ifw, 'dragover', WTU.onDisabledEvent);
		el.on(ifw, 'drop', WTU.onDisabledEvent);
		
		// Configure basic styling
		me.addStyleTag(ifd);
		
		me.scanDOMForLinks(ifd);
		me.addMailToListeners(ifdEl);
	},
	
	addStyleTag: function(doc) {
		var head = doc.getElementsByTagName('head')[0],
				css = doc.createElement('style');
		
		css.setAttribute('type', 'text/css');
		css.appendChild(document.createTextNode(
				'body { margin: 0; padding: 9px; font-family: "Lucida Grande",Verdana,Arial,Helvetica,sans-serif; } ' +
				// Make the blockquote element not use the default right margin of 40px
				'blockquote { margin-right: 0px; }' +
				// Make text in pre tags wrapped if too long for a line
				'pre { white-space: pre-wrap; margin: 0; font-family:monospace; !important; }'
		));

		// Add a wingdings compatible font (only the smilies)
		// for systems that don't have wingdings installed, and
		// always for firefox because that browser doesn't support
		// rendering with system installed symbol fonts.
		//if ((Ext.isGecko && !Ext.isIE && !Ext.isEdge) || !Zarafa.wingdingsInstalled) {
		//	var baseUrl = container.getServerConfig().getBaseUrl();
		//	css.appendChild(document.createTextNode(
		//			"@font-face {" +
		//			"font-family: 'Wingdings';" +
		//			"src: url('" + baseUrl + "client/resources/fonts/kopanowebappdings.eot');" +
		//			"src: url('" + baseUrl + "client/resources/fonts/kopanowebappdings.eot?#iefix') format('embedded-opentype')," +
		//			"url('" + baseUrl + "client/resources/fonts/kopanowebappdings.woff2') format('woff2')," +
		//			"url('" + baseUrl + "client/resources/fonts/kopanowebappdings.woff') format('woff')," +
		//			"url('" + baseUrl + "client/resources/fonts/kopanowebappdings.ttf') format('truetype');" +
		//			"font-weight: normal;" +
		//			"font-style: normal;" +
		//			"}"
		//			));
		//}

		head.appendChild(css);
	},
	
	onMailtoClick : function(e, el) {
		e.preventDefault();
		var href = this.href || el.href;
		console.log(href);
	},
	
	privates: {
		scanDOMForLinks: function(node) {
			var me = this;
			for (var i = 0; i < node.childNodes.length; i++) {
				var cnode = node.childNodes[i];
				if (cnode.nodeType === 1) { // Tag-node
					if (cnode.nodeName !== 'A') { // Ignore Anchor-node as they are already linkified
						me.scanDOMForLinks(cnode);
					}
				} else if (cnode.nodeType === 3) { // Text-node
					var s = cnode.nodeValue.trim();
					if (!Ext.isEmpty(s)) {
						// Check if this text node is HTML link or email address
						if (s.search(me.emailPattern) !== -1 || s.search(me.linkPattern) !== -1) {
							me.linkifyDOMNode(cnode, node);
						}
					}
				}
			}
		},
		
		linkifyDOMNode: function(node, parentNode) {
			var me = this,
					DHel = Ext.dom.Helper,
					SoString = me,
					tokens = SoString.splitStringByPattern(node.nodeValue, me.linkPattern),
					parts = [];
			
			for (var i=0; i<tokens.length; i++) {
				// Skip tokens if already contains a link
				if (tokens[i].search(me.linkPattern) === -1) {
					// Split tokens up based on whether they contain a link
					var arr = SoString.splitStringByPattern(tokens[i], me.emailPattern);
					parts.push.apply(parts, arr);
				} else {
					parts.push(tokens[i]);
				}
			}
			
			// Create a container node to append all the textnodes and anchor nodes to
			var ctNode = DHel.createDom({tag : 'span'});
			for (var i=0; i<parts.length; i++){
				// Create the node for a normal link
				if (parts[i].search(me.linkPattern) !== -1){
					// Create a new anchor-node for making url clickable.
					var aNode = DHel.append(ctNode, {tag: 'a', html: parts[i]});
					var link = parts[i];
					if (link.search(/(http|ftp)(s)?:\/\//gi) !== 0) {
						// Link has url in the pattern of www.something.com
						link = 'http://' + link;
					}
					aNode.setAttribute('href', link);
					aNode.setAttribute('target', '_blank');
				} else if(parts[i].search(me.emailPattern) !== -1) {
					// Create a new anchor-node for making an e-mail address clickable.
					var aNode = DHel.append(ctNode, {tag: 'a', html: parts[i]});
					var link = parts[i];
					if (link.indexOf('mailto:') !== 0){
						link = 'mailto:' + link;
					}
					aNode.setAttribute('href', link);
				} else {
					DHel.append(ctNode, Ext.String.htmlEncode(parts[i]));
				}
			}
			
			// Replace the original text node under the parent with the new anchor nodes and split up text nodes.
			for (var i=0, count=ctNode.childNodes.length; i<count; i++){
				// We remove the childNode from the parent by using this line so every loop we can add the first as the list shrinks
				parentNode.insertBefore(ctNode.childNodes.item(0), node);
			}

			// Remove the original node
			parentNode.removeChild(node);
		},
		
		addMailToListeners: function(docEl) {
			var me = this,
					els = docEl.query('a[href^="mailto:"]');
			if (!Ext.isEmpty(els)) {
				for (var i=0; i<els.length; i++) {
					els[i].on('click', me.onMailtoClick);
				}
			}
		},
		
		/**
		 * Split a string in pieces based on whether each piece matches the passed
		 * pattern. It returns both the pieces that match and that do not match the
		 * pattern.
		 * @param {String} str The input string to be split up
		 * @param {RegExp} pattern The regex pattern used to be split the string
		 * @returns {Array} The array of pieces
		 */
		splitStringByPattern: function(str, pattern) {	
			var cutOffPoints = [0],
					parts = [],
					found;
			// Find the cutOffPoints in the str
			while ((found = pattern.exec(str)) !== null){
				if (found.index !== 0) cutOffPoints.push(found.index);
				if (pattern.lastIndex < str.length) cutOffPoints.push(pattern.lastIndex);
			}
			// Cut the string up into the pieces based on the cutOffPoints
			if (cutOffPoints.length > 1){
				for (var i=0; i<cutOffPoints.length; i++) {
					// Use the current and the next cutOffPoint to calculate the number of character we need to extract.
					if (Ext.isDefined(cutOffPoints[i+1])) {
						parts.push(str.slice(cutOffPoints[i], cutOffPoints[i+1]));
					} else {
						parts.push(str.slice(cutOffPoints[i]));
					}
				}
			} else {
				parts = [str];
			}
			return parts;
		}
	}
});
