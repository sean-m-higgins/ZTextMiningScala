//import scala.reflect.runtime.universe.TypeTag
//import org.apache.spark.sql.expressions.UserDefinedFunction
//import org.apache.spark.sql.functions._
import org.scalatest.Matchers._

class zettelTestSuite extends zettelSparkFunSuite {

  private val sentence1: String = "This is a test sentence for data mining zettels zettels zettels."
  private val sentence2: String = "To see the full affect of the functions, this is another test sentence."
  private val newTags: String = "tags: zettel, sentence, test, cite:..."
  private val document: Seq[String] = Seq(sentence1, sentence2, newTags)

  val processMany = new ZettelsPreProcessor(document)
  val singleTokens: Seq[String] = processMany.simpleTokenizer(sentence1)
  val tokens: Seq[String] = processMany.returnSingleSeqOfTokens()
  val uniques: Seq[String] = processMany.createUniqueCorpus(tokens)
  val stopWords: Seq[String] = processMany.stopWordsRemover(singleTokens)
  val countMatrix: Array[Array[Double]] = processMany.createCountMatrix(uniques)
  val uniqueTags: Seq[String] = processMany.createUniqueTagCorpus(tokens)
  val uniqueBooleanMatrix: Array[Array[Boolean]] = processMany.createBooleanTagMatrix(uniqueTags)

  val distance = new Distance()

  val exampleDistances = Array(1.0, 2.0, 3.0, 4.0)
  val distanceMatrix = distance.createDistanceMatrix(exampleDistances)

  private def testFunction(input: Any, expected: Any): Unit = {
    input should equal (expected)
  }

  test("zTokenizer") {
    val expected: Seq[String] = Seq("this", "is", "a", "test", "sentence", "for", "data", "mining", "zettels", "zettels", "zettels")
    testFunction(singleTokens, expected)
  }

  test("zUniques") {
    val expected: Seq[String] = Seq("a", "affect", "another", "cite", "data", "for", "full", "functions", "is", "mining", "of", "see", "sentence", "tags", "test", "the", "this", "to", "zettel", "zettels")
    testFunction(uniques, expected)
  }

  test("zStopWords") {
    val expected: Seq[String] = Seq("test", "sentence", "data", "mining", "zettels", "zettels", "zettels")
    testFunction(stopWords, expected)
  }

  test("zCountMatrix") {
      val expected: Array[Array[Double]] = Array(
        Array(1.0, 0.0, 0.0, 0.0, 1.0, 1.0, 0.0, 0.0, 1.0, 1.0, 0.0, 0.0, 1.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 3.0),
        Array(0.0, 1.0, 1.0, 0.0, 0.0, 0.0, 1.0, 1.0, 1.0, 0.0, 1.0, 1.0, 1.0, 0.0, 1.0, 2.0, 1.0, 0.0, 0.0, 0.0),
        Array(0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0, 1.0, 0.0, 0.0, 0.0, 1.0, 0.0))
      testFunction(countMatrix, expected)
  }

  test("zNGramer") {
    val expected: Seq[String] = Seq("test sentence", "sentence data", "data mining", "mining zettels", "zettels zettels", "zettels zettels")
    val nGrams = processMany.NGram(processMany.stopWordsRemover(processMany.simpleTokenizer(sentence1)), 2)
    testFunction(nGrams, expected)
  }

  test("zUniqueTags") {
    val expected: Seq[String] = Seq("sentence", "tags", "test", "zettel")
    testFunction(uniqueTags, expected)
  }

  test("zUniqueTagBooleanMatrix") {
    val expected: Array[Array[Boolean]] = Array(Array(true, false, true, false), Array(true, false, true, false),
      Array(true, true, true, true))
    testFunction(uniqueBooleanMatrix, expected)
  }

  test("Jaccard") {
    val expected: Array[Double] = Array(0.0, 0.4, 0.35)
    val distances = distance.calculateDistances(countMatrix, 0)
    testFunction(distances, expected)
  }

  test("Cosine") {
    val expected: Array[Double] = Array(0.0, 0.2004459314343183, 0.23904572186687872)
    val distances = distance.calculateDistances(countMatrix, 1)
    testFunction(distances, expected)
  }

  test("Euclidean") {
    val expected: Array[Double] = Array(0.0, 4.898979485566356, 3.872983346207417)
    val distances = distance.calculateDistances(countMatrix, 2)
    testFunction(distances, expected)
  }

  test("Manhattan") {
    val expected: Array[Double] = Array(0.0, 16.0, 13.0)
    val distances = distance.calculateDistances(countMatrix, 3)
    testFunction(distances, expected)
  }

  test("zDistanceMatrix") {
    val expected: Array[Array[Double]] = Array(
      Array(1.0, 2.0, 3.0),
      Array(3.0, 1.0, 2.0),
      Array(2.0, 3.0, 1.0))
    testFunction(distanceMatrix, expected)
  }

} // end of zettelTestSuite

//
//  OLD TESTS SUPPORTING SPARK BELOW
//
//
/*
  private def testFunction[T: TypeTag](function: UserDefinedFunction, input: Seq[String], expected: Any): Unit ={
    val seqZ: Seq[String] = Seq.empty
    input.foreach { zettel: String =>
      seqZ :+ zettel
    }
    // create data frame from the given inputm
    val df  = spark.createDataFrame(Seq((0,seqZ))).toDF("id","value")
    // get only the input from the data frame
    val actual = df.select(function(col("value"))).first().get(0)
    // check equality between the input and the expected
    assert(actual == expected)
  }


  test("zTokenizer") {
    val expected: String = "to, see, the, full, affect, of, the, functions, this, is, another, test, sentence"
    testFunction(zettelFunctions.zTokenizer, document, expected)
  }


  test("zUniques") {
    val expected: String = "This, a, data, for, is, mining, sentence, test, zettels"
    testFunction(zettelFunctions.zUniques, document, expected)
  }


  test("zStopWords") {
    val expected: String = "see, full, affect, functions, another, test, sentence"
    testFunction(zettelFunctions.zStopWords, document, expected)
  }


  test("zNGramer") {
    val expected: String = "test sentence, sentence data, data mining, mining zettels"
    testFunction(zettelFunctions.zNGramer, document, expected)
  }


  test("zCountMatrix") {
    val expected: String = "111111113"
    testFunction(zettelFunctions.zCountMatrix, document, expected)
  }
*/

