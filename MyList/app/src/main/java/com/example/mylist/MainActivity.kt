package com.example.mylist

import AppDatabase
import UserReview
import UserReviewDao
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var addButton: Button
    private lateinit var generoFiltro: Spinner
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ReviewAdapter

    private lateinit var db: AppDatabase
    private lateinit var reviewDao: UserReviewDao
    private var reviews = listOf<UserReview>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializar Firebase
        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()

        val currentUser = auth.currentUser
        if (currentUser == null) {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }

        addButton = findViewById(R.id.buttonAdd)
        generoFiltro = findViewById(R.id.spinnerGenero)
        recyclerView = findViewById(R.id.recyclerViewReview)

        db = AppDatabase.getDatabase(this)
        reviewDao = db.userReviewDao()

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ReviewAdapter(reviews) { review -> mostrarDialogoEditar(review) }
        recyclerView.adapter = adapter

        val generosFiltro = resources.getStringArray(R.array.generos_array)

        generoFiltro.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, generosFiltro)

        generoFiltro.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: android.view.View?,
                pos: Int,
                id: Long
            ) {
                filtrarLista(generosFiltro[pos])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        addButton.setOnClickListener { mostrarDialogoAñadir() }

        val imageProfile = findViewById<ImageView>(R.id.imageProfile)
        imageProfile.setOnClickListener { view ->
            PopupMenu(this, view).apply {
                menuInflater.inflate(R.menu.profile_menu, menu)

                setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.menu_ver_perfil -> {
                            startActivity(Intent(this@MainActivity, ProfileActivity::class.java))
                            true
                        }

                        R.id.menu_cerrar_sesion -> {
                            auth.signOut()
                            Intent(this@MainActivity, LoginActivity::class.java).also { intent ->
                                intent.flags =
                                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                            }
                            finish()
                            true
                        }

                        else -> false
                    }
                }
                show()
            }
        }


        filtrarLista("Todos")
    }

    private fun filtrarLista(genero: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val lista = if (genero == "Todos") reviewDao.getAll() else reviewDao.getByGenero(genero)
            withContext(Dispatchers.Main) {
                reviews = lista
                adapter.actualizarDatos(reviews)
            }
        }
    }

    private var vistaPreviaTemporal: ImageView? = null
    private var imagenSeleccionada: String? = null

    private val selectorImagenLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { imagenElegida: Uri? ->
        if (imagenElegida != null) {
            imagenSeleccionada = imagenElegida.toString()
            vistaPreviaTemporal?.let { imageView ->
                Glide.with(this)
                    .load(imagenElegida)
                    .centerCrop()
                    .placeholder(R.drawable.placeholder)
                    .into(imageView)
            }
        }
    }

    private fun mostrarDialogoAñadir() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_resena, null)
        val tituloInput = dialogView.findViewById<EditText>(R.id.editTitulo)
        val comentarioInput = dialogView.findViewById<EditText>(R.id.editComentario)
        val generoSpinner = dialogView.findViewById<Spinner>(R.id.spinnerGenero)
        val ratingBar = dialogView.findViewById<RatingBar>(R.id.ratingBarValoracion)
        val imageView = dialogView.findViewById<ImageView>(R.id.imagePreview)
        val btnSeleccionarImagen = dialogView.findViewById<Button>(R.id.btnSeleccionarImagen)

        vistaPreviaTemporal = imageView
        imagenSeleccionada = null

        val generos = resources.getStringArray(R.array.generos_array).drop(1)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, generos)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        generoSpinner.adapter = adapter

        btnSeleccionarImagen.setOnClickListener {
            selectorImagenLauncher.launch("image/*")
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Añadir Reseña")
            .setView(dialogView)
            .setPositiveButton("Guardar", null)
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.setOnShowListener {
            val botonGuardar = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            botonGuardar.setOnClickListener {
                val titulo = tituloInput.text.toString().trim()
                val comentario = comentarioInput.text.toString().trim()
                val genero = generoSpinner.selectedItem?.toString()?.trim() ?: ""
                val valoracion = ratingBar.rating
                val imagen = imagenSeleccionada

                when {
                    titulo.isEmpty() -> Toast.makeText(
                        this,
                        "El título no puede estar vacío",
                        Toast.LENGTH_SHORT
                    ).show()

                    comentario.isEmpty() -> Toast.makeText(
                        this,
                        "El comentario no puede estar vacío",
                        Toast.LENGTH_SHORT
                    ).show()

                    valoracion == 0f -> Toast.makeText(
                        this,
                        "Debes dar una valoración",
                        Toast.LENGTH_SHORT
                    ).show()

                    imagen.isNullOrEmpty() -> Toast.makeText(
                        this,
                        "Debes seleccionar una imagen",
                        Toast.LENGTH_SHORT
                    ).show()

                    else -> {
                        val resena = UserReview(
                            titulo = titulo,
                            genero = genero,
                            comentario = comentario,
                            valoracion = valoracion.toInt(),
                            imagen = imagen
                        )

                        lifecycleScope.launch(Dispatchers.IO) {
                            reviewDao.insert(resena)
                            filtrarLista(generoFiltro.selectedItem.toString())
                        }

                        dialog.dismiss()
                    }
                }
            }
        }

        dialog.show()
    }

    private fun mostrarDialogoEditar(resena: UserReview) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_resena, null)
        val tituloInput = dialogView.findViewById<EditText>(R.id.editTitulo)
        val comentarioInput = dialogView.findViewById<EditText>(R.id.editComentario)
        val generoSpinner = dialogView.findViewById<Spinner>(R.id.spinnerGenero)
        val ratingBar = dialogView.findViewById<RatingBar>(R.id.ratingBarValoracion)
        val imageView = dialogView.findViewById<ImageView>(R.id.imagePreview)
        val btnSeleccionarImagen = dialogView.findViewById<Button>(R.id.btnSeleccionarImagen)

        vistaPreviaTemporal = imageView
        imagenSeleccionada = resena.imagen

        val generos = resources.getStringArray(R.array.generos_array).drop(1)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, generos)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        generoSpinner.adapter = adapter

        tituloInput.setText(resena.titulo)
        comentarioInput.setText(resena.comentario)
        ratingBar.rating = resena.valoracion.toFloat()

        if (resena.imagen.isNotEmpty()) {
            Glide.with(this)
                .load(Uri.parse(resena.imagen))
                .centerCrop()
                .placeholder(R.drawable.placeholder)
                .into(imageView)
        }

        val pos = generos.indexOf(resena.genero)
        if (pos >= 0) generoSpinner.setSelection(pos)

        btnSeleccionarImagen.setOnClickListener {
            selectorImagenLauncher.launch("image/*")
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Editar Reseña")
            .setView(dialogView)
            .setPositiveButton("Guardar", null)
            .setNeutralButton("Borrar", null)
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.setOnShowListener {
            val botonGuardar = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val botonBorrar = dialog.getButton(AlertDialog.BUTTON_NEUTRAL)

            botonGuardar.setOnClickListener {
                val titulo = tituloInput.text.toString().trim()
                val comentario = comentarioInput.text.toString().trim()
                val genero = generoSpinner.selectedItem?.toString()?.trim() ?: resena.genero
                val valoracion = ratingBar.rating
                val imagen = imagenSeleccionada

                when {
                    titulo.isEmpty() -> Toast.makeText(
                        this,
                        "El título no puede estar vacío",
                        Toast.LENGTH_SHORT
                    ).show()

                    comentario.isEmpty() -> Toast.makeText(
                        this,
                        "El comentario no puede estar vacío",
                        Toast.LENGTH_SHORT
                    ).show()

                    valoracion == 0f -> Toast.makeText(
                        this,
                        "Debes dar una valoración",
                        Toast.LENGTH_SHORT
                    ).show()

                    imagen.isNullOrEmpty() -> Toast.makeText(
                        this,
                        "Debes seleccionar una imagen",
                        Toast.LENGTH_SHORT
                    ).show()

                    else -> {
                        val resenaActualizada = resena.copy(
                            titulo = titulo,
                            genero = genero,
                            comentario = comentario,
                            valoracion = valoracion.toInt(),
                            imagen = imagen
                        )

                        lifecycleScope.launch(Dispatchers.IO) {
                            reviewDao.update(resenaActualizada)
                            filtrarLista(generoFiltro.selectedItem.toString())
                        }

                        dialog.dismiss()
                    }
                }
            }

            botonBorrar.setOnClickListener {
                lifecycleScope.launch(Dispatchers.IO) {
                    reviewDao.delete(resena)
                    filtrarLista(generoFiltro.selectedItem.toString())
                }
                dialog.dismiss()
            }
        }

        dialog.show()
    }
}
