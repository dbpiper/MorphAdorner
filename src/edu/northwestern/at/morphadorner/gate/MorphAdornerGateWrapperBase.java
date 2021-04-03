package edu.northwestern.at.morphadorner.gate;

/*  Please see the license information at the end of this file. */

import gate.*;
import gate.creole.*;
import gate.util.*;
import gate.event.*;

import java.util.*;
import java.io.*;
import java.net.URL;
import java.net.MalformedURLException;
import java.text.NumberFormat;

import edu.northwestern.at.morphadorner.corpuslinguistics.adornedword.*;
import edu.northwestern.at.morphadorner.corpuslinguistics.lemmatizer.*;
import edu.northwestern.at.morphadorner.corpuslinguistics.lexicon.*;
import edu.northwestern.at.morphadorner.corpuslinguistics.namerecognizer.*;
import edu.northwestern.at.morphadorner.corpuslinguistics.partsofspeech.*;
import edu.northwestern.at.morphadorner.corpuslinguistics.postagger.*;
import edu.northwestern.at.morphadorner.corpuslinguistics.postagger.guesser.*;
import edu.northwestern.at.morphadorner.corpuslinguistics.spellingstandardizer.*;
import edu.northwestern.at.morphadorner.corpuslinguistics.tokenizer.*;

/** Base class for Gate wrappers for MorphAdorner.
*/

