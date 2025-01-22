package com.esmapolat.artbookkotlin

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.esmapolat.artbookkotlin.databinding.ActivityArtBinding
import com.google.android.material.snackbar.Snackbar
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class ArtActivity : AppCompatActivity() {
    private lateinit var binding: ActivityArtBinding
    private lateinit var activityResultLancher:ActivityResultLauncher<Intent>// GALERİYE GİTMEK için kullancayız
    private lateinit var permissonLancher:ActivityResultLauncher<String>//KAYITlarını oncreate altında yapmalıyız
    var selectedbitmap:Bitmap?=null
    private lateinit var database:SQLiteDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityArtBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database=this.openOrCreateDatabase("Arts", MODE_PRIVATE,null)

        registerLancher()
        val intent=intent
        val info=intent.getStringExtra("info")
        if(info.equals("new")){
            println("iffff")
            binding.artnameText.setText("")
            binding.artisnameText.setText("")
            binding.yearText.setText("")
            binding.button.visibility=View.VISIBLE
            //binding.imageView.setImageResource(R.drawable)//dikkattt selectimage yok
            val selectedImageBackground = BitmapFactory.decodeResource(applicationContext.resources,R.drawable.select)
            binding.imageView.setImageBitmap(selectedImageBackground)



        }
        else{
            println("kaydettşşşii")



            binding.button.visibility=View.INVISIBLE
            val selectedId=intent.getIntExtra("id",1)
            val cursor=database.rawQuery("SELECT * FROM arts WHERE id=?", arrayOf(selectedId.toString()))
            val artNameIx=cursor.getColumnIndex("artname")
            val artistNameIx=cursor.getColumnIndex("artistname")
            val yearIx=cursor.getColumnIndex("year")
            val imageIx=cursor.getColumnIndex("image")

            while(cursor.moveToNext()){
                println("whileeee")
                binding.artnameText.setText(cursor.getString(artNameIx))
                binding.artisnameText.setText(cursor.getString(artistNameIx))
                binding.yearText.setText(cursor.getString(yearIx))


                val byteArray=cursor.getBlob(imageIx)
                val bitmap=BitmapFactory.decodeByteArray(byteArray,0,byteArray.size)
                binding.imageView.setImageBitmap(bitmap)
                println("oldıı")
            }
            cursor.close()

        }
    }
    fun savebuttonClick(view: View){
        val artName=binding.artnameText.text.toString()
        val artisName=binding.artisnameText.text.toString()
        val  year=binding.yearText.text.toString()
        if(selectedbitmap!=null){
            val smallBitmap=makeSmallerBitmap(selectedbitmap!!,300)
            // asagidaki 3 kod gorseli veriye cevirmede kullanılır
            val outputStream=ByteArrayOutputStream()
            smallBitmap.compress(Bitmap.CompressFormat.PNG,50,outputStream)
            val byteArray=outputStream.toByteArray()
            try{
                //val database=this.openOrCreateDatabase("Arts", MODE_PRIVATE,null)
                database.execSQL("CREATE TABLE IF NOT EXISTS arts(id INTEGER PRİMARY KEY, artname VARCHAR,artistname VARCHAR,year VARCHAR,image BLOB)")
                val Sqlstring="INSERT INTO arts (artname,artistname,year,image) VALUES (?,?,?,?)"
                val statement=database.compileStatement(Sqlstring)
                statement.bindString(1,artName)
                statement.bindString(2,artisName)
                statement.bindString(3,year)
                statement.bindBlob(4,byteArray)
                statement.execute()

            }
            catch (e:Exception){
                e.printStackTrace()
            }
            val intent=Intent(this@ArtActivity,MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }

    }
    private fun makeSmallerBitmap(image:Bitmap, maksimumSize:Int) :Bitmap{
        var width=image.width
        var heigth=image.height
        var bitmapRetio :Double =width.toDouble()/heigth.toDouble()
        if(bitmapRetio>1){
            width=maksimumSize
            val scaleHeight=width/bitmapRetio
            heigth=scaleHeight.toInt()

        }
        else{
            heigth=maksimumSize
            val scaleHeight=heigth/bitmapRetio
            width=scaleHeight.toInt()

        }
        return Bitmap.createScaledBitmap(image,width,heigth,true)

    }
    fun selectClick(view :View){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.TIRAMISU){
            //33 ve üzeri için
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_MEDIA_IMAGES)!=PackageManager.PERMISSION_GRANTED){
                if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_MEDIA_IMAGES)){
                    Snackbar.make(view,"Permisson needed for Gallery",Snackbar.LENGTH_INDEFINITE).setAction("Give Permisson",View.OnClickListener {
                        permissonLancher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                    }).show()
                }
                else{
                    permissonLancher.launch(Manifest.permission.READ_MEDIA_IMAGES)

                }
            }
            else{
                val intentGallery=Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLancher.launch(intentGallery)
            }

        }
        else{
            //32 ve aşağısı için
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
                if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)){
                    Snackbar.make(view,"Permisson needed for Gallery",Snackbar.LENGTH_INDEFINITE).setAction("Give Permisson",View.OnClickListener {
                        permissonLancher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }).show()
                }
                else{
                    permissonLancher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)

                }
            }
            else{
                val intentGallery=Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLancher.launch(intentGallery)
            }

        }


    }
    private fun registerLancher() {
        activityResultLancher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->  //Burda yanlış yapmıstım Contratck kullanmısım
            if (result.resultCode == RESULT_OK) {
                println("oaaskk")
                val intentFrom = result.data
                if (intentFrom != null) {
                    val imagedata = intentFrom.data

                    println("okkkk")


                    try {
                        if (Build.VERSION.SDK_INT >= 28) {
                            val source = ImageDecoder.createSource(
                                this@ArtActivity.contentResolver,
                                imagedata!!
                            )
                            selectedbitmap = ImageDecoder.decodeBitmap(source)
                            binding.imageView.setImageBitmap(selectedbitmap)
                        } else {
                            selectedbitmap = MediaStore.Images.Media.getBitmap(
                                this@ArtActivity.contentResolver,
                                imagedata
                            )
                            binding.imageView.setImageBitmap(selectedbitmap)
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()

                    }
                }
            }
        }
        permissonLancher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
                if (result) {
                    val intentToGallery =
                        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    activityResultLancher.launch(intentToGallery)

                } else {
                    Toast.makeText(this@ArtActivity, "Permisson needed!", Toast.LENGTH_LONG).show()
                }

            }


    }




}