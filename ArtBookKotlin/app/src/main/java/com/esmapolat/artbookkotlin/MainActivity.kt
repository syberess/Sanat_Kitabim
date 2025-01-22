package com.esmapolat.artbookkotlin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.esmapolat.artbookkotlin.databinding.ActivityArtBinding
import com.esmapolat.artbookkotlin.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private lateinit var artList:ArrayList<Art>
    private lateinit var artAdapter:ArtAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        artList=ArrayList<Art>()

        artAdapter= ArtAdapter(artList)
        binding.recyclerView.layoutManager=LinearLayoutManager(this)
        binding.recyclerView.adapter=artAdapter

        try {
            val database=this.openOrCreateDatabase("Arts", MODE_PRIVATE,null)
             val cursor=database.rawQuery("SELECT * FROM arts",null)
             val nameIx=cursor.getColumnIndex("artname")
             val idIx=cursor.getColumnIndex("id")

            while(cursor.moveToNext()){
                val name=cursor.getString(nameIx)
                val id=cursor.getInt(idIx)
                val art=Art(name,id)
                artList.add(art)
            }
            artAdapter.notifyDataSetChanged() // adaptere kendine çeki düzen vermesi gerektiğini bildiriyor
            cursor.close()

        }
        catch (e:Exception){
            e.printStackTrace()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuinflater=menuInflater
        menuinflater.inflate(R.menu.art_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId==R.id.art_add_item){
            val intent=Intent(this@MainActivity,ArtActivity::class.java)
            intent.putExtra("info","new")
            startActivity(intent)

        }
        return super.onOptionsItemSelected(item)
    }
}