package com.bhavishaymankani.architecturecomponentrecyclerview;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final int ADD_NOTE_REQUEST_CODE = 1;
    public static final int EDIT_NOTE_REQUEST_CODE = 2;
    public static final int DEFAULT_PRIORITY_VALUE = 1;

    // Initializing variable(s).
    private NoteViewModel noteViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setTitle("Notes");

        FloatingActionButton floatingActionButton = findViewById(R.id.floating_action_button);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddEditNoteActivity.class);
                startActivityForResult(intent, ADD_NOTE_REQUEST_CODE);
            }
        });

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        final NoteAdapter adapter = new NoteAdapter();
        recyclerView.setAdapter(adapter);

        // Assigning variables.
        noteViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(this.getApplication())).get(NoteViewModel.class);

        noteViewModel.getAllNotes().observe(this, new Observer<List<Note>>() {
            @Override
            public void onChanged(List<Note> notes) {
                // update RecyclerView
                adapter.submitList(notes);

            }
        });

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                noteViewModel.delete(adapter.getNoteAt(viewHolder.getAdapterPosition()));
                Toast.makeText(MainActivity.this, "Note deleted", Toast.LENGTH_SHORT).show();
            }
        }).attachToRecyclerView(recyclerView);

        adapter.setOnItemClickListener(new NoteAdapter.OnItemClickListner() {
            @Override
            public void onItemClick(Note note) {
                Intent intent = new Intent(MainActivity.this, AddEditNoteActivity.class);
                intent.putExtra(AddEditNoteActivity.EXTRA_ID, note.getId());
                intent.putExtra(AddEditNoteActivity.EXTRA_TITLE, note.getTitle());
                intent.putExtra(AddEditNoteActivity.EXTRA_DESCRIPTION, note.getDescription());
                intent.putExtra(AddEditNoteActivity.EXTRA_PRIORITY, note.getPriority());

                startActivityForResult(intent, EDIT_NOTE_REQUEST_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ADD_NOTE_REQUEST_CODE && resultCode == RESULT_OK) {

            String title = data.getStringExtra(AddEditNoteActivity.EXTRA_TITLE);
            String description = data.getStringExtra(AddEditNoteActivity.EXTRA_DESCRIPTION);
            int priority = data.getIntExtra(AddEditNoteActivity.EXTRA_PRIORITY, DEFAULT_PRIORITY_VALUE);

            Note note = new Note(title, description, priority);
            noteViewModel.insert(note);

            Toast.makeText(this, "Your Note is Saved Succesfully!!!!", Toast.LENGTH_SHORT).show();

        } else if (requestCode==EDIT_NOTE_REQUEST_CODE && resultCode == RESULT_OK) {
            int id = data.getIntExtra(AddEditNoteActivity.EXTRA_ID, -1);

            if (id==-1) {
                Toast.makeText(this, "Note not updated", Toast.LENGTH_SHORT).show();
                return;
            }


            String title = data.getStringExtra(AddEditNoteActivity.EXTRA_TITLE);
            String description = data.getStringExtra(AddEditNoteActivity.EXTRA_DESCRIPTION);
            int priority = data.getIntExtra(AddEditNoteActivity.EXTRA_PRIORITY, DEFAULT_PRIORITY_VALUE);

            Note note = new Note(title, description, priority);

            note.setId(id);

            noteViewModel.update(note);

            Toast.makeText(this, "Note succesfully updated!!!!", Toast.LENGTH_SHORT).show();

        } else {

            Toast.makeText(this, "Unable to save Note", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case R.id.delete_all_notes:
                deleteAllNotes();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void deleteAllNotes() {
        noteViewModel.deleteAllNotes();
        Toast.makeText(this, "All Notes Deleted", Toast.LENGTH_SHORT).show();
    }
}