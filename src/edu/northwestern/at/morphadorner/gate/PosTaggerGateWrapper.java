package edu.northwestern.at.morphadorner.gate;

/*  Please see the license information at the end of this file. */

import edu.northwestern.at.morphadorner.corpuslinguistics.adornedword.*;
import edu.northwestern.at.morphadorner.corpuslinguistics.lemmatizer.*;
import edu.northwestern.at.morphadorner.corpuslinguistics.lexicon.*;
import edu.northwestern.at.morphadorner.corpuslinguistics.partsofspeech.*;
import edu.northwestern.at.morphadorner.corpuslinguistics.postagger.*;
import edu.northwestern.at.morphadorner.corpuslinguistics.spellingstandardizer.*;
import edu.northwestern.at.morphadorner.corpuslinguistics.tokenizer.*;
import edu.northwestern.at.utils.*;
import gate.*;
import gate.creole.*;
import gate.event.*;
import gate.util.*;
import java.io.*;
import java.text.NumberFormat;
import java.util.*;

/** Gate wrapper for default MorphAdorner morphological adorner. */
@SuppressWarnings("unchecked")
public class PosTaggerGateWrapper extends MorphAdornerGateWrapperBase {
  /** Part of speech tagger. */
  protected PartOfSpeechTagger partOfSpeechTagger = null;

  /** Part of speech tags used by tagger. */
  protected PartOfSpeechTags partOfSpeechTags = null;

  /** Lemmatizer. */
  protected Lemmatizer lemmatizer = null;

  /** Spelling standardizer. */
  protected SpellingStandardizer standardizer = null;

  /** Spelling tokenizer for lemmatization. */
  protected WordTokenizer spellingTokenizer = new PennTreebankTokenizer();

  /** Gate wrapper for MorphAdorner. */
  public PosTaggerGateWrapper() {}

  /** Initialize resources. */
  public Resource init() throws ResourceInstantiationException {
    //  Perform common initialization.
    commonInit();

    try {
      //  Get default part of speech tagger.

      partOfSpeechTagger = new DefaultPartOfSpeechTagger();

      //  Get default lemmatizer.

      lemmatizer = new DefaultLemmatizer();

      //  Get spelling standardizer.

      standardizer = new ExtendedSimpleSpellingStandardizer();

      //  Load standard spellings.

      standardizer.loadStandardSpellings(new File(spellingsURL).toURI().toURL(), "utf-8");
      //  Load alternate spellings.

      standardizer.loadAlternativeSpellings(
          new File(alternateSpellingsURL).toURI().toURL(), "utf-8", "\t");
      //  Get part of speech tags.

      partOfSpeechTags = wordLexicon.getPartOfSpeechTags();
    } catch (Exception e) {
      throw new ResourceInstantiationException(e.getMessage());
    }

    return super.init();
  }

