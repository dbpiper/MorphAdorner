java -Xmx2048m -Xss1m -cp bin\;dist\*;lib\*; ^
	edu.northwestern.at.morphadorner.MorphAdorner ^
	-p emd.properties ^
	-l data/emelexicon.lex ^
	-t data/emetransmat.mat ^
	-u data/emesuffixlexicon.lex ^
	-a data/ememergedspellingpairs.tab ^
	-s data/standardspellings.txt ^
	-w data/spellingsbywordclass.txt ^
	-o "%1" ^
	"%2" "%3" "%4" "%5" "%6" "%7" "%8" "%9"
