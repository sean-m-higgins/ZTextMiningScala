import java.io.{ File, FileNotFoundException, IOException }

object datamining extends App {
  val baseball = "src/main/data/zettels/baseball"
  val bibs = "src/main/data/zettels/bibs"
  val examples = "src/main/data/zettels/examples"
  val rheingold = "src/main/data/zettels/rheingold-examples"
  val zStopWords = "src/main/data/processedData/stopWords/zettelStopWords.txt"
  val sStopWords = "src/main/data/processedData/stopWords/sparkStopWords.txt"
  private val sentence1: String = "This is a test sentence for data mining zettels zettels zettels."
  private val sentence2: String = "This is a new test sentence."
  private val sentence3: String = "This is the last test sentence."
  val document = Seq(sentence1, sentence2, sentence3)

  val zettels = getZettelsFromDirectory(baseball)

  val process = new ZettelsPreProcessor(zettels)

  val singleZettelTokens = process.simpleTokenizer(sentence1)
  val seqOfTokens = process.returnSingleSeqOfTokens()
  val uniqueCorpus = process.createUniqueCorpus(seqOfTokens)
  val countMatrix = process.createCountMatrix(uniqueCorpus)
  val nGrams = process.NGram(seqOfTokens, 2)
  val stopWords = process.stopWordsRemover(seqOfTokens)
  val uniqueTagCorpus = process.createUniqueTagCorpus(seqOfTokens)
  val tagCountMatrix = process.createCountMatrix(uniqueTagCorpus)
  val tagBooleanMatrix = process.createBooleanTagMatrix(uniqueTagCorpus)

  // returns a seq of zettels from a given directory
  def getZettelsFromDirectory(directory: String): Seq[String] = {
    var zettelSeq: Seq[String] = Seq()
    try {
      val newProcess = new ZettelsPreProcessor(zettelSeq)
      newProcess.retrieveSources(directory).foreach { file: File =>
        val text: String = scala.io.Source.fromFile(file)
          .mkString
        zettelSeq = zettelSeq ++ Seq(text)
      }
    } catch {
      case e: FileNotFoundException => println("Couldn't find that file.")
      case e: IOException => println("Got an IOException!")
    }
    zettelSeq
  }

} // end of object Datamining

//#1 Convert source to List of String
//#2 Make list of words unique
//#3 Make 1&2 a function
//#4 Create class to process all zettels
//#5 Form unique word corpus
//TODO #6 Apply hierarchical clustering methods agglomerative, ... (spark)kmeans
// TODO Dendrogram?
//#7 create matrix of word counts of the file's words in uniqueCorpus
//#8 tokenize corpus
//TODO #9 stop words
//TODO #10 lemmatize/stem
//#11 function to write to text file
//#12 LDA
//#13 function to create n-grams
//#14 function to binarize?
//#15 bag of words...(uniqueCorpus)
//#16 test Suite
//#17 distances  ... bootstrap? ... spearmans rank?... others? TODO (spark)TF-IDF
//#18 part of speech??
//#19 uniqueTagCorpus
//TODO #20 predicted tag counts
//TODO #21 visual graphs, breeze
//TODO Tag/Autotag