<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format"
	xmlns:java="http://xml.apache.org/xslt/java">
	<xsl:output method="xml" version="1.0" omit-xml-declaration="no"
		indent="yes" />



	<xsl:attribute-set name="border_shaded_box">
		<xsl:attribute name="margin">0</xsl:attribute>
		<xsl:attribute name="padding-before">1em</xsl:attribute>
		<xsl:attribute name="padding-after">1em</xsl:attribute>
		<xsl:attribute name="padding-start">1em</xsl:attribute>
		<xsl:attribute name="padding-end">1em</xsl:attribute>
		<xsl:attribute name="border-before-style">solid</xsl:attribute>
		<xsl:attribute name="border-after-style">solid</xsl:attribute>
		<xsl:attribute name="border-start-style">solid</xsl:attribute>
		<xsl:attribute name="border-end-style">solid</xsl:attribute>
		<xsl:attribute name="border-before-width">.1mm</xsl:attribute>
		<xsl:attribute name="border-after-width">.1mm</xsl:attribute>
		<xsl:attribute name="border-start-width">.1mm</xsl:attribute>
		<xsl:attribute name="border-end-width">.1mm</xsl:attribute>
		<xsl:attribute name="background-color">#dedede</xsl:attribute>
	</xsl:attribute-set>

	<xsl:attribute-set name="xs_paragraph">
		<xsl:attribute name="font-family">Helvetica</xsl:attribute>
		<xsl:attribute name="font-size">8pt</xsl:attribute>
		<xsl:attribute name="margin-top">2px</xsl:attribute>
		<xsl:attribute name="margin-left">4em</xsl:attribute>
		<xsl:attribute name="margin-right">.5em</xsl:attribute>
		<xsl:attribute name="margin-bottom">1em</xsl:attribute>
	</xsl:attribute-set>

	<xsl:attribute-set name="meat_paragraph">
		<xsl:attribute name="text-indent">1em</xsl:attribute>
		<xsl:attribute name="font-family">Times</xsl:attribute>
		<xsl:attribute name="font-size">8pt</xsl:attribute>
		<xsl:attribute name="margin-top">2px</xsl:attribute>
		<xsl:attribute name="margin-left">1em</xsl:attribute>
		<xsl:attribute name="margin-right">1em</xsl:attribute>
		<xsl:attribute name="margin-bottom">1em</xsl:attribute>
	</xsl:attribute-set>


	<xsl:attribute-set name="table_head">
		<xsl:attribute name="font-family">Helvetica</xsl:attribute>
		<xsl:attribute name="font-size">8pt</xsl:attribute>
		<xsl:attribute name="font-weight">bold</xsl:attribute>
		<xsl:attribute name="padding">2px</xsl:attribute>
		<xsl:attribute name="color">white</xsl:attribute>
		<xsl:attribute name="background-color">#A40311</xsl:attribute>
	</xsl:attribute-set>
	<xsl:attribute-set name="table_content">
		<xsl:attribute name="font-family">Helvetica</xsl:attribute>
		<xsl:attribute name="font-size">8pt</xsl:attribute>
		<xsl:attribute name="padding">2px</xsl:attribute>
		<xsl:attribute name="color">#A40311</xsl:attribute>
		<xsl:attribute name="border-before-style">solid</xsl:attribute>
		<xsl:attribute name="border-after-style">solid</xsl:attribute>
		<xsl:attribute name="border-start-style">solid</xsl:attribute>
		<xsl:attribute name="border-end-style">solid</xsl:attribute>
		<xsl:attribute name="border-before-width">.1mm</xsl:attribute>
		<xsl:attribute name="border-after-width">.1mm</xsl:attribute>
		<xsl:attribute name="border-start-width">.1mm</xsl:attribute>
		<xsl:attribute name="border-end-width">.1mm</xsl:attribute>
		<xsl:attribute name="background-color">#dedede</xsl:attribute>
	</xsl:attribute-set>

	<xsl:param name="header.column.widths">
		1 0 0
	</xsl:param>


	<xsl:template match="/">
		<fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">



			<fo:layout-master-set>
				<fo:simple-page-master master-name="first"
					page-height="11in" page-width="8.5in" margin-top="3in"
					margin-bottom="2in" margin-left="2in" margin-right="2in">
					<fo:region-body margin-bottom="2in" />
					<fo:region-after region-name="footer-first" extent=".65in" />
				</fo:simple-page-master>
				<fo:simple-page-master master-name="execsummary"
					page-height="11in" page-width="8.5in" margin-top=".25in"
					margin-bottom=".25in" margin-left=".25in" margin-right=".25in">
					<fo:region-body margin-bottom=".65in" margin-top=".65in" />
					<fo:region-before region-name="header-normal-exec"
						extent=".65" />
					<fo:region-after region-name="footer-normal"
						extent=".65" margin-bottom=".25in" margin-left=".25in"
						margin-right=".25in" />
				</fo:simple-page-master>
				<fo:simple-page-master master-name="normal"
					page-height="11in" page-width="8.5in" margin-top=".25in"
					margin-bottom=".25in" margin-left=".25in" margin-right=".25in">
					<fo:region-body margin-bottom=".65in" margin-top=".65in"
						column-count="2" />
					<fo:region-before region-name="header-normal"
						extent=".65" />
					<fo:region-after region-name="footer-normal"
						extent=".65" margin-bottom=".25in" margin-left=".25in"
						margin-right=".25in" />
				</fo:simple-page-master>
				<fo:page-sequence-master master-name="document">
					<fo:single-page-master-reference
						master-reference="first" />
					<fo:single-page-master-reference
						master-reference="execsummary" />
					<fo:repeatable-page-master-reference
						master-reference="normal" />

				</fo:page-sequence-master>
			</fo:layout-master-set>
			<fo:page-sequence master-reference="document">

				<fo:static-content flow-name="header-normal">
					<fo:block text-align="left" font-size="7pt" color="#676767">
						<xsl:value-of select="/dummy/title" />
					</fo:block>
				</fo:static-content>

				<fo:static-content flow-name="footer-first">
					<fo:block text-align="center" font-size="7pt" color="#676767">
						<xsl:value-of
							select="java:format(java:java.text.SimpleDateFormat.new
