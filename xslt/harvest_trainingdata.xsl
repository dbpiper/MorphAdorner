<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:hdw="http://hdw.wustl.edu/ns/1.0"
    xmlns:tei="http://www.tei-c.org/ns/1.0"
    xmlns:ep="http://earlyprint.org/ns/1.0"
    exclude-result-prefixes="xs hdw" version="3.0">

    <xsl:strip-space elements="tei:w" />

    <!-- Run with something like:

        $ saxon -xsl:harvest_trainingdata.xsl -s:harvest_trainingdata.xsl -o:trainingdata.tab input-dir='/Users/me/TEI Files'
    -->

    <xsl:output method="text" version="1.0" encoding="UTF-8"/>
    <xsl:strip-space elements="*"/>
    <xsl:mode on-no-match="shallow-copy"/>
    
    <xsl:param name="input-dir">eebochron</xsl:param>

    <!-- Walk the input directory tree and scan each file in it. -->

    <xsl:template match="/">
        <xsl:for-each select="collection(concat($input-dir, '?recurse=yes;select=*.xml'))">
            <xsl:variable name="year" select="(//tei:xenoData/ep:epHeader/ep:creationYear/text(), //tei:xenoData/ep:epHeader/ep:publicationYear/text())[1]"/>
            <xsl:variable name="textid" select="/tei:TEI/@xml:id/data()"/>
            <xsl:apply-templates>
                <xsl:with-param name="year" select="$year"/>
            </xsl:apply-templates>
            <xsl:message select="concat(position(), '&#x09;', $textid)"></xsl:message>
        </xsl:for-each>
    </xsl:template>

    <!-- Output a tab-separated line for each tei:w that has a lemma -->
    <xsl:template match="tei:w[@lemma and @lemma != '' and @pos != 'zz']">
        <xsl:param name="year" required="true"/>
        <xsl:variable name="spelling" select="normalize-space((@orig, text())[1])"/>
        <xsl:value-of select="concat($spelling, '&#x09;', @pos, '&#x09;', @lemma, '&#x09;', @reg, '&#x09;', $year, '&#xA;')"></xsl:value-of>
    </xsl:template>
    
    <!-- Output a blank line for each sentence-ending punctuation mark -->
    <xsl:template match="tei:pc[@unit='sentence']">
        <xsl:param name="year" required="true"/>
        <xsl:value-of select="concat('&#x09;&#x09;&#x09;&#x09;', $year, '&#xA;')"></xsl:value-of>
    </xsl:template>

    <!-- Skip content not explicitly captured above. -->

    <xsl:template match="attribute() | text() | comment() | processing-instruction()"></xsl:template>

</xsl:stylesheet>
