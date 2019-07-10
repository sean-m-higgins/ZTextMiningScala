import java.io.{ File, PrintWriter }
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.feature.{ Binarizer, NGram, RegexTokenizer, StopWordsRemover }
import org.apache.spark.sql.{ DataFrame, Row, SparkSession }
import org.apache.spark.sql.functions._
import org.apache.spark.sql.expressions.UserDefinedFunction
import org.apache.spark.sql.types.{ StringType, StructField, StructType }
import org.apache.spark.rdd.RDD
import scala.collection.mutable.ArrayBuffer
import com.johnsnowlabs.nlp.base._
import com.johnsnowlabs.nlp.annotator._

class Zettel(zettels: Seq[String]) { //TODO should take Seq[String]

  // create spark session to have access to spark
  val spark: SparkSession = SparkSession
    .builder
    .master("local[*]")
    .appName(s"ZettelExample")
    .getOrCreate()

  // create zettel in form of dataframe to have for other methods to use
  val zettelRddDF: DataFrame = returnDFZettel()

  // create uniqueCorpus to use in all methods
  val uniqueCorpus: Seq[String] = zettelUniques()

  /**
   * final post processed zettel
   */
  def returnDFZettel(): DataFrame = {
    val rdd: RDD[Row] = spark.sparkContext.emptyRDD

    zettels.foreach { zettel: String =>
      val seqZ: Seq[String] = Seq(zettel)

      // create RDD from zettel //TODO possibly change to be all zettels or ? this may be all in one row so maybe change to be zettel per row
      val rddZettel: RDD[String] = spark.sparkContext.parallelize(seqZ)

      // convert records of the RDD to Rows
      val rowRDD: RDD[Row] = rddZettel
        .map(_.split(","))
        .map(attributes => Row(attributes(0), attributes(1).trim))

      rdd.++(rowRDD)
    }

    // create the schema... from: https://spark.apache.org/docs/2.3.0/sql-programming-guide.html#programmatically-specifying-the-schema
    val schemaString: String = "words"

    // generate the schema based on schemaString
    val fields: Array[StructField] = schemaString.split(" ")
      .map(fieldName => StructField(fieldName, StringType, nullable = true))

    val schema: StructType = StructType(fields)

    // create data frame from rdd[Row]
    val zettelRddDF: DataFrame = spark.createDataFrame(rdd, schema).toDF()

    //val newzettelRddDF: DataFrame = zettelRddDF.map(row => row.getString(0).split("""\s+""")).toDF("words")

    zettelRddDF
  }

  /**
   * Transform the given WrappedArray to a string separated by ", "
   */
  def arrayToString: UserDefinedFunction = udf {
    arr: collection.mutable.WrappedArray[String] => arr.mkString(", ")
  }

  /**
   * Retrieve sources from given directory
   * @return sources: list of files from given directory
   */
  def retrieveSources(): List[File] = {
    var sources: List[File] = List.empty
    zettels.foreach { directory =>
      val d = new File(directory)
      if (d.exists && d.isDirectory) {
        sources = d.listFiles.filter(_.isFile).toList
      } else {
        sources = List[File]()
      }
    }
    sources
  }

  /**
   * Write to a file with an incremented name for each given zettel
   * Writes the tokens without stop words version of the file
   */ //TODO change to generalize?
  def zettelWriter(): Unit = {
    var iter: Int = 0
    retrieveSources().foreach { file =>
      val lines = scala.io.Source.fromFile(file.toString).mkString
      val writer = new PrintWriter(new File("src/main/data/processedData/processedZettels/zettel" + iter + ".txt"))
      writer.write(lines)
      writer.close()
      iter += 1
    }
  }

  /**
   * Returns a data frame with the tokens of the given sentence
   * from: https://spark.apache.org/docs/latest/ml-features.html
   * from: https://spark.apache.org/docs/1.6.0/sql-programming-guide.html
   */
  def zettelTokenizer(): DataFrame = {
    val regexTokenizer = new RegexTokenizer()
      .setInputCol("words")
      .setOutputCol("tokens")
      .setPattern("(?U)[^\\p{Alpha}0-9']+")
      .setGaps(true)

    // creates user defined function to get the length of the tokens //TODO check??
    val tokenCount = udf { tokens: Seq[String] => tokens.length }

    // transforms the dataset to tokens, and adds a column for the token count
    val regexTokenized = regexTokenizer.transform(zettelRddDF)
    regexTokenized.select("words", "tokens")
      .withColumn("token length", tokenCount(col("tokens")))
      .withColumn("raw tokens", arrayToString(col("tokens")))

    regexTokenized
  }

  //  /**
  //    * creates a Seq[Seq[String]] from DataFrame
  //    */
  //  def createSeqFromDF(data: DataFrame): Seq[Seq[String]] = {
  //
  //    val newMatrix: Array[Seq[]] =
  //    newMatrix
  //  }