  /** Perform adornment. */
  public void execute() throws ExecutionException {
    if (document == null) {
      throw new GateRuntimeException("There is no document to process.");
    }

    if ((inputASName != null) && (inputASName.length() == 0)) {
      inputASName = null;
    }

    if ((baseSentenceAnnotationType == null) || (baseSentenceAnnotationType.trim().length() == 0)) {
      throw new GateRuntimeException("No base Sentence Annotation Type provided.");
    }

    AnnotationSet inputAS =
        (inputASName == null) ? document.getAnnotations() : document.getAnnotations(inputASName);

    if ((outputASName != null) && (outputASName.length() == 0)) {
      outputASName = null;
    }

    AnnotationSet outputAS =
        (outputASName == null) ? document.getAnnotations() : document.getAnnotations(outputASName);

    try {
      document
          .getFeatures()
          .put("Number of tokens", new Integer(inputAS.get("Token").size()).toString());
    } catch (NullPointerException e) {
      throw new ExecutionException("You need to run a Tokenizer first!");
    }

    try {
      document
          .getFeatures()
          .put("Number of sentences", new Integer(inputAS.get("Sentence").size()).toString());

    } catch (NullPointerException e) {
      throw new ExecutionException("You need to run a Sentence Splitter first.");
    }

    AnnotationSet sentencesAS = inputAS.get(SENTENCE_ANNOTATION_TYPE);

    List sentenceForTagger = new ArrayList();

    Comparator offsetComparator = new OffsetComparator();

    List sentencesList = new ArrayList(sentencesAS);

    Collections.sort(sentencesList, offsetComparator);

    List tokensList = new ArrayList(inputAS.get(TOKEN_ANNOTATION_TYPE));

    Collections.sort(tokensList, offsetComparator);

    Iterator sentencesIter = sentencesList.iterator();

    ListIterator tokensIter = tokensList.listIterator();

    List tokensInCurrentSentence = new ArrayList();

    Annotation currentToken = (Annotation) tokensIter.next();

    int sentIndex = 0;

    int sentCnt = sentencesAS.size();

    fireStatusChanged("Adorning " + sentCnt + " sentences in " + document.getName());

    fireProgressChanged(0);

    long startTime = System.currentTimeMillis();

    while (sentencesIter.hasNext()) {
      Annotation currentSentence = (Annotation) sentencesIter.next();

      tokensInCurrentSentence.clear();

      sentenceForTagger.clear();

      while ((currentToken != null)
          && (currentToken
                  .getEndNode()
                  .getOffset()
                  .compareTo(currentSentence.getEndNode().getOffset())
              <= 0)) {
        tokensInCurrentSentence.add(currentToken);

        sentenceForTagger.add(currentToken.getFeatures().get(TOKEN_STRING_FEATURE_NAME));

        currentToken = (Annotation) (tokensIter.hasNext() ? tokensIter.next() : null);
      }

      List taggerResults = partOfSpeechTagger.tagSentence(sentenceForTagger);

      Iterator resIter = taggerResults.iterator();

      Iterator tokIter = tokensInCurrentSentence.iterator();

      while (resIter.hasNext()) {
        AdornedWord word = (AdornedWord) resIter.next();
        Annotation annotation = (Annotation) tokIter.next();
        /*
                        addFeatures
                        (
                            annotation ,
                            TOKEN_CATEGORY_FEATURE_NAME,
                            word.getPartsOfSpeech()
                        );
        */
        String partOfSpeechTag = word.getPartsOfSpeech();
        String correctedSpelling = word.getSpelling();

        //  Get standardized spelling.

        String standardizedSpelling = getStandardizedSpelling(correctedSpelling, partOfSpeechTag);
        //  See if lexicon contains lemma.

        String lemma = wordLexicon.getLemma(correctedSpelling, partOfSpeechTag);
        //  Lexicon does not contain lemma.
        //  Use lemmatizer.

        if (lemma.equals("*")) {
          lemma = getLemma(lemmatizer, standardizedSpelling, partOfSpeechTag);
        }

        annotation.getFeatures().put(TOKEN_CATEGORY_FEATURE_NAME, partOfSpeechTag);

        annotation.getFeatures().put(TOKEN_LEMMA_FEATURE_NAME, lemma);

        annotation.getFeatures().put(TOKEN_SPELLING_FEATURE_NAME, correctedSpelling);

        annotation.getFeatures().put(TOKEN_STANDARD_SPELLING_FEATURE_NAME, standardizedSpelling);
      }

      fireProcessFinished();

      fireStatusChanged(
          document.getName()
              + " adorned in "
              + NumberFormat.getInstance()
                  .format((double) (System.currentTimeMillis() - startTime) / 1000)
              + " seconds.");
    }

    fireStatusChanged("Adornment complete.");

    fireProgressChanged(0);
  }

