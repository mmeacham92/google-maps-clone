package com.example.mymaps

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mymaps.models.Place
import com.example.mymaps.models.UserMap
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

// using this variable as a key for our putExtra methods
const val EXTRA_USER_MAP = "EXTRA_USER_MAP"
const val EXTRA_MAP_TITLE = "EXTRA_MAP_TITLE"
private const val FILENAME = "UserMaps.data"
private const val REQUEST_CODE = 8888
private const val TAG = "MainActivity"
class MainActivity : AppCompatActivity() {
    private lateinit var rvMaps: RecyclerView
    private lateinit var fabCreateMap: FloatingActionButton

    private lateinit var userMaps: MutableList<UserMap>
    private lateinit var mapAdapter: MapsAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.title = "Maps by Meach"

        userMaps = deserializeUserMaps(this).toMutableList()

        // Set layout manager on the RecyclerView
        rvMaps = findViewById<RecyclerView>(R.id.rvMaps)
        rvMaps.layoutManager = LinearLayoutManager(this)



        // Set adapter on the RecyclerView
        mapAdapter = MapsAdapter(this, userMaps
            , object: MapsAdapter.OnClickListener {

            override fun onItemClick(position: Int) {
                // When a user taps on view in RV, navigate to new activity
                Log.i(TAG, "onItemClick $position")

                val intent = Intent(this@MainActivity, DisplayMapActivity::class.java)
                intent.putExtra(EXTRA_USER_MAP, userMaps[position])
                startActivity(intent)

                // add an activity transition to make UI experience a little more responsive
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
//                overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, R.anim.slide_in_right, R.anim.slide_out_left)
            }
        }, object: MapsAdapter.OnLongClickListener {
            override fun onItemLongClick(position: Int) {
                Log.i(TAG, "onLongItemClick $position")
                // remove item at position
                    // must remove from markers and from file
                    // probably have to overwrite the file?
                mapAdapter.notifyItemRemoved(position)
                userMaps.removeAt(position)
                serializeUserMaps(this@MainActivity, userMaps)
            }
        })
        rvMaps.adapter = mapAdapter




        fabCreateMap = findViewById<FloatingActionButton>(R.id.fabCreateMap)
        fabCreateMap.setOnClickListener {
            Log.i(TAG, "Tap on FAB")

            // create a view instance for the AlertDialog
            val mapFormView = LayoutInflater.from(this).inflate(R.layout.dialog_create_map, null)

            // create a dialog so user can set the title of new map
            val dialog = AlertDialog.Builder(this)
                .setTitle("Title of new map")
                .setView(mapFormView)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("OK", null)
                .show()

            // set a click listener on the positive button
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                val title = mapFormView.findViewById<EditText>(R.id.etNewMapTitle).text.toString()
                if (title.trim().isEmpty()) {
                    Toast.makeText(this, "Title must not be empty", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val intent = Intent(this@MainActivity, CreateMapActivity::class.java)
                intent.putExtra(EXTRA_MAP_TITLE, title)
                startActivityForResult(intent, REQUEST_CODE)

                // close the alert dialog
                dialog.dismiss()
            }
        }
        // end FAB onClickListener
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // Get new map data from the data
            val userMap = data?.getSerializableExtra(EXTRA_USER_MAP) as UserMap
            Log.i(TAG, "onActivityResult with new map title ${userMap.title}")
            userMaps.add(userMap)
            serializeUserMaps(this, userMaps)
            mapAdapter.notifyItemInserted(userMaps.size - 1)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    // read from file
    private fun deserializeUserMaps(context: Context): List<UserMap> {
        Log.i(TAG, "deserializeUserMaps")

        // get the file object from our getDataFile method
        val dataFile = getDataFile(context)
        // Check if the file exists yet
        if (!dataFile.exists()) {
            Log.i(TAG, "Data file does not exist yet")
            return emptyList()
        }
        // read all information from file and create a list of UserMaps from it
        val fileInputStream = FileInputStream(dataFile)
        val objectInputStream = ObjectInputStream(fileInputStream)
        objectInputStream.use {
            return it.readObject() as List<UserMap>
        }
        // return objectInputStream.readObject() as List<UserMap>
    }
    // write to file
    private fun serializeUserMaps(context: Context, userMaps: List<UserMap>) {
        Log.i(TAG, "serializeUserMaps")
        ObjectOutputStream(FileOutputStream(getDataFile(context))).use { it.writeObject(userMaps) }
    }

    private fun getDataFile(context: Context): File {
        Log.i(TAG, "Getting file from directory ${context.filesDir}")
        return File(context.filesDir, FILENAME)
    }

    private fun generateSampleData(): List<UserMap> {
        return listOf(
            UserMap(
                "Memories from University",
                listOf(
                    Place("Branner Hall", "Best dorm at Stanford", 37.426, -122.163),
                    Place("Gates CS building", "Many long nights in this basement", 37.430, -122.173),
                    Place("Pinkberry", "First date with my wife", 37.444, -122.170)
                )
            ),
            UserMap("January vacation planning!",
                listOf(
                    Place("Tokyo", "Overnight layover", 35.67, 139.65),
                    Place("Ranchi", "Family visit + wedding!", 23.34, 85.31),
                    Place("Singapore", "Inspired by \"Crazy Rich Asians\"", 1.35, 103.82)
                )),
            UserMap("Singapore travel itinerary",
                listOf(
                    Place("Gardens by the Bay", "Amazing urban nature park", 1.282, 103.864),
                    Place("Jurong Bird Park", "Family-friendly park with many varieties of birds", 1.319, 103.706),
                    Place("Sentosa", "Island resort with panoramic views", 1.249, 103.830),
                    Place("Botanic Gardens", "One of the world's greatest tropical gardens", 1.3138, 103.8159)
                )
            ),
            UserMap("My favorite places in the Midwest",
                listOf(
                    Place("Chicago", "Urban center of the midwest, the \"Windy City\"", 41.878, -87.630),
                    Place("Rochester, Michigan", "The best of Detroit suburbia", 42.681, -83.134),
                    Place("Mackinaw City", "The entrance into the Upper Peninsula", 45.777, -84.727),
                    Place("Michigan State University", "Home to the Spartans", 42.701, -84.482),
                    Place("University of Michigan", "Home to the Wolverines", 42.278, -83.738)
                )
            ),
            UserMap("Restaurants to try",
                listOf(
                    Place("Champ's Diner", "Retro diner in Brooklyn", 40.709, -73.941),
                    Place("Althea", "Chicago upscale dining with an amazing view", 41.895, -87.625),
                    Place("Shizen", "Elegant sushi in San Francisco", 37.768, -122.422),
                    Place("Citizen Eatery", "Bright cafe in Austin with a pink rabbit", 30.322, -97.739),
                    Place("Kati Thai", "Authentic Portland Thai food, served with love", 45.505, -122.635)
                )
            )
        )
    }
}