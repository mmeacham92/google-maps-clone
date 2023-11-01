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

private const val TAG = "CreateMapActivity"
class CreateMapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityCreateMapBinding
    private var markers: MutableList<Marker> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.title = intent.getStringExtra(EXTRA_MAP_TITLE)
//        val existingMap = intent?.getSerializableExtra(EXTRA_CURRENT_MAP) as UserMap
//        val existingMarkers= existingMap.places.map { place -> mMap.addMarker(MarkerOptions().position(LatLng(place.latitude, place.longitude)).title(place.title).snippet(place.description))
//        } as MutableList<Marker>
//        markers.addAll(existingMarkers)
        binding = ActivityCreateMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

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

            // bug happening here:
            // maybe we could remove it from the list right before we redirect to createmap, but then it would be added at the end
            // how could we keep it at the same position? pass the position of the clicked element and add it back to that same spot, but not sure how yet

            val data = Intent()
            data.putExtra(EXTRA_USER_MAP, userMap)
            setResult(Activity.RESULT_OK, data)
            finish()   // telling android to finish current activity (CreateMap) and then go back to the parent activity with RESULT_OK and pass the data along to it
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
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

        mMap.setOnInfoWindowClickListener {markerToDelete ->
            // remove from markers list
            markers.remove(markerToDelete)
            // remove marker from map
            markerToDelete.remove()
        }

        // add a long click listener to the map
        // should get position from there
        mMap.setOnMapLongClickListener {latLng ->
            showAlertDialog(latLng)
        }

        // Add a marker in Sydney and move the camera
//        val murfreesboro = LatLng(35.84535, -86.39152)
//        mMap.addMarker(MarkerOptions().position(murfreesboro).title("Murfreesboro").snippet("The geographic center of Tennessee"))
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(murfreesboro, 10f))
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