package com.example.yhplayer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yhplayer.Model.UploadSong;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
//import com.google.firebase.storage.FirebaseStorage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ShowSongsActivity extends AppCompatActivity implements View.OnClickListener {

    RecyclerView recyclerView;
    ProgressBar progressBar;
    Button play, next;
    List<UploadSong> mUpload;
    ArrayList arrayList;
    List<UploadSong> arrayListSongs1;
    SeekBar seekBar;

    DatabaseReference databaseReference;

    ValueEventListener valueEventListener;
    MediaPlayer mediaPlayer, mediaPlayer1;
    SongsAdapter adapter;
    Uri uri;
    int position;
    Thread thread;


    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_show_songs );

        recyclerView = findViewById( R.id.recyclerView );
        progressBar = findViewById( R.id.progressBarShowSongs );
        play = findViewById( R.id.ibplay );
        next = findViewById( R.id.ibnext );
        recyclerView.setHasFixedSize( true );
        recyclerView.setLayoutManager( new LinearLayoutManager( this ) );
        mUpload = new ArrayList<>();
        thread = new Thread() {
            @Override
            public void run() {
                int totalduration = mediaPlayer.getDuration();
                int currentduration = 0;
                while (currentduration < totalduration) {
                    Toast.makeText( ShowSongsActivity.this, "Hooo", Toast.LENGTH_LONG ).show();
                    try {
                        sleep( 500 );
                        currentduration = mediaPlayer.getCurrentPosition();
                        seekBar.setProgress( currentduration );
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }


                }
            }
        };


        adapter = new SongsAdapter( ShowSongsActivity.this, mUpload );
        recyclerView.setAdapter( adapter );

        databaseReference = FirebaseDatabase.getInstance().getReference( "songs" );

        valueEventListener = databaseReference.addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUpload.clear();
                for (DataSnapshot dss : dataSnapshot.getChildren()) {
                    UploadSong uploadSong = dss.getValue( UploadSong.class );
                    uploadSong.setMkey( dss.getKey() );
                    mUpload.add( uploadSong );
                }

                adapter.notifyDataSetChanged();
                progressBar.setVisibility( View.GONE );
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText( getApplicationContext(), "" + databaseError.getMessage(), Toast.LENGTH_LONG ).show();
                progressBar.setVisibility( View.GONE );
            }
        } );


    }

    protected void onDestroy() {
        super.onDestroy();
        databaseReference.removeEventListener( valueEventListener );
    }

    public void demo(int pos) {
        position = pos;
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void playSong(List<UploadSong> arrayListSongs, int adapterPosition) throws IOException {
        arrayListSongs1 = arrayListSongs;
        position = adapterPosition;
        Toast.makeText( this, position, Toast.LENGTH_SHORT ).show();
        UploadSong uploadSong = arrayListSongs.get( adapterPosition );
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;

        }
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setDataSource( uploadSong.getSongLink() );
        mediaPlayer.setOnPreparedListener( new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
                //  play.setText( "Pause" );
                play.setBackgroundResource( R.drawable.pause );
            }
        } );
        mediaPlayer.prepareAsync();
        play.setOnClickListener( this );
        next.setOnClickListener( this );


    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.ibplay:
                if (mediaPlayer.isPlaying()) {
                    //play.setText( "play" );
                    play.setBackgroundResource( R.drawable.play );
                    mediaPlayer.pause();
                } else {
//                    play.setText( "Pause");
                    play.setBackgroundResource( R.drawable.pause );
                    mediaPlayer.start();
                }
                break;

            case R.id.ibnext:
                if (position < arrayListSongs1.size() - 1) {
                    position++;
                    demo( position );
                } else {
                    position = 0;
                }

        }
    }
}



