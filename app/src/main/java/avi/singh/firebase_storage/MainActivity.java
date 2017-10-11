package avi.singh.firebase_storage;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class MainActivity extends AppCompatActivity {


    Button chooseImg, uploadImg;

    ImageView imgView;
    Button down;

    int PICK_IMAGE_REQUEST = 1011;
    Uri filePath;
    ProgressDialog pd;

    //creating reference to firebase storage
    FirebaseStorage storage = FirebaseStorage.getInstance();

    StorageReference storageRef = storage.getReferenceFromUrl("gs://fir-storage-e93ba.appspot.com");    //change the url according to your firebase app


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        chooseImg = (Button)findViewById(R.id.choose);      //choose button
        uploadImg = (Button)findViewById(R.id.upload);      //upload button
        imgView = (ImageView)findViewById(R.id.imageview);
        down=(Button)findViewById(R.id.download); //image view

        pd = new ProgressDialog(this);
        pd.setIndeterminate(true);
        pd.setTitle("Upload Process");                              //progress dialog box title
               // progress spinner


        down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,Main2Activity.class));
            }
        });


        chooseImg.setOnClickListener(new View.OnClickListener() {       //Image Selection start
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_PICK);
                startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
            }
        });                                                             // Image Selection end.

        uploadImg.setOnClickListener(new View.OnClickListener() {       // uploading the image start
            @Override
            public void onClick(View v) {
                if(filePath != null) {
                    pd.setMax(100);
                    pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    pd.show();



                     StorageReference childRef = storageRef.child(filePath.getLastPathSegment());


                    UploadTask uploadTask = childRef.putFile(filePath);

                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            pd.dismiss();
                            Toast.makeText(MainActivity.this, "Upload successful", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();
                            Toast.makeText(MainActivity.this, "Upload Failed -> " + e, Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                             double progress =(100.0 * taskSnapshot.getBytesTransferred())/taskSnapshot.getTotalByteCount();                // for displaying the upload percentage in progress bar.
                            pd.setMessage(((int)progress) + "% Uploaded..");
                        }
                    });
                }
                else {
                    Toast.makeText(MainActivity.this, "Select an image", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

                                                                                                     // Image Upload end

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();


            try {

                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);            //getting image from gallery


                imgView.setImageBitmap(bitmap);                                                               //Setting image to ImageView
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}