java -Xmx2048m -Xss1m -cp "%~dp0bin\;%~dp0dist\*;%~dp0lib\*" ^
	edu.northwestern.at.morphadorner.tools.applyxslt.ApplyXSLT ^
	"%1" xslt\tei2text.xsl "%2" "%3" "%4" "%5" "%6" "%7" "%8" "%9"
