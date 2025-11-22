package edu.northwestern.at.morphadorner.gate;

/*  Please see the license information at the end of this file. */

import edu.northwestern.at.morphadorner.corpuslinguistics.adornedword.*;
import edu.northwestern.at.morphadorner.corpuslinguistics.lemmatizer.*;
import edu.northwestern.at.morphadorner.corpuslinguistics.lexicon.*;
import edu.northwestern.at.morphadorner.corpuslinguistics.namerecognizer.*;
import edu.northwestern.at.morphadorner.corpuslinguistics.partsofspeech.*;
import edu.northwestern.at.morphadorner.corpuslinguistics.postagger.*;
import edu.northwestern.at.morphadorner.corpuslinguistics.postagger.guesser.*;
import edu.northwestern.at.morphadorner.corpuslinguistics.sentencesplitter.*;
import edu.northwestern.at.morphadorner.corpuslinguistics.spellingstandardizer.*;
import edu.northwestern.at.morphadorner.corpuslinguistics.tokenizer.*;
import edu.northwestern.at.utils.*;
import gate.*;
import gate.creole.*;
import gate.event.*;
import gate.util.*;
import java.io.*;
import java.util.*;

/** Gate wrapper for MorphAdorner's default sentence splitter and tokenizer. */
@SuppressWarnings("unchecked")
public class TokenizerGateWrapper extends MorphAdornerGateWrapperBase {
  /** Sentence splitter. */
  protected SentenceSplitter sentenceSplitter;

  /** Word tokenizer. */
  protected WordTokenizer tokenizer;

  /** Initialize resources. */
  public Resource init() throws ResourceInstantiationException {
    //  Common initialization.
    commonInit();
    //  Get default word tokenizer.

    tokenizer = new DefaultWordTokenizer();

    //  Get default sentence splitter.

    sentenceSplitter = new DefaultSentenceSplitter();

    //  Set guesser into sentence
    //  splitter.

    sentenceSplitter.setPartOfSpeechGuesser(guesser);

    //  Do standard initialization.
    return super.init();
  }

  public void execute() throws ExecutionException {
    try {
      //  Make sure we have a document
      //  to process.  Error if not.

      if (document == null) {
        throw new GateRuntimeException("No document to process!");
      }
      //  Get document text.

      String content = document.getContent().toString();

      DocumentContent docContent = document.getContent();
      long contLen = document.getContent().size();

      fireStatusChanged("Tokenizing " + document.getName());

      fireProgressChanged(0);

      //  Split document text into
      //  sentences and tokens.

      List<List<String>> sentences = sentenceSplitter.extractSentences(content, tokenizer);

      //  Get starting offsets in text
      //  for each sentence.

      int[] sentenceOffsets = sentenceSplitter.findSentenceOffsets(content, sentences);

      fireStatusChanged(
          "Extracted " + Formatters.formatIntegerWithCommas(sentences.size()) + " sentences");

      fireProgressChanged(0);

      //  Get annotation set for
      //  document so we can add
      //  sentence annotations.

      AnnotationSet inputAS =
          (inputASName == null) ? document.getAnnotations() : document.getAnnotations(inputASName);

      //  Get output annotation set.

      if ((outputASName != null) && (outputASName.length() == 0)) {
        outputASName = null;
      }

      AnnotationSet outputAS =
          (outputASName == null)
              ? document.getAnnotations()
              : document.getAnnotations(outputASName);

      //  Loop over sentences and
      //  generation sentence and
      //  token annotations for each.

      for (int sentenceNumber = 0; sentenceNumber < sentences.size(); sentenceNumber++) {
        //  Get starting and ending
        //  offsets for this sentence.

        long sentenceStart = sentenceOffsets[sentenceNumber];

        long sentenceEnd = sentenceOffsets[sentenceNumber + 1];

        //  Get sentence text.

        DocumentContent sentenceContent = docContent.getContent(sentenceStart, sentenceEnd);

        String sentenceText = sentenceContent.toString();

        //  Get tokens already extracted
        //  for this sentence text.

        List<String> sentenceTokens = sentences.get(sentenceNumber);

        //  Find starting and ending offsets
        //  of each token in sentence.

        int[] tokenOffsets = tokenizer.findWordOffsets(sentenceText, sentenceTokens);

        //  Collects token annotation spans.

        List<TokenAnnotation> annotationSpans = new ArrayList<TokenAnnotation>();

        //  Loop over tokens in sentence.

        for (int tokenNumber = 0; tokenNumber < sentenceTokens.size(); tokenNumber++) {
          //  Get starting and ending
          //  positions for each token.

          long tokenStart = tokenOffsets[tokenNumber];

          long tokenEnd = tokenStart + sentenceTokens.get(tokenNumber).length();

          //  Create token annotation entty.

          TokenAnnotation tokenAnnotation = new TokenAnnotation();

          tokenAnnotation.start = tokenStart + sentenceStart;
          tokenAnnotation.end = tokenEnd + sentenceStart;

          tokenAnnotation.string = sentenceTokens.get(tokenNumber);

          //  Add to list of annotations.

          annotationSpans.add(tokenAnnotation);
        }
        //  We now have a list of the the
        //  token annotations for this sentence.
        //  Emit each annotation to the
        //  output annotation set.

        for (TokenAnnotation span : annotationSpans) {
          FeatureMap tokenFeats = new SimpleFeatureMapImpl();

          tokenFeats.put(ANNIEConstants.TOKEN_STRING_FEATURE_NAME, span.string);

          tokenFeats.put(ANNIEConstants.TOKEN_CATEGORY_FEATURE_NAME, "");

          outputAS.add(span.start, span.end, baseTokenAnnotationType, tokenFeats);
        }
        //  Emit sentence annotation.

        if (sentenceEnd > sentenceStart) {
          FeatureMap sentFeats = new SimpleFeatureMapImpl();

          outputAS.add(sentenceStart, sentenceEnd, baseSentenceAnnotationType, sentFeats);
        }

        fireStatusChanged("Added sentence " + sentenceNumber);

        fireProgressChanged(0);
      }

    } catch (Exception e) {
      throw new ExecutionException(e);
    }
  }

  /** Hold a single token annotation. */
  class TokenAnnotation {
    /** Starting offset of annotation. */
    long start;

    /** Ending offset of annotation. */
    long end;

    /** Annotation value. */
    String string;
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
