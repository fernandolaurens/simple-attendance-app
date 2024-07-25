package com.laurens.absensiapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.provider.MediaStore
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.laurens.database.NoteDatabase
import com.laurens.model.ModelNote
import kotlinx.android.synthetic.main.activity_createnote.*
import kotlinx.android.synthetic.main.layout_delete.*
import kotlinx.android.synthetic.main.layout_url.*
import kotlinx.android.synthetic.main.layout_url.view.*

import java.text.SimpleDateFormat
import java.util.*

class CreateNoteActivity : AppCompatActivity() {

    private var alertDialog: AlertDialog? = null
    private var selectImagePath: String? = null
    private var modelNoteExtra: ModelNote? = null

    @SuppressLint("SetTextI18n", "RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_createnote)

        // Display current date
        tvDateTime.text = "Terakhir diubah : " + SimpleDateFormat(
            "dd MMMM yyyy", Locale.getDefault()
        ).format(Date())

        selectImagePath = ""

        if (intent.getBooleanExtra("EXTRA", false)) {
            modelNoteExtra = intent.getSerializableExtra("EXTRA_NOTE") as ModelNote
            setViewOrUpdateNote()
        }

        if (modelNoteExtra != null) {
            linearDelete.visibility = View.VISIBLE
            btnDelete.setOnClickListener {
                showDeleteDialog()
            }
        }

        btnHapusUrl.setOnClickListener {
            tvUrlNote.text = null
            tvUrlNote.visibility = View.GONE
            btnHapusUrl.visibility = View.GONE
        }

        btnAddUrl.setOnClickListener {
            showDialogUrl()
        }

        btnAddImage.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_PERMISSION
                )
            } else {
                selectImage()
            }
        }

        fabDeleteImage.setOnClickListener {
            imageNote.setImageBitmap(null)
            imageNote.visibility = View.GONE
            fabDeleteImage.visibility = View.GONE
            selectImagePath = ""
        }

        fabSaveNote.setOnClickListener {
            if (editTextTitle.text.toString().isEmpty()) {
                Toast.makeText(this, "Judul Tidak Boleh Kosong", Toast.LENGTH_SHORT).show()
            } else if (editTextSubTitle.text.toString().isEmpty() && editTextDesc.text.toString().isEmpty()) {
                Toast.makeText(this, "Catatan Tidak Boleh Kosong", Toast.LENGTH_SHORT).show()
            } else {
                saveNote()
            }
        }
    }

    @SuppressLint("RestrictedApi")
    private fun setViewOrUpdateNote() {
        editTextTitle.setText(modelNoteExtra?.title)
        editTextSubTitle.setText(modelNoteExtra?.subTitle)
        editTextDesc.setText(modelNoteExtra?.noteText)

        if (!modelNoteExtra?.imagePath.isNullOrEmpty()) {
            imageNote.setImageBitmap(BitmapFactory.decodeFile(modelNoteExtra?.imagePath))
            imageNote.visibility = View.VISIBLE
            selectImagePath = modelNoteExtra?.imagePath
            fabDeleteImage.visibility = View.VISIBLE
        }

        if (!modelNoteExtra?.url.isNullOrEmpty()) {
            tvUrlNote.text = modelNoteExtra?.url
            tvUrlNote.visibility = View.VISIBLE
            btnHapusUrl.visibility = View.VISIBLE
        }
    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, REQUEST_SELECT)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            selectImage()
        } else {
            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("RestrictedApi")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_SELECT && resultCode == RESULT_OK) {
            data?.data?.let { selectImgUri ->
                try {
                    contentResolver.openInputStream(selectImgUri)?.let { inputStream ->
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        imageNote.setImageBitmap(bitmap)
                        imageNote.visibility = View.VISIBLE
                        fabDeleteImage.visibility = View.VISIBLE
                        selectImagePath = getPathFromUri(selectImgUri)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getPathFromUri(contentUri: Uri): String? {
        val cursor = contentResolver.query(contentUri, null, null, null, null)
        return cursor?.let {
            it.moveToFirst()
            val index = it.getColumnIndex("_data")
            val path = it.getString(index)
            it.close()
            path
        } ?: contentUri.path
    }

    private fun showDeleteDialog() {
        val dialog = Dialog(this@CreateNoteActivity)
        dialog.setContentView(R.layout.layout_delete)
        dialog.tvHapusCatatan.setOnClickListener {
            deleteNote()
            dialog.dismiss()
        }
        dialog.tvBatalHapus.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun deleteNote() {
        class DeleteNoteAsyncTask : AsyncTask<Void, Void, Void>() {
            override fun doInBackground(vararg params: Void?): Void? {
                NoteDatabase.getInstance(applicationContext)?.noteDao()?.delete(modelNoteExtra)
                return null
            }

            override fun onPostExecute(aVoid: Void?) {
                super.onPostExecute(aVoid)
                val intent = Intent().apply {
                    putExtra("NoteDelete", true)
                }
                setResult(RESULT_OK, intent)
                finish()
            }
        }
        DeleteNoteAsyncTask().execute()
    }

    private fun showDialogUrl() {
        if (alertDialog == null) {
            val builder = AlertDialog.Builder(this)
            val view = LayoutInflater.from(this).inflate(
                R.layout.layout_url,
                findViewById<ViewGroup>(R.id.layoutUrl)
            )
            builder.setView(view)
            alertDialog = builder.create()
            alertDialog?.window?.setBackgroundDrawable(ColorDrawable(0))

            val etUrl = view.editTextAddUrl
            etUrl.requestFocus()

            view.tvOk.setOnClickListener {
                val url = etUrl.text.toString().trim()
                if (url.isEmpty()) {
                    Toast.makeText(this, "Masukan Url", Toast.LENGTH_SHORT).show()
                } else if (!Patterns.WEB_URL.matcher(url).matches()) {
                    Toast.makeText(this, "Url Anda Tidak Benar", Toast.LENGTH_SHORT).show()
                } else {
                    tvUrlNote.text = url
                    tvUrlNote.visibility = View.VISIBLE
                    btnHapusUrl.visibility = View.VISIBLE
                    alertDialog?.dismiss()
                }
            }

            view.tvBatal.setOnClickListener {
                alertDialog?.dismiss()
            }
        }
        alertDialog?.show()
    }

    private fun saveNote() {
        val modelNote = ModelNote().apply {
            title = editTextTitle.text.toString()
            subTitle = editTextSubTitle.text.toString()
            noteText = editTextDesc.text.toString()
            dateTime = tvDateTime.text.toString()
            imagePath = selectImagePath
            if (tvUrlNote.visibility == View.VISIBLE) {
                url = tvUrlNote.text.toString()
            }
        }

        if (modelNoteExtra != null) {
            modelNote.id = modelNoteExtra!!.id
        }

        class SaveNoteAsyncTask : AsyncTask<Void, Void, Void>() {
            override fun doInBackground(vararg params: Void?): Void? {
                NoteDatabase.getInstance(applicationContext)?.noteDao()?.insert(modelNote)
                return null
            }

            override fun onPostExecute(aVoid: Void?) {
                super.onPostExecute(aVoid)
                setResult(RESULT_OK, Intent())
                finish()
            }
        }
        SaveNoteAsyncTask().execute()
    }

    companion object {
        private const val REQUEST_PERMISSION = 1
        private const val REQUEST_SELECT = 2
    }
}
