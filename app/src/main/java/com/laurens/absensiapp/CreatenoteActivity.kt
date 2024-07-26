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
import com.laurens.absensiapp.databinding.ActivityCreatenoteBinding
import com.laurens.absensiapp.databinding.LayoutDeleteBinding
import com.laurens.absensiapp.databinding.LayoutUrlBinding
import com.laurens.database.NoteDatabase
import com.laurens.model.ModelNote
import java.text.SimpleDateFormat
import java.util.*

class CreateNoteActivity : AppCompatActivity() {

    private var alertDialog: AlertDialog? = null
    private var selectImagePath: String? = null
    private var modelNoteExtra: ModelNote? = null
    private lateinit var binding: ActivityCreatenoteBinding

    @SuppressLint("SetTextI18n", "RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreatenoteBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_createnote)

        // Display current date
        binding.tvDateTime.text = "Terakhir diubah : " + SimpleDateFormat(
            "dd MMMM yyyy", Locale.getDefault()
        ).format(Date())

        selectImagePath = ""

        if (intent.getBooleanExtra("EXTRA", false)) {
            modelNoteExtra = intent.getSerializableExtra("EXTRA_NOTE") as ModelNote
            setViewOrUpdateNote()
        }

        if (modelNoteExtra != null) {
            binding.linearDelete.visibility = View.VISIBLE
            binding.btnDelete.setOnClickListener {
                showDeleteDialog()
            }
        }

        binding.btnHapusUrl.setOnClickListener {
            binding.tvUrlNote.text = null
            binding.tvUrlNote.visibility = View.GONE
            binding.btnHapusUrl.visibility = View.GONE
        }

        binding.btnAddUrl.setOnClickListener {
            showDialogUrl()
        }

        binding.btnAddImage.setOnClickListener {
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

        binding.fabDeleteImage.setOnClickListener {
            binding.imageNote.setImageBitmap(null)
            binding.imageNote.visibility = View.GONE
            binding.fabDeleteImage.visibility = View.GONE
            selectImagePath = ""
        }

        binding.fabSaveNote.setOnClickListener {
            if (binding.editTextTitle.text.toString().isEmpty()) {
                Toast.makeText(this, "Judul Tidak Boleh Kosong", Toast.LENGTH_SHORT).show()
            } else if (binding.editTextSubTitle.text.toString().isEmpty() && binding.editTextDesc.text.toString().isEmpty()) {
                Toast.makeText(this, "Catatan Tidak Boleh Kosong", Toast.LENGTH_SHORT).show()
            } else {
                saveNote()
            }
        }
    }

    @SuppressLint("RestrictedApi")
    private fun setViewOrUpdateNote() {
        binding.editTextTitle.setText(modelNoteExtra?.title)
        binding.editTextSubTitle.setText(modelNoteExtra?.subTitle)
        binding.editTextDesc.setText(modelNoteExtra?.noteText)

        if (!modelNoteExtra?.imagePath.isNullOrEmpty()) {
            binding.imageNote.setImageBitmap(BitmapFactory.decodeFile(modelNoteExtra?.imagePath))
            binding.imageNote.visibility = View.VISIBLE
            selectImagePath = modelNoteExtra?.imagePath
            binding.fabDeleteImage.visibility = View.VISIBLE
        }

        if (!modelNoteExtra?.url.isNullOrEmpty()) {
            binding.tvUrlNote.text = modelNoteExtra?.url
            binding.tvUrlNote.visibility = View.VISIBLE
            binding.btnHapusUrl.visibility = View.VISIBLE
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
                        binding.imageNote.setImageBitmap(bitmap)
                        binding.imageNote.visibility = View.VISIBLE
                        binding.fabDeleteImage.visibility = View.VISIBLE
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
        val dialogBinding = LayoutDeleteBinding.inflate(layoutInflater)
        val dialog = Dialog(this@CreateNoteActivity)
        dialog.setContentView(dialogBinding.root)
        dialogBinding.tvHapusCatatan.setOnClickListener {
            deleteNote()
            dialog.dismiss()
        }
        dialogBinding.tvBatalHapus.setOnClickListener {
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
            val dialogBinding = LayoutUrlBinding.inflate(layoutInflater)
            builder.setView(dialogBinding.root)
            alertDialog = builder.create()
            alertDialog?.window?.setBackgroundDrawable(ColorDrawable(0))

            dialogBinding.editTextAddUrl.requestFocus()

            dialogBinding.tvOk.setOnClickListener {
                val url = dialogBinding.editTextAddUrl.text.toString().trim()
                if (url.isEmpty()) {
                    Toast.makeText(this, "Masukan Url", Toast.LENGTH_SHORT).show()
                } else if (!Patterns.WEB_URL.matcher(url).matches()) {
                    Toast.makeText(this, "Url Anda Tidak Benar", Toast.LENGTH_SHORT).show()
                } else {
                    binding.tvUrlNote.text = url
                    binding.tvUrlNote.visibility = View.VISIBLE
                    binding.btnHapusUrl.visibility = View.VISIBLE
                    alertDialog?.dismiss()
                }
            }

            dialogBinding.tvBatal.setOnClickListener {
                alertDialog?.dismiss()
            }
        }
        alertDialog?.show()
    }

    private fun saveNote() {
        val modelNote = ModelNote().apply {
            title = binding.editTextTitle.text.toString()
            subTitle = binding.editTextSubTitle.text.toString()
            noteText = binding.editTextDesc.text.toString()
            dateTime = binding.tvDateTime.text.toString()
            imagePath = selectImagePath
            if (binding.tvUrlNote.visibility == View.VISIBLE) {
                url = binding.tvUrlNote.text.toString()
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