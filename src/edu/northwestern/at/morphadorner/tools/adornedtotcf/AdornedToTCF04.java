package edu.northwestern.at.morphadorner.tools.adornedtotcf;

/*  Please see the license information at the end of this file. */

import de.tuebingen.uni.sfs.dspin.tcf04.api.*;
import de.tuebingen.uni.sfs.dspin.tcf04.data.*;
import de.tuebingen.uni.sfs.dspin.tcf04.descriptor.*;
import edu.northwestern.at.morphadorner.corpuslinguistics.sentencemelder.*;
import edu.northwestern.at.morphadorner.tools.*;
import edu.northwestern.at.utils.*;
import edu.northwestern.at.utils.xml.*;
import java.io.*;
import java.lang.reflect.*;
import java.text.*;
import java.util.*;

/**
 * Converts adorned files to TCF 0.4 format.
 *
 * <p>AdornedToTCF04 converts one or more adorned files to the Text Corpus Format (TCF) v0.4 used by
 * the CLARIN-D project.
 *
 * <p>Usage:
 *
 * <blockquote>
 *
 * <p><code>
 * adornedtotcf04 outputdirectory adorned1.xml adorned2.xml ...
 * </code>
 *
 * </blockquote>
 *
 * <p>where
 *
 * <ul>
 *   <li><strong>outputdirectory</strong> specifies the output directory to receive the TCF v0.4
 *       formatted files.
 *   <li><strong>adorned1.xml adorned2.xml ...</strong> specifies the input MorphAdorned XML files
 *       from which to produce the TCF v0.4 versions.
 * </ul>
 *
 * <p>The <a href="weblicht.sfs.uni-tuebingen.de/weblichtwiki/index.php/The_TCF_Format">Text Corpus
 * Format (TCF)</a> is used by the European CLARIN-D project to allow interchange of corpora among
 * different web-based services. TCF is an XML-based format which consists of a plain text
 * representation of a work along with a series of annotation layers.
 *
 * <p>AdornedToTCF04 converts one or more MorphAdorned TEI XML files to TCF format. The text
 * (without tags) is extracted and output, along with the following annotation layers:
 *
 * <ul>
 *   <li>Tokens (using the MorphAdorner word IDs)
 *   <li>Lemmata
 *   <li>Part of speech tags
 *   <li>Sentences
 * </ul>
 */
public class AdornedToTCF04 {
  /** Number of documents to process. */
  protected static int docsToProcess = 0;

  /** Current document. */
  protected static int currentDocNumber = 0;

  /** Input directory. */
  protected static String inputDirectory;

  /** Output directory. */
  protected static String outputDirectory;

  /** Output file stream. */
  protected static PrintStream outputFileStream;

  /** Wrapper for printStream to allow utf-8 output. */
  protected static PrintStream printStream;

  /** # params before input file specs. */
  protected static final int INITPARAMS = 1;

