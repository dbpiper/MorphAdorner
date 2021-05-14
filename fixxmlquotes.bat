java -Xmx2048m -cp "%~dp0bin\;%~dp0dist\*;%~dp0lib\*;" ^
	edu.northwestern.at.morphadorner.tools.fixquotes.FixXMLQuotes ^
	data\softtags.txt data\jumptags.txt ^
	"%1" "%2" "%3" "%4" "%5" "%6" "%7" "%8" "%9"
