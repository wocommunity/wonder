See: http://wiki.objectstyle.org/confluence/display/WO/ERDivaLook

Prerequisites
=============

1. WO 5.4.1+
2. Eclipse 3.4.1/3.5


Installation
============

1. Import and build the two frameworks WO2 and ERDivaLook and the example ERDivaLookExample in Eclipse.
2. Run ERDivaLookExample (Choose Application.java from the WO Explorer, control/right-click and select Run As...WOApplication)

Troubleshooting
===============

1. If you don't see any images check to see that your web server is running and that JavaDirectToWeb.framework has been installed


Features (Demo-ed)
========

1. Clicking on "Toggle Skin" will toggle between the two included CSS skins
2. Vertical bar is a collapsible with Scriptaculous slide effect
3. Entities list are now hyperlinks with CSS effects to reveal Search/New functions on hover
4. List page - delete action use Ajax+Scriptaculous fade effect
5. EditRelationships use Ajax+Scriptaculous fade effect to remove items
6. Inspect/EditCustomer page demos Ajax tabs
7. Inspect/EditCustomer -> Contact tab reveals Scriptaculous accordion
8. Ajax date picker for query date range and edit date
9. EditRelationships add... button features ajax light window to choose object.
10. Opt-in for create eos on EditRelationshipPage (e.g: Edit Movie -> directors). Set the d2wRule "readOnly" to false for an entity to use this.
11. Use of CSS image replacement + dynamic D2W rule in place of custom components - even D2WDisplayBoolean can be deprecated. e.g: Talent List (isDirector).
12. Use of CSS to display batch navigation on top and/or bottom as demoed in WebObjects/Neutral look.
13. "Detail" Customer list page (or use of embedded inspect page on D2W list).
14. Embedded lists (via ERDEditableList). See Studio movies on Inspect/Edit pages. (Neutral look only).
15. Google-style "quick Search" feature on Movies List page. (Neutral look only). Turn on via D2w key allowsFiltering.
16. Modal confirmation panel (ajax) on Rental edit page (on save).
17. Property level ajax updates set via the d2wRule "isAjax" for some components. See Edit Movie.studio for a (contrived) example.
18. Ajax exception page (to use the ERDivaLook default exception page, you need to order ERDivaLook framework before JavaWOExtensions).
19. Dynamic D2W. See Edit Studio. Add movie.

Features (Not demo-ed)
========

1. Support for ERAttachments and ajax file uploads
2. Global "loading..." that may be styled as a busy spinner (for ajax updates).
3. Error page/alert panel.
4. Excel/report function added to list page. Hidden via CSS.
5. Old school tooltips set via D2W key "title".
6. Wizard (Modal) EO creation
7. allowsCollapsing of embedded lists (ERDEditableList)
8. Dynamic D2W toOne relationship filter components (query + edit)

Notes on XHTML, CSS and D2W
===========================

1. All markup has been stripped of images, tables (unless for tabular data), fonts, colors and any other presentation. 
This is often a pre-requisite for unobtrusive Javascript libraries (like the accordion, lightwindow, etc)
2. All images are presented using CSS image replacement
3. D2W rules are (mostly) stripped of presentation rules like colour, number of row, width, height, etc.
The presentation related rules (including the ones for stylesheet) are in the user.d2wmodel

Compatible browsers (ERDivaLookExample)
===================

CSS is universal for the following browsers:
Safari4, FireFox3+, Chrome, IE8 (IE7 forthcoming)