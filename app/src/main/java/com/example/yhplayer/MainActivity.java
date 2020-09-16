package com.example.yhplayer;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yhplayer.Model.UploadSong;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    AppCompatEditText editTextTitle;
    TextView textViewimage;
    ProgressBar progressBar;
    Uri audioUri;
    StorageReference mstorageRef;
    StorageTask mUploadTask;
    DatabaseReference referencesongs;
    LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );


        editTextTitle=findViewById( R.id.songtitle );
        textViewimage=findViewById( R.id.txtviewsongfileselected );
        progressBar=findViewById( R.id.progressbar );
        referencesongs= FirebaseDatabase.getInstance().getReference().child( "songs" );
        mstorageRef= FirebaseStorage.getInstance().getReference().child( "songs" );
        linearLayout=findViewById( R.id.music1 );


    }

    public void openAudioFile(View v)
    {
        Intent i=new Intent( Intent.ACTION_GET_CONTENT );
        i.setType( "audio/*" );
        startActivityForResult( i,101 );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult( requestCode, resultCode, data );
        if (requestCode == 101 && resultCode == RESULT_OK && data.getData()!=null){

            audioUri=data.getData();
            String fileName=getFileName(audioUri);
            textViewimage.setText( fileName );

        }
    }

    private String getFileName(Uri uri)
    {

        String result=null;
        if (uri.getScheme().equals( "content" ))
        {

            Cursor cursor=getContentResolver().query( uri,null,null,null,null );

            try
            {
                if (cursor!=null && cursor.moveToFirst())
                {

                    result=cursor.getString( cursor.getColumnIndex( OpenableColumns.DISPLAY_NAME ) );

                }
            }finally {

                cursor.close();

            }
        }
        if (result==null)
        {
            result=uri.getPath();
            int cut=result.lastIndexOf( '/' );
            if (cut!=-1)
            {
                result=result.substring( cut + 1 );
            }
        }
        return result;
    }

    public void UploadAudioToFirebase(View v)
    {
        if (textViewimage.getText().toString().equals( "No file selected" ))
        {
            Toast.makeText( getApplicationContext(),"Please Select An Image",Toast.LENGTH_LONG ).show();
        }
        else
        {
            if (mUploadTask!=null && mUploadTask.isInProgress())
            {
                Toast.makeText( getApplicationContext(),"Song Upload is Already In Progress",Toast.LENGTH_LONG ).show();
            }
            else
            {
                uploadFile();
            }

        }
    }

    private void uploadFile()
    {
        if (audioUri!=null)
        {
            String durationTxt;
            Toast.makeText( getApplicationContext(),"Uploading Please Wait",Toast.LENGTH_LONG ).show();

            progressBar.setVisibility( View.VISIBLE );

            final StorageReference storageReference = mstorageRef.child( System.currentTimeMillis() + "."+getFileExtension(audioUri) );
            int durationInMillis=findSongDuration(audioUri);

            if (durationInMillis == 0)
            {
                durationTxt = "NA";
            }

            durationTxt = getDurationFromMilli(durationInMillis);

            final String finalDurationTxt = durationTxt;
            mUploadTask = storageReference.putFile( audioUri ).addOnSuccessListener( new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    storageReference.getDownloadUrl().addOnSuccessListener( new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {

                            UploadSong uploadSong = new UploadSong( editTextTitle.getText().toString(), finalDurationTxt,uri.toString() );

                            String uploadId =  referencesongs.push().getKey();

                            referencesongs.child( uploadId ).setValue( uploadSong );
                        }
                    } );


                }
            } )
            .addOnProgressListener( new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                    double progress = (100.0 * taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());

                    progressBar .setProgress((int)progress);

                }
            } );



        }
        else
        {
            Toast.makeText( getApplicationContext(),"No File Selected To Upload",Toast.LENGTH_LONG ).show();
        }
    }

    private String getDurationFromMilli(int durationInMillis)
    {
        Date date = new Date( durationInMillis );
        SimpleDateFormat simple = new SimpleDateFormat( "mm:ss", Locale.getDefault() );
        String myTime = simple.format( date );
        return myTime;
    }

    private int findSongDuration(Uri audioUri)
    {
        int timeInMillisec=0;
        try
        {
            MediaMetadataRetriever retriever=new MediaMetadataRetriever();
            retriever.setDataSource( this,audioUri );
            String time =retriever.extractMetadata( MediaMetadataRetriever.METADATA_KEY_DURATION );
            timeInMillisec = Integer.parseInt( time );
            retriever.release();

            return timeInMillisec;
        }catch (Exception e)
        {
            e.printStackTrace();
            return 0;
        }
    }

    private String getFileExtension(Uri audioUri)
    {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType( contentResolver.getType( audioUri ) );
    }

    public void openSongsActivity(View v)
    {
        Intent i=new Intent( MainActivity.this,ShowSongsActivity.class );
        startActivity( i );
    }


}
