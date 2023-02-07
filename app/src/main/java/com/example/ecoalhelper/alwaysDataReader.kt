package com.example.ecoalhelper

import android.database.SQLException
import android.os.AsyncTask
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.util.Log
import kotlinx.coroutines.Dispatchers
import java.sql.Connection
import java.sql.DriverManager
import kotlinx.coroutines.*
import java.sql.PreparedStatement


/*
class alwaysDataReader(context: Context, factory: SQLiteDatabase.CursorFactory?) :
    SQLiteOpenHelper(context, "ecoal_db", factory, "10.6") {


}
*/

class AlwaysDataReader{

    private val TAG = "AlwaysDataReader"


    //@JvmStatic
    suspend fun connection() = coroutineScope {
        Log.d(TAG,"I am here");
        println("Connecting")
            //launch { // launch a new coroutine and continue
                //delay(1000L) // non-blocking delay for 1 second (default time unit is ms)
                println("World!") // print after delay

                try {

                    val c = withContext(Dispatchers.IO) {
                        /*DriverManager.getConnection(
                            "jdbc:mariadb://mysql-ecoal.alwaysdata.net/ecoal_db",
                            "ecoal",
                            "ognik02"
                        )*/
                        DriverManager.getConnection(
                            "jdbc:mariadb://mysql-ecoal.alwaysdata.net/ecoal_db?user=ecoal&password=ognik02"
                        )
                    }
                    Class.forName("org.mariadb.jdbc.Driver")
                    println("Completed")

                    val sql_select : PreparedStatement = (c.prepareStatement("select * from ognik_log order by ts;"))

                    val result = withContext(Dispatchers.IO) {
                        sql_select.executeQuery()
                    }

                    if (result.next()){
                        Log.v(TAG, result.getString("next_fuel_time"))
                    }


                } catch (e: ClassNotFoundException) {
                    e.printStackTrace()
                    Log.e(TAG, "ClassNotFoundException on DriverManager.getConnection")
                    //Toast.makeText(this@alwaysDataReader, "Class fail", Toast.LENGTH_SHORT).show()
                } catch (e: SQLException) {
                    e.printStackTrace()
                    Log.e(TAG, "SQLException on DriverManager.getConnection")
                    //Toast.makeText(this@alwaysDataReader, "Connected no", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.e(TAG, "Unknown exception on DriverManager.getConnection")
                    //Toast.makeText(this@alwaysDataReader, "Generig exception", Toast.LENGTH_SHORT).show()
                }
      //  }

        println("Hello") // main coroutine continues while a previous one is delayed

        println("ja pierdole co za gowno...")

        //Toast.makeText(this, "No exception", Toast.LENGTH_SHORT).show()

    }
/*
    fun coDoWork(binding: AlwaysDataReader) {
        lifecycleScope.launch(Dispatchers.IO) {
            println("running task on ${Thread.currentThread().name}")
            val result = factorial(input)
            val text = "Factorial of $result = $result"
            withContext(Dispatchers.Main) {
                println("Accessing UI on ${Thread.currentThread().name}")
                binding.displayText.text = text
            }
        }
        println("running end on ${Thread.currentThread().name}")
        */

}

/*
class AlwaysDataReader(){

    //companion object {

        //general
        val dbVersion = 1
        private val dbName = "ecoal_db"

        //table and columns
        val maincat_tbl = "ognik_log"
        val _ts = "ts"
        val _state = "tryb_auto_state"
        val _fuelLevel = "fuel_level"
        val _nextFuelTime = "next_fuel_time"

        private val ip = "mysql-ecoal.alwaysdata.net" // this is the host ip that your data base exists on you can use 10.0.2.2 for local host                                                    found on your pc. use if config for windows to find the ip if the database exists on                                                    your pc

        private val port = "1433" // the port sql server runs on

        private val Classes =
            "net.sourceforge.jtds.jdbc.Driver" // the driver that is required for this connection use                                                                           "org.postgresql.Driver" for connecting to postgresql

        private val username = "ecoal" // the user name

        private val password = "ognik02" // the password

        private val url =
            "jdbc:mariadb://$ip:$port/$dbName" // the connection url string


        private var connection: Connection? = null
    //}

    /*
    val database:SQLiteDatabase

*/
    init {
        //database = open()
    }

    fun start() {
        /*
        ActivityCompat.requestPermissions(
            this@alwaysDataReader,
            arrayOf(Manifest.permission.INTERNET),
            PackageManager.PERMISSION_GRANTED
        )

         */
        val policy = ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        try {
            Class.forName(Classes)
            connection = DriverManager.getConnection(url, username, password)
            //Toast.makeText(Context, "Connected", Toast.LENGTH_SHORT).show()
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
            //Toast.makeText(this@alwaysDataReader, "Class fail", Toast.LENGTH_SHORT).show()
        } catch (e: SQLException) {
            e.printStackTrace()
            //Toast.makeText(this@alwaysDataReader, "Connected no", Toast.LENGTH_SHORT).show()
        }
    }

    fun con_state(): Boolean{
        if(connection != null)
        {
           return true;
        }
        return false;
    }
}
*/