  /**
   * Returns a data frame with the given sentence without any stop words
   * from: https://spark.apache.org/docs/latest/ml-features.html
   */
  def zettelStopWords(): DataFrame = {
    // create a stop word remover from spark
    val remover = new StopWordsRemover()
      .setInputCol("words")
      .setOutputCol("filtered words")
    //.setStopWords(stopWords)

    import spark.implicits._
    // create a new DF containing the words without any stop words only from the column "words"
    val newDF = remover.transform(zettelRddDF.map(row => row.getString(0).split("""\s+""")).toDF("words"))
    newDF.select("words", "filtered words") //TODO change column names
      .withColumn("raw words", arrayToString(col("filtered words")))

    newDF
  }

  /**
   * Returns a data frame with the n-grams of the given tokens for any given n
   * from:  https://spark.apache.org/docs/latest/ml-features.html
   */
  def zettelNGramer(n: Int): DataFrame = {
    // create a n-gram creator from spark
    val ngram = new NGram()
      .setN(n)
      .setInputCol("words")
      .setOutputCol("ngrams")

    import spark.implicits._
    // create a new DF containing the n-grams from only the column "words"
    val nGramDF = ngram.transform(zettelRddDF.map(row => row.getString(0).split("""\s+""")).toDF("words"))
    nGramDF.select("ngrams")
      .withColumn("raw ngrams", arrayToString(col("ngrams")))

    nGramDF
  }

  /**
   * Create a sorted seq of unique words from all zettels
   */
  def zettelUniques(): Seq[String] = {
    val seqOfAllZettels: Seq[String] = Seq.empty

    zettels.foreach { zettel: String =>
      val words = zettel.split("(?U)[^\\p{Alpha}0-9']+")
      val uniques = words.toSet.toSeq.sortWith(_ < _)
      seqOfAllZettels :+ uniques
    }
    seqOfAllZettels
  }

  /**
   * Create a matrix of the count of each word of the zettel based on the unique words
   */
  def zettelCountMatrix(): Seq[Seq[Integer]] = {
    // create seq to be starting row for matrix
    val innerSeq: Seq[Integer] = Seq.fill(uniqueCorpus.size)(0)
    // create a matrix from the seq above
    val countMatrix: Seq[Seq[Integer]] = Seq(innerSeq)

    // add counts to matrix for each file in seqZ
    zettels.foreach { zettel =>
      var k = 0
      val countSeq: Seq[Integer] = getWordCount(zettel)
      countMatrix(k) ++ countSeq
      k += 1
    }
    //    // to return an RDD[RDD[Integer]]... ->
    //    val countDF: DataFrame = spark.createDataFrame(Seq((0,countMatrix2))).toDF("id","count")
    //    countDF.withColumn("raw value", arrayToString(col("count")))
    countMatrix
  }

  /**
   * Retrieves the count for each word in the zettel corresponding to the unique word corpus
   * @return
   *         //TODO do we want this to compare un-edited words or lowercase tokens?... answer pertains to method above
   */
  def getWordCount(zettel: String): Seq[Integer] = {
    val countSeq: Array[Integer] = Array.fill(uniqueCorpus.length)(0)
    val uniqueWordIter: Iterator[String] = uniqueCorpus.toIterator

    val splitZettel: Array[String] = zettel.split("(?U)[^\\p{Alpha}0-9']+")

    splitZettel.foreach { word =>
      val iter: Iterator[String] = uniqueWordIter
      var i = 0
      while (iter.hasNext) {
        if (word == iter.next) {
          countSeq(i) += 1
        }
        i += 1
      }
    }
    countSeq
  }

  //  /**
  //    * Create function to lemmatize the given tokens
  //    * from: https://nlp.johnsnowlabs.com/components.html
  //    */
  //  def zettelLemmatizer(): Unit = {
  //    val ref: String = "src/main/data/processedData/lemmas001.txt"
  //    val lemmatizer = new Lemmatizer()
  //      .setInputCols(Array("token"))
  //      .setOutputCol("lemma")
  //      .setDictionary(ref)
  //
  //    val newDF = lemmatize.
  //  }

  //  /**
  //    * Create Binarizer to set values to binary 0 or 1 depending on threshold
  //    * Binarization - the process of thresholding numerical features to binary (0/1) features
  //    * from: https://spark.apache.org/docs/latest/ml-features.html#binarizer
  //    */
  //  def zettelBinarizer(): sql.DataFrame = {
  //    //
  //    val binarizer = new Binarizer()
  //      .setInputCol("value")
  //      .setOutputCol("binarized value")
  //      .setThreshold(0.5)
  //
  //    //
  //    val binarizedDF = binarizer.transform(???) //TODO edit to have correct input
  //
  //    binarizedDF
  //
  //  }

  //spark.stop()
}
