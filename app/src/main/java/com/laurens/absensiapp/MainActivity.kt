package com.laurens.absensiapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.laurens.absensiapp.databinding.ActivityMainBinding
import com.laurens.adapater.NoteAdapter
import com.laurens.database.NoteDatabase
import com.laurens.model.ModelNote
import com.laurens.utils.onClickItemListener

class MainActivity : AppCompatActivity(), onClickItemListener {

    private val modelNoteList: MutableList<ModelNote> = ArrayList()
    private var noteAdapter: NoteAdapter? = null
    private var onClickPosition = -1
    private lateinit var binding: ActivityMainBinding

    @SuppressLint("Assert")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        assert(supportActionBar != null)

        binding.fabCreateNote.setOnClickListener {
            startActivityForResult(Intent(this@MainActivity, CreateNoteActivity::class.java), REQUEST_ADD)
        }

        noteAdapter = NoteAdapter(modelNoteList, this)
        binding.rvListNote.adapter = noteAdapter

        // Change mode List to Grid
        modeGrid()

        // Get Data Catatan
        getNote(REQUEST_SHOW, false)
    }

    private fun modeGrid() {
        binding.rvListNote.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
    }

    private fun modeList() {
        binding.rvListNote.layoutManager = LinearLayoutManager(this)
    }

    private fun getNote(requestCode: Int, deleteNote: Boolean) {

        @Suppress("UNCHECKED_CAST")
        class GetNoteAsyncTask : AsyncTask<Void, Void, List<ModelNote>>() {
            override fun doInBackground(vararg params: Void?): List<ModelNote>? {
                return NoteDatabase.getInstance(this@MainActivity)?.noteDao()?.allNote as List<ModelNote>?
            }

            override fun onPostExecute(notes: List<ModelNote>) {
                super.onPostExecute(notes)
                if (requestCode == REQUEST_SHOW) {
                    modelNoteList.addAll(notes)
                    noteAdapter?.notifyDataSetChanged()
                } else if (requestCode == REQUEST_ADD) {
                    modelNoteList.add(0, notes[0])
                    noteAdapter?.notifyItemInserted(0)
                    binding.rvListNote.smoothScrollToPosition(0)
                } else if (requestCode == REQUEST_UPDATE) {
                    modelNoteList.removeAt(onClickPosition)
                    if (deleteNote) {
                        noteAdapter?.notifyItemRemoved(onClickPosition)
                    } else {
                        modelNoteList.add(onClickPosition, notes[onClickPosition])
                        noteAdapter?.notifyItemChanged(onClickPosition)
                    }
                }
            }
        }
        GetNoteAsyncTask().execute()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ADD && resultCode == RESULT_OK) {
            getNote(REQUEST_ADD, false)
        } else if (requestCode == REQUEST_UPDATE && resultCode == RESULT_OK) {
            if (data != null) {
                getNote(REQUEST_UPDATE, data.getBooleanExtra("NoteDelete", false))
            }
        }
    }

    override fun onClick(modelNote: ModelNote, position: Int) {
        onClickPosition = position
        val intent = Intent(this, CreateNoteActivity::class.java)
        intent.putExtra("EXTRA", true)
        intent.putExtra("EXTRA_NOTE", modelNote)
        startActivityForResult(intent, REQUEST_UPDATE)
    }

    companion object {
        private const val REQUEST_ADD = 1
        private const val REQUEST_UPDATE = 2
        private const val REQUEST_SHOW = 3
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.listView -> modeList()
            R.id.gridView -> modeGrid()
        }
        return super.onOptionsItemSelected(item)
    }
}