  /**
   * Get lemma (possibly compound) for a spelling.
   *
   * @param lemmatizer The lemmatizer.
   * @param spelling The spelling.
   * @param partOfSpeech The part of speech tag.
   * @return Lemma for spelling. May contain compound spelling in form "lemma1:lemma2:...".
   */
  protected String getLemma(Lemmatizer lemmatizer, String spelling, String partOfSpeech) {
    String lemmata = spelling;

    //  Get lemmatization word class
    //  for part of speech.
    String lemmaClass = partOfSpeechTags.getLemmaWordClass(partOfSpeech);

    //  Do not lemmatize words which
    //  should not be lemmatized,
    //  including proper names.

    if (lemmatizer.cantLemmatize(spelling) || lemmaClass.equals("none")) {
    } else {
      //  Extract individual word parts.
      //  May be more than one for a
      //  contraction.

      List wordList = spellingTokenizer.extractWords(spelling);

      //  If just one word part,
      //  get its lemma.

      if (!partOfSpeechTags.isCompoundTag(partOfSpeech) || (wordList.size() == 1)) {
        if (lemmaClass.length() == 0) {
          lemmata = lemmatizer.lemmatize(spelling, "compound");

          if (lemmata.equals(spelling)) {
            lemmata = lemmatizer.lemmatize(spelling);
          }
        } else {
          lemmata = lemmatizer.lemmatize(spelling, lemmaClass);
        }
      }
      //  More than one word part.
      //  Get lemma for each part and
      //  concatenate them with the
      //  lemma separator to form a
      //  compound lemma.
      else {
        lemmata = "";
        String lemmaPiece = "";
        String[] posTags = partOfSpeechTags.splitTag(partOfSpeech);

        if (posTags.length == wordList.size()) {
          for (int i = 0; i < wordList.size(); i++) {
            String wordPiece = (String) wordList.get(i);

            if (i > 0) {
              lemmata = lemmata + lemmaSeparator;
            }

            lemmaClass = partOfSpeechTags.getLemmaWordClass(posTags[i]);

            lemmaPiece = lemmatizer.lemmatize(wordPiece, lemmaClass);

            lemmata = lemmata + lemmaPiece;
          }
        }
      }
    }
    //  Use spelling if lemmata not defined.

    if (lemmata.equals("*")) {
      lemmata = spelling;
    }
    //  Force lemma to lowercase except
    //  for proper noun tagged word.

    if (lemmata.indexOf(lemmaSeparator) < 0) {
      if (!partOfSpeechTags.isProperNounTag(partOfSpeech)) {
        lemmata = lemmata.toLowerCase();
      }
    }

    return lemmata;
  }

  /**
   * Get standardized spelling.
   *
   * @param spelling The spelling.
   * @param partOfSpeech The part of speech tag.
   * @return Standardized spelling.
   */
  protected String getStandardizedSpelling(String spelling, String partOfSpeech) {
    String result = spelling;

    if (partOfSpeechTags.isProperNounTag(partOfSpeech)) {
    } else if (partOfSpeechTags.isNounTag(partOfSpeech) && CharUtils.hasInternalCaps(spelling)) {
    } else if (partOfSpeechTags.isForeignWordTag(partOfSpeech)) {
    } else if (partOfSpeechTags.isNumberTag(partOfSpeech)) {
    } else {
      result =
          standardizer.standardizeSpelling(
              spelling, partOfSpeechTags.getMajorWordClass(partOfSpeech));
    }

    return result;
  }
}

/*
Copyright (c) 2008, 2013 by Northwestern University.
All rights reserved.

Developed by:
   Academic and Research Technologies
   Northwestern University
   http://www.it.northwestern.edu/about/departments/at/

Permission is hereby granted, free of charge, to any person
obtaining a copy of this software and associated documentation
files (the "Software"), to deal with the Software without
restriction, including without limitation the rights to use,
copy, modify, merge, publish, distribute, sublicense, and/or
sell copies of the Software, and to permit persons to whom the
Software is furnished to do so, subject to the following
conditions:

    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimers.

    * Redistributions in binary form must reproduce the above
      copyright notice, this list of conditions and the following
      disclaimers in the documentation and/or other materials provided
      with the distribution.

    * Neither the names of Academic and Research Technologies,
      Northwestern University, nor the names of its contributors may be
      used to endorse or promote products derived from this Software
      without specific prior written permission.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE CONTRIBUTORS OR
COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
*/
