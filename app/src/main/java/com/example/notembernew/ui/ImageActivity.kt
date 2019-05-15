package  com.example.notembernew.ui;

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.divyanshu.androiddraw.IMAGE_PATH
import com.example.notembernew.R
import kotlinx.android.synthetic.main.activity_image.*

class ImageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image)

        val path = intent.getStringExtra(IMAGE_PATH)
        Glide.with(this).load(path).into(image_view)
    }
}
