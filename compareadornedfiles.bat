java -Xmx2048m -Xss1m -cp "%~dp0bin\;%~dp0dist\*;%~dp0lib\*;" ^
	edu.northwestern.at.morphadorner.tools.compareadornedfiles.CompareAdornedFiles ^
	"%1" "%2" "%3"