('MMMM d, yyyy, h:mm:ss a (zz)'), java:java.util.Date.new())" />
					</fo:block>
				</fo:static-content>
				<fo:static-content flow-name="footer-normal">
					<fo:block text-align-last="right" font-size="10pt" color="#767676">
						page
						<fo:page-number />
						of
						<fo:page-number-citation ref-id="lastBlock" />
					</fo:block>
				</fo:static-content>
				<fo:flow flow-name="xsl-region-body">
					<fo:block font-size="24pt" font-weight="bold">
						<xsl:value-of select="/dummy/title" />
					</fo:block>
					<fo:block xsl:use-attribute-sets="border_shaded_box">
						<xsl:value-of select="/dummy/subject" />
					</fo:block>
					<fo:block margin-top="4em" font-size="10pt" font-style="italic">
						<xsl:value-of select="/dummy/creator" />
					</fo:block>


					<fo:block break-before="page" />

					<xsl:apply-templates select="/dummy/executiveSummary" />

					<fo:block break-before="page" />

					<xsl:apply-templates select="/dummy/meat/section" />
					<fo:block id="lastBlock" />
				</fo:flow>
			</fo:page-sequence>
		</fo:root>



	</xsl:template>

	<xsl:template match="/dummy/executiveSummary">

		<fo:block margin="1em" font-size="18pt" font-weight="bold"
			color="gray" keep-with-next="always">
			<xsl:value-of select="heading" />
		</fo:block>
		<xsl:for-each select="content/p">
			<fo:block xsl:use-attribute-sets="xs_paragraph">
				<xsl:value-of select="." />
			</fo:block>
		</xsl:for-each>
	</xsl:template>

	<xsl:template match="/dummy/meat/section">
		<fo:block margin="1em" font-size="12pt" font-weight="bold"
			color="gray">
			<xsl:value-of select="heading" />
		</fo:block>

		<xsl:for-each select="p">
			<fo:block xsl:use-attribute-sets="meat_paragraph">
				<xsl:value-of select="." />
			</fo:block>
		</xsl:for-each>
		<xsl:apply-templates select="data" />
	</xsl:template>

	<xsl:template match="data">
		<fo:table table-layout="fixed" width="95%" margin-right=".2in"
			margin-left=".2in">
			<fo:table-column column-width="proportional-column-width(1)" />
			<fo:table-column column-width="proportional-column-width(1)" />
			<fo:table-column column-width="proportional-column-width(1)" />
			<fo:table-body>
				<fo:table-row xsl:use-attribute-sets="table_head">
					<fo:table-cell>
						<fo:block>Manny</fo:block>
					</fo:table-cell>
					<fo:table-cell>
						<fo:block>Moe</fo:block>
					</fo:table-cell>
					<fo:table-cell>
						<fo:block>Jack</fo:block>
					</fo:table-cell>
				</fo:table-row>
				<xsl:for-each select="finding">
					<fo:table-row xsl:use-attribute-sets="table_content">
						<fo:table-cell>
							<fo:block>
								<xsl:value-of select="a" />
							</fo:block>
						</fo:table-cell>
						<fo:table-cell>
							<fo:block>
								<xsl:value-of select="b" />
							</fo:block>
						</fo:table-cell>
						<fo:table-cell>
							<fo:block>
								<xsl:value-of select="c" />
							</fo:block>
						</fo:table-cell>
					</fo:table-row>

				</xsl:for-each>

			</fo:table-body>
		</fo:table>
	</xsl:template>

</xsl:stylesheet>

