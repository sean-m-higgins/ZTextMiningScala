import org.scalatest.{ BeforeAndAfterAll, FunSuite }

import org.apache.spark.sql.SparkSession

trait zettelSparkFunSuite extends FunSuite with BeforeAndAfterAll {
  //@transient var spark: SparkSession = _

  override def beforeAll(): Unit = {
    //    spark = SparkSession.builder
    //      .master("local[4]")
    //      .appName(this.getClass.getSimpleName)
    //      .getOrCreate()
  }

  override def afterAll(): Unit = {
    //    spark.stop()
    //    spark = null
  }
}
