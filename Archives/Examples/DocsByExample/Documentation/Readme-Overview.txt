DynaReporter: December 6, 1998.



Included are a couple frameworks, java wrapper, and test application. This is a pretty general purpose reporting tool. But it is also a multidimensional modeling tool and basically does everything that Lotus Improv did (for those few of you that remember so far back).  Improv let you slice/dice data by moving tiles around. I tried to simulate the same effect with DynaReporter with 'arrow tiles' on the presentation side and 'coordinate-based' algorithm on the grouping side.  You have 'Z' dimensions (pages), and horizontal/vertical dimensions (rows and columns).  Now at this point you might be tempted to compare DynaReporter to those really expensive 3D Modeling packages such as Oracle Express or Cognos.  The the DRGrouping.framework is NOT in that class (I can't slice/dice GB of data in memory like those guys can). However, DynaReporter is web based which is hard to come by in a nice fashion with the big boys. Furthermore a combination of DRGrouping/WRReporting on top of records pulled back using DB features like Oracle GROUP BY >can< allow you to view totalling of really large data sets.  The 'GroupingOnly' example an example of this. 



For those of you not familiar with 3D data mining...



Let's say you had a sales report where your grouped your company's sales by Region, Quarter, Year, and Product.  With DynaReporter, you might run the years along the top, Each Year column might be broken into 4 Quarter sub-columns.  Then running along the vertical axis, you might have Products for rows, and each product might be broken into more rows for each Region. But after generating the report, you can click on the vertical dimensions and look at Products by region instead of regions by products. Or you might move the Year group to the Z-axis so only Quarters appear along the top with Years showing up as 'pages' that you step through. Or you might swap axes so what was columns are now rows. Or everything as rows, or everything as columns...



Basically what you have is an 'interactive' report.  



The intersections of your dimensions can contain a single representative total, a table of all totals, or a table of all records (including the totals for those records). I also have some layout configuration controls so you can make these choices graphically (in addition to the report dimension navigation controls already mentioned). These preferences can also be made via bindings. The bindings also support keys letting you turn these controls off if you want a traditional static report with no controls at all.



You can also group attributes within a row just like in Improv (choosing to total or not to total them). And groups of attributes can live within groups of attributes.



The grouping engine and presentation codes have been separated into two frameworks (DRGrouping for grouping, WRReporting for presentation).  I wanted to do some complex things with HTML tables, so I put my own presentation on top of the engine. However, in theory, any alternative presentation could be placed on top (even a non-WOF one). Since I don't have the luxury of a slick layout tool like ReportMill, this was the best I could do: punt. Using the wonderful WOComponentContent stuff in WO4, I tried to make it possible for someone to drop their own report look into the WRReport component so they could take advantage of as much of my stuff as possible such as the Navigation controls and report layout UI if they so desired.



There are web based components for editing your 'report model' and there are two parts to a model:

	1) Attribute Definition

	2) Grouping Criteria





1) Stuff you can do with Attributes. 



Classes: DRAttribute, DRAttributeGroup 



DRAttributes are to display what EOAttributes are to the back-end.  Each DRAttribute can be defined with:

	- 'keyPath' (what you want to ask the dictionary or EO)

	- 'label' (what you want to call the attribute for display)

	- a boolean for 'shouldTotal'

	- a boolean for 'shouldSort'

	- 'format' for formatting dates

	- a toggle turning a attribute into a group. Each group can have 0 or more DRAttributes within it.



The DRAttributeList editor lets you add/remove/mod attributes as well as reorder them.





2) Stuff you can do with Grouping Criteria. 



Classes: DRMasterCriteria, DRSubMasterCriteria, DRCriteria



There are many different ways one can group records. I tried to imagine every possible way but this still probably isn't close.  



You can group records by:

	- the equality of a keyPath

	- the equality of an object's response to a method

	- the equality of a date keyPath or method using a dateFormat



The number of groups can be predefined (fixed), or allowed to dynamically grow as different values are encountered.  The default is to dynamically grow the list. For example if you had 100 movies from 15 different studios and you grouped by 'studio.name', you'd have 15 groups. But suppose you just wanted to group by 'Paramount' and 'MGM'; then you'd use the pre-defined option. Note that NO records might meet such criteria (and that's OK). If you fix the number of possible groups with the pre-defined options, a special group called 'Other' is created where any objects not meeting your predefined criteria are placed.



Criteria can be combined into 'compound criteria'. For instance a group may be defined as Movies "from Paramount AND rated R".  Each 'DRMasterCriteria' (corresponding to the combination of rules) can be configured with one or more DRSubMasterCriteria (corresponding to the concrete rules themselves) for the compound groupings.



If you use pre-defined criteria and the values you are grouping-by are numerical or dates, you can use an "in-between'ness" rule.  This groups all records that lie between your specified criteria instead of their equality to those criteria. I call this the 'useRange' pre-defined grouping type. When using 'useRange' you can also specify a boolean letting you 'groupEdges'. This automatically adds two groups covering records lying after or before the extremes in your list of possible values.



Another option for pre-defined criteria is the 'usePeriodic' grouping type. With this type, the kit will dynamically add new ranges for any records lying beyond the currently defined ranges. The range size is determined from whatever delta exists between your initial two possible values.





Installation



Make sure that: $(NEXT_ROOT)\Developer\Libraries is in your PATH. It wasn't in mine for some reason.



Do a make install on DRGrouping framework, then the WRReporting framework, then the DynaReporterJava JavaWrapper project.



Now you should be able to build the examples. There are Java and Obj-C/Script versions located in the Examples folder.



You also will most likely have to do a Switch Adaptor on the EOModel in the demo example projects.





Usage Notes



The system has been designed so you can store a DRReportModel's defining attributes/grouping-criteria as ASCII plists.  The only limitation is if you use non-numerical or non-string objects in any pre-defined lists. Those get archived as NSDatas. This does mean you can store them OK (save a model persistently) but you really can't START with just a hand-edited plist if your pre-defines are custom objects or dates.



Another alternative is to create the attributes and groups programmatically. The example program uses the plist approach. BUT it also has (commented out) the code that originally created the report model in the first place.



When you run the demo, and click the link to the report page, try rearranging your report with the navigation controls. Scroll all the way down so you can see the 'tiles' for the Vertical dimension. The Horizontal and Z Dimensions will be there but empty. Try reordering the Vertical dimensions. Then try moving them to the Horizontal list (H arrow) and/or to the Page list (Z arrow). Note: If you move at least one dimension to the Z list, you'll see better response times (far fewer table cell renderings).



After doing that, click the triangle labeled 'Expose Layout'. And then pick 'SINGLE_TOTAL' from the list and punch the 'Regenerate Report' button.  Next select 'TABLE' from the popup list and regenerate. You will have to move any Horizontal dimensions over to your Z or vertical lists for this layout to work. 



BTW, if you don't like my Starbucks color scheme, you can configure the color list with a 'colors' binding on WRReport (an array of hex encoded color strings as used in HTML, "#ff0000" is red).





Performance Notes



This is very processor intensive when you go with a complex attribute group model and use 3 or more defined Groups (Dimensions) as my test app uses. But On a 200MHz Pentium Pro, for 2 defined groups, you can expect 0.3 sec grouping times and another 0.7 sec for report generation. Not too bad. However, a triple grouping with lots of nested attribute groups can take 0.6 sec to group and another 2 to 3 sec to report gen (when showing ALL dimensions in the X-Y 'axes' - no Z pages).    





dave









