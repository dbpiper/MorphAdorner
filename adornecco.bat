java -Xmx2048m -Xss1m -cp "%~dp0bin\;%~dp0dist\*;%~dp0lib\*;" ^
	edu.northwestern.at.morphadorner.MorphAdorner ^
	-p ecco.properties \
	-l data/eccolexicon.lex \
	-t data/ncftransmat.mat \
	-u data/eccosuffixlexicon.lex \
	-a data/ncfmergedspellingpairs.tab \
	-a data/eccomergedspellingpairs.tab \
	-s data/standardspellings.txt \
	-w data/spellingsbywordclass.txt \
	-o "%1" ^
	"%2" "%3" "%4" "%5" "%6" "%7" "%8" "%9"