  /**
   * Main program.
   *
   * @param args Program parameters.
   */
  public static void main(String[] args) {
    //  Initialize.
    try {
      if (!initialize(args)) {
        System.exit(1);
      }
      //  Process all files.

      long startTime = System.currentTimeMillis();

      int filesProcessed = processFiles(args);

      long processingTime = (System.currentTimeMillis() - startTime + 999) / 1000;

      //  Terminate.

      terminate(filesProcessed, processingTime);
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
  }

  /** Initialize. */
  protected static boolean initialize(String[] args) throws Exception {
    //  Allow utf-8 output to printStream .
    printStream = new PrintStream(new BufferedOutputStream(System.out), true, "utf-8");
    //  Get the file to check for non-standard
    //  spellings.

    if (args.length < (INITPARAMS + 1)) {
      System.err.println("Not enough parameters.");
      return false;
    }
    //  Get output directory name.

    outputDirectory = args[0];

    return true;
  }

  /**
   * Process one file.
   *
   * @param xmlFileName Adorned XML file name to reformat for CWB.
   */
  protected static void processOneFile(String xmlFileName) {
    String xmlOutputFileName = "";

    try {
      //  Strip path from file name.

      String shortInputXmlFileName = FileNameUtils.stripPathName(xmlFileName);

      String strippedFileName = FileNameUtils.changeFileExtension(shortInputXmlFileName, "");

      //  Generate output file name.

      xmlOutputFileName = new File(outputDirectory, strippedFileName + ".xml").getAbsolutePath();

      //  Make sure output directory exists.

      FileUtils.createPathForFile(xmlOutputFileName);

      //  Load words from input file.

      AdornedXMLReader xmlReader = new AdornedXMLReader(xmlFileName);

      //  Get sentences.

      List<List<ExtendedAdornedWord>> sentences = xmlReader.getSentences();

      //  Open output file.

      FileOutputStream fos = new FileOutputStream(xmlOutputFileName);

      //  Set layers to write.

      TextCorpusLayerTag[] layersToWrite =
          new TextCorpusLayerTag[] {
            TextCorpusLayerTag.TEXT,
            TextCorpusLayerTag.TOKENS,
            TextCorpusLayerTag.LEMMAS,
            TextCorpusLayerTag.POSTAGS,
            TextCorpusLayerTag.SENTENCES
          };
      //  Create empty text corpus
      //  with layers to write.

      TextCorpusData tc = new TextCorpusData(fos, layersToWrite, "en");

      //  Reconstitute text from
      //  individual sentences and
      //  pick up individual tokens as well.

      List<Token> tokens = new ArrayList<Token>();
      List<Lemma> lemmata = new ArrayList<Lemma>();
      List<Tag> posTags = new ArrayList<Tag>();
      List<Sentence> tcfSentences = new ArrayList<Sentence>();

      StringBuilder sb = new StringBuilder();
      SentenceMelder melder = new SentenceMelder();
      TextCorpusFactory tcf = tc.getFactory();

      for (int i = 0; i < sentences.size(); i++) {
        List<ExtendedAdornedWord> sentence = sentences.get(i);

        String sentenceText = melder.reconstituteSentence(sentence);

        sb.append(sentenceText);
        sb.append(Env.LINE_SEPARATOR);

        List<String> tokenRefs = new ArrayList<String>();

        for (int j = 0; j < sentence.size(); j++) {
          ExtendedAdornedWord word = sentence.get(j);

          Token token = tcf.createToken(word.getSpelling());

          MyToken myToken = new MyToken(token);
          myToken.setID(word.getID());

          tokens.add(token);
          tokenRefs.add(token.getID());

          Tag tag = tcf.createTag(word.getPartsOfSpeech(), token.getID());

          posTags.add(tag);

          Lemma lemma = tcf.createLemma(word.getLemmata(), token.getID());

          lemmata.add(lemma);

          //                  outputFileStream.println( word.getStandardSpelling() );
        }

        Sentence tcfSentence = tcf.createSentence(tokenRefs);

        tcfSentences.add(tcfSentence);
      }
      //  Write reconstituted text.

      tc.writeTextLayer(sb.toString());

      //  Write tokens.

      tc.writeTokensLayer(tokens);

      //  Write postags.

      tc.writePOSTagsLayer(posTags, "NUPOS");

      //  Write lemmata.

      tc.writeLemmasLayer(lemmata);

      //  Write sentences.

      tc.writeSentencesLayer(tcfSentences);
    } catch (Exception e) {
      printStream.println(
          "Problem converting " + xmlFileName + " to " + xmlOutputFileName + ": " + e.getMessage());
    }
  }

  /** Process files. */
  protected static int processFiles(String[] args) throws Exception {
    int result = 0;
    //  Get file name/file wildcard specs.

    String[] wildCards = new String[args.length - INITPARAMS];

    for (int i = INITPARAMS; i < args.length; i++) {
      wildCards[i - INITPARAMS] = args[i];
    }
    //  Expand wildcards to list of
    //  file names,

    String[] fileNames = FileNameUtils.expandFileNameWildcards(wildCards);

    docsToProcess = fileNames.length;

    //  Process each file.

    for (int i = 0; i < fileNames.length; i++) {
      processOneFile(fileNames[i]);
    }
    //  Return # of files processed.

    return fileNames.length;
  }

  /**
   * Terminate.
   *
   * @param filesProcessed Number of files processed.
   * @param processingTime Processing time in seconds.
   */
  protected static void terminate(int filesProcessed, long processingTime) {
    printStream.println(
        "Processed "
            + Formatters.formatIntegerWithCommas(filesProcessed)
            + " files in "
            + Formatters.formatLongWithCommas(processingTime)
            + " seconds.");
  }

  public static class MyToken {
    protected Token token;

    public MyToken(Token token) {
      this.token = token;
    }

    protected LayerDescriptor getDescriptor() throws Exception {
      Field privateLayerDescriptor = Token.class.getDeclaredField("layerDescriptor");

      privateLayerDescriptor.setAccessible(true);

      return (LayerDescriptor) privateLayerDescriptor.get(token);
    }

    public void setID(String ID) {
      try {
        getDescriptor().getAttrs().remove(new AttrBase("ID", token.getID()));
        getDescriptor().getAttrs().add(new AttrBase("ID", ID));
      } catch (Exception e) {
      }
    }
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
