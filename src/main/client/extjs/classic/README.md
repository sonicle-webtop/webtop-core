
# Theme Variables to define in *-override.css:
Heavily inspired by https://m3.material.io/styles/color/roles

## Surface

Base surface color is defined as elevation from Level 0 to Level 4.
Use surface colors for elements like cards, buttons, modals, toolbars, and other components.

--wt-theme-color-surface-l0       : Surface – Default color for backgrounds (L0)
--wt-theme-color-surface-l1       : Surface – Color for backgrounds at L1
--wt-theme-color-surface-l2       : Surface – Color for backgrounds at L2
--wt-theme-color-surface-l2-inv   : Surface – Inverted color of the above L2
--wt-theme-color-surface-l3       : Surface – Color for backgrounds at L3
--wt-theme-color-surface-l4       : Surface – Color for backgrounds at L4

--wt-theme-color-n1      : On color – Neutral primary text and icons against any base surface color
--wt-theme-color-n1-inv  : On color – Inverted color of the above primary
--wt-theme-color-n2      : On color – Neutral secondary text and icons with lower-emphasis against any base surface color
--wt-theme-color-n3      : On color – Neutral tertiary text and icons with low-emphasis against any base surface color

--wt-theme-color-1         : Primary – High-emphasis fills, texts, and icons against surface
--wt-theme-color-2         : Secondary – Less prominent fills, text, and icons against surface
--wt-theme-color-3         : Tertiary – Neutral fills, text, and icons against surface
--wt-theme-color-off       : Off - Low-attention (indicating off/disabled status) fills, texts, and icons against surface 
--wt-theme-color-hyperlink : Hyperlinks - Emphatized fills, text, and icons against surface

Use primary roles for the most prominent components across the UI, such as the FAB, high-emphasis buttons, and active states.

--wt-theme-color-1-on    : On primary – Text and icons against primary
--wt-theme-color-1-ct    : Primary container – Standout fill color against surface, for key components like FAB
--wt-theme-color-1-ct-on : On primary container – Text and icons against primary container

--wt-theme-color-2-on    : On secondary – Text and icons against secondary
--wt-theme-color-ct-2    : Secondary container – Less prominent fill color against surface, for recessive components like tonal buttons
--wt-theme-color-ct-2-on : On secondary container – Text and icons against secondary container

--wt-theme-color-ok          : Ok – Attention-grabbing color against surface for fills, icons, and text; indicating safe status
--wt-theme-color-ok-on       : On ok – Text and icons against safe status
--wt-theme-color-ok-ct       : Ok container – Attention-grabbing fill color against surface
--wt-theme-color-ok-ct-on    : On ok container – Text and icons against warning container
--wt-theme-color-warn        : Warning – Attention-grabbing color against surface for fills, icons, and text; indicating attention
--wt-theme-color-warn-on     : On warning – Text and icons against warning
--wt-theme-color-warn-ct     : Warning container – Attention-grabbing fill color against surface
--wt-theme-color-warn-ct-on  : On warning container – Text and icons against warning container
--wt-theme-color-error       : Error – Attention-grabbing color against surface for fills, icons, and text; indicating urgency
--wt-theme-color-error-on    : On error – Text and icons against error
--wt-theme-color-error-ct    : Error container – Attention-grabbing fill color against surface
--wt-theme-color-error-ct-on : On error container – Text and icons against error container
--wt-theme-color-info        : Info – Attention-grabbing color against surface for fills, icons, and text; indicating information
--wt-theme-color-info-on     : On info – Text and icons against info
--wt-theme-color-info-ct     : Info container – Attention-grabbing fill color against surface
--wt-theme-color-info-ct-on  : On info container – Text and icons against info container

--wt-theme-color-scrollbar-thumb : Color for scrollbar thumb component
--wt-theme-color-scrollbar-track : Color for scrollbar track component
--wt-theme-color-mask            : Color for overlay masks
--wt-theme-color-dialog          : Dialog - Color for dialog (body) backgrounds
--wt-theme-color-dialog-on       : On dialog - Text and icons against dialogs surface
--wt-theme-color-dialog-hd       : Dialog Header - Color for dialog header backgrounds
--wt-theme-color-dialog-hd-on    : On dialog header - Text and icons against dialog headers surface
--wt-theme-color-mbox            : Mbox - Color for message-box dialog (body) backgrounds
--wt-theme-color-mbox-on         : On mbox dialog - Text and icons against message-box dialogs surface
--wt-theme-color-mbox-hd         : Mbox header - Color for message-box dialog header backgrounds
--wt-theme-color-mbox-hd-on      : On mbox header - Text and icons against message-box dialog headers surface
--wt-theme-color-popup           : Popup - Color for popup backgrounds
--wt-theme-color-popup-on        : On popup - Text and icons against popups surface
--wt-theme-color-topbar          : Color for topbar background
--wt-theme-color-navbar          : Color for navbar background
--wt-theme-color-taskbar         : Color for taskbar background

--wt-theme-fontfamily    : Font family of texts
--wt-theme-fontsize-xs   : Font size for x-small texts
--wt-theme-fontsize-sm   : Font size for small texts
--wt-theme-fontsize-base : Font size for base texts
--wt-theme-fontsize-lg   : Font size for large texts
--wt-theme-fontsize-xl   : Font size for x-large texts
--wt-theme-fontsize-2xl  : Font size for 2x-large texts
--wt-theme-fontsize-3xl  : Font size for 3x-large texts
--wt-theme-fontsize-4xl  : Font size for 4x-large texts
--wt-theme-fontsize-5xl  : Font size for 5x-large texts
--wt-theme-fontsize-6xl  : Font size for 6x-large texts
--wt-theme-fontweight-regular : Font weight for regular texts
--wt-theme-fontweight-medium  : Font weight for medium texts
--wt-theme-fontweight-bold    : Font weight for bold texts
--wt-theme-lineheight-xs   : Line height for x-small texts
--wt-theme-lineheight-sm   : Line height for small texts
--wt-theme-lineheight-base : Line height for base texts
--wt-theme-lineheight-lg   : Line height for large texts
--wt-theme-lineheight-xl   : Line height for x-large texts
--wt-theme-lineheight-2xl  : Line height for 2x-large texts
--wt-theme-lineheight-3xl  : Line height for 3x-large texts
--wt-theme-lineheight-4xl  : Line height for 4x-large texts
--wt-theme-lineheight-5xl  : Line height for 5x-large texts
--wt-theme-lineheight-6xl  : Line height for 6x-large texts

--wt-theme-dialog-radius : Radius for dialog corners
--wt-theme-menu-radius   : Radius for menu corners
--wt-theme-field-radius  : Radius for field corners
--wt-theme-grid-radius   : Radius for grid corners