#!/bin/sh

cd xslt

./harvest_trainingdata.sh trainingdata_by_year.tab "$1"

perl -F'\t' -anE \
	'next if $F[0] =~ m/^\S+\s+\S+$/; print ((length($F[0]) == 0 ? "" : (join qq/\t/, @F[0,1,2,3])) . qq/\n/) if $F[4] < 1570;' \
	< trainingdata_by_year.tab \
	> trainingdata_before_1570.tab

perl -F'\t' -anE \
	'next if $F[0] =~ m/^\S+\s+\S+$/; print ((length($F[0]) == 0 ? "" : (join qq/\t/, @F[0,1,2,3])) . qq/\n/) if $F[4] >= 1570 && $F[4] < 1640;' \
	< trainingdata_by_year.tab \
	> trainingdata_1570_to_1639.tab

perl -F'\t' -anE \
	'next if $F[0] =~ m/^\S+\s+\S+$/; print ((length($F[0]) == 0 ? "" : (join qq/\t/, @F[0,1,2,3])) . qq/\n/) if $F[4] >= 1640;' \
	< trainingdata_by_year.tab \
	> trainingdata_1640_and_after.tab

perl -F'\t' -anE \
	'next if $F[0] =~ m/^\S+\s+\S+$/; print ((length($F[0]) == 0 ? "" : (join qq/\t/, @F[0,1,2,3])) . qq/\n/);' \
	< trainingdata_by_year.tab \
	> emetrainingdata.tab
	
cd ..

./createlexicon xslt/trainingdata_before_1570.tab \
	data/wordlexiconeme_before_1570.lex \
	data/suffixlexiconeme_before_1570.lex
	
./createlexicon xslt/trainingdata_1570_to_1639.tab \
	data/wordlexiconeme_1570_to_1639.lex \
	data/suffixlexiconeme_1570_to_1639.lex


./createlexicon xslt/trainingdata_1640_and_after.tab \
	data/wordlexiconeme_1640_and_after.lex \
	data/suffixlexiconeme_1640_and_after.lex

./createlexicon xslt/emetrainingdata.tab \
	data/emelexicon.lex \
	data/emesuffixlexicon.lex

./ngramtaggertrainer xslt/trainingdata_before_1570.tab \
 data/wordlexiconeme_before_1570.lex \
 data/emetransmat_before_1570.mat
 
./ngramtaggertrainer xslt/trainingdata_1570_to_1639.tab \
 data/wordlexiconeme_1570_to_1639.lex \
 data/emetransmat_1570_to_1639.mat
 
./ngramtaggertrainer xslt/trainingdata_1640_and_after.tab \
 data/wordlexiconeme_1640_and_after.lex \
 data/emetransmat_1640_and_after.mat

./ngramtaggertrainer xslt/emetrainingdata.tab \
 data/emelexicon.lex \
 data/emetransmat.mat
