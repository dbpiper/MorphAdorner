java -Xmx2048m -Xss1m -cp "%~dp0bin\;%~dp0dist\*;%~dp0lib\*;" ^
	edu.northwestern.at.morphadorner.MorphAdorner ^
	-p ncf.properties ^
	-l data/ncflexicon.lex ^
	-t data/ncftransmat.mat ^
	-u data/ncfsuffixlexicon.lex ^
	-a data/ncfmergedspellingpairs.tab ^
	-s data/standardspellings.txt ^
	-w data/spellingsbywordclass.txt ^
	-o "%1" ^
	"%2" "%3" "%4" "%5" "%6" "%7" "%8" "%9"
