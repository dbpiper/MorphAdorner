java -Xmx1024m -Xss1m -cp bin\;dist\*;lib\*; ^
	edu.northwestern.at.morphadorner.MorphAdorner ^
	-p ncfa.properties ^
	-l data/ncflexicon.lex ^
	-t data/ncftransmat.mat ^
	-u data/ncfsuffixlexicon.lex ^
	-a data/ncfmergedspellingpairs.tab ^
	-s data/standardspellings.txt ^
	-w data/spellingsbywordclass.txt ^
	-o %1 ^
	%2 %3 %4 %5 %6 %7 %8 %9
