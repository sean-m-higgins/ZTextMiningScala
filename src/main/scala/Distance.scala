import Math._

class Distance {

  /**
   * Create Symmetric Distance Matrix
   */
  def createDistanceMatrix(distances: Array[Double]): Array[Array[Double]] = {
    val distanceMatrix: Array[Array[Double]] = Array.fill(distances.length - 1) { Array() }
    var newDistances = distances
    var i = 0
    distanceMatrix.foreach { row =>
      var j = 0
      for (a <- 1 until distances.length) {
        distanceMatrix(i) = distanceMatrix(i) :+ newDistances(j)
        j = j + 1
      }
      newDistances = newDistances.slice(distances.length - 2, distances.length - 1) ++ newDistances.slice(0, distances.length - 2)
      i = i + 1
    }
    distanceMatrix
  }

  /**
   * Calculate Distance of a given matrix
   * @param matrix:
   * @param n: corresponds to the type of distance formula
   * @return
   */
  def calculateDistances(matrix: Array[Array[Double]], n: Int): Array[Double] = {
    var k = 0
    var i = 1
    var seqOfDistances: Array[Double] = Array(0)
    for (a <- 1 until matrix.length) {
      val currentRows: Array[Array[Double]] = Array(matrix(k), matrix(i))
      val distance = distanceCalculator(currentRows, n)
      seqOfDistances = seqOfDistances :+ distance
      i += 1
      k += 1
    }
    seqOfDistances
  }

  /**
   * Direct the given rows to the desired distance formula
   * @param sequence
   * @return
   */
  def distanceCalculator(rows: Array[Array[Double]], n: Int): Double = n match {
    case 0 => { jaccardSimilarity(rows.head, rows(1)) }
    case 1 => { cosineSimilarity(rows.head, rows(1)) }
    case 2 => { euclideanDistance(rows.head, rows(1)) }
    case 3 => { manhattanDistance(rows.head, rows(1)) }
  }


  /**
   * The Jaccard Similarity between A and B sequences
   *  simmilarity =[A intersect B] / [A Union B]
   * @param one: List[Int]
   * @param two: List[Int]
   * @return
   */
  def jaccardSimilarity(one: Seq[Double], two: Seq[Double]): Double = {
    // for a given list, group the elements and map them to a pair of (element, list size)
    val countsByElement = (list: Seq[Double]) => list.groupBy(element => element).map(element => element._1 -> element._2.size)

    val firstCounts = countsByElement(one)
    val secondCounts = countsByElement(two)

    // with the given count and map of points from firstCounts,
    // if the element is in firstCounts and secondCounts, add min of secondCounts value at element key, or (list size??)
    def addIntersectToCount(count: Double, firstCounts: (Double, Int)): Double = {
      val (element, listSize) = firstCounts
      val value = secondCounts getOrElse (element, 0)
      count + min(value, listSize)
    }

    val unionOfOneTwo = (one.length + two.length).toDouble

    // using the fold left operator on the list of firstCounts, pass the updating count and next iteration of the list
    // to the addIntersectToCount method to create a count of intersections between the two lists
    val similarity: Double = (0.0 /: firstCounts)(addIntersectToCount) / unionOfOneTwo
    similarity
  }

  /**
   * The Cosine Similarity between A and B sequences
   *  similarity = [A dot B] / [Magnitude(A) * Magnitude(B)]
   */
  def cosineSimilarity(one: Seq[Double], two: Seq[Double]): Double = {
    // calculate the dot product of the two lists
    // dotProduct = sum(A1*B1 + A2*B2...)
    val dotProduct: Double = (for ((x, y) <- one zip two) yield x * y).sum

    // calculate the magnitude of both lists
    // magnitude = sqrt( (x1*x1) + (y1*y2) )
    val magnitudeOne: Double = math.sqrt(one.map(i => i * i).sum)
    val magnitudeTwo: Double = math.sqrt(two.map(i => i * i).sum)

    val similarity: Double = dotProduct / (magnitudeOne * magnitudeTwo)
    similarity
  }

  /**
   * The Euclidean Distance
   *  distance = sqrt( sum( (differences between Ai and Bi)(squared) ) )
   */
  def euclideanDistance(one: Seq[Double], two: Seq[Double]): Double = {
    val distance = sqrt((one zip two).map { case (x, y) => Math.pow(y - x, 2) }.sum)
    distance
  }

  /**
   * The Manhattan Distance - [a lower length indicates the vectors are more similar]
   *  distance = abs(Ax - Bx) + abs(Ay - By)
   */
  def manhattanDistance(one: Seq[Double], two: Seq[Double]): Double = {
    // collection1.zip(collection2) - merges collection2 with collection1
    val distance = one.zip(two).map(element => Math.abs(element._1 - element._2)).sum
    distance
  }

}
