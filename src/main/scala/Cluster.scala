import breeze.linalg._
import breeze.numerics._
//import breeze.plot._
import smile.clustering.hclust
import smile.plot._

object Cluster extends App {
  val rheingold = "src/main/data/zettels/rheingold-examples"
  val baseball = "src/main/data/zettels/baseball"
  val examples = "src/main/data/zettels/examples"

  val zettels = datamining.getZettelsFromDirectory(baseball)

  val process = new ZettelsPreProcessor(zettels)
  val countMatrix = process.preprocess("countMatrix").asInstanceOf[Array[Array[Double]]]

  val distance = new Distance()

  val jaccard = distance.calculateDistances(countMatrix, 0)
  val cosine = distance.calculateDistances(countMatrix, 1)
  val euclidean = distance.calculateDistances(countMatrix, 2)
  val manhattan = distance.calculateDistances(countMatrix, 3)

  val distanceMatrixJaccard = distance.createDistanceMatrix(jaccard)
  // from http://haifengl.github.io/smile/clustering.html
  val clustersJaccard = hclust(distanceMatrixJaccard, "single")
  dendrogram(clustersJaccard)

  val distanceMatrixCosine = distance.createDistanceMatrix(cosine)
  val clustersCosine = hclust(distanceMatrixCosine, "single")
  dendrogram(clustersCosine)

  val distanceMatrixEuclidean = distance.createDistanceMatrix(euclidean)
  val clustersEuclidean = hclust(distanceMatrixEuclidean, "single")
  dendrogram(clustersEuclidean)

  val distanceMatrixManhattan = distance.createDistanceMatrix(manhattan)
  val clustersManhattan = hclust(distanceMatrixManhattan, "single")
  dendrogram(clustersManhattan)


  //  val y = clusters.partition(6)
  //  plot(distanceMatrix, y, '.', Palette.COLORS)

  //  // from https://github.com/scalanlp/breeze/wiki/Quickstart
  //  val f = Figure()
  //  val p = f.subplot(0)
  //  val x = linspace(-10.0, 10.0)
  //  p += plot(x, sin(x), '.')
  //  p += plot(x, cos(x), '.')
  //  p.title = "lines plotting"
  //
  ////  val p2 = f.subplot(2, 2, 1)
  ////  val g2 = breeze.stats.distributions.Gaussian(0, 1)
  ////  p2 += hist(g2.sample(100000), 100)
  ////  p2.title = "A normal distribution"
  ////
  ////  val p3 = f.subplot(2, 2, 2)
  ////  val g3 = breeze.stats.distributions.Poisson(5)
  ////  p3 += hist(g3.sample(100000), 100)
  ////  p3.title = "A poisson distribution"
  ////
  ////  val p4 = f.subplot(2, 2, 3)
  ////  p4 += image(DenseMatrix.rand(200, 200))
  ////  p4.title = "A random distribution"
  //  f.saveas("image.png")

}
