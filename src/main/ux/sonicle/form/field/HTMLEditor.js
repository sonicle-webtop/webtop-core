/*
 * WebTop Services is a Web Application framework developed by Sonicle S.r.l.
 * Copyright (C) 2014 Sonicle S.r.l.
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
 * display the words "Copyright (C) 2014 Sonicle S.r.l.".
 */

Ext.define('Sonicle.form.field.HTMLEditor', {
    extend: 'Ext.form.FieldContainer',
    mixins: {
        field: 'Ext.form.field.Field'
    },
	alias: ['widget.sohtmleditor'],
	requires: [
		'Ext.ux.form.TinyMCETextArea'
	],

	layout: 'border',
    border: false,
	
	tmce: null,
	toolbar: null,
	
    /**
     * @cfg {String} defaultButtonUI
     * A default {@link Ext.Component#ui ui} to use for the HtmlEditor's toolbar
     * {@link Ext.button.Button Buttons}
     */
    defaultButtonUI: 'default-toolbar',
	
	enableFont: false,
	enableFontSize: false,
	enableFormat: false,
	enableColors: false,
	enableAlignments: false,
	enableLists: false,
	enableSourceEdit: false,
	
	initComponent: function() {
		var me=this;
		
		me.items = [ me.createToolbar(), me.createTinyMCE() ];
		
        //me.layout = {
        //    type: 'vbox',
        //    align: 'stretch'
        //};
		
		me.callParent(arguments);
		me.initField();
	},
	
	createTinyMCE: function(){
		this.tmce=Ext.create({
			xtype: 'tinymce_textarea',
			region: 'center',
			fieldStyle: 'font-family: Courier New; font-size: 12px;',
			style: { border: '0' },
			tinyMCEConfig: {
				plugins: [
				"advlist autolink lists link image charmap print preview hr anchor pagebreak",
				"searchreplace visualblocks visualchars code fullscreen",
				"insertdatetime media nonbreaking save table contextmenu directionality",
				"emoticons template paste textcolor"
				],

				toolbar: false,
				statusbar: false,
				//toolbar1: "newdocument fullpage | bold italic underline strikethrough | alignleft aligncenter alignright alignjustify | styleselect formatselect fontselect fontsizeselect",
				//toolbar2: "cut copy paste | searchreplace | bullist numlist | outdent indent blockquote | undo redo | link unlink anchor image media code | inserttime preview | forecolor backcolor",
				//toolbar3: "table | hr removeformat | subscript superscript | charmap emoticons | print fullscreen | ltr rtl | spellchecker | visualchars visualblocks nonbreaking template pagebreak restoredraft",
				menubar: false,
				toolbar_items_size: 'small'
			},
			value: 'This is the WebTop-TinyMCE HTML Editor'

		});
		return this.tmce;
		
    },
	
/*
     * Called when the editor creates its toolbar. Override this method if you need to
     * add custom toolbar buttons.
     * @param {Ext.form.field.HtmlEditor} editor
     * @protected
     */
    createToolbar: function(){
        this.toolbar = Ext.widget(this.getToolbarCfg());
        return this.toolbar;
    },
    
    getToolbarCfg: function(){
        var me = this,
            items = [], i,
            tipsEnabled = Ext.quickTipsActive && Ext.tip.QuickTipManager.isEnabled(),
            baseCSSPrefix = Ext.baseCSSPrefix,
            undef;

        function btn(id, toggle, handler){
            return {
                itemId: id,
                cls: baseCSSPrefix + 'btn-icon',
                iconCls: baseCSSPrefix + 'edit-'+id,
                enableToggle:toggle !== false,
                scope: me,
                handler:handler||me.relayBtnCmd,
                clickEvent: 'mousedown',
                tooltip: tipsEnabled ? me.buttonTips[id] || undef : undef,
                overflowText: me.buttonTips[id].title || undef,
                tabIndex: -1
            };
        }


        if (me.enableFont) {

            items.push(
				{
					xtype: 'combo', 
					width: 140,
					store: Ext.create('Ext.data.Store', {
						fields: ['fn'],
						data : [
							{ fn: "Arial" },
							{ fn: "Comic Sans MS" },
							{ fn: "Courier New" },
							{ fn: "Helvetica" },
							{ fn: "Tahoma" },
							{ fn: "Times New Roman" },
							{ fn: "Verdana" }
						]
					}),
					forceSelection: true,
					autoSelect: true,
					displayField: 'fn',
					valueField: 'fn',
					queryMode: 'local',
					listeners: {
						'select': function(c,r,o) {
							me.execCommand('fontname',false,r.get('fn'));
						}
					}
				},
				'-'
            );
        }
		
        if (me.enableFontSize) {
			items.push(
				{
					xtype: 'combo', 
					width: 50,
					store: Ext.create('Ext.data.Store', {
						fields: ['id','fs'],
						data : [
							{ id: 1, fs: "8" },
							{ id: 2, fs: "10"},
							{ id: 3, fs: "12"},
							{ id: 4, fs: "14"},
							{ id: 5, fs: "18"},
							{ id: 6, fs: "24"},
							{ id: 7, fs: "36"}
						]
					}),
					forceSelection: true,
					autoSelect: true,
					displayField: 'fs',
					valueField: 'id',
					queryMode: 'local',
					listeners: {
						'select': function(c,r,o) {
							me.execCommand('fontsize',false,r.get('id'));
						}
					}
				},
				'-'
			);
			
		}		

        if (me.enableFormat) {
            items.push(
                btn('bold'),
                btn('italic'),
                btn('underline')
            );
        }
		
        if (me.enableColors) {
            items.push(
                '-', {
                    itemId: 'forecolor',
                    cls: baseCSSPrefix + 'btn-icon',
                    iconCls: baseCSSPrefix + 'edit-forecolor',
                    overflowText: me.buttonTips.forecolor.title,
                    tooltip: tipsEnabled ? me.buttonTips.forecolor || undef : undef,
                    tabIndex:-1,
                    menu: Ext.widget('menu', {
                        plain: true,

                        items: [{
                            xtype: 'colorpicker',
                            allowReselect: true,
                            focus: Ext.emptyFn,
                            value: '000000',
                            plain: true,
                            clickEvent: 'mousedown',
                            handler: function(cp, color) {
                                me.execCommand('forecolor', false, Ext.isWebKit || Ext.isIE ? '#'+color : color);
                                this.up('menu').hide();
                            }
                        }]
                    })
                }, {
                    itemId: 'backcolor',
                    cls: baseCSSPrefix + 'btn-icon',
                    iconCls: baseCSSPrefix + 'edit-backcolor',
                    overflowText: me.buttonTips.backcolor.title,
                    tooltip: tipsEnabled ? me.buttonTips.backcolor || undef : undef,
                    tabIndex:-1,
                    menu: Ext.widget('menu', {
                        plain: true,

                        items: [{
                            xtype: 'colorpicker',
                            focus: Ext.emptyFn,
                            value: 'FFFFFF',
                            plain: true,
                            allowReselect: true,
                            clickEvent: 'mousedown',
                            handler: function(cp, color) {
                                if (Ext.isGecko) {
                                    me.execCommand('useCSS', false, false);
                                    me.execCommand('hilitecolor', false, '#'+color);
                                    me.execCommand('useCSS', false, true);
                                    me.deferFocus();
                                } else {
                                    me.execCommand(Ext.isOpera ? 'hilitecolor' : 'backcolor', false, Ext.isWebKit || Ext.isIE || Ext.isOpera ? '#'+color : color);
                                }
                                this.up('menu').hide();
                            }
                        }]
                    })
                }
            );
        }

        if (me.enableAlignments) {
            items.push(
                '-',
                btn('justifyleft'),
                btn('justifycenter'),
                btn('justifyright')
            );
        }

        if (!Ext.isSafari2) {
            if (me.enableLinks) {
                items.push(
                    '-',
                    btn('createlink', false, me.createLink)
                );
            }

            if (me.enableLists) {
                items.push(
                    '-',
                    btn('insertorderedlist'),
                    btn('insertunorderedlist')
                );
            }
            if (me.enableSourceEdit) {
                items.push(
                    '-',
                    btn('sourceedit', true, function(){
                        //me.toggleSourceEdit(!me.sourceEditMode);
						me.execCommand('mceToggleEditor');
                    })
                );
            }
        }
        
/*        // Everything starts disabled.
        for (i = 0; i < items.length; i++) {
            if (items[i].itemId !== 'sourceedit') {
                items[i].disabled = true;
            }
        }*/

        // build the toolbar
        // Automatically rendered in Component.afterRender's renderChildren call
        return {
            xtype: 'toolbar',
			region: 'north',
            defaultButtonUI: me.defaultButtonUI,
			cls: Ext.baseCSSPrefix + 'html-editor-tb',
//            bodyCls: 'wt-theme-bg-2',
            enableOverflow: true,
            items: items,

            // stop form submits
            listeners: {
                click: function(e){
                    e.preventDefault();
                },
                element: 'el'
            }
        }; 
    },	

    disableItems: function(disabled) {
        var items = this.getToolbar().items.items,
            i,
            iLen  = items.length,
            item;

        for (i = 0; i < iLen; i++) {
            item = items[i];

            if (item.getItemId() !== 'sourceedit') {
                item.setDisabled(disabled);
            }
        }
    },
	
    // @private
    relayBtnCmd: function(btn) {
        this.execCommand(btn.getItemId());
    },	
	
	execCommand: function(cmd, ui, value, obj) {
		var ed = tinymce.get(this.tmce.getInputId());
		ed.execCommand(cmd,ui,value,obj);
	},
	
	
   //<locale>
    /**
     * @property {Object} buttonTips
     * Object collection of toolbar tooltips for the buttons in the editor. The key is the command id associated with
     * that button and the value is a valid QuickTips object. For example:
     *
     *     {
     *         bold: {
     *             title: 'Bold (Ctrl+B)',
     *             text: 'Make the selected text bold.',
     *             cls: 'x-html-editor-tip'
     *         },
     *         italic: {
     *             title: 'Italic (Ctrl+I)',
     *             text: 'Make the selected text italic.',
     *             cls: 'x-html-editor-tip'
     *         }
     *         // ...
     *     }
     */
    buttonTips: {
        bold: {
            title: 'Bold (Ctrl+B)',
            text: 'Make the selected text bold.',
            cls: Ext.baseCSSPrefix + 'html-editor-tip'
        },
        italic: {
            title: 'Italic (Ctrl+I)',
            text: 'Make the selected text italic.',
            cls: Ext.baseCSSPrefix + 'html-editor-tip'
        },
        underline: {
            title: 'Underline (Ctrl+U)',
            text: 'Underline the selected text.',
            cls: Ext.baseCSSPrefix + 'html-editor-tip'
        },
        increasefontsize: {
            title: 'Grow Text',
            text: 'Increase the font size.',
            cls: Ext.baseCSSPrefix + 'html-editor-tip'
        },
        decreasefontsize: {
            title: 'Shrink Text',
            text: 'Decrease the font size.',
            cls: Ext.baseCSSPrefix + 'html-editor-tip'
        },
        backcolor: {
            title: 'Text Highlight Color',
            text: 'Change the background color of the selected text.',
            cls: Ext.baseCSSPrefix + 'html-editor-tip'
        },
        forecolor: {
            title: 'Font Color',
            text: 'Change the color of the selected text.',
            cls: Ext.baseCSSPrefix + 'html-editor-tip'
        },
        justifyleft: {
            title: 'Align Text Left',
            text: 'Align text to the left.',
            cls: Ext.baseCSSPrefix + 'html-editor-tip'
        },
        justifycenter: {
            title: 'Center Text',
            text: 'Center text in the editor.',
            cls: Ext.baseCSSPrefix + 'html-editor-tip'
        },
        justifyright: {
            title: 'Align Text Right',
            text: 'Align text to the right.',
            cls: Ext.baseCSSPrefix + 'html-editor-tip'
        },
        insertunorderedlist: {
            title: 'Bullet List',
            text: 'Start a bulleted list.',
            cls: Ext.baseCSSPrefix + 'html-editor-tip'
        },
        insertorderedlist: {
            title: 'Numbered List',
            text: 'Start a numbered list.',
            cls: Ext.baseCSSPrefix + 'html-editor-tip'
        },
        createlink: {
            title: 'Hyperlink',
            text: 'Make the selected text a hyperlink.',
            cls: Ext.baseCSSPrefix + 'html-editor-tip'
        },
        sourceedit: {
            title: 'Source Edit',
            text: 'Switch to source editing mode.',
            cls: Ext.baseCSSPrefix + 'html-editor-tip'
        }
    }
    //</locale>	
	
	
});