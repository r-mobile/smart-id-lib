package kg.onoi.smartid

import android.graphics.Rect
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import kotlinx.android.synthetic.main.activity_test.*

class TestRectagle : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        box.setOnClickListener {
            val rect = Rect()
            box.getGlobalVisibleRect(rect)

            val rect2 = Rect()
            box2.getGlobalVisibleRect(rect2)

            Log.d("RECT", rect.contains(rect2).toString())
        }
    }
}