@SuppressWarnings("unchecked")
abstract public class MorphAdornerGateWrapperBase
    extends AbstractLanguageAnalyser
{
    public static final String
        TAG_DOCUMENT_PARAMETER_NAME = "document";

    public static final String
        TAG_INPUT_AS_PARAMETER_NAME = "inputASName";

    public static final String
        TAG_OUTPUT_AS_PARAMETER_NAME = "outputASName";

    public static final String
        BASE_TOKEN_ANNOTATION_TYPE_PARAMETER_NAME =
            "baseTokenAnnotationType";

    public static final String
        BASE_SENTENCE_ANNOTATION_TYPE_PARAMETER_NAME =
            "baseSentenceAnnotationType";

    public static final String
        TOKEN_LEMMA_FEATURE_NAME    = "lemma";

    public static final String
        TOKEN_SPELLING_FEATURE_NAME = "spelling";

    public static final String
        TOKEN_STANDARD_SPELLING_FEATURE_NAME    = "standard";

    /** Input annotation name. */

    protected String inputASName;

    /** Output annotation name. */

    protected String outputASName;

    /** Output anotation type. */

    protected String outputAnnotationType;

    /** Base annotation type for tokens. */

    protected String baseTokenAnnotationType;

    /** Base annotation type for sentences. */

    protected String baseSentenceAnnotationType;

    /** Document encoding. */

    protected String encoding;

    /** Word lexicon. */

    protected Lexicon wordLexicon;

    /** Suffix lexicon. */

    protected Lexicon suffixLexicon;

    /** Part of speech guesser. */

    protected PartOfSpeechGuesser guesser;

    /** Spelling tokenizer for lemmatization.
     */

    protected WordTokenizer spellingTokenizer   =
        new PennTreebankTokenizer();

    /** Standard spellings URL. */

    protected String spellingsURL   =
        "data/standardspellings.txt";

    /** Alternate spellings URL. */

    protected String alternateSpellingsURL  =
        "data/ncfmergedspellingpairs.tab";

    /** Part of speech tag separator. */

    protected static String tagSeparator    = "|";

    /*  Lemmata separator. */

    protected static String lemmaSeparator  = "|";

    /** Gate wrapper for MorphAdorner. */

    public MorphAdornerGateWrapperBase()
    {
    }

    /** Initialize resources. */

    public Resource init()
        throws ResourceInstantiationException
    {
        return super.init();
    }

    /** Check for required Gate resources.
     */

    public void checkGateResources()
        throws ResourceInstantiationException
    {
                                //  Make sure we have
                                //  token annotation type.

        if  (   ( baseTokenAnnotationType == null ) ||
                ( baseTokenAnnotationType.trim().length() == 0 )
            )
        {
            throw new ResourceInstantiationException(
                "No base Token Annotation Type provided!" );
        }
                                //  Make sure we have
                                //  sentence annotation type.

        if  (   ( baseSentenceAnnotationType == null ) ||
                ( baseSentenceAnnotationType.trim().length() == 0 )
            )
        {
            throw new ResourceInstantiationException(
                "No base Sentence Annotation Type provided!" );
        }
    }

    /** Perform adornment. */

    public void execute()
        throws ExecutionException
    {
        super.execute();
    }

    /** Get lexicons.
     */

    protected void getLexicons()
        throws ResourceInstantiationException
    {
        try
        {
            wordLexicon = new DefaultWordLexicon();
            suffixLexicon   = new DefaultSuffixLexicon();
        }
        catch ( Exception e )
        {
            throw new ResourceInstantiationException(
                e.getMessage() );
        }
    }

    /** Get part of speech guesser.
     *
     *  <p>
     *  Note:  Must call getLexicons() first!
     *  </p>
     */

    protected void getPartOfSpeechGuesser()
        throws ResourceInstantiationException
    {
                                //  Get part of speech guesser.

        guesser = new DefaultPartOfSpeechGuesser();

                                //  Set lexicons into guesser.

        guesser.setWordLexicon( wordLexicon );
        guesser.setSuffixLexicon( suffixLexicon );
    }

    /** Common initialization. */

    protected void commonInit()
        throws ResourceInstantiationException
    {
                                //  Check for needed Gate
                                //  resources.

        checkGateResources();

                                //  Get lexicons.
        getLexicons();
                                //  Get part of speech guesser.

        getPartOfSpeechGuesser();
    }

    public void setInputASName( String newInputASName )
    {
        inputASName = newInputASName;
    }

    public String getInputASName()
    {
        return inputASName;
    }

    public String getEncoding()
    {
        return this.encoding;
    }

    public String getBaseTokenAnnotationType()
    {
        return this.baseTokenAnnotationType;
    }

    public String getBaseSentenceAnnotationType()
    {
        return this.baseSentenceAnnotationType;
    }

    public String getOutputAnnotationType()
    {
        return this.outputAnnotationType;
    }

    public void setBaseTokenAnnotationType
    (
        String baseTokenAnnotationType
    )
    {
        this.baseTokenAnnotationType    = baseTokenAnnotationType;
    }

    public void setBaseSentenceAnnotationType
    (
        String baseSentenceAnnotationtype
    )
    {
        this.baseSentenceAnnotationType = baseSentenceAnnotationtype;
    }

    public void setEncoding( String encoding )
    {
        this.encoding   = encoding;
    }

    public void setOutputAnnotationType( String outputAnnotationType )
    {
        this.outputAnnotationType   = outputAnnotationType;
    }

    public String getOutputASName()
    {
        return this.outputASName;
    }

    public void setOutputASName( String outputASName )
    {
        this.outputASName   = outputASName;
    }

    protected void addFeatures
    (
        Annotation annot ,
        String featureName ,
        String featureValue
    )
        throws GateRuntimeException
    {
        String tempIASN =
            ( inputASName == null ) ? "" : inputASName;

        String tempOASN =
            ( outputASName == null ) ? "" : outputASName;

        if  (   outputAnnotationType.equals(
                    baseTokenAnnotationType ) &&
                tempIASN.equals( tempOASN )
            )
        {
            annot.getFeatures().put( featureName , featureValue );
            return;
        }
        else
        {
            int start   = annot.getStartNode().getOffset().intValue();
            int end     = annot.getEndNode().getOffset().intValue();

                                //  Get the annotations of type
                                //  outputAnnotationType .

            AnnotationSet outputAS =
                ( outputASName == null ) ?
                    document.getAnnotations() :
                    document.getAnnotations( outputASName );

            AnnotationSet annotations   =
                outputAS.get( outputAnnotationType );

            if ( ( annotations == null ) || ( annotations.size() == 0 ) )
            {
                                //  Add new annotation.

                FeatureMap features = Factory.newFeatureMap();
                features.put( featureName , featureValue );

                try
                {
                    outputAS.add
                    (
                        new Long( start ) ,
                        new Long( end ) ,
                        outputAnnotationType ,
                        features
                    );
                }
                catch ( Exception e )
                {
                    throw new GateRuntimeException( "Invalid Offsets" );
                }
            }
            else
            {
                                //  Search for the annotation if there is
                                //  one with the same start and
                                //  end offsets.

                ArrayList tempList  = new ArrayList( annotations.get() );

                boolean found       = false;

                for ( int i = 0 ; i < tempList.size() ; i++ )
                {
                    Annotation annotation   = (Annotation)tempList.get( i );

                    int nodeStart   =
                        annotation.getStartNode().getOffset().intValue();

                    int nodeEnd     =
                        annotation.getEndNode().getOffset().intValue();

                    if ( ( nodeStart == start ) && ( nodeEnd == end ) )
                    {
                                //  This is the one.

                        annotation.getFeatures().put
                        (
                            featureName ,
                            featureValue
                        );

                        found = true;
                        break;
                    }
                }

                if( !found )
                {
                                //  Add new annotation.

                    FeatureMap features = Factory.newFeatureMap();
                    features.put( featureName , featureValue );

                    try
                    {
                        outputAS.add
                        (
                            new Long( start ) ,
                            new Long( end ) ,
                            outputAnnotationType ,
                            features
                        );
                    }
                    catch( Exception e )
                    {
                        throw new GateRuntimeException(
                            "Invalid Offsets" );
                    }
                }
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



