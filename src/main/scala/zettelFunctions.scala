import org.apache.spark.sql.expressions.UserDefinedFunction
import org.apache.spark.sql.functions.udf

object zettelFunctions {

  /**
   * Write each zettel to a file
   */
  def zWriter: UserDefinedFunction = udf { zettel: Seq[String] =>
    new Zettel(zettel).zettelWriter().toString
  }

  /**
   * Creates tokens of all words in all the files of each directory
   */
  def zTokenizer: UserDefinedFunction = udf { sentence: Seq[String] =>
    new Zettel(sentence).zettelTokenizer().first().toString()
  }

  /**
   * Creates set of tokens to create uniqueCorpus
   */
  def zUniques: UserDefinedFunction = udf { sentence: Seq[String] =>
    new Zettel(sentence).zettelUniques().take(0).mkString(", ")
  }

  /**
   * Removes stop words from tokens
   */
  def zStopWords: UserDefinedFunction = udf { sentence: Seq[String] =>
    new Zettel(sentence).zettelStopWords().toString
  }

  /**
   * Returns n-grams of the given tokens for any given n
   */
  def zNGramer: UserDefinedFunction = udf { tokens: Seq[String] =>
    val n: Int = 2
    new Zettel(tokens).zettelNGramer(n).toString
  }

  //  /**
  //    * Returns a binarized
  //    */
  //  def zBinarizer: UserDefinedFunction = udf { zettelNumbers: Integer =>
  //    new Zettel(zettelNumbers).zettelBinarizer().toString
  //  }

  /**
   * Returns a count matrix
   */
  def zCountMatrix: UserDefinedFunction = udf { sentence: Seq[String] =>
    new Zettel(sentence).zettelCountMatrix().flatten.toString
  }

}