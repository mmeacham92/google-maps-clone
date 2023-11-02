package com.example.mymaps

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.example.mymaps.MainActivity.Companion.EXTRA_EDITMAP_POSITION
import com.example.mymaps.MainActivity.Companion.EXTRA_MAP_TITLE
import com.example.mymaps.MainActivity.Companion.EXTRA_USER_MAP

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.mymaps.databinding.ActivityCreateMapBinding
import com.example.mymaps.models.Place
import com.example.mymaps.models.UserMap
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.material.snackbar.Snackbar

class CreateMapActivity : AppCompatActivity(), OnMapReadyCallback {
    companion object {
        private const val TAG = "CreateMapActivity"
    }

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityCreateMapBinding
    private var markers: MutableList<Marker> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.title = intent.getStringExtra(EXTRA_MAP_TITLE)
        binding = ActivityCreateMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Snackbar
        mapFragment.view?.let {
            Snackbar.make(it, "Long press to add a marker", Snackbar.LENGTH_INDEFINITE)
                .setAction("OK") {}
                .setActionTextColor(ContextCompat.getColor(this, android.R.color.white))
                .show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_create_map, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Check that "item" is the save menu icon
        if (item.itemId == R.id.miSave) {
            Log.i(TAG, "Tapped on save")
            if (markers.isEmpty()) {
                Toast.makeText(this, "There must be at least one marker on the map", Toast.LENGTH_SHORT).show()
                return true
            }

            val places = markers.map { marker -> Place(marker.title, marker.snippet, marker.position.latitude, marker.position.longitude) }
            val userMap = UserMap(intent.getStringExtra(EXTRA_MAP_TITLE), places)

            val data = Intent()
            data.putExtra(EXTRA_USER_MAP, userMap)
            data.putExtra(EXTRA_EDITMAP_POSITION, intent.getIntExtra(EXTRA_EDITMAP_POSITION, 0))
            setResult(Activity.RESULT_OK, data)
            finish()   // telling android to finish current activity (CreateMap) and then go back to the parent activity with RESULT_OK and pass the data along to it
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val boundsBuilder = LatLngBounds.Builder()
        // add markers from existing user map if it exists
        if (intent.getSerializableExtra(EXTRA_USER_MAP) != null) {
            val existingMap = intent?.getSerializableExtra(EXTRA_USER_MAP) as UserMap
            for (place in existingMap.places) {
                val location = LatLng(place.latitude, place.longitude)
                boundsBuilder.include(location)
                val marker = mMap.addMarker(MarkerOptions().position(location).title(place.title).snippet(place.description))
                marker?.let { markers.add(it) }
            }
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 1000, 1000, 0))
        }

        // when user clicks on the info window
        mMap.setOnInfoWindowClickListener {markerToDelete ->
            // remove from markers list
            markers.remove(markerToDelete)
            // remove marker from map
            markerToDelete.remove()
        }

        // add a long click listener to the map
        mMap.setOnMapLongClickListener {latLng ->
            showAlertDialog(latLng)
        }

        // TODO: Start new CreateMap activity at current location
    }

    private fun showAlertDialog(latLng: LatLng) {
        // create a view instance for what the dialog box should look like?
        val placeFormView = LayoutInflater.from(this).inflate(R.layout.dialog_create_place, null)

        // create the actual alert dialog?
        val dialog = AlertDialog.Builder(this)
            .setTitle("Create a marker")
            .setView(placeFormView)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("OK", null)
            .show()

        // set a click listener on the positive button
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
            val title = placeFormView.findViewById<EditText>(R.id.etTitle).text.toString()
            val description = placeFormView.findViewById<EditText>(R.id.etDescription).text.toString()
            if (title.trim().isEmpty() || description.trim().isEmpty()) {
                Toast.makeText(this, "Place must have non-empty title and description", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val marker = mMap.addMarker(
                MarkerOptions().position(latLng).title(title)
                    .snippet(description)
            )
            // essentially a null check on if marker is valid
            marker?.let { markers.add(it) }
            // close the alert dialog
            dialog.dismiss()
        }
    }
}