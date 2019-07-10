import java.io.File

import com.johnsnowlabs.nlp.annotator.{ LemmatizerModel, Stemmer }

import scala.collection.immutable.HashSet
import scala.io.Source
import scala.util.control.Breaks.{ break, breakable }

class ZettelsPreProcessor(zettels: Seq[String]) {

  /** TODO
   * Return a type of matrix from a given function keyword
   * @param function
   * @return
   */
  def preprocess(function: String): Any = function match {
    case "countMatrix" => {
      val tokens = returnSingleSeqOfTokens()
      val unique = createUniqueCorpus(tokens)
      val countMatrix = createCountMatrix(unique)
      countMatrix
    }
    case "tagMatrix" => {
      val tokens = returnSingleSeqOfTokens()
      val uniqueTagCorpus = createUniqueTagCorpus(tokens)
      val tagBooleanMatrix = createBooleanTagMatrix(uniqueTagCorpus)
      tagBooleanMatrix
    }
  }

  /** 
   * Return seq of tokens
   * @param zettel: String
   * @return
   */
  def simpleTokenizer(zettel: String): Seq[String] = {
    val tokens: Seq[String] = zettel.split("(?U)[^\\p{Alpha}0-9']+").map(_.toLowerCase())
    tokens
  }

  /**
   * Return uniqueCorpus of all zettels given the individual unique zettel
   * @param tokens: Seq[String
   * @return
   */
  def createUniqueCorpus(tokens: Seq[String]): Seq[String] = {
    var uniqueCorpus: Seq[String] = Seq()
    tokens.foreach { word: String => uniqueCorpus = uniqueCorpus :+ word }
    uniqueCorpus.distinct.sortWith(_ < _)
  }

  /**
   * Returns a uniqueCorpus of all tags in all zettels given a seq of all tokens
   */
  def createUniqueTagCorpus(tokens: Seq[String]): Seq[String] = {
    var uniqueTagCorpus: Seq[String] = Seq()
    var lock: Int = 0
    tokens.foreach { word: String =>
      if (word == "tags") {
        lock = 1
      }
      if (word == "cite") {
        lock = 0
      }
      breakable {
        while (lock == 1) {
          uniqueTagCorpus = uniqueTagCorpus :+ word
          break
        }
      }
    }
    uniqueTagCorpus.distinct.sortWith(_ < _)
  }

  /**
   * Returns a single Seq of all (Seq of tokens)
   */
  def returnSingleSeqOfTokens(): Seq[String] = {
    //Return a Seq of (Seq of tokens) for each zettel
    var seqOfTokens: Seq[Seq[String]] = Seq(Seq())
    zettels.foreach { zettel =>
      val tokens = simpleTokenizer(zettel)
      seqOfTokens = seqOfTokens :+ tokens
    }

    //Returns a single Seq of all (Seq of tokens)
    var newSingleSeq: Seq[String] = Seq()
    seqOfTokens.foreach { tokens: Seq[String] =>
      tokens.foreach(token => newSingleSeq = newSingleSeq :+ token)
    }
    newSingleSeq
  }

  /**
   *
   */
  def createBooleanTagMatrix(uniqueTags: Seq[String]): Array[Array[Boolean]] = {
    val uniqueTagCountMatrix: Array[Array[Double]] = createCountMatrix(uniqueTags)
    var booleanTagMatrix: Array[Array[Boolean]] = Array()
    uniqueTagCountMatrix.foreach { row: Array[Double] =>
      var booleanInnerSeq: Array[Boolean] = Array()
      row.foreach { count: Double =>
        if (count == 0) {
          booleanInnerSeq = booleanInnerSeq :+ false
        } else {
          booleanInnerSeq = booleanInnerSeq :+ true
        }
      }
      booleanTagMatrix = booleanTagMatrix :+ booleanInnerSeq
    }
    booleanTagMatrix
  }

