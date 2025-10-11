package com.example.mylist

import AppDatabase
import UserReview
import UserReviewDao
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var logoutButton: Button
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


        logoutButton = findViewById(R.id.buttonLogout)
        addButton = findViewById(R.id.buttonAdd)
        generoFiltro = findViewById(R.id.spinnerGenero)
        recyclerView = findViewById(R.id.recyclerViewReview)


        db = AppDatabase.getDatabase(this)
        reviewDao = db.userReviewDao()


        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ReviewAdapter(reviews) { review -> mostrarDialogoEditar(review) }
        recyclerView.adapter = adapter


        val generos = arrayOf(
            "Todos",
            "Accion",
            "Comedia",
            "Drama",
            "Terror",
            "Ciencia Ficcion",
            "Romance",
            "Aventura",
            "Animación",
            "Musical",
            "Suspenso",
            "Documental",
            "Fantasia",
            "Historia",
            "Bélica"
        )

        generoFiltro.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, generos)

        generoFiltro.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: android.view.View?,
                pos: Int,
                id: Long
            ) {
                filtrarLista(generos[pos])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }


        addButton.setOnClickListener { mostrarDialogoAñadir() }


        logoutButton.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
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

    private fun mostrarDialogoAñadir() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_resena, null)
        val tituloInput = dialogView.findViewById<EditText>(R.id.editTitulo)
        val generoInput = dialogView.findViewById<EditText>(R.id.editGenero)
        val comentarioInput = dialogView.findViewById<EditText>(R.id.editComentario)

        AlertDialog.Builder(this)
            .setTitle("Añadir Reseña")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val resena = UserReview(
                    titulo = tituloInput.text.toString(),
                    genero = generoInput.text.toString(),
                    comentario = comentarioInput.text.toString()
                )
                lifecycleScope.launch(Dispatchers.IO) {
                    reviewDao.insert(resena)
                    filtrarLista(generoFiltro.selectedItem.toString())
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarDialogoEditar(resena: UserReview) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_resena, null)
        val tituloInput = dialogView.findViewById<EditText>(R.id.editTitulo)
        val generoInput = dialogView.findViewById<EditText>(R.id.editGenero)
        val comentarioInput = dialogView.findViewById<EditText>(R.id.editComentario)

        tituloInput.setText(resena.titulo)
        generoInput.setText(resena.genero)
        comentarioInput.setText(resena.comentario)

        AlertDialog.Builder(this)
            .setTitle("Editar Reseña")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val resenaActualizada = resena.copy(
                    titulo = tituloInput.text.toString(),
                    genero = generoInput.text.toString(),
                    comentario = comentarioInput.text.toString()
                )
                lifecycleScope.launch(Dispatchers.IO) {
                    reviewDao.update(resenaActualizada)
                    filtrarLista(generoFiltro.selectedItem.toString())
                }
            }
            .setNeutralButton("Borrar") { _, _ ->
                lifecycleScope.launch(Dispatchers.IO) {
                    reviewDao.delete(resena)
                    filtrarLista(generoFiltro.selectedItem.toString())
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
