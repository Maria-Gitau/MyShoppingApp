package com.example.myshoppingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myshoppingapp.Model.Data;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.util.Date;

public class HomeActivity extends AppCompatActivity {

    private Toolbar toolbar;

    private FloatingActionButton fab_btn;

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    private RecyclerView recyclerView;

    private TextView totalSumResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        toolbar = findViewById(R.id.home_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("My Shopping List");

        totalSumResult=findViewById(R.id.total_amount);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mUser = mAuth.getCurrentUser();
        String uId=mUser.getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Shopping List").child(uId);

        mDatabase.keepSynced(true);

        recyclerView=findViewById(R.id.recycler_home);
        LinearLayoutManager layoutManager=new LinearLayoutManager(this);

        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        //Total sum number

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                int totalamount=0;

                for (DataSnapshot snap:dataSnapshot.getChildren()){
                    Data data=snap.getValue(Data.class);

                    totalamount+=data.getAmount();

                    String sttotal=String.valueOf(totalamount+".00");

                    totalSumResult.setText(sttotal);
                }
            }

            @Override
            public void onCancelled( DatabaseError databaseError) {

            }
        });

        fab_btn = findViewById(R.id.fab);

        fab_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customDialog();
            }
        });

    }

    private void customDialog() {
        AlertDialog.Builder mydialog = new AlertDialog.Builder(HomeActivity.this);

        LayoutInflater inflater = LayoutInflater.from(HomeActivity.this);

        View myview = inflater.inflate(R.layout.input_data, null);
        AlertDialog dialog = mydialog.create();

        dialog.setView(myview);

        EditText type = myview.findViewById(R.id.edt_type);
        EditText amount = myview.findViewById(R.id.edt_amount);
        EditText note = myview.findViewById(R.id.edt_note);
        Button btnSave = myview.findViewById(R.id.btn_save);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String mType = type.getText().toString().trim();
                String mAmount = amount.getText().toString().trim();
                String mNote = note.getText().toString().trim();

                int ammount=Integer.parseInt(mAmount);

                if (TextUtils.isEmpty(mType)) {
                    type.setError("Required Field!");
                    return;
                }
                if (TextUtils.isEmpty(mAmount)) {
                    amount.setError("Required Field!");
                    return;
                }
                if (TextUtils.isEmpty(mNote)) {
                    note.setError("Required Field!");
                    return;
                }

                String id=mDatabase.push().getKey();
                String date= DateFormat.getDateInstance().format(new Date());
                Data data=new Data(mType,ammount,mNote,date,id);
                mDatabase.child(id).setValue(data);

                Toast.makeText(getApplicationContext(), "Data Added Successfully",Toast.LENGTH_SHORT).show();


                dialog.dismiss();
            }
        });


        dialog.show();
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Data, myViewHolder>adapter=new FirebaseRecyclerAdapter<Data, myViewHolder>
                (
                        Data.class,
                        R.layout.item_data,
                        myViewHolder.class,
                        mDatabase
        ) {
            @Override
            protected void populateViewHolder(myViewHolder viewHolder, Data model, int position) {
                viewHolder.setDate(model.getDate());
                viewHolder.setType(model.getType());
                viewHolder.setNote(model.getNote());
                viewHolder.setAmount(model.getAmount());
            }
        };

        recyclerView.setAdapter(adapter);
    }

    public static class myViewHolder extends RecyclerView.ViewHolder{

        View myview;

        public myViewHolder(View itemView) {
            super(itemView);
            myview=itemView;
        }

        public void setType(String type){
            TextView mType=myview.findViewById(R.id.type);
            mType.setText(type);
        }
        public void setNote(String note){
            TextView mNote=myview.findViewById(R.id.note);
            mNote.setText(note);
        }
        public void setDate(String date){
            TextView mDate=myview.findViewById(R.id.date);
            mDate.setText(date);
        }
        public void setAmount(int amount){
            TextView mAmount=myview.findViewById(R.id.amount);
            String stam=String.valueOf(amount);
            mAmount.setText(stam);
        }


    }
}