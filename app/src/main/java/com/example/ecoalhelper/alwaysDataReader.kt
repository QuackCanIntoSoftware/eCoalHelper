package com.example.ecoalhelper

import android.database.SQLException
import android.util.Log
import kotlinx.coroutines.Dispatchers
import java.sql.Connection
import java.sql.DriverManager
import kotlinx.coroutines.*
import java.sql.PreparedStatement
import java.sql.ResultSet


enum class ConnectionStates {
    None, Unconnected, Connected
}

class AlwaysDataReader{

    companion object {
        private const val TAG = "AlwaysDataReader"
        private const val DB_DRIVER = "jdbc:mariadb://"
        private const val DB_ADDRESS = "mysql-ecoal.alwaysdata.net/ecoal_db"
        private const val DB_USER = "ecoal"
        private const val DB_PSWD = "ognik02"
        private const val DB_READ_ALL = "select * from ognik_log order by ts;"
        private const val DB_READ_LAST = "select * from ognik_log order by ts desc limit 1;"
    }

    private lateinit var connection: Connection
    var connectionStatus = ConnectionStates.None

    init {
        runBlocking {
            connect()
        }
    }

    private suspend fun connect() = coroutineScope {
        try {
            Log.d(TAG, "Connecting")

            connection = withContext(Dispatchers.IO) {
                DriverManager.getConnection(
                    DB_DRIVER + DB_ADDRESS,
                    DB_USER,
                    DB_PSWD
                )
            }
            Class.forName("org.mariadb.jdbc.Driver")
            Log.d(TAG, "Connection completed")
            connectionStatus = ConnectionStates.Connected

        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
            Log.e(TAG, "ClassNotFoundException on DriverManager.getConnection")
        } catch (e: SQLException) {
            e.printStackTrace()
            Log.e(TAG, "SQLException on DriverManager.getConnection")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "Unknown exception on DriverManager.getConnection")
        }

    }

    private suspend fun sendSqlQuery(query: String) = coroutineScope {
        Log.v(TAG, String.format("Preparing SQL query: %s", query))
        if (connectionStatus != ConnectionStates.Connected)
        {
            Log.d(TAG, "No connection. Trying to connect again")
            connect()
        }
        val sqlSelect : PreparedStatement = (connection.prepareStatement(query))

        try {
            val result = withContext(Dispatchers.IO) {
                Log.d(TAG, "Executing query")
                sqlSelect.executeQuery()
            }

            Log.d(TAG, "Query executed")
            return@coroutineScope result
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
            Log.e(TAG, "ClassNotFoundException on DriverManager.getConnection")
            connectionStatus = ConnectionStates.Unconnected
        } catch (e: SQLException) {
            e.printStackTrace()
            Log.e(TAG, "SQLException on DriverManager.getConnection")
            connectionStatus = ConnectionStates.Unconnected
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "Unknown exception on DriverManager.getConnection")
            connectionStatus = ConnectionStates.Unconnected
        }
    } as ResultSet

    fun readOgnikLog(): ResultSet? {
        var result : ResultSet? = null
        runBlocking {
            result = sendSqlQuery(DB_READ_ALL)
        }
        return result
    }

    fun readLastState(): ResultSet? {
        var result : ResultSet? = null
        runBlocking {
            result = sendSqlQuery(DB_READ_LAST)
        }
        return result
    }
}