  /**
   * Create a matrix of the count of each word of the zettel based on the unique words of all zettels
   */
  def createCountMatrix(uniqueCorpus: Seq[String]): Array[Array[Double]] = {
    // create a matrix from the seq above
    val countMatrix: Array[Array[Double]] = Array.fill(zettels.size) { Array() }

    var i = 0
    // add counts to matrix for each file in seqZ
    zettels.foreach { zettel => //TODO do we want this to compare un-edited words or lowercase tokens?
      val countSeq: Array[Double] = getWordCount(zettel, uniqueCorpus)
      countMatrix(i) = countSeq
      i += 1
    }
    countMatrix
  }

  /**
   * Retrieves the individual count for each word in a zettel corresponding to the unique word corpus
   * @return
   */
  def getWordCount(zettel: String, uniqCorpus: Seq[String]): Array[Double] = {
    val countSeq: Array[Double] = Array.fill(uniqCorpus.length)(0)
    val splitZettel: Array[String] = zettel.split("(?U)[^\\p{Alpha}0-9']+")

    splitZettel.foreach { word: String =>
      val iter: Iterator[String] = uniqCorpus.toIterator
      var i = 0
      while (iter.hasNext) {
        if (word == iter.next) {
          countSeq(i) = countSeq(i) + 1
        }
        i += 1
      }
    }
    countSeq
  }

  /**
   * Return seq of n-grams
   * @param tokens: Seq[String]
   * @param n: Int
   * @return
   */
  def NGram(tokens: Seq[String], n: Int): Seq[String] = {
    val nGrams = tokens.sliding(n).map(p => p.toList)
    var nGramSeq: List[String] = List()
    nGrams.foreach { nGram: List[String] =>
      val newList = nGramSeq ::: List(nGram.mkString(" "))
      nGramSeq = newList
    }
    nGramSeq
  }

  /**
   * Return the list of stop words
   * @param directory: String
   * @return
   */
  def getStopWordsHash(directory: String): HashSet[String] = {
    // create source from file of stop words
    val bufferedSource: Source = Source.fromFile(directory)
    // turn stop words into iterator
    val textWords: Iterator[String] = bufferedSource.getLines().flatMap(_.split("(?U)[^\\p{Alpha}0-9']+"))
    // turn stop words into seq
    val wordList: Seq[String] = textWords.toSeq
    // turn stop words into hashSet
    val ignoreHash: HashSet[String] = scala.collection.immutable.HashSet() ++ wordList
    bufferedSource.close()
    ignoreHash
  }

  /**
   * Filters out stop words
   * @param tokens: Seq[String]
   * @return
   */
  def stopWordsRemover(tokens: Seq[String]): Seq[String] = {
    val stopWords: HashSet[String] = getStopWordsHash("src/main/data/processedData/stopWords/sparkStopWords.txt")
    val newTokens = tokens.filter(testWord => !stopWords.contains(testWord))
    newTokens //withFilter is inPlace
  }

  /**
   * Return list of files from given directory
   * @param directory: String
   * @return
   */
  def retrieveSources(directory: String): List[File] = {
    var sources: List[File] = List()
    val d = new File(directory)
    if (d.exists && d.isDirectory) {
      sources = d.listFiles.filter(_.isFile).toList
    } else {
      sources = List[File]()
    }
    sources
  }

  /**
   * TODO
   * Create function to stem the given tokens
   * from: https://nlp.johnsnowlabs.com/components.html
   */
  def zettelStemmer(): Unit = {
    val stemmer = new Stemmer()
      .setInputCols(Array("token"))
      .setOutputCol("stem")

    //val newDF = stemmer.
  }

  /**
   * TODO
   * Create function to lemmatize the given tokens
   * from: https://nlp.johnsnowlabs.com/components.html
   */
  def zettelLemmatizer(): Unit = {
    LemmatizerModel.pretrained()

    //    val lemmatizer = new Lemmatizer()
    //      .setInputCols(Array("token"))
    //      .setOutputCol("lemma")
    //      .setDictionary("src/main/data/processedData/lemmas001.txt")

    //val newDF = lemmatizer.train()
  }

} // end of class processManyZettels