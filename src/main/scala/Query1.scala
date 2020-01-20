import org.apache.spark.{SparkConf, SparkContext, sql}
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}
import org.apache.spark.sql.functions._
import org.apache.spark.input.PortableDataStream
import java.util.zip.ZipInputStream
import java.io.BufferedReader
import java.io.InputStreamReader
import org.apache.spark.rdd.RDD
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.auth.BasicSessionCredentials
import java.io.File
//Cassandra
import com.datastax.spark.connector.cql.CassandraConnector
import org.apache.spark.sql.cassandra._

object Query1 {

  def main(args: Array[String]): Unit = {

    // Des réglages optionnels du job spark. Les réglages par défaut fonctionnent très bien pour ce TP.
    // On vous donne un exemple de setting quand même
    val conf = new SparkConf().setAll(Map(
      "spark.scheduler.mode" -> "FIFO",
      "spark.speculation" -> "false",
      "spark.reducer.maxSizeInFlight" -> "48m",
      "spark.serializer" -> "org.apache.spark.serializer.KryoSerializer",
      "spark.kryoserializer.buffer.max" -> "1g",
      "spark.shuffle.file.buffer" -> "32k",
      "spark.default.parallelism" -> "12",
      "spark.sql.shuffle.partitions" -> "12"
    ))
      .set("spark.cassandra.connection.host", "192.168.123.10")

    // Initialisation du SparkSession qui est le point d'entrée vers Spark SQL (donne accès aux dataframes, aux RDD,
    // création de tables temporaires, etc., et donc aux mécanismes de distribution des calculs)
    val spark = SparkSession
      .builder
      .config(conf)
      .appName("Projet Gdelt : Query 1")
      .getOrCreate()

    val sc = new SparkContext(conf)

    import spark.implicits._

    val AWS_ID = "TODO"
    val AWS_KEY = "TODO"
    val AWS_TOKEN = "TODO"
    val s3_name = "TODO"

    sc.hadoopConfiguration.set("fs.s3a.aws.credentials.provider", "org.apache.hadoop.fs.s3a.TemporaryAWSCredentialsProvider")
    sc.hadoopConfiguration.set("fs.s3a.access.key", AWS_ID) // mettre votre ID du fichier credentials.csv
    sc.hadoopConfiguration.set("fs.s3a.secret.key", AWS_KEY) // mettre votre secret du fichier credentials.csv
    sc.hadoopConfiguration.set("fs.s3a.session.token", AWS_TOKEN)


    val dfEventsRenamed: DataFrame = textRDDEvents.toDF.withColumn("GLOBALEVENTID", split($"value", "\\t").getItem(0))
      .withColumn("Day", split($"value", "\\t").getItem(1))
      .withColumn("MonthYear", split($"value", "\\t").getItem(2))
      .withColumn("Year", split($"value", "\\t").getItem(3))
      .withColumn("FractionDate", split($"value", "\\t").getItem(4))
      .withColumn("Actor1Code", split($"value", "\\t").getItem(5))
      .withColumn("Actor1Name", split($"value", "\\t").getItem(6))
      .withColumn("Actor1CountryCode", split($"value", "\\t").getItem(7))
      .withColumn("Actor1KnownGroupCode", split($"value", "\\t").getItem(8))
      .withColumn("Actor1EthnicCode", split($"value", "\\t").getItem(9))
      .withColumn("Actor1Religion1Code", split($"value", "\\t").getItem(10))
      .withColumn("Actor1Religion2Code", split($"value", "\\t").getItem(11))
      .withColumn("Actor1Type1Code", split($"value", "\\t").getItem(12))
      .withColumn("Actor1Type2Code", split($"value", "\\t").getItem(13))
      .withColumn("Actor1Type3Code", split($"value", "\\t").getItem(14))
      .withColumn("Actor2Code", split($"value", "\\t").getItem(15))
      .withColumn("Actor2Name", split($"value", "\\t").getItem(16))
      .withColumn("Actor2CountryCode", split($"value", "\\t").getItem(17))
      .withColumn("Actor2KnownGroupCode", split($"value", "\\t").getItem(18))
      .withColumn("Actor2EthnicCode", split($"value", "\\t").getItem(19))
      .withColumn("Actor2Religion1Code", split($"value", "\\t").getItem(20))
      .withColumn("Actor2Religion2Code", split($"value", "\\t").getItem(21))
      .withColumn("Actor2Type1Code", split($"value", "\\t").getItem(22))
      .withColumn("Actor2Type2Code", split($"value", "\\t").getItem(23))
      .withColumn("Actor2Type3Code", split($"value", "\\t").getItem(24))
      .withColumn("IsRootEvent", split($"value", "\\t").getItem(25))
      .withColumn("EventCode", split($"value", "\\t").getItem(26))
      .withColumn("EventBaseCode", split($"value", "\\t").getItem(27))
      .withColumn("EventRootCode", split($"value", "\\t").getItem(28))
      .withColumn("QuadClass", split($"value", "\\t").getItem(29))
      .withColumn("GoldsteinScale", split($"value", "\\t").getItem(30))
      .withColumn("NumMentions", split($"value", "\\t").getItem(31))
      .withColumn("NumSources", split($"value", "\\t").getItem(32))
      .withColumn("NumArticles", split($"value", "\\t").getItem(33))
      .withColumn("AvgTone", split($"value", "\\t").getItem(34))
      .withColumn("Actor1Geo_Type", split($"value", "\\t").getItem(35))
      .withColumn("Actor1Geo_FullName", split($"value", "\\t").getItem(36))
      .withColumn("Actor1Geo_CountryCode", split($"value", "\\t").getItem(37))
      .withColumn("Actor1Geo_ADM1Code", split($"value", "\\t").getItem(38))
      .withColumn("Actor1Geo_ADM2Code", split($"value", "\\t").getItem(39))
      .withColumn("Actor1Geo_Lat", split($"value", "\\t").getItem(40))
      .withColumn("Actor1Geo_Long", split($"value", "\\t").getItem(41))
      .withColumn("Actor1Geo_FeatureID", split($"value", "\\t").getItem(42))
      .withColumn("Actor2Geo_Type", split($"value", "\\t").getItem(43))
      .withColumn("Actor2Geo_FullName", split($"value", "\\t").getItem(44))
      .withColumn("Actor2Geo_CountryCode", split($"value", "\\t").getItem(45))
      .withColumn("Actor2Geo_ADM1Code", split($"value", "\\t").getItem(46))
      .withColumn("Actor2Geo_ADM2Code", split($"value", "\\t").getItem(47))
      .withColumn("Actor2Geo_Lat", split($"value", "\\t").getItem(48))
      .withColumn("Actor2Geo_Long", split($"value", "\\t").getItem(49))
      .withColumn("Actor2Geo_FeatureID", split($"value", "\\t").getItem(50))
      .withColumn("ActionGeo_Type", split($"value", "\\t").getItem(51))
      .withColumn("ActionGeo_FullName", split($"value", "\\t").getItem(52))
      .withColumn("ActionGeo_CountryCode", split($"value", "\\t").getItem(53))
      .withColumn("ActionGeo_ADM1Code", split($"value", "\\t").getItem(54))
      .withColumn("ActionGeo_ADM2Code", split($"value", "\\t").getItem(55))
      .withColumn("ActionGeo_Lat", split($"value", "\\t").getItem(56))
      .withColumn("ActionGeo_Long", split($"value", "\\t").getItem(57))
      .withColumn("ActionGeo_FeatureID", split($"value", "\\t").getItem(58))
      .withColumn("DATEADDED", split($"value", "\\t").getItem(59))
      .withColumn("SOURCEURL", split($"value", "\\t").getItem(60))
      .drop("value")

    // MENTIONS
    val dfMentionsRenamed: DataFrame = textRDDMentions.toDF.withColumn("GLOBALEVENTID", split($"value", "\\t").getItem(0))
      .withColumn("GLOBALEVENTID", split($"value", "\\t").getItem(0))
      .withColumn("EventTimeDate", split($"value", "\\t").getItem(1))
      .withColumn("MentionTimeDate", split($"value", "\\t").getItem(2))
      .withColumn("MentionType", split($"value", "\\t").getItem(3))
      .withColumn("MentionSourceName", split($"value", "\\t").getItem(4))
      .withColumn("MentionIdentifier", split($"value", "\\t").getItem(5))
      .withColumn("SentenceID", split($"value", "\\t").getItem(6))
      .withColumn("Actor1CharOffset", split($"value", "\\t").getItem(7))
      .withColumn("Actor2CharOffset", split($"value", "\\t").getItem(8))
      .withColumn("ActionCharOffset", split($"value", "\\t").getItem(9))
      .withColumn("InRawText", split($"value", "\\t").getItem(10))
      .withColumn("Confidence", split($"value", "\\t").getItem(11))
      .withColumn("MentionDocLen", split($"value", "\\t").getItem(12))
      .withColumn("MentionDocTone", split($"value", "\\t").getItem(13))
      .withColumn("MentionDocTranslationInfo", split($"value", "\\t").getItem(14))
      .withColumn("Extras", split($"value", "\\t").getItem(15))
      .drop("value")

    // RELATION GRAPH
    val dfRelationsRenamed: DataFrame = textRDDRelations.toDF.withColumn("GLOBALEVENTID", split($"value", "\\t").getItem(0))
      .withColumn("GKGRECORDID", split($"value", "\\t").getItem(0))
      .withColumn("DATE", split($"value", "\\t").getItem(1))
      .withColumn("SourceCollectionIdentifier", split($"value", "\\t").getItem(2))
      .withColumn("SourceCommonName", split($"value", "\\t").getItem(3))
      .withColumn("DocumentIdentifier", split($"value", "\\t").getItem(4))
      .withColumn("Counts", split($"value", "\\t").getItem(5))
      .withColumn("V2Counts", split($"value", "\\t").getItem(6))
      .withColumn("Themes", split($"value", "\\t").getItem(7))
      .withColumn("V2Themes", split($"value", "\\t").getItem(8))
      .withColumn("Locations", split($"value", "\\t").getItem(9))
      .withColumn("V2Locations", split($"value", "\\t").getItem(10))
      .withColumn("Persons", split($"value", "\\t").getItem(11))
      .withColumn("V2Persons", split($"value", "\\t").getItem(12))
      .withColumn("Organizations", split($"value", "\\t").getItem(13))
      .withColumn("V2Organizations", split($"value", "\\t").getItem(14))
      .withColumn("V2Tone", split($"value", "\\t").getItem(15))
      .withColumn("Dates", split($"value", "\\t").getItem(16))
      .withColumn("GCAM", split($"value", "\\t").getItem(17))
      .withColumn("SharingImage", split($"value", "\\t").getItem(18))
      .withColumn("RelatedImages", split($"value", "\\t").getItem(19))
      .withColumn("SocialImageEmbeds", split($"value", "\\t").getItem(20))
      .withColumn("SocialVideoEmbeds", split($"value", "\\t").getItem(21))
      .withColumn("Quotations", split($"value", "\\t").getItem(22))
      .withColumn("AllNames", split($"value", "\\t").getItem(23))
      .withColumn("Amounts", split($"value", "\\t").getItem(24))
      .withColumn("TranslationInfo", split($"value", "\\t").getItem(25))
      .withColumn("Extras", split($"value", "\\t").getItem(26))
      .drop("value")

    val dfEventsForJoin = dfEventsRenamed.select("GLOBALEVENTID","DAY","Actor1CountryCode")
    val dfMentionsForJoin = dfMentionsRenamed.select("GLOBALEVENTID","MentionDocTranslationInfo")

    val dfJoin = dfEventsForJoin.join(dfMentionsForJoin, Seq("GLOBALEVENTID"))

    val dfGroup = dfJoin
      .groupBy("DAY", "Actor1CountryCode", "MentionDocTranslationInfo")
      .agg(count("GLOBALEVENTID").alias("count_events"))
      .withColumnRenamed("DAY","date")
      .withColumnRenamed("Actor1CountryCode", "country")
      .withColumnRenamed("MentionDocTranslationInfo", "language_0")

    def preprocessLanguages(s: String): String = {
      if(s == "") {
        "eng"
      } else {
        s.split(";")(0).split(":")(1)
      }
    }

    val preprocessLanguagesUDF = udf(preprocessLanguages _)

    val dfLanguagePreproc = dfGroup.withColumn("language", preprocessLanguagesUDF($"language_0")).drop("language_0")

    val reorderedColumnNames = Array("date","country","language","count_events")
    val dfReordered = dfLanguagePreproc.select(reorderedColumnNames.head, reorderedColumnNames.tail: _*)

    CassandraConnector(sc.getConf).withSessionDo { session =>
      session.execute(
        """
           CREATE KEYSPACE IF NOT EXISTS gdelt
           WITH REPLICATION =
           {'class': 'SimpleStrategy', 'replication_factor': 2 };
        """)
      session.execute(
        """
           CREATE TABLE IF NOT EXISTS gdelt.query1 (
              date text,
              country text,
              language text,
              count_events int,
              PRIMARY KEY (date, country, language)
            );
        """
      )
    }

    dfReordered.write
      .cassandraFormat("query1", "gdelt")
      .save()

    val query1 = spark.read
      .cassandraFormat("query1", "gdelt")
      .load()

  }
}
