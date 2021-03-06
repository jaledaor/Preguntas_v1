package todo_jaledaor.pruebatodolist.vistas;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import todo_jaledaor.pruebatodolist.R;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private RecyclerViewAdapter recyclerViewAdapter;
    private EditText addTaskBox;
    String uid;
    public String pregunta = "";
    public String respuesta = "";
    public String fecha = "";
    public String categoria ="";
    public Boolean respondida = false;

    private FirebaseAuth mAuth_control;
    private FirebaseDatabase database_control;
    private DatabaseReference reference_control;
    private List<Task> allTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        android.support.v7.widget.Toolbar toolbar =(android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        allTask = new ArrayList<Task>();
        mAuth_control = FirebaseAuth.getInstance();
        uid = "";
        uid = mAuth_control.getCurrentUser().getUid().toString();
        database_control = FirebaseDatabase.getInstance();
        reference_control = database_control.getReference("usuarios/" + uid + "/Tareas");
        addTaskBox = (EditText) findViewById(R.id.add_task_box);
        recyclerView = (RecyclerView) findViewById(R.id.task_list);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        FloatingActionButton addTaskButton = (FloatingActionButton) findViewById(R.id.add_task_button);
        addTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                View mView = getLayoutInflater().inflate(R.layout.dialog_datepicker, null);
                final EditText question_input = (EditText) mView.findViewById(R.id.question_input);
                final EditText category_input = (EditText) mView.findViewById(R.id.category_input);
                final TextView fecha_elegida = (TextView) mView.findViewById(R.id.fecha_elegida);

                SimpleDateFormat timeStampFormat = new SimpleDateFormat("yyyy/MM/dd");
                Date myDate = new Date();
                String filename = timeStampFormat.format(myDate);
                fecha_elegida.setText(filename);

                builder.setView(mView)
                        .setTitle("Adicionar Pregunta")
                        .setPositiveButton("OK", new AlertDialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {



                                pregunta = question_input.getText().toString();
                                if (TextUtils.isEmpty(pregunta)) {
                                    Toast.makeText(MainActivity.this, "No debe estar vacia la pregunta", Toast.LENGTH_LONG).show();
                                    return;
                                }
                                categoria = category_input.getText().toString();
                                if (TextUtils.isEmpty(categoria)) {
                                    Toast.makeText(MainActivity.this, "No debe estar vacia la categoria", Toast.LENGTH_LONG).show();
                                    return;
                                }

                                Task taskObject = new Task();
                                taskObject.setPregunta(pregunta);
                                taskObject.setCategoria(categoria);
                                taskObject.setRespuesta("");
                                taskObject.setFecha(fecha_elegida.getText().toString());
                                taskObject.setRespondida(false);

                                reference_control.push().setValue(taskObject);
                                dialog.dismiss();

                            }
                        })
                        .setNegativeButton("Cancel", new AlertDialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                final AlertDialog dialog = builder.create();
                dialog.show();

            }
        });
        reference_control.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                getAllTask(dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                getAllTask(dataSnapshot);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                taskDeletion(dataSnapshot);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void getAllTask(DataSnapshot dataSnapshot) {

        reference_control = database_control.getReference("usuarios/"+uid+"/Tareas");

        for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
            /*Task pregunta = singleSnapshot.getValue(Task.class);*/
            pregunta = dataSnapshot.child("pregunta").getValue(String.class);
            respuesta = dataSnapshot.child("respuesta").getValue(String.class);
            categoria = dataSnapshot.child("categoria").getValue(String.class);
            fecha = dataSnapshot.child("fecha").getValue(String.class);
            respondida = dataSnapshot.child("respondida").getValue(Boolean.class);
        }
        allTask.add(new Task(pregunta, categoria, respuesta, fecha, respondida));
        recyclerViewAdapter = new RecyclerViewAdapter(MainActivity.this, allTask);
        recyclerView.setAdapter(recyclerViewAdapter);
    }

    private void taskDeletion(DataSnapshot dataSnapshot) {
        for (DataSnapshot data : dataSnapshot.getChildren()) {
            String pregunta = data.child("pregunta").getValue().toString();
            for(int i = 0; i < allTask.size(); i++){
                if(allTask.get(i).getPregunta().equals(pregunta)){
                    allTask.remove(i);
                }
            }
            Log.d(TAG, "Task title " + pregunta);
            recyclerViewAdapter.notifyDataSetChanged();
            recyclerViewAdapter = new RecyclerViewAdapter(MainActivity.this, allTask);
            recyclerView.setAdapter(recyclerViewAdapter);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.salida) {
            Intent intent = new Intent(this, AuthActivity.class);
            startActivity(intent);
            //TODO: llamar el presenter el metodo de signout de firebase
            finish();
        }


        return super.onOptionsItemSelected(item);
    }
}